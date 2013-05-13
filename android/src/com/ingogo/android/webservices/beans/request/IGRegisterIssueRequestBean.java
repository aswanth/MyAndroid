package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGRegisterIssueRequestBean extends IGBaseBean {
	private String issueReason;
	private String additionalInformation;
	private String mobileNumber;
	private String password;
	
	public IGRegisterIssueRequestBean(String mobileNumber, String password, String issueReason, String additionalInformation) {
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.issueReason = issueReason;
		this.additionalInformation = additionalInformation;

	}
	public String getIssueReason() {
		return issueReason;
	}
	public void setIssueReason(String issueReason) {
		this.issueReason = issueReason;
	}
	public String getAdditionalInformation() {
		return additionalInformation;
	}
	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
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
}
