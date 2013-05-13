package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGCreateBookingForPaymentResponseBean;

public interface IGCreateBookingForPaymentApiListener {
	public void createBookingForPaymentCompleted(IGCreateBookingForPaymentResponseBean response);
    public void createBookingForPaymentFailed(String errorMessage);
}
