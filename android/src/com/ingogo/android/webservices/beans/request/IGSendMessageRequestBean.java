package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGSendMessageRequestBean extends IGBaseBean {
	private String mobileNumber;
	private String password;
	private String bookingId;
	private String content;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
