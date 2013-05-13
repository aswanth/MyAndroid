package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;


public class IGProcessPaymentForUnknownPassengerRequestBean extends IGBaseBean {
	private String mobileNumber;
	private String password;
	private String bookingId;                                
    private String longitude;
    private String latitude;
    private String fare;
    private String totalFare;
    private String cardDetails;
    private long localityId;
    private String suburbName;
    
    public IGProcessPaymentForUnknownPassengerRequestBean(String bookingId, String longitude, String latitude, 
    		String mobileNumber, String password, String fare, String totalFare, String cardDetails, long localityId, String suburbName) {
    	if (bookingId==null) {
        	this.bookingId = "";

		} else {
	    	this.bookingId = bookingId;

		}
    	this.longitude = longitude;
    	this.latitude = latitude;
    	this.fare = fare;
    	this.totalFare = totalFare;
    	this.cardDetails = cardDetails;
    	this.mobileNumber = mobileNumber;
    	this.password = password;
    	this.localityId = localityId;
    	this.suburbName = suburbName;
    }
    
	public String getBookingId() {
		return bookingId;
	}
	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
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
	public String getFare() {
		return fare;
	}
	public void setFare(String fare) {
		this.fare = fare;
	}
	public String getCardDetails() {
		return cardDetails;
	}
	public void setCardDetails(String cardDetails) {
		this.cardDetails = cardDetails;
	}
	public String getTotalFare() {
		return totalFare;
	}
	public void setTotalFare(String totalFare) {
		this.totalFare = totalFare;
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

	public long getLocalityId() {
		return localityId;
	}

	public void setLocalityId(long localityId) {
		this.localityId = localityId;
	}

	public String getSuburbName() {
		return suburbName;
	}

	public void setSuburbName(String suburbName) {
		this.suburbName = suburbName;
	}

	
    
    // on the initial swipe this va;lue will be null. If there is a failure and a retry attempt is made, this field must be populated

}
