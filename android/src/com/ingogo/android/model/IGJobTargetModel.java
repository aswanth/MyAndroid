package com.ingogo.android.model;

import java.io.Serializable;

public class IGJobTargetModel implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3389965932671266784L;
	
	String jobsCompleted;
	String commissionPercentage;
	int displayPercentile;
	
	public String getJobsCompleted() {
		return jobsCompleted;
	}
	public void setJobsCompleted(String jobsCompleted) {
		this.jobsCompleted = jobsCompleted;
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
