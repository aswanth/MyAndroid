package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.model.IGContactInfoModel;
import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGContactInfoRequestBean extends IGBaseBean {

	private String mobileNumber;
	private String password;
	private IGContactInfoModel contactInformation;

	public IGContactInfoRequestBean(String mobileNumber, String password,
			IGContactInfoModel contactInformation) {
		super();
		this.mobileNumber = mobileNumber;
		this.password = password;
		this.contactInformation = contactInformation;
	}

	public IGContactInfoModel getContactInformation() {
		return contactInformation;
	}

	public void setContactInformation(IGContactInfoModel contactInformation) {
		this.contactInformation = contactInformation;
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
