package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGCancelJobRequestBean extends IGBaseBean {

	private String mobileNumber;
	private String password;
	private String bookingId;
	private String cancellationReason;
	
	public IGCancelJobRequestBean(String mobileNumber, String password,
			String bookingId, String cancellationReason) {
		super();
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.bookingId = bookingId;
		this.cancellationReason = cancellationReason;
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
	public String getCancellationReason() {
		return cancellationReason;
	}
	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}
	
	
	
	
}
