/*******************************************************************************
 *******************************************************************************/
'use strict';

var chai = require('chai');
var mqtt = require('mqtt');
var debug = require('debug')('test:mqtt-rpc:client');
var expect = chai.expect;

var Client = require('../lib/client.js');

var mqttclient; //initialized later

const params = [1,2];

describe('client', function () {
    before(function () {
	debug('mqttclient created');
	mqttclient = mqtt.connect();
    });
    
    it('should respond to a request', function (done) {
	var prefix = 'CalculatorService';
	var requestTopic = prefix + "/add/request";
	var replyTopic = prefix + '/add/reply';

	debug('requestTopic', requestTopic, 'replyTopic', replyTopic);
	mqttclient.subscribe(requestTopic)
	    .on('message', function (topic, message) {
		debug('requestTopic', topic);
		const requestMsg = JSON.parse(message);
		debug('requestMsg',requestMsg);
		var msg = JSON.stringify({ id: requestMsg.id, error: null, result: 3});
		debug('publish', replyTopic, msg);
		mqttclient.publish(replyTopic, msg);
	    });
	
	var client = new Client(mqtt.connect());
	client.callRemote(prefix, 'add', params, function(data, err, id){
	    debug('client.callRemote', data, err, id);
	    expect(data).to.exist;
	    expect(id).to.exist;
	    expect(err).to.not.exist;
	    done();
	});
    });
});


