package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGGetJobsRequestBean extends IGBaseBean {

	private String mobileNumber;
	private String password;
	private String latitude;
	private String longitude;

	public IGGetJobsRequestBean(String mobileNumber, String password,
			String latitude, String longitude) {
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
}
