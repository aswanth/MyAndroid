package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGInitialiseUnknownPassengerRequestBean extends IGBaseBean{

	private String longitude;
	private String latitude;
	private String mobileNumber;
	private String password;
	
	public IGInitialiseUnknownPassengerRequestBean(String longitude, String latitude, String mobileNumber, String password) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.mobileNumber = mobileNumber;
		this.password = password;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
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
}
