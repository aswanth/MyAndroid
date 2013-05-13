package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGContactInfoModel;

public class IGContactInfoResponseBean extends IGBaseResponseBean {
	
	private IGContactInfoModel contactInformation;

	public IGContactInfoModel getContactInformation() {
		return contactInformation;
	}

	public void setContactInformation(IGContactInfoModel contactInformation) {
		this.contactInformation = contactInformation;
	}

}
