/*******************************************************************************
 *******************************************************************************/
'use strict';
var mqtt = require('mqtt');
var mqttrpc = require('../index.js');
var debug = require('debug')('remote-calculator:server');
//var client = mqtt.connect();//currently defaults to localhost
var client = mqtt.connect('mqtt://test.mosquitto.org');
//var client = mqtt.connect('mqtt://test.mosquitto.org');
var server = mqttrpc.server(client);
var CalcService = require('./CalculatorService');

//user provides class that has implementation to rpc server
const calculator = new CalcService();
/*
//used to see result messages on arrival
client.on('message',function (topic, message){
    debug('message-> ', message.toString());
    //client.end(); //disconnects client on first msg received
});

//listen for requests(aka debugging messages)
client.subscribe('CalculatorService/add');
*/
server.provide('CalculatorService', 'add', function (data, cb) {
    debug('add', 'data',data);
    const arg1 = data.params[0];
    const arg2 = data.params[1];

    //null represents error
    cb(null, calculator.add(arg1, arg2));
});

server.provide('CalculatorService', 'sub', function (data,cb){
    debug('sub','data', data);
    const arg1 = data.params[0];
    const arg2 = data.params[1];
    
    cb(null, calculator.sub(arg1, arg2));
});


