import paho.mqtt.client as mqtt
import json

topic = "CalculatorService/add"

mqttClient = mqtt.Client()

mqttClient.connect('localhost',1883)
msg = json.dumps({"id":1234, "params": [1,2], "method":"add"})

mqttClient.publish(topic,msg)

