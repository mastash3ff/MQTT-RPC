/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

/**
 * @author Brandon Sheffield - 
 * Created on  Jan 22, 2016
 * Description:  Interface that can be used for polymorphism if implementations were to be changed out.  
 */
public interface MqttClientInterface {
	/**
	 * Connect client to local or remote MQTT broker.  Uses default qos of 0.
	 * @throws MqttSecurityException
	 * @throws MqttException
	 */
	public void connect() throws MqttSecurityException, MqttException;
	
	/**
	 * Connect to MQTT broker with provided options.
	 * @param opts
	 * @throws MqttSecurityException
	 * @throws MqttException
	 */
	public void connect(MqttConnectOptions opts) throws MqttSecurityException, MqttException;
	
	/**
	 * Subscribe this client to a provided topic
	 * @param topic
	 * @throws MqttException
	 */
	public void subscribe(String topic) throws MqttException;
	
	/**
	 * Publish provided MQTT message to given topic
	 * @param topicName
	 * @param msg
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */
	public void publish(String topicName, MqttMessage msg) throws MqttPersistenceException, MqttException;
	
	/**
	 * Disconnects the MQTT Client
	 * @throws MqttException
	 */
	public void disconnect() throws MqttException;
	
	/**
	 * Listens for a message to arrive.  Will block.
	 * @return
	 */
	public Object pollResult();
		
}
