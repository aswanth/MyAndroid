package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGReceiptInformationModel;

public class IGPrintReceiptResponseBean extends IGBaseResponseBean{
	
	private IGReceiptInformationModel receiptInformation;

	public IGReceiptInformationModel getReceiptInformation() {
		return receiptInformation;
	}

	public void setReceiptInformation(IGReceiptInformationModel receiptInformation) {
		this.receiptInformation = receiptInformation;
	}
	
}
