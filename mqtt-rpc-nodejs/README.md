=========
mqtt-rpc
=========

This module provides an rpc interface for an mqtt connection, in essence this is a request and response strategy which uses
an MQTT topic structure as transport.

# Installation

To install from a tarball, .tgz file, execute the following command at the commandline replacing the *version number*

```
npm install mqtt-rpc-VERSION_NUMBER.tgz
```

# Examples

**examples** folder contains examples for client and server use cases.  Also provided a **CalculatorService** implementation example to use.

## Server

Exposes an array of functions which retrieves and returns data.

```javascript
var mqtt = require('mqtt')
  , mqttrpc = require('mqtt-rpc')
  , debug = require('debug')('remote-time:server');

var settings = {
  reconnectPeriod: 5000 // chill on the reconnects
}

// client connection
var mqttclient = mqtt.connect('mqtt://localhost', settings);

// build a mqtt new RPC server
var server = mqttrpc.server(mqttclient);

// provide a new method
server.provide('Calculator', 'add', function (args, cb) {
  debug('Calculator:add.  args-> ', args);
  const calculator = new CalcService();//create calculator object w/ custom implementation.
  cb(null, calculator.add(args.params[0],args.params[1])); //call it's method.  args is to be supplied by callRemote method below!
});
```

## Client

Consumes the api exposed by the previous example.

```javascript
var mqtt = require('mqtt')
  , mqttrpc = require('mqtt-rpc')
  , debug = require('debug')('remote-time:client');

var settings = {
  reconnectPeriod: 5000 // chill on the reconnects
}

// client connection
var mqttclient = mqtt.connect('mqtt://localhost', settings);

// build a new RPC client
var client = mqttrpc.client(mqttclient);

//define arguments to be passed to server's *provided* function to make a rpc call.
const args = [1, 2];

// call the remote method
client.callRemote('CalculatorService', 'add', args, function(result, err, data){
  debug('callRemote', result, err, data);
});
```

# Debugging

This module uses the **debug** module.  To execute and see debug messages at the console, type at the command line:

```
DEBUG=* node client.js 
```

Examples folder has a good starting place for seeing how *server.js* & *client.js* communicate.

Additionally, the npm test script, **npm test**, can be debugged by typing:  

```
DEBUG=* npm test
```

## License
Copyright (c) 2013 Mark Wolfe
Licensed under the MIT license.