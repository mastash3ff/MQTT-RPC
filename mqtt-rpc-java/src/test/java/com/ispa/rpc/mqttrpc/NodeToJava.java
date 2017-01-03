/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqttrpc;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.ispa.rpc.mqtt.MqttRpcClient;
import com.ispa.rpc.mqtt.MqttRpcClientBuilder;

/**
 * @author Brandon Sheffield - 
 * Created on  Feb 29, 2016
 * Description:  Used for testing NodeJS to Java outside JUnit Testing.
 */
public class NodeToJava {

	/**
	 * @param args
	 * @throws MqttException 
	 */
	public static void main(String[] args) throws MqttException {
		final String REMOTE_HOST = "tcp://test.mosquitto.org:1883";
		MqttRpcClient client = new MqttRpcClientBuilder().build(REMOTE_HOST);
		client.connect();

		client.provide(CalculatorService.class, new CalculatorImpl());

	}

}
