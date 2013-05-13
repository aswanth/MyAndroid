package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

public class IGDriverLocalityResponseBean extends IGBaseResponseBean {

	// TODO: Currently a string is returned. Need to change this.

	private ArrayList<String> supportedPrefixes;
	private ArrayList<String> supportedMasks;
	private String jobReminderInterval;
	private long localityId;
	private String localityName;
	private String savingsPercentage;

	public ArrayList<String> getSupportedMasks() {
		return supportedMasks;
	}

	public void setSupportedMasks(ArrayList<String> supportedMasks) {
		this.supportedMasks = supportedMasks;
	}

	public String getJobReminderInterval() {
		return jobReminderInterval;
	}

	public void setJobReminderInterval(String jobReminderInterval) {
		this.jobReminderInterval = jobReminderInterval;
	}

	public ArrayList<String> getDriverLocality() {
		return supportedPrefixes;
	}

	public void setDriverLocality(ArrayList<String> supportedPrefixes) {
		this.supportedPrefixes = supportedPrefixes;
	}

	public long getLocalityId() {
		return localityId;
	}

	public void setLocalityId(long localityId) {
		this.localityId = localityId;
	}

	public String getLocalityName() {
		return localityName;
	}

	public void setLocalityName(String localityName) {
		this.localityName = localityName;
	}

	public String getSavingsPercentage() {
		return savingsPercentage;
	}

	public void setSavingsPercentage(String savingsPercentage) {
		this.savingsPercentage = savingsPercentage;
	}
}
