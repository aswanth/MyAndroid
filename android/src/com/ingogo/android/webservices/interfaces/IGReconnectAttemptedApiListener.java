package com.ingogo.android.webservices.interfaces;


public interface IGReconnectAttemptedApiListener {
	public void reconnectAttemptedRequestCompleted();
	public void reconnectAttemptedRequestFailed(String errorMessage);

}
