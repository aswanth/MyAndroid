package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGSendReceiptRequestBean extends IGBaseBean{
	private String bookingId;
    private String passengerEmailAddress;
    private String passengerMobileNumber;
    private String mobileNumber;
    private String password;
	
    public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}
	
    public String getBookingId() {
		return bookingId;
	}
	
    public void setPassengerEmailAddress(String passengerEmailAddress) {
		this.passengerEmailAddress = passengerEmailAddress;
	}
	
    public String getPassengerEmailAddress() {
		return passengerEmailAddress;
	}
	
    public void setPassengerMobileNumber(String passengerMobileNumber) {
		this.passengerMobileNumber = passengerMobileNumber;
	}
	
    public String getPassengerMobileNumber() {
		return passengerMobileNumber;
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
}
