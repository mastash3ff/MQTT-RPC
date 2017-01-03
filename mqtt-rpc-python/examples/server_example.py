import os, sys
from Calculator import CalculatorService as Calc            #our custom implementation to provide
sys.path.insert(1, os.path.join(sys.path[0], '..'))
from lib.server import MQTTRPCServer
import paho.mqtt.client as mqtt
import logging
logging.basicConfig(level=logging.INFO, filemode ='w', filename = 'logs/server_flow.log')

if __name__ == '__main__':
    calc = Calc()
    mqttClient = mqtt.Client()
    mqttClient.connect('localhost',1883)
    server = MQTTRPCServer( mqttClient, False)
    server.provide('CalculatorService', 'add', calc.add)  #calc.add is our implemented function
    #server.provideClass(calc)
    #server.provide('CalculatorService', 'sub', calc.sub)
    #server.provide('CalculatorService', 'mul', calc.mul)
    
