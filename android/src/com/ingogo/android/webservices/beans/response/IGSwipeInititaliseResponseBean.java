package com.ingogo.android.webservices.beans.response;

import java.util.List;

import com.ingogo.android.model.IGCreditCardModel;

public class IGSwipeInititaliseResponseBean extends IGBaseResponseBean {
	private Double minTotalDueValue;
	private Double maxTotalDueValue;
	private Double confirmationValue;
	private Double creditPercentage;
	private Double balance;
	private List<IGCreditCardModel> cardDetails;

	public IGSwipeInititaliseResponseBean(Double minTotalDueValue,
			Double maxTotalDueValue, Double confirmationValue,
			Double creditPercentage, Double balance,
			List<IGCreditCardModel> cardDetails) {
		this.minTotalDueValue = minTotalDueValue;
		this.maxTotalDueValue = maxTotalDueValue;
		this.confirmationValue = confirmationValue;
		this.balance = balance;
		this.cardDetails = cardDetails;
		this.setCreditPercentage(creditPercentage);

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
