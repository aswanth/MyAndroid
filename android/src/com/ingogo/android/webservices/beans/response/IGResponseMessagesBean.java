package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGResponseMessagesBean extends IGBaseBean {
	ArrayList<IGErrorMessageResponseBean> errorMessages;
	ArrayList<String> informationMessages;
	String errorMessage = "";

	public ArrayList<IGErrorMessageResponseBean> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(
			ArrayList<IGErrorMessageResponseBean> errorMessages) {
		this.errorMessages = errorMessages;
	}

	public ArrayList<String> getInformationMessages() {
		return informationMessages;
	}

	public void setInformationMessages(ArrayList<String> informationMessages) {
		this.informationMessages = informationMessages;
	}

	public String errorMessagesToString() {
		for (IGErrorMessageResponseBean errorMsg : errorMessages) {
			errorMessage += errorMsg.getContent();
		}
		return errorMessage;
	}

	public String informationMessagesToString() {
		return informationMessages.toString();
	}

}
