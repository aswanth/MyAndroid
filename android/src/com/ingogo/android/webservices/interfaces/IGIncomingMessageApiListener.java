package com.ingogo.android.webservices.interfaces;

import java.util.ArrayList;

import com.ingogo.android.model.IGIncomingMessageModel;

public interface IGIncomingMessageApiListener {
	
	public void successfullyGetIncomingMessage ( ArrayList<IGIncomingMessageModel> messages, String bookingStatus );
	public void failedToGetIncomingMessage();

}
