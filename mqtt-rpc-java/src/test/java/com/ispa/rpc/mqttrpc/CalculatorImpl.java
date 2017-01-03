/*******************************************************************************
 *******************************************************************************/
package com.ispa.rpc.mqttrpc;

/**
 * @author Brandon Sheffield - 
 * Created on  Jan 5, 2016
 * Description:  Houses implementation for the @see {@link CalculatorService}
 */
public class CalculatorImpl implements CalculatorService {

	/* (non-Javadoc)
	 * @see com.hileco.drpc.mqtt.CalculatorService#add(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Integer add(Integer arg0, Integer arg1) {
		return arg0 + arg1;
	}

	/* (non-Javadoc)
	 * @see asdf.CalculatorService#sub(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Integer sub(Integer a, Integer b) {
		return a - b;
	}

	/* (non-Javadoc)
	 * @see asdf.CalculatorService#mult(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Integer mult(Integer a, Integer b) {
		return a * b;
	}
}
