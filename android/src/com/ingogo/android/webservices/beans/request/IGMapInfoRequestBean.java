package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGMapInfoRequestBean extends IGBaseBean{

	private String mobileNumber;
	private String password;
	private String bookingId;
	
	public IGMapInfoRequestBean(String bookingId, String mobileNumber, String password) {
		super();
		this.mobileNumber = mobileNumber;
		this.password = password;
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

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}
	
	
}
