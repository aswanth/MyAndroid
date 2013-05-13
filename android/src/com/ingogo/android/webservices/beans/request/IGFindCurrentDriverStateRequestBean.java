package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGFindCurrentDriverStateRequestBean extends IGBaseBean {

	private String mobileNumber;
	private String password;
	private String licenseNumber;
	private boolean errorRecovery;

	public IGFindCurrentDriverStateRequestBean(String mobileNumber,
			String password, String licenseNumber, boolean errorRecovery) {
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.licenseNumber = licenseNumber;
		this.errorRecovery = errorRecovery;
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

	public String getLicenseNumber() {
		return licenseNumber;
	}

	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}

	public boolean isErrorRecovery() {
		return errorRecovery;
	}

	public void setErrorRecovery(boolean errorRecovery) {
		this.errorRecovery = errorRecovery;
	}

}
