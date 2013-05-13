package com.ingogo.android.model;

import java.io.Serializable;

public class IGContactInfoModel implements Serializable {

	private String emailAddress;
	private String addressLine1;
	private String addressLine2;
	private String suburb;
	private String postcode;
	private String state;

	public String getEmailAddress() {
		if (emailAddress != null)
			return emailAddress;

		return "";
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getAddressLine1() {
		if (addressLine1 != null)
			return addressLine1;

		return "";
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		if (addressLine2 != null)
			return addressLine2;

		return "";
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getSuburb() {
		if (suburb != null)
			return suburb;

		return "";
	}

	public void setSuburb(String suburb) {
		this.suburb = suburb;
	}

	public String getPostcode() {
		if (postcode != null)
			return postcode;

		return "";
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getState() {
		if (state != null)
			return state;

		return "";
	}

	public void setState(String state) {
		this.state = state;
	}

	private static final long serialVersionUID = 7177328717889185004L;
}
