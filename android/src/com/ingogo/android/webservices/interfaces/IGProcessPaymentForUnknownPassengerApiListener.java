package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGProcessPaymentForUnknownPassengerResponseBean;


public interface IGProcessPaymentForUnknownPassengerApiListener {
	public void processPaymentForUnknownPassengerCompleted(IGProcessPaymentForUnknownPassengerResponseBean details);
    public void processPaymentForUnknownPassengerFailed(String errorMessage, IGProcessPaymentForUnknownPassengerResponseBean details);

}
