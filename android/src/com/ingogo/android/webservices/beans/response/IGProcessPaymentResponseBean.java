package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGReceiptInformationModel;

public class IGProcessPaymentResponseBean extends IGBaseResponseBean {
	private IGBookingModel bookingSumary;
	private String receiptInformationPageText;
	private IGReceiptInformationModel receiptInformation;

	public IGBookingModel getBookingSumary() {
		return bookingSumary;
	}

	public void setBookingSumary(IGBookingModel bookingSumary) {
		this.bookingSumary = bookingSumary;
	}

	public String getReceiptInformationPageText() {
		return receiptInformationPageText;
	}

	public void setReceiptInformationPageText(String receiptInformationPageText) {
		this.receiptInformationPageText = receiptInformationPageText;
	}

	public IGReceiptInformationModel getReceiptInformation() {
		return receiptInformation;
	}

	public void setReceiptInformation(IGReceiptInformationModel receiptInformation) {
		this.receiptInformation = receiptInformation;
	}
	
	

}
