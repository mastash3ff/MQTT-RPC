import os, sys
sys.path.append(os.getcwd())
sys.path.insert(1, os.path.join(sys.path[0], '..'))
import paho.mqtt.client as mqtt
from lib.client import MQTTRPCClient
import logging
logging.basicConfig( filename = 'logs/client_flow.log', level=logging.INFO, filemode = 'w' )
                        
if __name__ == '__main__':
        logger = logging.getLogger(__name__)
        mqttClient = mqtt.Client()
        mqttClient.connect('localhost',1883)
        client = MQTTRPCClient( mqttClient, False )
        for i in range(10):
            #calls calc.add(1,2) and return result
            result = client.callRemote('CalculatorService','add',[1,2])  
            print("result-> " + str(result))

        '''        
        result = client.callRemote('CalculatorService','sub',[1,2])  
        print'result-> ', result
        result = client.callRemote('CalculatorService','mul',[1,2])  
        print'result-> ', result
        '''     

