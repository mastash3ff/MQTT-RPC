import json
import paho.mqtt.client as mqtt
import threading
from .protocol import MQTTRPC10Response
from jsonrpc.exceptions import JSONRPCError

class TimeoutError(Exception):
    pass

class AsyncResult(object):
    def __init__(self):
        self._event = threading.Event()
        self._result = None
        self._exception = None

    def set_result(self, result):
        self._result = result
        self._event.set()

    def set_exception(self, exception):
        self._exception = exception
        self._event.set()

    def result(self, timeout=None):
        if self._event.wait(timeout):
            return self._result
        else:
            raise TimeoutError()

    def exception(self, timeout=None):
        if self._event.wait(timeout):
            return self._exception
        else:
            raise TimeoutError()

class MQTTRPCClient(object):
    """Used for constructing an RPC Client to communicate with RPC Server """
    def __init__(self, client, debug=None):

        self.client = client
        self.counter = 0
        self.futures = {}
        self.subscribes = set()
        self.isDebug = False
        if (debug is True):
            self.isDebug = True
                    
    def on_mqtt_message(self, mosq, obj, msg):
        """ return True if the message was indeed an rpc call"""
        if (self.isDebug is True):
            logger.info('msg payload',msg.payload)
        topicDelim = [ x for x in msg.topic.split('/')]
        service_id = topicDelim[0]
        method_id  = topicDelim[1]

        result = MQTTRPC10Response.from_json(msg.payload)

        future = self.futures.pop((service_id, method_id, result._id), None)
        if future is None:
            return True

        if result.error:
            future.set_exception(RuntimeError(result.error))

        future.set_result(result.result)

        return True

    def callRemote(self, service, method, params, timeout=None):
        """Used to call RPC methods provided by server via provide(..) function """
        self.client.loop_start() #used for keeping networking alive in mqtt python
        self.client.on_message = self.on_mqtt_message
        
        future = self.call_async( service, method, params)

        try:
            result = future.result(1E100 if timeout is None else timeout)
        except TimeoutError as err:
            # delete callback
            self.futures.pop((service, method, future.packet_id), None)
            raise err
        else:
            return result

    #here it does the actual sending of the json message!
    def call_async(self, service, method, params):
        self.counter += 1
        result = AsyncResult()
        result.packet_id = self.counter
        self.futures[(service, method, self.counter)] = result
        topic = '%s/%s' % (service, method)
        subscribe_key = (service, method)
        payload = {'params': params,
                   'id' : self.counter,
                   'method' : topic } #using topic because need servicename+operation
        if (self.isDebug):
            logger = logging.getLogger(__name__)
            logger.info("payload constructed %s", str(payload))

        if subscribe_key not in self.subscribes:
            self.subscribes.add(subscribe_key)
            self.client.subscribe(topic + '/reply')
        self.client.publish(topic + '/request', json.dumps(payload))

        return result
