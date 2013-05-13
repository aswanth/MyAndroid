package com.ingogo.android.utilities;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ingogo.android.R;

public class DeviceAdapter extends BaseAdapter {
	private List<BluetoothDevice> _devices;
	private Context _context;

	public DeviceAdapter(List<BluetoothDevice> devices, Context context) {
		super();
		this._context = context;
		this._devices = devices;
	}
	
	public void updateData(List<BluetoothDevice> devices) {
		_devices = devices;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return _devices.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return _devices.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			LayoutInflater mInflater = LayoutInflater.from(_context);
			convertView = mInflater.inflate(R.layout.device_layout, null);
		}
		
		TextView deviceName = (TextView)convertView.findViewById(R.id.deviceName);
		deviceName.setText(_devices.get(position).getName());
		return convertView;
	}
	
	
}
