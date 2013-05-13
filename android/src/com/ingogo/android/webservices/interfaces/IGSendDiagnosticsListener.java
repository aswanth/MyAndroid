package com.ingogo.android.webservices.interfaces;

public interface IGSendDiagnosticsListener {
	
	public void successfullyySendDiagnostics( String mobileNumber );
	public void failedToSendDiagnostics( String errorMessage );

}
