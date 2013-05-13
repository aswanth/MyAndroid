package com.ingogo.android.adapters;


import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGCancellationReasonActivity;
public class IGCancelReasonAdapter extends BaseAdapter {
	
	private Context _context;
	private Map<String,String> _reasonHashMap ;
	public static View _lastClickedButton;
	public IGCancelReasonAdapter(Context context, Map<String,String> reasonHashMap ) {

		super();
		this._context = context;
		this._reasonHashMap = reasonHashMap;
	}
	
	@Override
	public int getCount() {
		return _reasonHashMap.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position,  View convertView, ViewGroup parent) {
		
		final int _position = position;
		if(convertView == null)
			convertView = LayoutInflater.from(_context).inflate(R.layout.cancel_reason_list_cell, null);
			final TextView cancelReasonTv = (TextView) convertView.findViewById(R.id.cancel_reason_tv);
			final Button radioButton = (Button)convertView.findViewById(R.id.radioButton);
			radioButton.setTag(position);
			//Click on the button is listened by this listener
			radioButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//Change the background of the button to selected state.
					radioButton.setBackgroundResource(R.drawable.radio_sel);
					//Change the background of the last clicked button to normal state.
					if(_lastClickedButton != null && _lastClickedButton != v) {
						_lastClickedButton.setBackgroundResource(R.drawable.radio_un);
					}
					IGCancellationReasonActivity._cancellationReasonKey = _reasonHashMap.keySet().toArray()[_position].toString();
					IGCancellationReasonActivity._continueButton.setEnabled(true);
					Log.i("Cancellation reason key",""+IGCancellationReasonActivity._cancellationReasonKey);
					_lastClickedButton = v;
				}
			});
			
			cancelReasonTv.setText(_reasonHashMap.get(_reasonHashMap.keySet().toArray()[position].toString()));
		return convertView;
	}



}