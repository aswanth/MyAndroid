package com.ingogo.android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ingogo.android.R;



public class IGSuburbListAdapter extends BaseAdapter  {
	
	private Context _context;
	private ArrayList<String> _suburbList;
	
	public IGSuburbListAdapter(Context context, ArrayList<String> suburbList) {
		super();
		this._context = context;
		this._suburbList = suburbList;
	}
	
	@Override
	public int getCount() {
		return _suburbList.size();
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
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null)
			convertView = LayoutInflater.from(_context).inflate(R.layout.suburb_list_cell, null);

		String localityNameString = _suburbList.get(position);
		TextView localityNameTv = (TextView) convertView.findViewById(R.id.suburb_name_tv);
		localityNameTv.setText(localityNameString);
		return convertView;
	}

	

	

}

