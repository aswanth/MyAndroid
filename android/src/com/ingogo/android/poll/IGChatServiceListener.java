/*
 * Package Name : com.ingogo.android.poll
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Listener class for incoming chats,extended from IGIncomingMessageListener.
 */

package com.ingogo.android.poll;

import java.util.ArrayList;


public interface IGChatServiceListener extends IGIncomingMessageListener{
	public void chatsRecieved(ArrayList<String> chats);	

}
