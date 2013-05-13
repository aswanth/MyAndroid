package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGSwipeInititaliseRequestBean extends IGBaseBean {
	private String mobileNumber;
	private String password;
	private String passengerId;

	public IGSwipeInititaliseRequestBean(String mobileNumber,String password, String passengerId){
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.passengerId = passengerId;

	}
	
	
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return password;
	}


	public void setPassengerId(String passengerId) {
		this.passengerId = passengerId;
	}


	public String getPassengerId() {
		return passengerId;
	}

}
