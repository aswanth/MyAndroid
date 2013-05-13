package com.ingogo.android.model;

import java.io.Serializable;

public class IGEarningTargetModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8136694879754785974L;

	
	String payments;
	String commissionPercentage;
	int displayPercentile;
	
	public String getPayments() {
		return payments;
	}
	public void setPayments(String payments) {
		this.payments = payments;
	}
	public String getCommissionPercentage() {
		return commissionPercentage;
	}
	public void setCommissionPercentage(String commissionPercentage) {
		this.commissionPercentage = commissionPercentage;
	}
	public int getDisplayPercentile() {
		return displayPercentile;
	}
	public void setDisplayPercentile(int displayPercentile) {
		this.displayPercentile = displayPercentile;
	}

}
