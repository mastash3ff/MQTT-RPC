#!/usr/bin/python
import argparse
import time, random
import os,sys
sys.path.append(os.getcwd())
sys.path.insert(1, os.path.join(sys.path[0], '..'))
from mqtt_rpc_python import MQTTRPCResponseManager, dispatcher
import paho.mqtt.client as mqtt
import json
import pdb
import logging
logging.basicConfig( level = logging.INFO )
logger = logging.getLogger(__name__)

#examples of using dispatcher to add methods
'''
@dispatcher.add_method
def add(*args):
    return args[0] + args[1]
'''
#or
'''
# Dispatcher is dictionary {<method_name>: callable}
dispatcher[("test", "echo")] = lambda s: s
'''
#dispatcher[("test", "add")] = lambda a, b: a + b

class MQTTRPCServer(object):
    def __init__(self, client, debug = None):
        self.client = client
        self.isDebug = False

        if (debug is True):
            self.isDebug = True
            
    def on_mqtt_message(self, mosq, obj, msg):
        jsonDict = json.loads(msg.payload)    #parts is a dict
        parts = jsonDict['method'].split('/') #break up service and method name
        service_id = parts[0]
        method_id = parts[1]
        response = MQTTRPCResponseManager.handle(msg.payload, service_id, method_id, dispatcher)
        if (self.isDebug is True):
            logger.info('response-> ', response)
            logger.info("publishing to %s/%s/reply", service_id, method_id)
        self.client.publish("%s/%s/reply" % (service_id, method_id), response.json)

    def setup(self):
        for service, method in dispatcher.keys():
            self.client.publish("%s/%s" % (service, method), "1", retain=True)
            if (self.isDebug is True):
                logger.info('setup: listening on subscriber %s/%s', service, method)
            self.client.subscribe("%s/%s/request" % (service, method))

    #possibly combine provide and provideClass functionality
    
    def provide(self, serviceName, operation, cb):
        dispatcher[(serviceName, operation)] = cb
        self.client.on_message = self.on_mqtt_message
        self.setup()
        self.client.loop_forever()
        
    def provideClass(self,cb):
        dispatcher.build_method_map( cb )
        self.client.on_message = self.on_mqtt_message
        self.setup()
        self.client.loop_forever()

if __name__ == '__main__':
    #place dispatcher methods here to test ELSE server does nothing when published to
    dispatcher[("test", "add")] = lambda a, b: a + b
    
    parser = argparse.ArgumentParser(description='Sample RPC server', add_help=False)
    parser.add_argument('-h', '--host', dest='host', type=str,
                        help='MQTT host', default='localhost')
    parser.add_argument('-u', '--username', dest='username', type=str,
                        help='MQTT username', default='')
    parser.add_argument('-P', '--password', dest='password', type=str,
                        help='MQTT password', default='')
    parser.add_argument('-p', '--port', dest='port', type=int,
                        help='MQTT port', default='1883')
    
    args = parser.parse_args()
    client = mqtt.Client()
    
    if args.username:
        client.username_pw_set(args.username, args.password)

    rpc_server = MQTTRPCServer(client)
    client.connect(args.host, args.port)
    client.on_message = rpc_server.on_mqtt_message
    rpc_server.setup()
    
    client.loop_forever()

