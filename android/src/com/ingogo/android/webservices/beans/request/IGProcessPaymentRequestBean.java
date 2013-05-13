package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.model.IGCreditCardInformation;
import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGProcessPaymentRequestBean extends IGBaseBean  {
	
	private IGCreditCardInformation cardInformation;
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

	public IGProcessPaymentRequestBean(String mobileNumber,
			String password, String bookingId, String latitude,
			String longitude, String fare, String totalFare, IGCreditCardInformation cardInformation, String cardDetails, long localityId, String suburbName) {
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.bookingId = bookingId;
		this.longitude = longitude;
		this.latitude = latitude;
		this.fare = fare;
		this.totalFare = totalFare;
		this.cardInformation = cardInformation;
		this.cardDetails = cardDetails;
		this.localityId = localityId;
		this.suburbName = suburbName;
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

	public String getBookingrId() {
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

	public void setFare(String fare) {
		this.fare = fare;
	}

	public String getFare() {
		return fare;
	}

	public void setTotalFare(String totalFare) {
		this.totalFare = totalFare;
	}

	public String getTotalFare() {
		return totalFare;
	}

	public void setCardInformation(IGCreditCardInformation cardInformation) {
		this.cardInformation = cardInformation;
	}

	public IGCreditCardInformation getCardInformation() {
		return cardInformation;
	}

	public void setCardDetails(String cardDetails) {
		this.cardDetails = cardDetails;
	}

	public String getCardDetails() {
		return cardDetails;
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

	

}
