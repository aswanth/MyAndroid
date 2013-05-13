package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGPrinterConfigRequestBean extends IGBaseBean {

	private String mobileNumber;
	private String password;
	private String deviceId;
	
	public IGPrinterConfigRequestBean(String mobileNumber, String password, String deviceId){
		super();
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.deviceId = deviceId;
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
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	
	
}
