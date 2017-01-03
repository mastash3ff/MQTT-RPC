/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

import java.lang.reflect.Method;

/**
 * Constructs topics out of identifyable service operations and callbacks.
 * <p>
 * Services should be published under these topics, callbacks registered under these topics and messages sent to these topics.
 *
 * @author Philipp Gayret & Brandon Sheffield
 */
public class MqttRpcTopicBuilder {
	private static final String REPLY = "reply";
	private static final String REQUEST = "request";
    
    public String operationRequest(Class<?> service, Method operation) {
        return String.format("%s/%s/%s", service.getSimpleName(), operation.getName(), REQUEST);
    }
    
    public String operation(Class<?> service, Method operation){
    	return String.format("%s/%s", service.getSimpleName(), operation.getName());
    }

    public String operation(Class<?> service, Method operation, String identifier) {
        return String.format("%s/%s/%s", service.getSimpleName(), operation.getName(), identifier);
    }
    
    public String callback(String correlationId) {
        return String.format("%s/%s", correlationId, REPLY);
    }
}
