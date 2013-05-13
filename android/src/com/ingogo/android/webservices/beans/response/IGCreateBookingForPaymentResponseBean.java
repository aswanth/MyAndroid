package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGBookingModel;

public class IGCreateBookingForPaymentResponseBean extends IGBaseResponseBean {
	private IGBookingModel bookingSummary;
	  private Double minTotalDueValue;
	   private Double maxTotalDueValue;
	   private Double confirmationValue;
	   private Double creditPercentage;
	   private Double balance;
	   private Double bookingFee;
	   private boolean applyBid;
	

	public IGBookingModel getBookingSummary() {
		return bookingSummary;
	}

	public void setBookingSummary(IGBookingModel bookingSummary) {
		this.bookingSummary = bookingSummary;
	}

	public Double getMinTotalDueValue() {
		return minTotalDueValue;
	}

	public void setMinTotalDueValue(Double minTotalDueValue) {
		this.minTotalDueValue = minTotalDueValue;
	}

	public Double getMaxTotalDueValue() {
		return maxTotalDueValue;
	}

	public void setMaxTotalDueValue(Double maxTotalDueValue) {
		this.maxTotalDueValue = maxTotalDueValue;
	}

	public Double getConfirmationValue() {
		return confirmationValue;
	}

	public void setConfirmationValue(Double confirmationValue) {
		this.confirmationValue = confirmationValue;
	}

	public Double getCreditPercentage() {
		return creditPercentage;
	}

	public void setCreditPercentage(Double creditPercentage) {
		this.creditPercentage = creditPercentage;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public Double getBookingFee() {
		return bookingFee;
	}

	public void setBookingFee(Double bookingFee) {
		this.bookingFee = bookingFee;
	}

	public boolean isApplyBid() {
		return applyBid;
	}

	public void setApplyBid(boolean applyBid) {
		this.applyBid = applyBid;
	}
}
