package com.ingogo.android.activities.payments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import android.os.Environment;
import android.util.Log;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.cardreader.helpers.LinePrintHelper;
import com.ingogo.android.utilities.IGBluetoothDeviceListDialog;
import com.ingogo.android.utilities.IGBluetoothHelper;
import com.ingogo.android.utilities.IGBluetoothReceiveListener;
import com.ingogo.android.utilities.IGUtility;

public class IGBluetoothBaseActivity extends IGBaseActivity implements
		IGBluetoothReceiveListener {

	BluetoothAdapter _blutoothAdapter;
	ProgressDialog myProgressDialog;
	protected ProgressDialog _blutoothProgressDialog;
	IGBluetoothDeviceListDialog _dialog;
	private static final int REQUEST_ENABLE_BT = 110;
	private static final int REQUEST_RECONNECT = 111;
	private static String PRINTER_NAME = "";

	protected BluetoothDevice _printerDevice = null;
	private boolean _isPrinterFound = false;
	public static boolean _bondingStarted = false;
	// private ConnectThread _bConnectThread;
	// private ConnectedThread _bConnectedThread;
	private boolean _isPrintReceipt = false;
	private boolean _isPrinterConnected = false;
	protected IGBluetoothHelper _bluetoothHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		_bluetoothHelper = new IGBluetoothHelper(this, this);

	}

	private void initData() {
		// TODO Auto-generated method stub
		_blutoothAdapter = BluetoothAdapter.getDefaultAdapter();

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);

	}

	protected void isPrintReceipt(boolean isPrintRecipt) {
		_isPrintReceipt = isPrintRecipt;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (_bluetoothHelper != null)
			_bluetoothHelper.stop();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		initData();

		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		this.unregisterReceiver(mReceiver);
		IGUtility.dismissProgressDialog(_blutoothProgressDialog);
		
		super.onPause();
	}

	protected boolean isPrinterConnected() {
		return _isPrinterConnected;
	}

	protected void setIsPrinterConnected(boolean _isPrinterConnected) {
		this._isPrinterConnected = _isPrinterConnected;
	}

	protected void turnOnBlutooth(boolean isPrintReceipt) {

		_bluetoothHelper.stop();
		setIsPrinterConnected(false);

		PRINTER_NAME = IGUtility.getDefaults(IGConstants.kDeviceName,
				IGBluetoothBaseActivity.this);
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
				_blutoothProgressDialog = IGUtility
						.showProgressDialogWithMsg(
								IGBluetoothBaseActivity.this,
								"Connecting with printer");
			}
			break;

		default:
			break;
		}

	}

	private void searchForDevices() {

		_isPrinterFound = false;
		IGUtility.dismissProgressDialog(_blutoothProgressDialog);
		// If we're already discovering, stop it
		if (_blutoothAdapter.isDiscovering()) {
			_blutoothAdapter.cancelDiscovery();
		}
		if (!isFinishing()) {
			_blutoothProgressDialog = IGUtility.showProgressDialogWithMsg(this,
					"Connecting with printer");
		}
		
		_blutoothAdapter.startDiscovery();

	}

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, final Intent intent) {

			IGBluetoothBaseActivity.this.runOnUiThread(new Runnable() {

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

							// pairDevice(device);
							_printerDevice = device;
							_bluetoothHelper.connect(device, !_isPrintReceipt);
							_isPrinterFound = true;
						}

					} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
							.equals(action)) {
						if (!_isPrinterFound) {
							enableButtons();
							IGUtility.showDialogOk("", getString(R.string.print_error_report_message),
									IGBluetoothBaseActivity.this);
							IGUtility
									.dismissProgressDialog(_blutoothProgressDialog);
						}

						IGUtility.dismissProgressDialog(myProgressDialog);

					} else if (BluetoothAdapter.ACTION_STATE_CHANGED
							.equals(action)) {

						IGUtility
								.dismissProgressDialog(_blutoothProgressDialog);
					}
				}

			});

		}
	};

	public boolean isPrinterConfigured() {
		if (IGUtility.getDefaults(IGConstants.KPrintDeviceName,
				IGBluetoothBaseActivity.this) == null) {
			return false;
		} else {
			return true;
		}
	}

	public void setPairedDeviceName(String printerName) {
		IGUtility.setDefaults(IGConstants.KPrintDeviceName, printerName,
				IGBluetoothBaseActivity.this);
	}

	public void removePairedDeviceName() {
		IGUtility.removeDefaults(IGConstants.KPrintDeviceName,
				IGBluetoothBaseActivity.this);
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


	//
	private void showFailedDialog() {
		// TODO Auto-generated method stub
		IGBluetoothBaseActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				IGUtility.dismissProgressDialog(_blutoothProgressDialog);
				setIsPrinterConnected(false);
				enableButtons();
				Dialog dlg = new AlertDialog.Builder(
						IGBluetoothBaseActivity.this)
						.setTitle("")
						.setMessage(
								IGBluetoothBaseActivity.this
										.getText(R.string.printer_config_failed_msg_start)
										+ IGUtility.getDefaults(
												IGConstants.kDevicePin,
												IGBluetoothBaseActivity.this)
										+ IGBluetoothBaseActivity.this
												.getText(R.string.printer_config_failed_msg_end))
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										turnOnBlutooth(false);
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

									}
								}).create();
				if (!IGBluetoothBaseActivity.this.isFinishing())
					dlg.show();

			}
		});

	}

	//
	private void showSucessDialog() {
		IGBluetoothBaseActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				setPairedDeviceName(PRINTER_NAME);
				IGUtility.dismissProgressDialog(_blutoothProgressDialog);
				enableButtons();
				setIsPrinterConnected(true);

				Dialog dlg = new AlertDialog.Builder(
						IGBluetoothBaseActivity.this)
						.setTitle("")
						.setMessage(
								IGBluetoothBaseActivity.this
										.getText(R.string.printer_config_complete_msg))
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

									}
								}).create();
				if (!IGBluetoothBaseActivity.this.isFinishing())
					dlg.show();

			}
		});
		// TODO Auto-generated method stub

	}

	//
	protected void enableButtons() {

	}

	protected void printData() {
		// _bluetoothHelper.stop();
		if (_printerDevice != null) {
			printFile(getTestPrintData());
		} else {
			isPrintReceipt(true);
			turnOnBlutooth(true);
		}

	}

	
	private String getTestPrintData() {

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String currentDate = dateFormat.format(new Date());
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
		String currentTime = timeFormat.format(new Date());

		String content = "\n~ingogo test receipt~" + "\nview e-receipt at:"
				+ " https://www.ingogo.mobi/rcp?r=test" + "\n\n"
				+ currentDate
				+ " at "
				+ currentTime
				+ "\nPaid at: "
				+ "Test Suburb"
				+ "\nTaxi Number: "
				+ "XYZ123"
				+ "\n"
				+ "\n~Fare~ (inc GST)      "
				+ "~"
				+ "$21.62"
				+ "~"
				+ "\n\n~ingogo TEST TAX INVOICE~\ningogo Pty Ltd\nABN "
				+ "11 111 111 111"
				+ "\nCard fee            "
				+ "$2.38"
				+ "\n~TOTAL PAID~          "
				+ "~"
				+ "$24.00"
				+ "~"
				+ "\n(inc GST)           " + "$2.18\n\n";

//		File sdcard = Environment.getExternalStorageDirectory();
//		File file = new File(sdcard, "testreceipt.txt");
		 File file = getFileWithName("testreceipt.txt");

		if (file.exists()) {
			file.delete();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
			buf.append(content);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "testreceipt.txt";

	}

		@Override
	public void changeOfStatusResult(int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionFailedResult(final String message) {
		// TODO Auto-generated method stub
		IGBluetoothBaseActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				_printerDevice = null;
				IGUtility.dismissProgressDialog(_blutoothProgressDialog);
				setIsPrinterConnected(false);

				IGUtility.showDialog("", message, IGBluetoothBaseActivity.this);
			}
		});
	}

	@Override
	public void connectionLostResult(String message) {
		// TODO Auto-generated method stub
		setIsPrinterConnected(false);
		_printerDevice = null;
		IGUtility.dismissProgressDialog(_blutoothProgressDialog);

	}

	@Override
	public void connectedResult(String message) {
		// TODO Auto-generated method stub
		IGBluetoothBaseActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				IGUtility.dismissProgressDialog(_blutoothProgressDialog);

				if (_isPrintReceipt) {
					printFile(getTestPrintData());
				}
			}
		});
		// Toast.makeText(this, "Sucess", Toast.LENGTH_LONG).show();
	}

	protected void printFile(String fileName) {
//		File sdcard = Environment.getExternalStorageDirectory();
//
//		// Get the text file
//		File file = new File(sdcard, fileName);
		 File file = getFileWithName(fileName);


		// Read text from file

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				/*
				 * if (line.length() > SINGLE_LINE_LENGTH &&
				 * !(line.contains("~_"))) {
				 * 
				 * printLargeReceiptItem(line, bluetoothHelper); continue; }
				 */
				String printingData = LinePrintHelper.getPrinterFeed(line);
				String data = printingData + LINE_FEED;
				_bluetoothHelper.write(data.getBytes());
			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}

		if (file.exists()) {
			file.delete();
		}
		Dialog dlg = new AlertDialog.Builder(IGBluetoothBaseActivity.this)
				.setTitle("Verify Print")
				.setMessage(
						"Verify that the Test Receipt has been printed. Ensure that printer is powered on.")
				.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).create();
		dlg.show();
	}
	
	protected void printFile(String fileName, String alertMessage, String alertTitle, boolean showAlert) {
//		File sdcard = Environment.getExternalStorageDirectory();
//
//		// Get the text file
//		File file = new File(sdcard, fileName);
		 File file = getFileWithName(fileName);

		// Read text from file

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				/*
				 * if (line.length() > SINGLE_LINE_LENGTH &&
				 * !(line.contains("~_"))) {
				 * 
				 * printLargeReceiptItem(line, bluetoothHelper); continue; }
				 */
				String printingData = LinePrintHelper.getPrinterFeed(line);
				String data = printingData + LINE_FEED;
				_bluetoothHelper.write(data.getBytes());
			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}

		if (file.exists()) {
			file.delete();
		}
		if (showAlert) {
			Dialog dlg = new AlertDialog.Builder(IGBluetoothBaseActivity.this)
			.setTitle(alertTitle)
			.setMessage(alertMessage)
			.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

				}
			}).create();
			dlg.show();
		}
		
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
	public void pairingCompletedResult() {
		// TODO Auto-generated method stub
		IGBluetoothBaseActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				showSucessDialog();

			}
		});
	}

	@Override
	public void pairingFailedResult() {
		// TODO Auto-generated method stub
		_printerDevice = null;
		IGBluetoothBaseActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showFailedDialog();

			}
		});
	}

	@Override
	public void onBackPressed() {
		if (_bluetoothHelper != null)
			_bluetoothHelper.stop();
		super.onBackPressed();
	}

}
