package com.ingogo.android.webservices.beans.response;

import java.util.List;

import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGCreditCardModel;

public class IGInitialiseUnknownPassengerResponseBean extends IGBaseResponseBean{

	IGBookingModel bookingSummary;
	private Double minTotalDueValue;
	private Double maxTotalDueValue;
	private Double confirmationValue;
	private Double creditPercentage;
	private Double balance;
	private List<IGCreditCardModel> cardDetails;

	public IGBookingModel getBookingSummary() {
		return bookingSummary;
	}

	public void setBookingSummary(IGBookingModel bookingSummary) {
		this.bookingSummary = bookingSummary;
	}
	
	public void setMinTotalDueValue(Double minTotalDueValue) {
		this.minTotalDueValue = minTotalDueValue;
	}
	public Double getMinTotalDueValue() {
		return minTotalDueValue;
	}
	public void setMaxTotalDueValue(Double maxTotalDueValue) {
		this.maxTotalDueValue = maxTotalDueValue;
	}
	public Double getMaxTotalDueValue() {
		return maxTotalDueValue;
	}
	public void setConfirmationValue(Double confirmationValue) {
		this.confirmationValue = confirmationValue;
	}
	public Double getConfirmationValue() {
		return confirmationValue;
	}

	public void setCreditPercentage(Double creditPercentage) {
		this.creditPercentage = creditPercentage;
	}

	public Double getCreditPercentage() {
		return creditPercentage;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public Double getBalance() {
		return balance;
	}

	public List<IGCreditCardModel> getCardDetails() {
		return cardDetails;
	}

	public void setCardDetails(List<IGCreditCardModel> cardDetails) {
		this.cardDetails = cardDetails;
	}
	
}
