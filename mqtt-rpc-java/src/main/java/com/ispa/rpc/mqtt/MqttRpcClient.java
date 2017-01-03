/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

import com.ispa.rpc.generic.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 * Allows publishing services and invoking remote services over MQTT.  
 *
 * @author Philipp Gayret & Brandon Sheffield
 */
public class MqttRpcClient implements MqttCallback {

	static Logger logger = org.slf4j.LoggerFactory.getLogger(MqttRpcClient.class);
	private final ServiceHost serviceHost;
	private final MqttRpcTopicBuilder topicBuilder;
	private final MqttClient mqttClient;
	private final RpcPacketStreamer rpcPacketStreamer;
	private final ExecutorService executorService;
	private final MqttRpcFailureHandler mqttDrpcFailureHandler;
	private final MqttConnectOptions connectOptions;
	private final int qualityOfServiceLevel;
	private static final String RPC_AVAIL_REQUEST_TOPIC = "isRpcMethodAvailable/request";
	private static final String REPLY_TOPIC = "isRpcMethodAvailable/reply";

	/**
	 * Delegates disconnect errors to the failure handler.
	 */
	@Override
	public void connectionLost(Throwable throwable) {
		mqttDrpcFailureHandler.handleDisconnect(throwable);
	}

	/**
	 * Delegates incoming messages to the service host.
	 */
	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
		serviceHost.accept(topic, new ByteArrayInputStream(mqttMessage.toString().getBytes()));
	}

	/**
	 * Null implementation, delivery is assured by quality of service level.
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}

	/**
	 * Connects the internal {@link #mqttClient} to the broker, automatically begins listening for callbacks.
	 *
	 * @throws MqttException
	 */
	public void connect() throws MqttException {
		mqttClient.connect(connectOptions);
	}

	/**
	 * Creates a new task out of a given task body, schedules it, and awaits its completion.
	 *
	 * @param taskBody task body to execute
	 */
	private void await(MqttRpcTask.TaskBody taskBody) {
		MqttRpcTask mqttDrpcTask = new MqttRpcTask(executorService, mqttDrpcFailureHandler, taskBody);
		mqttDrpcTask.start();
		mqttDrpcTask.join();
	}

	/**
	 * Provides a service that has it's implementation defined.
	 * @param type           type to publish, and class' defined methods to allow access to
	 * @param implementation remote procedure call receiver
	 * @param <T>            type of implementation
	 * @return closeable to use for unregistering
	 */
	public <T> SilentCloseable provide(Class<T> type, T implementation) {
		//extract methods and initialize arrays for settings later and receivers are setup
		Method[] methods = type.getMethods();
		SilentCloseable[] closeables = new SilentCloseable[methods.length];
		String[] topics = new String[methods.length];
		List<String> serviceNames = new ArrayList<String>(); //TODO change to array

		
		//setup receivers for request and response
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			MessageReceiver receiver = (String topic, InputStream content) -> {
				try {
					List<Class<?>> parameterTypes = Arrays.asList(method.getParameterTypes());
					RpcRequestPacket request = rpcPacketStreamer.readRequest(content, parameterTypes);
					try {
						Object result = method.invoke(implementation, request.getBody());
						String callback = topicBuilder.callback(request.getClientId());
						RpcResponsePacket response = new RpcResponsePacket(request.getCorrelationId(), new Object[]{result});
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						rpcPacketStreamer.writeResponse(outputStream, response);
						MqttMessage message = new MqttMessage(outputStream.toByteArray());
						JSONArray ja = new JSONArray(message.toString());
						//TODO error checking for JSONArray values?
						JSONObject jo = new JSONObject().put("id", Integer.parseInt(ja.get(0).toString())).put("result", ja.get(1)).put("error", JSONObject.NULL);
						MqttMessage msg = new MqttMessage(jo.toString().getBytes());
						message.setQos(qualityOfServiceLevel);
						await(() -> {
							mqttClient.publish(callback, msg);
						});
					} catch (ReflectiveOperationException e) {
						throw new MqttRpcRuntimeException("Erred invoking a service method.", e);
					}
				} catch (IOException e) {
					throw new MqttRpcRuntimeException("Deserialization of response message body failed.", e);
				}
			};
			//register each receiver as a service and store.
			String operation = topicBuilder.operationRequest(type, method);
			serviceNames.add(topicBuilder.operation(type, method)); //TODO make into an array
			SilentCloseable service = serviceHost.register(operation, receiver);
			topics[(i)] = operation;
			closeables[(i)] = service;
		}
		//TODO
		//setupRpcAvailSubscriber(this.mqttClient.getServerURI(),serviceNames);
		await(() -> mqttClient.subscribe(topics));
		return () -> {
			for (SilentCloseable closeable : closeables) {
				closeable.close();
			}
			await(() -> mqttClient.unsubscribe(topics));
		};
	}

	/**
	 * Function used to call rpc methods provided by custom MQTT-RPC nodejs module.  
	 * @param prefix Name of Service.
	 * @param name   Name of operation belonging to service to be called.
	 * @param objArray contains the following: cb_topic, request_uuid, arguments
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public Object callRemote(String serviceName, String opName, Object[] objArray) throws MqttPersistenceException, MqttException{
		return this.callRemote(serviceName, opName, objArray, false);
	}

	/**
	 * Debugging method for @see {@link MqttRpcClient#callRemote(String, String, Object[])}
	 * @param prefix Name of Service.
	 * @param name   Name of operation belonging to service to be called.
	 * @param objArray contains the following: cb_topic, request_uuid, arguments...
	 * @param isDebug determines whether messages are logged to console.
	 * @return {@link Object} the return value of the rpc call.
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public Object callRemote(String serviceName, String opName, Object[] objArray, Boolean isDebug) throws MqttPersistenceException, MqttException{
		final String FULL_SERVICE_NAME = serviceName + '/' + opName;

		//if rpc method not available, return this json-rpc message object telling so.
		//TODO
		/*
		if (!isRPCMethodAvailable(FULL_SERVICE_NAME)){
			return "Method Not Found"; //TODO
		}
		*/
		//sits out there listening for JSON result message
		if (isDebug)
			logger.info("server uri " + this.mqttClient.getServerURI());

		MqttClientInterface client;
		if (isDebug){
			client = new MqttVerboseClient( this.mqttClient.getServerURI(), generateRandomId(), isDebug);
		}
		else{
			client = new MqttSilentClient(this.mqttClient.getServerURI(), generateRandomId());
		}
		client.connect();
		client.subscribe(topicBuilder.callback(FULL_SERVICE_NAME));

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("method", FULL_SERVICE_NAME);
		jsonRequest.put("params", objArray);
		jsonRequest.put("id", generateRandomId());

		MqttMessage jsonMsg = new MqttMessage(jsonRequest.toString().getBytes());
		jsonMsg.setQos(0); //no reliability of delivery

		if (isDebug){
			logger.info("publishing to this request-> " + FULL_SERVICE_NAME + "/request with this JSON msg-> " + jsonRequest.toString());
		}
		client.publish( FULL_SERVICE_NAME + "/request", jsonMsg);
		Object result = client.pollResult();

		//if pollResult times out
		if (result == null){
			result = "Timeout";
		}

		return result;
	}

	/**
	 * 
	 * @return
	 * @throws MqttException
	 */
	//TODO SECOND PARAMAETER= CB eventually for asynchronous
	private boolean isRPCMethodAvailable(String methodName) throws MqttException{
		JSONObject requestJo = new JSONObject();
		requestJo.put("id", generateRandomId());
		requestJo.put("method", methodName);

		//setup listener for response
		MqttRpcMethodAvailListener responseClient = new MqttRpcMethodAvailListener(this.mqttClient.getServerURI(),generateRandomId());
		responseClient.connect();
		responseClient.subscribe(REPLY_TOPIC);

		//ask whether method available
		MqttClient client = new MqttClient(this.mqttClient.getServerURI(), generateRandomId(), new MemoryPersistence());
		client.connect();
		client.publish(RPC_AVAIL_REQUEST_TOPIC, new MqttMessage(requestJo.toString().getBytes()));
		//client.disconnect();

		Object result = responseClient.pollResult();

		Boolean isAvail = new Boolean(new String(result.toString()));
		return isAvail;
	}

	/**
	 * Helper method to generate random number string.
	 * @return
	 */
	private String generateRandomId(){
		Random rand = new Random();
		return String.valueOf(rand.nextInt(100000 + 1));
	}

	/**
	 * Helper method to setup mqtt client that listens and manages rpc methods available.
	 * @return
	 */
	private boolean setupRpcAvailSubscriber(String broker, List<String> serviceNames){
		try {
			MqttRpcMethodAvailClient rpcAvailClient = new MqttRpcMethodAvailClient(broker,generateRandomId(),serviceNames);
			rpcAvailClient.connect();
			rpcAvailClient.subscribe(RPC_AVAIL_REQUEST_TOPIC);
		} catch (MqttException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * The recommended way to create an instance is with {@link com.ispa.rpc.mqtt.MqttRpcClientBuilder}.
	 * @param mqttDrpcFailureHandler handles errors that are caught.
	 * @param executorService executes and manages processes concurrently.
	 * @param mqttClient standard mqttclient to be supplied and wrapped.
	 * @param topicBuilder Defines how the topics are built.
	 * @param serviceHost A simple service host, allowing for services to be registered by identifier, and connected to.
	 * @param callbackHost Simple service host that listens for callback results on from rpc calls.
	 * @param rpcPacketStreamer A general utility class for reading and writing packets using a given.
	 * @param connectOptions options that are set to the mqtt client.
	 * @param qualityOfServiceLevel defines the mqtt qos.
	 */
	public MqttRpcClient(MqttRpcFailureHandler mqttDrpcFailureHandler, ExecutorService executorService, MqttClient mqttClient,
			MqttRpcTopicBuilder topicBuilder, ServiceHost serviceHost, ServiceHost callbackHost, RpcPacketStreamer rpcPacketStreamer,
			MqttConnectOptions connectOptions, int qualityOfServiceLevel) {
		this.connectOptions = connectOptions;
		this.qualityOfServiceLevel = qualityOfServiceLevel;
		this.mqttDrpcFailureHandler = mqttDrpcFailureHandler;
		this.executorService = executorService;
		this.topicBuilder = topicBuilder;
		this.serviceHost = serviceHost;
		this.rpcPacketStreamer = rpcPacketStreamer;
		//deleted callbackHost
		this.mqttClient = mqttClient;
		this.mqttClient.setCallback(this);
		String callback = this.topicBuilder.callback(this.mqttClient.getClientId());
		this.serviceHost.register(callback, (topic, content) -> {
			content.mark(Integer.MAX_VALUE);
			RpcResponsePacket rpcResponsePacketHeaders = rpcPacketStreamer.readResponse(content, Collections.emptyList());
			content.reset();
			callbackHost.accept(rpcResponsePacketHeaders.getCorrelationId(), content);
		});
	}
}
