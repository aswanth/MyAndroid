package com.ingogo.android.webservices.interfaces;

public interface IGCompleteOfflineListener {
	
	public void completeOfflineSuccess ( String sucessString, String totalPaid );
	public void completeOfflineFailed ( String errorMessage, boolean isHandleDriverStaleState);

}
