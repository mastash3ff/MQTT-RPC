/*******************************************************************************
 *******************************************************************************/
'use strict';

var chai = require('chai');
var mqtt = require('mqtt');
var debug = require('debug')('test:mqtt-rpc:combined');
var expect = chai.expect;
var mqttrpc = require('../index.js');

const params = [ 1, 2 ];

describe('combined', function () {
    
    it('server respond to a request from the client', function (done) {
	const mqttclient = mqtt.connect();
	const prefix = 'CalculatorService';
	const server = mqttrpc.server(mqttclient);
	
	server.provide(prefix, 'add',function (data, cb) {
	    debug('add');
	    debug('data',data);
	    const params = data.params;
	    cb(null, params[0] + params[1]);
	});
	
	const client = mqttrpc.client(mqttclient);
	client.callRemote(prefix, 'add', params, function (data,err,id) {
	    debug('callRemote', data, err,id);
	    expect(data).to.exist;
	    expect(err).to.not.exist;
	    
	    client.callRemote(prefix, 'add', params, function (data,err,id) {
		debug('callRemote', data, err, id);
		expect(data).to.exist;
		expect(err).to.not.exist;
		done();
	    });
	});
    });
});
