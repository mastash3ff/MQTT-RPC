/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

import java.util.Random;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brandon Sheffield - 
 * Created on  Jan 4, 2016
 * Description:  Log's to console actions taken by this MQTT client.
 */
public class MqttVerboseClient implements MqttCallback, MqttClientInterface {

	private MqttClient client;
	private Object result;
	private Object id;
	private static Logger logger = LoggerFactory.getLogger(MqttVerboseClient.class);
	private Boolean isVerbose = false;
	private MemoryPersistence memPersist = new MemoryPersistence();


	private Boolean isMsgReceived = false;
	private Object error;
	private String jsonResponseMsg;
	private long DEFAULT_TIMEOUT = 10000;
	private final static String DEFAULT_HOST = "tcp://localhost:1883";

	/**
	 * 
	 * @throws MqttException
	 */
	public MqttVerboseClient() throws MqttException {
		this.client = new MqttClient(DEFAULT_HOST, generateRandomId(), memPersist);
		client.setCallback(this);
		isVerbose = true;
	}

	private String generateRandomId(){
		Random rand = new Random();
		return String.valueOf(rand.nextInt(100000 + 1));
	}

	/**
	 * 
	 * @throws MqttException
	 */
	public MqttVerboseClient(Boolean verbosity) throws MqttException {
		this.client = new MqttClient(DEFAULT_HOST, generateRandomId(), memPersist);
		client.setCallback(this);
		isVerbose = verbosity;
	}

	/**
	 * 
	 * @param brokerUrl represents the address location of where the MQTT broker is located at.
	 * @param clientId  a unique id representing this mqtt client.
	 * @throws MqttException
	 */
	public MqttVerboseClient(String brokerUrl, String clientId) throws MqttException{
		this.client = new MqttClient(brokerUrl, clientId, memPersist);
		client.setCallback(this);
	}

	/**
	 * 
	 * @param brokerUrl represents the address location of where the MQTT broker is located at.
	 * @param clientId  a unique id representing this mqtt client.
	 * @throws MqttException
	 */
	public MqttVerboseClient(String brokerUrl, String clientId, MemoryPersistence persist) throws MqttException{
		this.client = new MqttClient(brokerUrl, clientId, persist);
		client.setCallback(this);
	}

	/**
	 * 
	 * @param brokerUrl represents the address location of where the MQTT broker is located at.
	 * @param clientId  a unique id representing this mqtt client.
	 * @param isVerbose determines whether there is debug output to console.
	 * @throws MqttException
	 */
	public MqttVerboseClient(String brokerUrl, String clientId, Boolean isVerbose) throws MqttException{
		this.client = new MqttClient(brokerUrl, clientId);
		client.setCallback(this);
		this.isVerbose = isVerbose;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
	 */
	@Override
	public void connectionLost(Throwable arg0) {
		if (isVerbose)
			logger.info("Connection lost");
	}

	/**
	 * 
	 * @throws MqttException
	 */
	public void disconnect() throws MqttException{
		client.disconnect();
		if (isVerbose)
			logger.info("Disconnected");
	}

	/**
	 * 
	 * @param topicName
	 * @param msg
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public void publish(String topicName, MqttMessage msg) throws MqttPersistenceException, MqttException{
		client.publish(topicName, msg);
		if (isVerbose)
			logger.info("Published: " + msg.toString());
	}

	/**
	 * 
	 * @throws MqttSecurityException
	 * @throws MqttException
	 */
	public void connect() throws MqttSecurityException, MqttException{
		MqttConnectOptions connectOpts = new MqttConnectOptions();
		connectOpts.setCleanSession(true);
		client.connect(connectOpts);
		if (isVerbose)
			logger.info("Connected.");
	}

	/**
	 * 
	 * @param topic the mqtt topic to subscribe to
	 * @throws MqttException
	 */
	public void subscribe(String topic) throws MqttException{
		if (isVerbose)
			logger.info("Subscribed to: " + topic);
		client.subscribe(topic);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {}

	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
	 */
	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		if (isVerbose)
			logger.info("message arrived-> " + arg1.toString());

		Object json = new JSONTokener(arg1.toString()).nextValue(); //used for type determining
		if (this.isVerbose){
			if (json instanceof JSONArray){
				logger.info("JSONArray detected.  Extracting contents...");
				JSONArray ja = new JSONArray(arg1.toString());
				this.id = ja.get(0);
				this.result = ja.get(1);
			}
			else if (json instanceof JSONObject){
				logger.info("JSONObject detected.  JSONArray detected.  Extracting contents...");
				JSONObject jo = new JSONObject(arg1.toString());
				this.id = jo.get("id");
				this.result = jo.get("result");
				this.error = jo.get("error");
			}
			else{
				logger.info("well shit...Message received is not JSON!");
				return;
			}
		}
		else
		{
			if (json instanceof JSONArray){
				JSONArray ja = new JSONArray(arg1.toString());
				this.id = ja.get(0);
				this.result = ja.get(1);
			}
			else if (json instanceof JSONObject){
				JSONObject jo = new JSONObject(arg1.toString());
				this.id = jo.get("id");
				this.result = jo.get("result");
				this.error = jo.get("error");
			}
			else
				return;
		}
		JSONObject jo = new JSONObject();
		if (this.error != null) //check to see if 'error' was set.
			jo.put("error", this.error);
		else
			jo.put("error", 0);

		jo.put("id", this.id);
		jo.put("result", this.result);
		this.setJsonResponseMsg(jo.toString());

		if (this.isVerbose){
			logger.info("jsonMsgReceived-> " + jo.toString());
			logger.info("result-> " + this.result);
			logger.info("set isMsgReceived to true");
		}
		this.isMsgReceived = true;
	}

	/**
	 * @return the lastMessage received
	 */
	public Object pollJSONResponseMsg() {
		if (this.jsonResponseMsg == null){
			while (this.isMsgReceived != true){
				//do nothing
			}
		}
		this.isMsgReceived = false;
		return this.jsonResponseMsg.toString();
	}

	/**
	 * @return the lastMessage received
	 */
	public Object pollResult() {
		//TODO add a default timeout or custom passed one..
		if (this.isVerbose)
			logger.info("polling result");
		if (this.result == null){
			long startTime = System.currentTimeMillis();
			while (this.isMsgReceived != true && (System.currentTimeMillis()-startTime)<DEFAULT_TIMEOUT){
				try {
					//if no sleep, poll doesn't work correctly.
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		this.isMsgReceived = false;
		return this.result;
	}

	/**
	 * 
	 * @return the id received in the JSON message.
	 */
	public Object getId(){
		return this.id;
	}

	/**
	 * @return the error if set from within the JSON message
	 */
	public Object getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(Object error) {
		this.error = error;
	}

	/**
	 * @return the jsonResponseMsg representing the response
	 */
	public String getJsonResponseMsg() {
		return jsonResponseMsg;
	}

	/**
	 * @param jsonResponseMsg the jsonResponseMsg to set
	 */
	public void setJsonResponseMsg(String jsonResponseMsg) {
		this.jsonResponseMsg = jsonResponseMsg;
	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#connect(org.eclipse.paho.client.mqttv3.MqttConnectOptions)
	 */
	@Override
	public void connect(MqttConnectOptions opts) throws MqttSecurityException, MqttException {
		this.client.connect(opts);
	}

	/**
	 * @return the isVerbose
	 */
	public Boolean getIsVerbose() {
		return isVerbose;
	}

	/**
	 * @param isVerbose the isVerbose to set
	 */
	public void setIsVerbose(Boolean isVerbose) {
		this.isVerbose = isVerbose;
	}
}
