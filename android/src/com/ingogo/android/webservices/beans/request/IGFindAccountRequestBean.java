package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGFindAccountRequestBean extends IGBaseBean{

	private String mobileNumber;
	private String password;
	private String searchString;
	
	public IGFindAccountRequestBean(String mobileNumber, String password, String searchString) {
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.searchString = searchString;
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

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
}
