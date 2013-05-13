package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGTimeToPickUpRequestBean extends IGBaseBean {

	private String mobileNumber;
	private String password;
	private String bookingId;
	private String timeToPickup;
	private String messageType;
	
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

	public String getTimeToPickup() {
		return timeToPickup;
	}

	public void setTimeToPickUp(String timeToPickup) {
		this.timeToPickup = timeToPickup;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public IGTimeToPickUpRequestBean(String mobileNumber, String password,
			String bookingId, String timeToPickup, String messageType) {
		super();
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.bookingId = bookingId;
		this.timeToPickup = timeToPickup;
		this.messageType = messageType;
	}



}
