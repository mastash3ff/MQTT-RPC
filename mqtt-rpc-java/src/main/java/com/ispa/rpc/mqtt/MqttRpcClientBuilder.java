/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import com.ispa.rpc.generic.JSONStreamer;
import com.ispa.rpc.generic.RpcPacketStreamer;
import com.ispa.rpc.generic.ServiceHost;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility to create instances of {@link MqttRpcClient}
 *
 * @author Philipp Gayret & Brandon Sheffield
 */
public class MqttRpcClientBuilder {

    public static final int DEFAULT_MILLISECONDS_TIME_TO_WAIT_LIMIT = 5000;
    public static final int DEFAULT_SECONDS_KEEP_ALIVE_INTERVAL = 30;
    public static final int DEFAULT_LEVEL_QUALITY_OF_SERVICE = 0;//TODO perhaps change for more reliablity?  originally 2
    public static final int DEFAULT_MAX_THREADS = 10;
    public static final int DEFAULT_RETRY_LIMIT = 5;

    private String clientId;
    private MqttClientPersistence mqttClientPersistence;
    private MqttRpcFailureHandler mqttDrpcFailureHandler;
    private ExecutorService executorService;
    private ServiceHost callbackHost;
    private RpcPacketStreamer rpcPacketStreamer;
    private MqttRpcTopicBuilder topicBuilder;
    private ServiceHost serviceHost;
    private int keepaliveInterval;
    private int qualityOfServiceLevel;
    private MqttConnectOptions connectOptions;

    public MqttRpcClientBuilder() throws MqttException {
    	int randomInt = new Random().nextInt(10000) + 1; //TODO possibly more random?
    	this.clientId = String.valueOf(randomInt);
        this.mqttClientPersistence = new MemoryPersistence();
        this.executorService = Executors.newScheduledThreadPool(DEFAULT_MAX_THREADS);
        this.topicBuilder = new MqttRpcTopicBuilder();
        this.serviceHost = new ServiceHost();
        this.callbackHost = new ServiceHost();
        this.rpcPacketStreamer = new RpcPacketStreamer(new JSONStreamer());
        this.keepaliveInterval = DEFAULT_SECONDS_KEEP_ALIVE_INTERVAL;
        this.qualityOfServiceLevel = DEFAULT_LEVEL_QUALITY_OF_SERVICE;
        this.connectOptions = new MqttConnectOptions();
        this.connectOptions.setCleanSession(true);
        this.connectOptions.setKeepAliveInterval(keepaliveInterval);

        this.mqttDrpcFailureHandler = new MqttRpcFailureHandler() {
            @Override
            public boolean shouldRetry(Exception cause, MqttRpcTask task) {
                if (cause instanceof MqttException) {
                    if (((MqttException) cause).getReasonCode() == 32202) {
                        return true;
                    }
                }
                return task.getRetries() < DEFAULT_RETRY_LIMIT;
            }

            @Override
            public void handleDisconnect(Throwable throwable) {
                // Does not handle disconnects !
            }
        };
    }
    
    public MqttRpcClientBuilder withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public MqttRpcClientBuilder withMqttClientPersistence(MqttClientPersistence mqttClientPersistence) {
        this.mqttClientPersistence = mqttClientPersistence;
        return this;
    }

    public MqttRpcClientBuilder withMqttDrpcFailureHandler(MqttRpcFailureHandler mqttDrpcFailureHandler) {
        this.mqttDrpcFailureHandler = mqttDrpcFailureHandler;
        return this;
    }

    public MqttRpcClientBuilder withExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public MqttRpcClientBuilder withTopicBuilder(MqttRpcTopicBuilder topicBuilder) {
        this.topicBuilder = topicBuilder;
        return this;
    }

    public MqttRpcClientBuilder withServiceHost(ServiceHost serviceHost) {
        this.serviceHost = serviceHost;
        return this;
    }

    public MqttRpcClientBuilder withKeepaliveInterval(int keepaliveInterval) {
        this.keepaliveInterval = keepaliveInterval;
        return this;
    }

    public MqttRpcClientBuilder withQualityOfServiceLevel(int qualityOfServiceLevel) {
        this.qualityOfServiceLevel = qualityOfServiceLevel;
        return this;
    }

    public MqttRpcClientBuilder withRpcPacketStreamer(RpcPacketStreamer rpcPacketStreamer) {
        this.rpcPacketStreamer = rpcPacketStreamer;
        return this;
    }

    public MqttRpcClientBuilder withCallbackHost(ServiceHost callbackHost) {
        this.callbackHost = callbackHost;
        return this;
    }

    public MqttRpcClientBuilder withConnectOptions(MqttConnectOptions connectOptions) {
        this.connectOptions = connectOptions;
        return this;
    }

    public MqttRpcClient build(String broker) throws MqttException {
        MqttClient mqttClient = new MqttClient(broker, clientId, mqttClientPersistence);
        mqttClient.setTimeToWait(DEFAULT_MILLISECONDS_TIME_TO_WAIT_LIMIT);
        return new MqttRpcClient(mqttDrpcFailureHandler, executorService, mqttClient, topicBuilder,
                serviceHost, callbackHost, rpcPacketStreamer, connectOptions, qualityOfServiceLevel);
    }

}
