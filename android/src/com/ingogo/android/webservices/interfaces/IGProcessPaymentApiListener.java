package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGReceiptInformationModel;



public interface IGProcessPaymentApiListener {
	public void processPaymentCompleted(IGBookingModel bookingModel, String receiptInformationPageText, IGReceiptInformationModel receiptInformation);
    public void processPaymentFailed(String errorMessage);
}
