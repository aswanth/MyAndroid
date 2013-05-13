package com.ingogo.android.model;

import java.io.Serializable;

/**
 * 
 * @author suslov
 *
 */
public class IGSampleCalculation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5786663462430197036L;
	private float amount;
    private float earnings;
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public float getEarnings() {
		return earnings;
	}
	public void setEarnings(float earnings) {
		this.earnings = earnings;
	}
}
