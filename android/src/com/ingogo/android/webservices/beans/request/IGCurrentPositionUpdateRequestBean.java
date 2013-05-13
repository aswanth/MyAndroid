package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGCurrentPositionUpdateRequestBean extends IGBaseBean {
	
	private double longitude;
	private double latitude;
	private String mobileNumber;
	private String password;
	
	public IGCurrentPositionUpdateRequestBean (double latitude , double longitude ){
		this.latitude = latitude;
		this.longitude = longitude;
		this.mobileNumber = IngogoApp.getSharedApplication().getUserId();
		this.password = IngogoApp.getSharedApplication().getPassword();
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
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
