/*******************************************************************************
 *******************************************************************************/
'use strict';

var crypto = require('crypto');
var chai = require('chai');
var mqtt = require('mqtt');
var debug = require('debug')('test:mqtt-rpc:server');
var expect = chai.expect;

const Server = require('../lib/server.js');
const params = [1, 2];

function generator () {
  return crypto.randomBytes(5).readUInt32BE(0).toString(16);
}

describe('server', function () {
  it('should respond to a request', function (done) {
      debug('createClient');
      const client = mqtt.connect();
      const prefix = 'CalculatorService';
      const id = generator();
      const replyTopic = prefix + '/add/reply';
      const requestTopic = prefix + '/add/request';
      
      client.subscribe(replyTopic)
	  .on('message', function (topic, message) {
              // this sees all subscriptions so be careful.
              if (topic === replyTopic) {
		  debug('message!!!!!', topic, message);
		  done();
              }
	  });
      
      var server = new Server(client);
      server.provide(prefix, 'add', function (args, cb) {
	  debug('add',args);
	  const params = args.params;
	  debug('params',params);
	  cb(null, params[0] + params[1]);
      });

      debug('publish', requestTopic);
      client.publish(requestTopic, JSON.stringify(
	  {params: params, method:'add', id: id}
      ));
  });
});


