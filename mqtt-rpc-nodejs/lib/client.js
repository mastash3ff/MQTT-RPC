'use strict';

/*******************************************************************************
 *******************************************************************************/

var crypto = require('crypto');
var mqtt = require('mqtt');
var mqttrouter = require('mqtt-router');
var debug = require('debug')('mqtt-rpc:client');

/**  
@class Client                                                                                                       
@param mqttclient {Mqtt} Creates rpc client to call remote methods 
*/
var Client = function (mqttclient) {
    this.mqttclient = mqttclient || mqtt.connect();
    this.hostName = mqttclient.options['href'];
    this.router = mqttrouter.wrap(mqttclient);
    this.inFlight = {};
    var self = this;

    /**
       Generator for random id's
       @method _generator
       @private
       @return {String} 
     */
    this._generator = function () {
	//console.log('generated ', crypto.randomBytes(5).readUInt32BE(0).toString(16));
	//return crypto.randomBytes(5).readUInt32BE(0).toString(16);
	return Math.floor(Math.random() * (100000 - 1) + 1 ).toString();
    };

    /**
       Helper method for handling callback reponses
       @method _handleResponse
       @param topic 
       @param message
       @private
     */
    this._handleResponse = function(topic, message) {
	const msg = JSON.parse(message);
	const id = msg.id;
	debug('handleResponse', topic, 'msg ', msg);
	
	if (id && self.inFlight[id]) {
	    debug('inflight result', msg);
	    //callback response for callRemote()
	    self.inFlight[id].cb( msg.result, msg.error, msg.id);
	    delete self.inFlight[id];
	}
    };

    /**
       Helper method for publishing a json message
       @method _sendMessage
       @private
    */
    this._sendMessage = function(prefix,name,params, cb) {
	const id = self._generator();
	self.inFlight[id] = {cb: cb};
	var msg = {};
	msg.id = id;
	msg.method = prefix + "/" + name;
	msg.params = params;
	debug('topic', prefix + "/" + name,'stringified', JSON.stringify(msg));
	self.mqttclient.publish(prefix + "/" + name + "/request",JSON.stringify(msg));
    };

    /**
       Asks server if an rpc method is available
       @method _isRpcMethodAvailable
       @param methodName {String} method being asked if available on server
       @param cb {Function} Callback that should be set to True/False
       @private
    */
    this._isRpcMethodAvailable = function(methodName, client, cb){
	var msg = {};
	msg.id = self._generator();
	msg.method = methodName;

	client.subscribe('isRpcMethodAvailable/reply');
	client.on('message', function (topic, message) {
	    var parsedMsg = JSON.parse(message);
	    //console.log('ids-> ',msg.id, parsedMsg.id);
	    if (msg.id === parsedMsg.id){
		if (parsedMsg.result === true){
		    cb(true);
		}
	    	else{
		    cb(false);
		}
	    }
	    //if no match, ignore
	});
	
	client.publish('isRpcMethodAvailable/request',JSON.stringify(msg));
    };

     /**
       Generates error message if rpc method is not found on server
       @method _genMsgErr
       @param cb {Function} Callback passed to function
       @private
    */
    this._genMsgErr = function(cb){
	var msg = {};
	msg.id = self._generator();
	msg.error = "Method Not Found";
	msg.result = null;
	cb( msg.result, msg.error, msg.id);
    };

    /**
       Calls remote method that is provided by rpc server.
       @method callRemote
       @param prefix {String} prefix for topic name
       @param name {String} name of the method to be provided for rpc calls
       @param params {Array} argument{s} to be supplied to function
       @param cb {Function} callback  
     */
    this.callRemote = function(prefix, name, params, cb){
	if (params[0] === null ){
	    throw "Parameter(s) supplied are empty";
	}
	/*
	var client = mqtt.connect(self.hostName);
	self._isRpcMethodAvailable(prefix+'/'+name, client, function (rpcAvail){
	    if (rpcAvail){
		const replyTopic = prefix + '/' + name + '/reply';
		self.router.subscribe(replyTopic, self._handleResponse);
		self._sendMessage( prefix, name, params, cb);
	    }
	    else{
		self._genMsgErr(cb);
	    }
	    client.end();
	});
	*/
	
	const replyTopic = prefix + '/' + name + '/reply';
	const requestTopic = prefix + '/' + name + '/request';
	debug('callRemote', 'requestTopic', requestTopic, 'prefix',prefix,'name',name,'params',params);
	self.router.subscribe(replyTopic, self._handleResponse);
	self._sendMessage( prefix, name, params, cb);
    };
}	  
module.exports = Client;
