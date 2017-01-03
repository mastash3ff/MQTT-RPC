/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

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

/**
 * @author Brandon Sheffield - 
 * Created on  Feb 24, 2016
 * Description:  TODO
 */
public class MqttRpcMethodAvailListener implements MqttClientInterface, MqttCallback {
	
	private MqttClient mqttClient;
	private Object result;
	private boolean isMsgReceived = false;
	private MemoryPersistence memPersist = new MemoryPersistence();
	private static final int DEFAULT_TIMEOUT = 10000;

	/**
	 * Generates a client with given broker URL and unique client ID.
	 * @param broker
	 * @param clientId
	 * @throws MqttException 
	 */
	public MqttRpcMethodAvailListener(String broker, String clientId) throws MqttException{
		this.mqttClient = new MqttClient(broker,clientId, memPersist);
		this.mqttClient.setCallback(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
	 */
	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
	 */
	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		JSONObject responseMsg = new JSONObject(message.toString());
		this.result = responseMsg.get("result");
		this.isMsgReceived = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#connect()
	 */
	@Override
	public void connect() throws MqttSecurityException, MqttException {
		this.mqttClient.connect();
		
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
		if (this.result == null){
			long startTime = System.currentTimeMillis();
			while (this.isMsgReceived != true && (System.currentTimeMillis()-startTime)<DEFAULT_TIMEOUT){
				//wait for message arrival
				try {
					Thread.sleep(1); //TODO ghetto as shit
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		this.isMsgReceived = false;
		return this.result;
	}

	/* (non-Javadoc)
	 * @see com.ispa.rpc.mqtt.MqttClientInterface#connect(org.eclipse.paho.client.mqttv3.MqttConnectOptions)
	 */
	@Override
	public void connect(MqttConnectOptions opts) throws MqttSecurityException, MqttException {
		this.mqttClient.connect(opts);
	}
}
