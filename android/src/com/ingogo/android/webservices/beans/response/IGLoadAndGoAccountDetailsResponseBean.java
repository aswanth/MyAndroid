package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

import com.ingogo.android.model.IGLoadAndGoAccountDetailsModel;

public class IGLoadAndGoAccountDetailsResponseBean extends IGBaseResponseBean {
	
	private ArrayList<IGLoadAndGoAccountDetailsModel> accounts;
	private String balancesAreAsAt;
	public ArrayList<IGLoadAndGoAccountDetailsModel> getAccounts() {
		return accounts;
	}
	public void setAccounts(ArrayList<IGLoadAndGoAccountDetailsModel> accounts) {
		this.accounts = accounts;
	}
	public String getBalancesAreAsAt() {
		return balancesAreAsAt;
	}
	public void setBalancesAreAsAt(String balancesAreAsAt) {
		this.balancesAreAsAt = balancesAreAsAt;
	}
	
	

}
