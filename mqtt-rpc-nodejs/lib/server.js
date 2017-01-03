'use strict';

/*******************************************************************************
 *******************************************************************************/

var mqtt = require('mqtt');
var mqttrouter = require('mqtt-router');
var debug = require('debug')('mqtt-rpc:server');

/**
@class Server
@param mqttclient {Mqtt} Creates rpc server to handle requests and provide rpc functionality
*/
var Server = function (mqttclient) {
    this.mqttclient = mqttclient || mqtt.connect();//creates default if not present
    this.router = mqttrouter.wrap(mqttclient);
    this.rpcMethods = [];
    this.hostName = mqttclient.options['href'];
    this.isRpcMethodAvailClient = mqtt.connect(this.hostName);
    var self = this;

    //listens for method requests and responds if method available
    /*
    self.isRpcMethodAvailClient.on('message', function(topic,message){
	
	console.log('message-> ', message.toString());
	debug('isRpcMethodAvailClient', 'on message');
	var msg;

	try{
	    msg = JSON.parse(message);
	}
	catch(e){
	    console.log('Malformed JSON Message!  Exiting.');
	    process.exit();//TODO fail gracefully
	}
	var sendMsg = {};
	sendMsg.id = msg.id;
	
	const isAvail = self.getRpcMethods().indexOf(msg.method);
	if (isAvail !== -1) {
	    sendMsg.result = true;
	    self.isRpcMethodAvailClient.publish('isRpcMethodAvailable/reply',JSON.stringify(sendMsg));
	}
	else {
	    sendMsg.result = false;
	    self.isRpcMethodAvailClient.publish('isRpcMethodAvailable/reply',JSON.stringify(sendMsg));
	}
    });
    
    self.isRpcMethodAvailClient.subscribe('isRpcMethodAvailable/request');
    */
    /**
       When requests comes in, send off msg.  'err,result' is the cb's passed in.
       @method _handleReq
       @private
     */
    this._handleReq = function (id, prefix, name, err, result) {
	debug('_handleReq','id',id,'prefix',prefix,'name',name,'result',result);
	const replyTopic =  prefix + '/' + name + '/reply';
	const msg = {error: err, result: result, id: id};
	self.mqttclient.publish(replyTopic, JSON.stringify(msg));
    };

    /**
       Helper method to set callback
       @method _buildRequestHandler
       @private
     */
    this._buildRequestHandler = function (prefix, name, cb) {
	debug('buildRequestHandler', prefix, name);

	//topic,message corresponds to 'on message' in mqtt-router node module.
	return function (topic, message) { 
	    const msg = JSON.parse(message);
	    //console.log('msg->', msg);
	    const id = msg.id;
	    debug('handleMsg', topic, msg);
	    //corresponds to data,cb arguments in provide function
	    //console.log('cb-> ', cb);
	    cb.call(null, msg, self._handleReq.bind(null, id, prefix, name));
	};
    };

      /**
	 Used to provided functionality as an rpc server.  Method can be called by mqtt-rpc clients.
	 @method provide 
	 @param prefix {String} prefix for topic name
	 @param name {String} name of the method to be provided for rpc calls
	 @param cb {Function} functionality this rpc method provides
      */
    this.provide = function (prefix, name, cb) {
	debug('provide', prefix, name);
	const requestTopic = prefix + '/' + name + '/request';
	self.router.subscribe(requestTopic, self._buildRequestHandler(prefix,name, cb));
	//debug('provide','pushing->',prefix+'/'+name);
	//self.getRpcMethods().push(prefix+'/'+name);
    };

    /**
       Returns an array of rpc methods that have been added by the server via provide()
       @method getRpcMethods
       @return {Array} an Array of strings representing method names
    */
    /*this.getRpcMethods = function(){
	return self.rpcMethods;
    };*/
};

module.exports = Server;
