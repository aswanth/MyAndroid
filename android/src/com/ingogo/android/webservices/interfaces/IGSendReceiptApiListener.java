package com.ingogo.android.webservices.interfaces;

public interface IGSendReceiptApiListener {
	public void sendReceiptCompleted(String statusResponse);
    public void sendReceiptFailed(String errorMessage);
}
