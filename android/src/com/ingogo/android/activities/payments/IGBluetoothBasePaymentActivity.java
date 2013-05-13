package com.ingogo.android.activities.payments;

import java.lang.reflect.Method;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.model.IGReceiptInformationModel;
import com.ingogo.android.utilities.IGBluetoothDeviceListDialog;
import com.ingogo.android.utilities.IGBluetoothHelper;
import com.ingogo.android.utilities.IGBluetoothReceiveListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGPrintReceiptApi;
import com.ingogo.android.webservices.interfaces.IGBluetoothDeviceSelectListener;
import com.ingogo.android.webservices.interfaces.IGPrintReceiptApiListener;

public class IGBluetoothBasePaymentActivity extends IGBaseActivity implements
		IGPrintReceiptApiListener, IGBluetoothReceiveListener,
		IGBluetoothDeviceSelectListener {

	
	BluetoothAdapter _blutoothAdapter;
	IGBluetoothHelper _blutoothHelper;
	ProgressDialog myProgressDialog;
	ProgressDialog _blutoothProgressDialog;
	IGBluetoothDeviceListDialog _dialog;
	private static final int REQUEST_ENABLE_BT = 110;
	private static final int REQUEST_RECONNECT = 111;
	private static String PRINTER_NAME = "";

	private BluetoothDevice _printerDevice = null;
	private boolean _isPrinterFound = false;
	private static boolean _isPrintReceipt = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	protected void setIsPrintReceipt(boolean isPrintReceipt) {
		_isPrintReceipt = isPrintReceipt;
	}

	protected void initData() {
		// TODO Auto-generated method stub
		_blutoothAdapter = BluetoothAdapter.getDefaultAdapter();
		_blutoothHelper = new IGBluetoothHelper(this, this);
		_dialog = new IGBluetoothDeviceListDialog(this, this);

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(myProgressDialog);
		IGUtility.dismissProgressDialog(_blutoothProgressDialog);
		_isPrinterFound = false;
		initData();

		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (_blutoothAdapter.isDiscovering()) {
			_isPrinterFound = true;
			_blutoothAdapter.cancelDiscovery();
		}
		this.unregisterReceiver(mReceiver);

		if (_blutoothHelper != null) {
			_blutoothHelper.stop();
		}
		super.onPause();
	}

	protected void callPrintReceiptApi(String jobId) {
		if (IGUtility.isNetworkAvailable(this)) {
			myProgressDialog = IGUtility.showProgressDialog(this);
			IGPrintReceiptApi receiptApi = new IGPrintReceiptApi(this, this);
			receiptApi.printReceipt(jobId);

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	@Override
	public void printReceiptCompleted(IGReceiptInformationModel contactInfo) {
		IGUtility.dismissProgressDialog(myProgressDialog);

		IGPaymentCompletedActivity.writeReceipt(contactInfo);
		// TODO Auto-generated method stub
		turnOnBlutooth(true);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(myProgressDialog);
		IGBluetoothBasePaymentActivity.this.finishPrinting();
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(myProgressDialog);
		IGBluetoothBasePaymentActivity.this.finishPrinting();
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(myProgressDialog);
		IGBluetoothBasePaymentActivity.this.finishPrinting();
	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(myProgressDialog);
		IGBluetoothBasePaymentActivity.this.finishPrinting();
	}

	protected void turnOnBlutooth(boolean isPrintReceipt) {

		_isPrintReceipt = isPrintReceipt;
		PRINTER_NAME = IGUtility.getDefaults(IGConstants.KPrintDeviceName,
				IGBluetoothBasePaymentActivity.this);

		if (_blutoothAdapter != null) {
			if (!_blutoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			} else {
				this.searchForDevices();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == RESULT_OK) {
				this.searchForDevices();
			}
			break;

		case REQUEST_RECONNECT:
			if (_printerDevice != null) {
				_blutoothHelper.connect(_printerDevice,false);
				_blutoothProgressDialog = IGUtility.showProgressDialogWithMsg(
						IGBluetoothBasePaymentActivity.this,
						"Connecting with printer");
			}
			break;

		default:
			break;
		}

	}

	private void searchForDevices() {

		// if (_printerDevice == null) {
		if (_isPrintReceipt) {
			_blutoothHelper.stop();
		}
		_isPrinterFound = false;
		IGUtility.dismissProgressDialog(_blutoothProgressDialog);
		// If we're already discovering, stop it
		if (_blutoothAdapter.isDiscovering()) {
			_blutoothAdapter.cancelDiscovery();
		}
		_blutoothProgressDialog = IGUtility.showProgressDialogWithMsg(this,
				"Connecting with printer");
		_blutoothAdapter.startDiscovery();

	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, final Intent intent) {

			IGBluetoothBasePaymentActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					String action = intent.getAction();
					// When discovery finds a device
					if (BluetoothDevice.ACTION_FOUND.equals(action)) {
						// Get the BluetoothDevice object from the Intent
						BluetoothDevice device = intent
								.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						if (device != null && device.getName() != null
								&& device.getName().trim().equals(PRINTER_NAME)) {
							connectToDevice(device);
							_isPrinterFound = true;
						}

					} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
							.equals(action)) {
						if (!_isPrinterFound) {
							IGUtility
							.dismissProgressDialog(_blutoothProgressDialog);
							Dialog dlg = new AlertDialog.Builder(
									IGBluetoothBasePaymentActivity.this)
									.setTitle("")
									.setMessage(getString(R.string.print_error_report_message))
									.setPositiveButton("OK",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {

													IGBluetoothBasePaymentActivity.this
															.finishReprintReceipt();
													IGBluetoothBasePaymentActivity.this.finishPrinting();
												}
											}).create();
							if (!IGBluetoothBasePaymentActivity.this.isFinishing())
								dlg.show();
							
						}
						
						IGUtility.dismissProgressDialog(myProgressDialog);

					} else if (BluetoothAdapter.ACTION_STATE_CHANGED
							.equals(action)) {
						IGBluetoothBasePaymentActivity.this
						.finishReprintReceipt();
						IGBluetoothBasePaymentActivity.this.finishPrinting();
						IGUtility
								.dismissProgressDialog(_blutoothProgressDialog);
					}
				}
			});

		}
	};

	@Override
	public void printReceiptFailed(String errorMessage) {
		// TODO Auto-generated method stubaa
		IGUtility.dismissProgressDialog(myProgressDialog);
		IGUtility.showDialogOk("", errorMessage, this);
		IGBluetoothBasePaymentActivity.this.finishPrinting();
	}

	@Override
	public void changeOfStatusResult(int state) {
		// TODO Auto-generated method stub

		if (state != IGBluetoothHelper.STATE_CONNECTED) {
			// _isPrinterConnected = false;

		}
	}

	@Override
	public void connectionFailedResult(final String message) {
		IGUtility.dismissProgressDialog(_blutoothProgressDialog);
		_printerDevice = null;

		// TODO Auto-generated method stub
		IGBluetoothBasePaymentActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {

				if (_isPrintReceipt) {
					// TODO Auto-generated method stub
					Dialog dlg = new AlertDialog.Builder(
							IGBluetoothBasePaymentActivity.this)
							.setTitle("")
							.setMessage(message)
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {

											IGBluetoothBasePaymentActivity.this
													.finishReprintReceipt();
											IGBluetoothBasePaymentActivity.this.finishPrinting();
										}
									}).create();
					if (!IGBluetoothBasePaymentActivity.this.isFinishing())
						dlg.show();
				}

				// _isPrinterConnected = false;
			}
		});
	}

	@Override
	public void connectionLostResult(final String message) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_blutoothProgressDialog);
		_printerDevice = null;

		IGBluetoothBasePaymentActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				IGBluetoothBasePaymentActivity.this
				.finishReprintReceipt();
				IGBluetoothBasePaymentActivity.this.finishPrinting();
			}
		});

	}

	@Override
	public void connectedResult(String message) {

		IGBluetoothBasePaymentActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				IGUtility.dismissProgressDialog(_blutoothProgressDialog);

				if (_isPrintReceipt) {
					printFile(_blutoothHelper);
				}
			}
		});

		// TODO Auto-generated method stub

	}

	@Override
	public void writeDataResult(byte[] dataByte) {
		// TODO Auto-generated method stub
	}

	@Override
	public void readDataResult(byte[] buffer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectToDevice(BluetoothDevice device) {
		// TODO Auto-generated method stub
		if (device != null) {
			_printerDevice = device;
			_blutoothHelper.connect(device,false);

		}
	}

	public boolean isPrinterConfigured() {
		if (IGUtility.getDefaults(IGConstants.KPrintDeviceName,
				IGBluetoothBasePaymentActivity.this) == null) {
			return false;
		} else {
			return true;
		}
	}

	public void setPairedDeviceName(String printerName) {
		IGUtility.setDefaults(IGConstants.KPrintDeviceName, printerName,
				IGBluetoothBasePaymentActivity.this);
	}

	public void removePairedDeviceName() {
		IGUtility.removeDefaults(IGConstants.KPrintDeviceName,
				IGBluetoothBasePaymentActivity.this);
	}

	
	protected void unpairDevice(BluetoothDevice device) {

		removePairedDeviceName();
		try {
			Method m = device.getClass()
					.getMethod("removeBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			Log.e("UNPAIR", e.getMessage());
		}
	}

	@Override
	public void pairingCompletedResult() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pairingFailedResult() {
		// TODO Auto-generated method stub
		
	}

}
