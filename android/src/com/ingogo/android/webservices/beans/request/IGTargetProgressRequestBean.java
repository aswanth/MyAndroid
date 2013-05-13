package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGTargetProgressRequestBean extends IGBaseBean{
	
	
	private String mobileNumber;
	private String password;
	
	public IGTargetProgressRequestBean(String mobileNumber, String password) {
		this.mobileNumber = mobileNumber;
		this.password = password;
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
