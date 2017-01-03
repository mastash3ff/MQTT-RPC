/*******************************************************************************
 *******************************************************************************/
/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqttrpc;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ispa.rpc.mqtt.MqttRpcClient;
import com.ispa.rpc.mqtt.MqttRpcClientBuilder;

/**
 * @author Brandon Sheffield - 
 * Created on  Dec 29, 2015
 * Description:  Test Java2Java, Java2Node, & NodeToJava functionality for correctness.
 * 				 Comment out @Ignore tags to run the tests!
 */

import junit.runner.Version;

/**
 * 
 * @author Brandon Sheffield - 
 * Created on  Mar 1, 2016
 * Description:  Comment out @Ignore to run each test.
 */
public class RemoteCalcTest{

	final static String CALLBACK_TOPIC = "cb_topic_uuid";
	final static String LOCALHOST = "tcp://localhost:1883";
	final static String REQUEST_ID = "request_id";
	final static String CALC_SERVICE_NAME = "CalculatorService";
	static Logger logger = LoggerFactory.getLogger(RemoteCalcTest.class);

	
	@Ignore(value = "Test this Java implementation against it's self")
	@Test
	public void testJava2Java() throws MqttException{
		//---Performance Notes---
		//one call 0.520s
		//100 calls 5.277s
		
		MqttRpcClient client = new MqttRpcClientBuilder().build(LOCALHOST);
		client.connect();
		client.provide(CalculatorService.class, new CalculatorImpl());

		List<Object> sampleArray = new ArrayList<>();
		sampleArray.add(6);
		sampleArray.add(4);
		Object result = client.callRemote(CALC_SERVICE_NAME, "add", sampleArray.toArray());
		assertEquals(10, result);
		
		int i = 0;
		for (; i < 100; ++i){
			Object resultDup = client.callRemote(CALC_SERVICE_NAME, "add", sampleArray.toArray());
			System.out.println("got back-> " + resultDup);
		}
		assertEquals(i, 100); //assert 100 counts
	}

	/**
	 * With Node server running, call it's methods through Java.
	 * @throws MqttException
	 */
	@Ignore(value = "requires Node server running") 
	@Test
	public void testJava2Node() throws MqttException{
		final String REMOTE_HOST = "tcp://test.mosquitto.org:1883";
		MqttRpcClient client = new MqttRpcClientBuilder().build(REMOTE_HOST);
		client.connect();

		List<Object> sampleArray = new ArrayList<>();
		sampleArray.add(6);
		sampleArray.add(4);
		
		Object result = client.callRemote(CALC_SERVICE_NAME, "add", sampleArray.toArray());
		
		int i = 0;
		for ( ; i < 100; ++i){
			System.out.println("result-> " + result);
		}
		assertEquals(10, result);
		
		assertEquals(100, i); //assert 100 calls
		
	}
	/**
	 * With Java service running, call Java rpc methods through Node rpc calls.
	 * @throws MqttException
	 */
	@Ignore(value = "requires Node client to send a JSON Request Message")
	@Test
	public void testNodeToJava() throws MqttException{
		//TODO test without RPC MEthod avail.  see how it performs with loop calls.
		final String REMOTE_HOST = "tcp://test.mosquitto.org:1883";
		MqttRpcClient client = new MqttRpcClientBuilder().build(REMOTE_HOST); //test remote_host
		client.connect();

		client.provide(CalculatorService.class, new CalculatorImpl());

		while (true){
			//wait for something to come in or else JUnit runs to end.
		}
	}

	/**
	 * 
	 * @throws MqttException
	 */
	@Ignore(value = "requires Python client to send a JSON Request Message")
	@Test
	public void testPythontoJava() throws MqttException{
		MqttRpcClient client = new MqttRpcClientBuilder().build(LOCALHOST);
		client.connect();
		System.out.println("Python To Java Test.  Listening...");
		client.provide(CalculatorService.class, new CalculatorImpl());

		while (true){
			//wait for publish
		}
	}

	/**
	 * 
	 * @throws MqttException
	 */
	@Ignore(value = "requires Node client to send a JSON Request Message")
	@Test
	public void testJavatoPython() throws MqttException{
		MqttRpcClient client = new MqttRpcClientBuilder().build(LOCALHOST);
		client.connect();

		List<Object> sampleArray = new ArrayList<>();
		sampleArray.add(6);
		sampleArray.add(4);

		Object result = client.callRemote(CALC_SERVICE_NAME, "add", sampleArray.toArray());

		assertEquals(10, result);
	}

	public static void main(String[] args) throws MqttException {
		System.out.println("JUnit version is: " + Version.id());
	}

}