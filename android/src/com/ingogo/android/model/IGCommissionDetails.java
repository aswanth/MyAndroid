package com.ingogo.android.model;

import java.io.Serializable;

/**
 * 
 * @author suslov
 *
 */
public class IGCommissionDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7638039331068690600L;
	private String target;
	private float commission;
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public float getCommission() {
		return commission;
	}
	public void setCommission(float commission) {
		this.commission = commission;
	}


}
