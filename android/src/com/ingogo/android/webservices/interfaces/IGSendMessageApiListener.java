package com.ingogo.android.webservices.interfaces;

public interface IGSendMessageApiListener {
	
	public void messageSent() ;
	public void messageSentingFailed( String sentMessage , int chatIndex);

}
