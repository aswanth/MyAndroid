package com.ingogo.android.webservices.interfaces;


public interface IGTakePaymentApiListener {
	public void takePaymentCompleted(boolean status);
    public void takePaymentFailed(String errorMessage);
}
