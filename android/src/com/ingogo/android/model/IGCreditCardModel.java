package com.ingogo.android.model;

import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IGCreditCardModel implements Serializable {

	private static final long serialVersionUID = 7281307330509388923L;
	private boolean isDefault;
	private String token;
	private String creditPercentage;
	private String cardNickname;

	
	public String toJsonString() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	public String getCreditPercentage() {
		return creditPercentage;
	}
	public void setCreditPercentage(String creditPercentage) {
		this.creditPercentage = creditPercentage;
	}
	public String getCardNickname() {
		return cardNickname;
	}
	public void setCardNickname(String cardNickname) {
		this.cardNickname = cardNickname;
	}

	
}