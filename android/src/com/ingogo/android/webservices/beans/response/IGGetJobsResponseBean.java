package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

import com.ingogo.android.model.IGJobAvailableModel;

public class IGGetJobsResponseBean extends IGBaseResponseBean {

	private ArrayList<IGJobAvailableModel> bookingSummaries;
	private String previousJobMessage;
	private String driverStatus;

	public ArrayList<IGJobAvailableModel> getJobs() {
		return bookingSummaries;
	}

	public void setJobs(ArrayList<IGJobAvailableModel> bookingSummaries) {
		this.bookingSummaries = bookingSummaries;
	}

	public String getPreviousJobMessage() {
		return previousJobMessage;
	}

	public void setPreviousJobMessage(String previousJobMessage) {
		this.previousJobMessage = previousJobMessage;
	}

	public String getDriverStatus() {
		return driverStatus;
	}

	public void setDriverStatus(String driverStatus) {
		this.driverStatus = driverStatus;
	}
}
