package com.ingogo.android.webservices.beans.response;

public class IGLoginResponseBean extends IGBaseResponseBean {
	private String creditPercentage;
	private String jobReminderInterval;
	private String minTotalDueValue;
	private String maxTotalDueValue;
	private String confirmationValue;
	private boolean captureDiagnostics;
	private String pollBookingsInterval;
	private String broadcastPositionInterval;
	private String webServiceTimeout;
	public String getCreditPercentage() {
		return creditPercentage;
	}
	public void setCreditPercentage(String creditPercentage) {
		this.creditPercentage = creditPercentage;
	}
	public String getJobReminderInterval() {
		return jobReminderInterval;
	}
	public void setJobReminderInterval(String jobReminderInterval) {
		this.jobReminderInterval = jobReminderInterval;
	}
	public String getMinTotalDueValue() {
		return minTotalDueValue;
	}
	public void setMinTotalDueValue(String minTotalDueValue) {
		this.minTotalDueValue = minTotalDueValue;
	}
	public String getMaxTotalDueValue() {
		return maxTotalDueValue;
	}
	public void setMaxTotalDueValue(String maxTotalDueValue) {
		this.maxTotalDueValue = maxTotalDueValue;
	}
	public String getConfirmationValue() {
		return confirmationValue;
	}
	public void setConfirmationValue(String confirmationValue) {
		this.confirmationValue = confirmationValue;
	}
	public boolean isCaptureDiagnostics() {
		return captureDiagnostics;
	}
	public void setCaptureDiagnostics(boolean captureDiagnostics) {
		this.captureDiagnostics = captureDiagnostics;
	}
	public String getPollBookingsInterval() {
		return pollBookingsInterval;
	}
	public void setPollBookingsInterval(String pollBookingsInterval) {
		this.pollBookingsInterval = pollBookingsInterval;
	}
	public String getBroadcastPositionInterval() {
		return broadcastPositionInterval;
	}
	public void setBroadcastPositionInterval(String broadcastPositionInterval) {
		this.broadcastPositionInterval = broadcastPositionInterval;
	}
	public String getWebServiceTimeout() {
		return webServiceTimeout;
	}
	public void setWebServiceTimeout(String webServiceTimeout) {
		this.webServiceTimeout = webServiceTimeout;
	}

}
