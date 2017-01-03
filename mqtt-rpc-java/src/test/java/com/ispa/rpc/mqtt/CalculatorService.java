/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

import com.ispa.rpc.mqtt.MqttRpcClient;

/**
 * An example interface to publish and invoke using an {@link MqttRpcClient}.
 *
 * @author Philipp Gayret
 */
public interface CalculatorService {

    public Integer add(Integer a, Integer b);
}
