package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

import com.ingogo.android.model.IGJobAvailableModel;
import com.ingogo.android.model.IGReceiptInformationModel;

public class IGFindCurrentDriverStateResponseBean extends IGBaseResponseBean {

	private ArrayList<IGJobAvailableModel> bookingSummaries;
	private IGReceiptInformationModel receiptInformation;

	public ArrayList<IGJobAvailableModel> getBookingSummaries() {
		return bookingSummaries;
	}

	public void setBookingSummaries(
			ArrayList<IGJobAvailableModel> bookingSummaries) {
		this.bookingSummaries = bookingSummaries;
	}

	public IGReceiptInformationModel getReceiptInformation() {
		return receiptInformation;
	}

	public void setReceiptInformation(
			IGReceiptInformationModel receiptInformation) {
		this.receiptInformation = receiptInformation;
	}
}
