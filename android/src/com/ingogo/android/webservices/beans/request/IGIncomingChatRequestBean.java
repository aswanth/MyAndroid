package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGIncomingChatRequestBean extends IGBaseBean {
	
	private int bookingId;
	private String mobileNumber;
	private String password;
	public IGIncomingChatRequestBean ( int bookingId ) {
		this.bookingId = bookingId;
		this.mobileNumber = IngogoApp.getSharedApplication().getUserId();
		this.password = IngogoApp.getSharedApplication().getPassword();
	}
	public int getBookingId() {
		return bookingId;
	}
	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
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
