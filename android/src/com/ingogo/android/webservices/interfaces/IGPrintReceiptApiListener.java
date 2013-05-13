package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.model.IGReceiptInformationModel;

public interface IGPrintReceiptApiListener {

	public void printReceiptCompleted(IGReceiptInformationModel contactInfo);
	public void printReceiptFailed(String errorMessage);
}
