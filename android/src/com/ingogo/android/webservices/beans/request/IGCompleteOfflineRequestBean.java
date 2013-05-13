package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGCompleteOfflineRequestBean extends IGBaseBean {
	private String bookingId;
	private String latitude;
	private String longitude;
	private String mobileNumber;
	private String password;
	public IGCompleteOfflineRequestBean ( String bookingId, String latitude, String longitude ) {
		this.bookingId = bookingId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.mobileNumber = IngogoApp.getSharedApplication().getUserId();
		this.password = IngogoApp.getSharedApplication().getPassword();
	}
	public String getBookingId() {
		return bookingId;
	}
	public void setBookingId(String bookingId) {
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
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	

}
