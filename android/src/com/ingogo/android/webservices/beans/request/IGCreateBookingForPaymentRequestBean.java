package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGCreateBookingForPaymentRequestBean extends IGBaseBean {
	private String mobileNumber;
	private String password;
	private String passengerId;
	private String longitude;
	private String latitude;

	public IGCreateBookingForPaymentRequestBean(String mobileNumber,
			String password, String passengerId, String latitude,
			String longitude) {
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.passengerId = passengerId;
		this.longitude = longitude;
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

	public String getPassengerId() {
		return passengerId;
	}

	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
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

	
}
