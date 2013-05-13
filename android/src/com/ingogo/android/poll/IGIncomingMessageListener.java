/*
 * Package Name : com.ingogo.android.poll
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Listener class for Incoming chat service used by Job Details activity.
 */

package com.ingogo.android.poll;


public interface IGIncomingMessageListener {
	public void incomingMessageReceived(String incomingMessage, boolean playNotification);
	public void incomingMessageError(String errorMessage);
	public void bookingStatusReceived(String bookingStatus);
	

}
