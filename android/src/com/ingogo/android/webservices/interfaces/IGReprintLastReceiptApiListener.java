package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.model.IGReceiptInformationModel;

public interface IGReprintLastReceiptApiListener {
	
	public void reprintLastReceiptCompleted(IGReceiptInformationModel contactInfo);
	public void reprintLastReceiptFailed(String errorMessage);
}
