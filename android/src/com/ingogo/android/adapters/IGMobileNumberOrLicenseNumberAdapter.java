package com.ingogo.android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ingogo.android.R;

public class IGMobileNumberOrLicenseNumberAdapter extends BaseAdapter {
	
	private Context _context;
	private ArrayList<String> _arrayList;
	
	public IGMobileNumberOrLicenseNumberAdapter(Context context, ArrayList<String> arrayList) {
		_context = context;
		_arrayList = arrayList;
		
	}
	
	@Override
	public int getCount() {
		return _arrayList.size();
	}
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return _arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null)
			convertView = LayoutInflater.from(_context).inflate(R.layout.mobilenumber_license_listcell, null);
		    TextView mobileNumberOrLicenseNumberTv = (TextView)convertView.findViewById(R.id.mobileNumber_license_tv);
		    mobileNumberOrLicenseNumberTv.setText(_arrayList.get(position));
			
		
		return convertView;
		
	}

}
