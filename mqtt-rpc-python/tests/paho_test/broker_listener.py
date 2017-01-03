import paho.mqtt.client as mqtt

#callback for when client receives CONNACK response from server.
def on_connect(client, userdata, rc):
    print("Connected with the result code " + str(rc))
    #subscribing in on_connect() means that if we lose the connect
    #& reconnect then subscriptions will be renewed

    client.subscribe("$SYS/#");

#the cb for when a publish message is received from the server.
def on_message(client,userdata,msg ):
    print(msg.topic + " " + str(msg.payload))

client = mqtt.Client()

#overrides default implementations with our custom ones
client.on_connect = on_connect
client.on_message = on_message

client.connect("localhost", 1883, 60)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()
