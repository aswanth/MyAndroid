package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGAcceptJobRequestBean extends IGBaseBean {

	private String mobileNumber;
	private String password;
	private String latitude;
	private String longitude;
	private String bookingId;
	private int bidExtra;
	private int bidInterval;
	
	public IGAcceptJobRequestBean(String mobileNumber,String password, String longitude, String latitude , String bookingId , int bidInterval, int bidExtra ){

		this.mobileNumber = mobileNumber;
		this.password = password;
		this.latitude = latitude;
		this.longitude = longitude;
		this.bookingId = bookingId;
		this.bidExtra = bidExtra;
		this.bidInterval = bidInterval;
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
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
    public String getBookingId(){
    	return bookingId;
    }
    public void setBookingId(String bookingId){
    	this.bookingId = bookingId;
    }

	public int getBidExtra() {
		return bidExtra;
	}

	public void setBidExtra(int bidExtra) {
		this.bidExtra = bidExtra;
	}

	public int getBidInterval() {
		return bidInterval;
	}

	public void setBidInterval(int bidInterval) {
		this.bidInterval = bidInterval;
	}
	

}
