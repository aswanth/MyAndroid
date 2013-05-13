package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGPaymentJobRequestBean extends IGBaseBean {

	private String bookingId;
	private String totalFare;
	private String fare;
	private String mobileNumber;
	private String password;
	private String token;
	private String confCode;
	private String paymentMethod;
	private String latitude;
	private String longitude;

	public IGPaymentJobRequestBean(String bookingId, String totalFare,
			String fare, String token, String confCode, String paymentMethod) {
		this.bookingId = bookingId;
		this.totalFare = totalFare;
		this.fare = fare;
		this.token = token;
		this.confCode = confCode;
		this.paymentMethod = paymentMethod;
		this.mobileNumber = IngogoApp.getSharedApplication().getUserId();
		this.password = IngogoApp.getSharedApplication().getPassword();
		this.latitude = String.valueOf(IGLocationListener.getCurrentLatitude());
		this.longitude = String.valueOf(IGLocationListener
				.getCurrentLongitude());
	}

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}

	public String getTotalFare() {
		return totalFare;
	}

	public void setTotalFare(String totalFare) {
		this.totalFare = totalFare;
	}

	public String getFare() {
		return fare;
	}

	public void setFare(String fare) {
		this.fare = fare;
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getConfCode() {
		return confCode;
	}

	public void setConfCode(String confCode) {
		this.confCode = confCode;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
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
