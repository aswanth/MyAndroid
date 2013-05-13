package com.ingogo.android.webservices.interfaces;


public interface IGPaymentJobApiListener {
	public void paymentJobCompleted(String receipt);

	public void paymentJobFailed(String errorMessagesToString);
}
