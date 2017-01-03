import sys
import paho.mqtt.client as mqtt
import json

topic = "CalculatorService/add"

def on_message(mosq,obj,msg):
    #print 'msg payload-> ', msg.payload
    #print 'msg topic  -> ', msg.topic
    message = json.loads(msg.payload)
    #print'msssssg ', message, 'type of', type(message)
    
    if (type(message) is dict):
        print'message-> ',message['params']
    
mqttClient = mqtt.Client()
        
#mqttClient.message_callback_add(topic, on_message)
mqttClient.on_message = on_message
mqttClient.connect('localhost',1883)
mqttClient.subscribe(topic)

mqttClient.loop_forever()    
        
