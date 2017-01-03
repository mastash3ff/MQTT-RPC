/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brandon Sheffield - 
 * Created on  Feb 23, 2016
 * Description:  Handles whether an RPC method is available or not.
 */
public class MqttRpcMethodAvailClient implements MqttCallback, MqttClientInterface{ 

	private MqttClient mqttClient;
	private static final String REPLY_TOPIC =   "isRpcMethodAvailable/reply";
	private List<String> serviceNames = new ArrayList<>();
	private MemoryPersistence memPersist = new MemoryPersistence();
	private Queue<String> queue = new LinkedList<>();
	static Logger logger = LoggerFactory.getLogger(MqttRpcClient.class);

	/**
	 * Generates a client with given broker URL and unique client ID.
	 * @param broker
	 * @param clientId
	 * @throws MqttException 
	 */
	public MqttRpcMethodAvailClient(String broker, String clientId, List<String> servNamesList) throws MqttException{
		this.mqttClient = new MqttClient(broker,clientId, memPersist);
		this.serviceNames = servNamesList;
		this.mqttClient.setCallback(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
	 */
	@Override
	public void connectionLost(Throwable arg0) {
		logger.debug("Connection lost on " + getClass().getSimpleName());
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
	public void messageArrived(String arg0, MqttMessage msg) throws Exception {
		JSONObject jo = new JSONObject(msg.toString());
		String methodName = jo.getString("method");
		String id = jo.getString("id");

		System.out.println("added msg to queue-> " + msg.toString());
		queue.add(msg.toString());
		if ( serviceNames.contains(methodName)){
			//send yes response
			sendResponse(true, id);
		}
		else{//else send no response
			sendResponse(false, id);
		}
	}

	/**
	 * Helper method to send off true or false if an RPC method is available.
	 * @param boolVal
	 * @param id
	 */
	private void sendResponse(boolean boolVal, String id){
		JSONObject responseMsg = new JSONObject();
		responseMsg.put("id", id );
		responseMsg.put("result", boolVal);

		if (boolVal){
			responseMsg.put("error", JSONObject.NULL);
		}
		else{
			responseMsg.put("error", "Method Not Available");
		}

		try {
			this.mqttClient.publish( REPLY_TOPIC, new MqttMessage(responseMsg.toString().getBytes()) );
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#connect()
	 */
	@Override
	public void connect() throws MqttSecurityException, MqttException {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		this.mqttClient.connect(connOpts);
	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#subscribe(java.lang.String)
	 */
	@Override
	public void subscribe(String topic) throws MqttException {
		this.mqttClient.subscribe(topic);
	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#publish(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
	 */
	@Override
	public void publish(String topicName, MqttMessage msg) throws MqttPersistenceException, MqttException {
		this.mqttClient.publish(topicName, msg);
	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#disconnect()
	 */
	@Override
	public void disconnect() throws MqttException {
		this.mqttClient.disconnect();
	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#pollResult()
	 */
	@Override
	public Object pollResult() {
		System.err.println("Has not been implemented.");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#connect(org.eclipse.paho.client.mqttv3.MqttConnectOptions)
	 */
	@Override
	public void connect(MqttConnectOptions opts) throws MqttSecurityException, MqttException {
		this.mqttClient.connect(opts);
	}
}
