/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqtt;

/**
 * An exception for any kind of error which cannot be attempted to recover from.
 *
 * @author Philipp Gayret
 */
public class MqttRpcRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MqttRpcRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
