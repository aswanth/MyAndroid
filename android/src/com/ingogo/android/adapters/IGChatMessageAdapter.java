package com.ingogo.android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;

public class IGChatMessageAdapter extends BaseAdapter {

	ArrayList<String> _messages;
	Context _context;
	
	/**
	 * Constructor.
	 * @param context
	 */
	public IGChatMessageAdapter(Context context) {
		_messages = new ArrayList<String>();
		_context = context;
	}
	
	/**
	 * To set the messages that may be incoming or outgoing.
	 * @param messages
	 */
	public void setMessages(ArrayList<String> messages) {
		_messages = messages;
	}
	
	/**
	 * To get the message count
	 */
	@Override
	public int getCount() {
		return _messages.size();
	}

	@Override
	public Object getItem(int index) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {

		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		TextView inChatTextView = null;
		TextView passengerNotReceiveTv = null;
		String chatMsg = _messages.get(i);
		
		//if the chatMsg startsWith "I:",then it is incoming message. Otherwise outgoing.
		if(chatMsg.startsWith("I:")){
			//Inflate layout for incoming message.
			view =  LayoutInflater.from(_context).inflate(R.layout.incoming_chat, null);
			inChatTextView = (TextView)view.findViewById(R.id.incoming_chat_text_view);
			inChatTextView.setText(chatMsg.substring(2));
		} else if(chatMsg.startsWith("O:")){
			//Inflate layout for outgoing message.
			view =  LayoutInflater.from(_context).inflate(R.layout.outgoing_chat, null);
			inChatTextView = (TextView)view.findViewById(R.id.outgoing_chat_text_view);
			passengerNotReceiveTv = (TextView)view.findViewById(R.id.passenger_did_not_receive_tv);
			if(chatMsg.lastIndexOf(IGConstants.kChatFailures) == -1) {
				inChatTextView.setText(chatMsg.substring(2));
			}else {
				passengerNotReceiveTv.setVisibility(View.VISIBLE);
				chatMsg = chatMsg.replace(IGConstants.kChatFailures, "");
				inChatTextView.setText(chatMsg.substring(2));
			}
		}


		return view;

	}
	

}
