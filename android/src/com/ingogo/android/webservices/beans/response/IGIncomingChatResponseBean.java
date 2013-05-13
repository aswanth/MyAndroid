package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

import com.ingogo.android.model.IGIncomingMessageModel;

public class IGIncomingChatResponseBean extends IGBaseResponseBean {
	
	private ArrayList<IGIncomingMessageModel> messages;
	private String bookingStatus;
	public String getBookingStatus() {
		return bookingStatus;
	}
	public void setBookingStatus(String bookingStatus) {
		this.bookingStatus = bookingStatus;
	}
	public ArrayList<IGIncomingMessageModel> getMessages() {
		return messages;
	}
	public void setMessages(ArrayList<IGIncomingMessageModel> messages) {
		this.messages = messages;
	}

}
