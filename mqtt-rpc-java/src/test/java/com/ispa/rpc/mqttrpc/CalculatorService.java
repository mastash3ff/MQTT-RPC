/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqttrpc;

/**
 * @author Brandon Sheffield - 
 * Created on  Jan 5, 2016
 * Description:  Methods below are represented as a Service to a Client to call remotely.
 */
public interface CalculatorService {
	
	public Integer add(Integer a, Integer b);
	public Integer sub(Integer a, Integer b);
	public Integer mult(Integer a, Integer b);

}
