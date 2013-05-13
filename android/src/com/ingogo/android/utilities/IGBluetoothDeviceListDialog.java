package com.ingogo.android.utilities;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ingogo.android.R;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.webservices.interfaces.IGBluetoothDeviceSelectListener;

public class IGBluetoothDeviceListDialog extends Dialog implements
		OnItemClickListener {

	private List<BluetoothDevice> _deviceList = new ArrayList<BluetoothDevice>();
	private Context _currentContext;
	BluetoothAdapter _blutoothAdapter;
	private ListView _deviceListView;
	private DeviceAdapter _adapter;
	private IGBluetoothDeviceSelectListener _listener;

	public IGBluetoothDeviceListDialog(Context context,IGBluetoothDeviceSelectListener listener) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.devicelist_layout);
		_currentContext = context;
		_listener = listener;
		_blutoothAdapter = BluetoothAdapter.getDefaultAdapter();

		initViews();
		// TODO Auto-generated constructor stub
		IGUpdatePositionPollingTask.ignoreStaleState = true;
	}

	private void initViews() {
		// TODO Auto-generated method stub
		_deviceListView = (ListView) findViewById(R.id.deviceList);
		_deviceListView.setOnItemClickListener(this);
		_adapter = new DeviceAdapter(_deviceList, _currentContext);
		_deviceListView.setAdapter(_adapter);

	}

	public void updateDevicelist(BluetoothDevice device) {
		_deviceList.add(device);
		_adapter = new DeviceAdapter(_deviceList, _currentContext);
		_deviceListView.setAdapter(_adapter);

	}
	public void clearDeviceList() {
		_deviceList.clear();
		_adapter = new DeviceAdapter(_deviceList, _currentContext);
		_deviceListView.setAdapter(_adapter);		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		_listener.connectToDevice(_deviceList.get(arg2));
		dismiss();
	}

}
