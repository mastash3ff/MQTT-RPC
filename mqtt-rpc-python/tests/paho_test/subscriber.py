
import paho.mqtt.client as mqtt

def on_connect(client,userdata,rc):
    print("Connected with result code " + str(rc))

    #everytime time it connects, resubscribe
    client.subscribe("test")

def on_message(client,userdata,msg):
    print("Received msg on " + msg.topic + " with contents-> " + str(msg.payload))

client = mqtt.Client()
#overrides
client.on_connect = on_connect
client.on_message = on_message

client.connect("localhost")

client.loop_forever()
    
    
