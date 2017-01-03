/*******************************************************************************
 *******************************************************************************/
'use strict';
var mqtt = require('mqtt');
var mqttrpc = require('../index.js');
var debug = require('debug')('remote-calculator:client');
//var mqttclient = mqtt.connect();
var mqttclient = mqtt.connect('tcp://test.mosquitto.org');
//var mqttclient = mqtt.connect("tcp://192.168.10.244");
var client = mqttrpc.client(mqttclient);

const params = [6,4]
/*
client.callRemote('CalculatorService','add',params, function(result, err, id){
    debug('callRemote', result, err, id);
    console.log('result of add is-> ' + result);
});


client.callRemote('CalculatorService','sub',params, function(result,err,id){
    debug('callRemote',result, err, id);
    console.log('result of sub is-> ' + result);
});

client.callRemote('CalculatorService','ErrorMessage',params,function(result, err, id){
    debug('callRemote','should error');
    console.log('error-> ' + err);
});
*/
for (var i = 0; i < 50; ++i){
    client.callRemote('CalculatorService','add',params, function(result, err, id){
	debug('callRemote', result, err, id);
	console.log('result of add is-> ' + result);
    });
}

/*
for (var i = 0; i < 50; ++i){
    client.callRemote('CalculatorService','sub',params, function(result,err,id){
	debug('callRemote',result, err, id);
	console.log('result of sub is-> ' + result);
    });
}
*/
