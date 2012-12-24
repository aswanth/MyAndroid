package com.qburst.docphin.adapters;

import java.util.ArrayList;

import android.R.integer;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qburst.docphin.R;
import com.qburst.docphin.datamodels.DocphinMessageModel;

public class DocphinMyMessageListAdapter extends BaseAdapter{
	
	Context context;
	ArrayList<DocphinMessageModel> messageList;
	TextView msgSender, msgSnippet, msgHeader;
	
	public DocphinMyMessageListAdapter(Context context, ArrayList<DocphinMessageModel> messageList) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.messageList = messageList;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return messageList.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return messageList.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		View convertView = view;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.docphin_messagelistitem, null);
			Log.i("new View", "pos-" + position);
		}
		msgHeader = (TextView)convertView.findViewById(R.id.textviewHeader);
		msgSender = (TextView)convertView.findViewById(R.id.textviewSender);
		msgSnippet = (TextView)convertView.findViewById(R.id.textviewMsgSnippet);
		
		setUpView(position);
		return convertView;
	}
	public void setUpView(int pos) {
		DocphinMessageModel msg = (DocphinMessageModel) getItem(pos);
		msgHeader.setText(msg.getTitle());
		msgSender.setText(msg.getSender());
		msgSnippet.setText(msg.getMessageSnippet());
	}

}
