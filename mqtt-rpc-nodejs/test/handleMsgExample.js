/*******************************************************************************
 *******************************************************************************/
'use strict'

var mqtt = require('mqtt');
var client = mqtt.connect();


client.handleMessage = function(packet,callback){
    console.log('sup');
    callback();
};

client.subscribe('test');

