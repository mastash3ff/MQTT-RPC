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

/**
 * @author Brandon Sheffield - 
 * Created on  Jan 27, 2016
 * Description:  Opposite of @see {@link MqttSilentClient}.  More efficient since less branching.
 */

public class MqttSilentClient implements MqttCallback, MqttClientInterface {

	private MqttClient client;
	private Object result;
	private Object id;
	private Boolean isMsgReceived = false; //TODO eventually steer away to event driven notification...
	private Object error;
	private String jsonResponseMsg;
	private final static String DEFAULT_HOST = "tcp://localhost:1883";
	private MemoryPersistence memPersist = new MemoryPersistence();

	/**
	 * 
	 * @throws MqttException
	 */
	public MqttSilentClient() throws MqttException {

		this.client = new MqttClient(DEFAULT_HOST, generateRandomId(), memPersist);
		client.setCallback(this);
	}

	private String generateRandomId(){
		Random rand = new Random();
		return String.valueOf(rand.nextInt(100000 + 1));
	}

	/**
	 * Generates a client with given broker URL and unique client ID.
	 * @param broker
	 * @param clientId
	 * @throws MqttException 
	 */
	public MqttSilentClient(String broker, String clientId) throws MqttException{
		this.client = new MqttClient(broker,clientId, memPersist);
		client.setCallback(this);
	}

	public MqttSilentClient(String broker, String clientId, MemoryPersistence persist) throws MqttException{
		this.client = new MqttClient(broker,clientId, persist);
		client.setCallback(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
	 */
	@Override
	public void connectionLost(Throwable arg0) {
	}

	/**
	 * Disconnect client from MQTT broker
	 * @throws MqttException
	 */
	public void disconnect() throws MqttException{
		client.disconnect();
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
	}

	/**
	 * 
	 * @param topic
	 * @throws MqttException
	 */
	public void subscribe(String topic) throws MqttException{
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
		//used for json type determination
		Object json = new JSONTokener(arg1.toString()).nextValue(); 
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
			return; //ignore non json object and return to listen for next message

		JSONObject jo = new JSONObject();

		if (this.error != null) //check to see if 'error' was set.
			jo.put("error", this.error);
		else
			jo.put("error", JSONObject.NULL);

		jo.put("id", this.id);
		jo.put("result", this.result);
		this.setJsonResponseMsg(jo.toString());

		this.isMsgReceived = true;
	}

	/**
	 * @return the lastMessage
	 */
	public Object pollResult() {

		if (this.result == null){
			while (this.isMsgReceived != true){
				//wait for message arrival
				try {
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
	 * @return
	 */
	public Object getId(){
		return this.id;
	}

	/**
	 * @return the jsonResponseMsg
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
}
