package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGPaymentDailyHistoryRequestBean extends IGBaseBean {
	
	private String mobileNumber;
	private String password;
	private long paymentId;
	
	public IGPaymentDailyHistoryRequestBean(String username,String password,long paymentId){
		this.mobileNumber = username;
		this.password = password;
		this.paymentId = paymentId;
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
	public long getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(long paymentId) {
		this.paymentId = paymentId;
	}
	
}
