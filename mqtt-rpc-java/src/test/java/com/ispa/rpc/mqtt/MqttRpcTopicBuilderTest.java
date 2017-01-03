/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

import org.junit.Assert;
import org.junit.Test;

import com.ispa.rpc.mqtt.MqttRpcTopicBuilder;

import java.lang.reflect.Method;

/**
 * @author Philipp Gayret
 */
public class MqttRpcTopicBuilderTest {

    public static final String IDENTIFIER = "1234";

    /**
     * Verifies that an operation with identifier
     * - Start with goperation and service topic string
     * - Ends with a slash and then the identifier
     * <p>
     * This is important for MQTT, messages sent to generic topics must also arrive on the identied topics.
     */
    @Test
    public void test() {
        MqttRpcTopicBuilder mqttDrpcTopicBuilder = new MqttRpcTopicBuilder();
        Class<CalculatorService> service = CalculatorService.class;
        Method method = service.getMethods()[0];
        String operation = mqttDrpcTopicBuilder.operationRequest(service, method);
        String operationWithIdentifier = mqttDrpcTopicBuilder.operation(service, method, IDENTIFIER);
        Assert.assertTrue(operationWithIdentifier.startsWith(operation));
        Assert.assertTrue(operationWithIdentifier.endsWith("/" + IDENTIFIER));
    }

}
