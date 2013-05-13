package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGPaymentHistoryRequestBean extends IGBaseBean {
	
	private String mobileNumber;
	private String password;
	
	public IGPaymentHistoryRequestBean(String username,String password){
		this.mobileNumber = username;
		this.password = password;
	}
	public String getUserName() {
		return mobileNumber;
	}
	public void setUserName(String userName) {
		this.mobileNumber = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	

}
