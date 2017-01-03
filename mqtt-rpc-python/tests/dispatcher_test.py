#!/usr/bin/python
import argparse
import time, random
import os,sys
sys.path.append(os.getcwd())
sys.path.insert(1, os.path.join(sys.path[0], '..'))
from lib import MQTTRPCResponseManager, dispatcher
import logging
import paho.mqtt.client as mqtt
import pdb
from examples.Calculator import CalcServiceImpl as Calc

if __name__ == '__main__':
    calc = Calc()
    dispatcher.build_method_map( calc )
