package com.ingogo.android.activities;

import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGBluetoothBaseActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGPrinterConfigApi;
import com.ingogo.android.webservices.interfaces.IGPrinterConfigApiListener;

public class IGPrinterConfigActivity extends IGBluetoothBaseActivity implements
		IGPrinterConfigApiListener {

	private ProgressDialog _progressDialog;
	ImageButton _saveBtn, _exitBtn;
	private EditText _printerCodeET;
	private static String _printerCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.printer_config);
		initViews();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		initViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IGUpdatePositionPollingTask.setIgnoreStaleState(true);
	}

	private void initViews() {
		_printerCodeET = (EditText) findViewById(R.id.printerCodeTextField);
		_printerCodeET
				.setFilters(new InputFilter[] { IGUtility.alphaNumericFilter });

		_saveBtn = (ImageButton) findViewById(R.id.save_button);
		_exitBtn = (ImageButton) findViewById(R.id.exitBtn);

		if (_printerCode != null) {
			_printerCodeET.setText(_printerCode);
			_printerCodeET.setSelection(_printerCodeET.getText().length());

		}
	}

	/**
	 * OnclickListener for the Submit Button
	 * 
	 * @param view
	 */
	public void saveButtonClicked(View view) {

		if (!_printerCodeET.getText().toString().trim().equals(""))
			saveButtonAction();
	}

	public void saveButtonAction() {

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_printerCodeET.getWindowToken(), 0);

		if (IGUtility.isNetworkAvailable(this)) {

			_saveBtn.setEnabled(false);
			_exitBtn.setEnabled(false);

			_progressDialog = IGUtility
					.showProgressDialog(IGPrinterConfigActivity.this);
			IGPrinterConfigApi printerConfig = new IGPrinterConfigApi(this,
					this);
			printerConfig.initialisePrinterConfig(_printerCodeET.getText()
					.toString().trim());

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		try {
			IGUtility.dismissProgressDialog(_progressDialog);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (_bluetoothHelper != null) {
			_bluetoothHelper.stop();
			setIsPrinterConnected(false);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void printerConfigCompleted(String deviceName, String devicePin) {
		// TODO Auto-generated method stub
		_progressDialog.dismiss();
		enableButtons();

		if (deviceName != null && devicePin != null) {

			Dialog dlg = new AlertDialog.Builder(IGPrinterConfigActivity.this)
					.setTitle("")
					.setMessage(
							this.getText(R.string.printer_pin_msg) + " "
									+ devicePin + ".")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									isPrintReceipt(false);
									turnOnBlutooth(false);

								}
							}).create();

			IGUtility.setDefaults(IGConstants.kDeviceName, deviceName,
					IGPrinterConfigActivity.this);
			IGUtility.setDefaults(IGConstants.kDevicePin, devicePin,
					IGPrinterConfigActivity.this);

			int deviceCount = 0;

			BluetoothAdapter _adapter = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> pairedDevices = _adapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					String deviceBTName = device.getName();

					if (deviceName.trim()
							.equals(deviceBTName.trim().toString())) {
						unpairDevice(device);

						if (!IGPrinterConfigActivity.this.isFinishing())
							dlg.show();
						break;
					} else {
						deviceCount++;
					}
				}
			}

			if (deviceCount == pairedDevices.size()) {
				if (!IGPrinterConfigActivity.this.isFinishing())
					dlg.show();
			}

		} else {
			IGUtility.showDialogOk("",
					this.getText(R.string.printer_device_id_error_msg)
							.toString(), this);
		}

	}

	@Override
	public void printerConfigFailed(String errorMessage) {
		// TODO Auto-generated method stub
		_progressDialog.dismiss();
		enableButtons();
		IGUtility.showDialogOk("Error", errorMessage, this);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		_progressDialog.dismiss();
		enableButtons();

	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		_progressDialog.dismiss();
		enableButtons();

	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		_progressDialog.dismiss();
		enableButtons();

	}

	@Override
	public void onNullResponseRecieved() {
		_progressDialog.dismiss();
		enableButtons();

	}

	/**
	 * Button action to change the current theme If the current theme is day
	 * then set night as current theme and restart the activity. If the current
	 * theme is night then set day as current theme and restart the activity.
	 * 
	 * @param view
	 */
	public void changeTheme(View view) {
		if (IngogoApp.getThemeID() == 1) {
			IngogoApp.setThemeID(2);
		} else {
			IngogoApp.setThemeID(1);
		}

		restartActivty();
	}

	/**
	 * Function to restart the activity to apply the new theme.
	 */
	private void restartActivty() {

		_printerCode = _printerCodeET.getText().toString();

		Intent intent = getIntent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	/**
	 * exit button tapped
	 * 
	 * @param v
	 */
	public void onExitButtonClick(View v) {
		_printerCode = null;
		this.onBackPressed();
	}

	@Override
	protected void enableButtons() {
		_saveBtn.setEnabled(true);
		_exitBtn.setEnabled(true);
	}

	/*
	 * Action for test receipt button.
	 */
	public void onPrintReceiptClick(View v) {
		isPrintReceipt(true);
		if (isPrinterConfigured()) {
			if(_bluetoothHelper!=null) {
				_bluetoothHelper.stop();
			}
			//if (isPrinterConnected()) {
			//	printData();
		//	} else {
				turnOnBlutooth(false);
		//	}
		} else {
			IGUtility.showDialogOk("",
					getResources().getString(R.string.printer_not_paired),
					IGPrinterConfigActivity.this);

		}

	}
}
