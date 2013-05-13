package com.ingogo.android.model;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IGCreditCardInformation implements Serializable  {

	private static final long serialVersionUID = 7281307330509388923L;
//	private String cvv;
	private String cardNumber;
	private String cardName;
	private String expiryMonth;
	private String expiryYear;
//	private boolean primary;
	
	
//	public boolean isPrimary() {
//		return primary;
//	}
//	public void setPrimary(boolean primary) {
//		this.primary = primary;
//	}
//	public String getCvv() {
//		return cvv;
//	}
//	public void setCvv(String cvv) {
//		this.cvv = cvv;
//	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
//	public String getCardNickname() {
//		return cardNickname;
//	}
//	public void setCardNickname(String cardNickname) {
//		this.cardNickname = cardNickname;
//	}
	public String getExpiryMonth() {
		return expiryMonth;
	}
	public void setExpiryMonth(String expiryMonth) {
		this.expiryMonth = expiryMonth;
	}
	public String getExpiryYear() {
		return expiryYear;
	}
	public void setExpiryYear(String expiryYear) {
		this.expiryYear = expiryYear;
	}
	
	public String toJsonString() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}
	public void setCardName(String cardName) {
		this.cardName = cardName;
	}
	public String getCardName() {
		return cardName;
	}
}
