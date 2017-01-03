# mqtt-rpc-java

This library lets you make distributed remote procedure calls and get results back, with plain Java.

- Requires Java 8.
- Remote procedure calls go over MQTT, using Eclipse Paho.
- Remote procedure calls' content is streamed in JSON.
- You can publish and call standard Java interfaces.
- Source code builds with maven.

## References

- MQTT client implementation used is [Eclipse Paho](https://eclipse.org/paho/)
- MQTT broker implementation used for testing is [Mosquitto](http://mosquitto.org/)
- Build tool used is [Apache Maven](http://maven.apache.org/)

## Services

Define the interface of your service(s)

```java
public interface CalculatorService {

    public Integer calculate(Integer a, Integer b);

}
```

Instantiate a client, the client will require an MQTT broker URL.

```java
MqttRpcClient client = new MqttRpcClientBuilder().build("tcp://iot.eclipse.org:1883");
client.connect();
```

Publish the service, this will create MQTT subscriptions with a lambda expression.

```java
client.publish(CalculatorService.class, // functionality to expose
               (a, b) -> a + b);        // implementation of our service
```

or the more intuitive route...

```java
client.publish(CalculatorService.class, new CalcServiceImplementation());
```

## Clients

Instantiate a client, the client will require an MQTT broker URL.

```java
MqttRpcClient client = new MqttRpcClientBuilder().build("tcp://iot.eclipse.org:1883");
client.connect();
```

## Examples

com.ispa.rpc.mqttrpc.RemoteCalcTest.java contains several use case examples for communicating between Java and NodeJS mqtt-rpc module.

Call a Service.

```java
		MqttRpcClient client = new MqttRpcClientBuilder().build(LOCALHOST);
		client.connect();
		
		client.provide(CalculatorService.class, new CalculatorImpl());
		
		List<Object> sampleArray = new ArrayList<>();
		sampleArray.add(6);
		sampleArray.add(4);
		
		Object result = client.callRemote(CALC_SERVICE_NAME, "add", sampleArray.toArray());

```

Provide a service that can be called by Java or Nodejs
```java
		MqttRpcClient client = new MqttRpcClientBuilder().build(LOCALHOST);
		client.connect();
		
		client.provide(CalculatorService.class, new CalculatorImpl());

```

## Requests and Responses

Request bodies are defined as a JSON array containing:

- The arguments, as part of the array

Response bodies are defined as a JSON Object containing:

- The result
- ID
- Error

# Development
## Building the Jar with Maven

mvn package