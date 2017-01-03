mqtt-rpc-python
===============

## About
Ability to make RPC calls like a client/server model utilizing MQTT as the messaging protocol. 

## Installation
Archive file of project can be found in *dist* folder

```
pip install mqtt_rpc_python-1.0.tar.gz
```

## Examples

The following examples are pulled from the *examples* folder


### Create an rpc server with provided functionality

**Example 1 providing a single function**

```python
from mqtt_rpc_python import MQTTRPCServer
from Calculator import CalculatorService as Calc  #our custom implementation to provide
calc = Calc()
provide('test', 'add', calc.add)                  #calc.add is our implemented function
```

**Example 2 providing multiple functions**

```python
from mqtt_rpc_python.server import MQTTRPCServer
from Calculator import CalculatorService as Calc  #our custom implementation to provide

calc = Calc()
provideClass(calc) #where each method in the class is then callable
```

### Call a remote method provided by a rpc server

```python
from mqtt_rpc_python.client import MQTTRPCClient

mqttClient = mqtt.Client()               #make standard mqtt client
mqttClient.connect('localhost',1883)
client = MQTTRPCClient( mqttClient )     #wrap it up now

result = callRemote('test','add',[1,2])  #calls calc.add(1,2) and return result
print'result-> ', result
```




