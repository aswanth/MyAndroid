package com.ingogo.android.activities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGBluetoothBaseActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGSuburbModel;
import com.ingogo.android.utilities.IGBluetoothReceiveListener;
import com.ingogo.android.utilities.IGSuburbListDialog;
import com.ingogo.android.utilities.IGSuburbParser;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGGetPaidAtApi;
import com.ingogo.android.webservices.beans.response.IGGetPaidAtResponseBean;
import com.ingogo.android.webservices.interfaces.IGGetPaidAtApiListener;
import com.ingogo.android.webservices.interfaces.IGSuburbsReadsFileListener;

public class IGCashReceiptActivity extends IGBluetoothBaseActivity implements
		IGBluetoothReceiveListener, IGSuburbsReadsFileListener,
		IGGetPaidAtApiListener {

	private TextView _pickUpLabel;
	private TextView _paidAtLabel;

	private EditText _fareEditText;
	private TextView _dateTextView;
	private TextView _timeTextView;
	private ImageButton _printReceiptButton;

	private IGSuburbListDialog _pickUpsuburbListDialog;
	private IGSuburbListDialog _paidAtsuburbListDialog;

	private ProgressDialog _progressDialog;
	private boolean calledPrintFunction;
	String _currentFare = IGConstants.zeroBalance;
	DecimalFormat _decimalFormat;
	private Dialog _verifyPrintDialog;
	IGGetPaidAtResponseBean _paidAtsuburbDetails;
	private Runnable keyBoardRunnable = new Runnable() {

		@Override
		public void run() {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(_fareEditText, InputMethodManager.SHOW_FORCED);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cash_receipt);

		_decimalFormat = new DecimalFormat(IGConstants.zeroBalance);

		initViews();
		setupViews();
		showKeyboard();
		callRetrievePaidAtApi();
	}

	private void initViews() {
		_fareEditText = (EditText) findViewById(R.id.fareEditText);
		_pickUpLabel = (TextView) findViewById(R.id.pickupTv);
		_paidAtLabel = (TextView) findViewById(R.id.paidAtTv);
		_dateTextView = (TextView) findViewById(R.id.dateTv);
		_timeTextView = (TextView) findViewById(R.id.timeTv);
		_printReceiptButton = (ImageButton) findViewById(R.id.printReceiptButton);
	}

	/**
	 * Call the api to retrieve the reason list to be populated as check boxes
	 */
	private void callRetrievePaidAtApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGGetPaidAtApi api = new IGGetPaidAtApi(this, this);
			api.retrieveSuburb();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
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

		Intent intent = getIntent();
		intent.putExtra("fareValue", _fareEditText.getText().toString());
		intent.putExtra("paidAt", _paidAtLabel.getText().toString());
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	private void setupViews() {
		if (IngogoApp.getCurrentActivityOnTop() instanceof IGCashReceiptActivity) {
			String selectedSuburbName = IngogoApp.getSharedApplication()
					.getSelectedSuburbName();
			if (selectedSuburbName != null && !selectedSuburbName.equals("")) {
				_pickUpLabel.setText(IngogoApp.getSharedApplication()
						.getSelectedSuburbName());
			}
		} else {
			IngogoApp.getSharedApplication().setSelectedSuburbName("");
		}

		try {
			if (getIntent().getExtras().getString("paidAt") != null
					&& getIntent().getExtras().getString("paidAt").length() > 0) {
				_paidAtLabel.setText(getIntent().getExtras()
						.getString("paidAt"));

			}
		} catch (Exception e) {
			_paidAtLabel.setText("mandatory");

		}
		updateFareValue();

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
		String currentDate = dateFormat.format(new Date());
		currentDate = currentDate.toUpperCase();
		_dateTextView.setText(currentDate);

		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
		String currentTime = timeFormat.format(new Date());
		_timeTextView.setText(currentTime);

		setUpEditTextListeners();
		updatePrintButtonState();
	}

	private void updateFareValue() {
		try {
			if (getIntent().getExtras().getString("fareValue") != null
					&& getIntent().getExtras().getString("fareValue").length() > 2) {
				_fareEditText.setText(getIntent().getExtras().getString(
						"fareValue"));

			} else {
				_fareEditText.setText(IGConstants.zeroBalance);

			}
		} catch (Exception e) {
			_fareEditText.setText(IGConstants.zeroBalance);

		}
	}

	private void updatePrintButtonAlpha(boolean isEnabled) {
		_printReceiptButton.setEnabled(isEnabled);
		if (isEnabled) {
			_printReceiptButton.setAlpha(255);

		} else {
			_printReceiptButton.setAlpha(100);

		}
	}

	private void showKeyboard() {
		_fareEditText.postDelayed(keyBoardRunnable, 750);

		_fareEditText.setSelection(_fareEditText.getText().length());
	}

	private void setUpEditTextListeners() {
		_fareEditText
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {

					/*
					 * Respond to soft keyboard events, look for the DONE press
					 * on the fare entered field.
					 */

					@Override
					public boolean onEditorAction(TextView view, int keyCode,
							KeyEvent event) {
						if ((keyCode == EditorInfo.IME_ACTION_SEARCH
								|| keyCode == EditorInfo.IME_ACTION_DONE || event
								.getAction() == KeyEvent.ACTION_DOWN
								&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							// NEXT pressed. Hide the soft keyboard
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									_fareEditText.getWindowToken(), 0);

						}
						// Returning false allows other listeners to react to
						// the press.
						return false;

					}

				});

		_fareEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// When 1 is entered, it will be shown as '00.01'
				if (!s.toString().equals(_currentFare)) {

					_fareEditText.removeTextChangedListener(this);

					String cleanString = s.toString().replace(".", "");

					double parsed = 00.00;
					try {
						parsed = Double.parseDouble(cleanString);
					} catch (Exception e) {
					}

					String formated = (String.valueOf(_decimalFormat
							.format(parsed / 100)));

					// The formatted text will contain a ','. For converting
					// this to double for calculation
					// purpose the comma is removed
					_currentFare = formated.replace(",", "");
					if (_currentFare.equals("") || _currentFare.equals("0")) {
						_currentFare = IGConstants.zeroBalance;
					}
					_fareEditText.setText(_currentFare);
					_fareEditText.setSelection(_currentFare.length());

					_fareEditText.addTextChangedListener(this);
				}
				updatePrintButtonState();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				updatePrintButtonState();

			}
		});

	}

	private void updatePrintButtonState() {
		Double fareEntered = 0.0;
		try {
			fareEntered = Double
					.parseDouble(_fareEditText.getText().toString());
		} catch (NumberFormatException e) {
			fareEntered = 0.0;
		}
		if (fareEntered > 0) {
			if (_paidAtLabel.getText().length() < 0) {
				updatePrintButtonAlpha(false);

			} else if (_paidAtLabel.getText().toString()
					.equalsIgnoreCase("mandatory")) {
				updatePrintButtonAlpha(false);

			} else {
				updatePrintButtonAlpha(true);

			}
		} else {
			updatePrintButtonAlpha(false);

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		if(_fareEditText!=null && keyBoardRunnable!=null){
			_fareEditText.removeCallbacks(keyBoardRunnable);
		}
		super.onPause();
	}

	public void onPrintReceiptButtonClick(View view) {
		if (isPrinterConfigured()) {
			if (isPrinterConnected()) {
				printCashReceiptData();

			} else {
				calledPrintFunction = true;
				isPrintReceipt(true);
				turnOnBlutooth(false);
			}

		} else {
			IGUtility.showDialogOk("",
					getResources().getString(R.string.printer_not_paired),
					IGCashReceiptActivity.this);
		}

	}

	/**
	 * Call for print Referral Data
	 */
	private void printCashReceiptData() {
		// _bluetoothHelper.stop();
		if (_printerDevice != null) {
			calledPrintFunction = false;
			printFile(getPrintData(), null, null, false);
			showVerifyPrintAlert(getString(R.string.receipt_not_printed_alert));

		} else {
			calledPrintFunction = true;
			isPrintReceipt(true);
			turnOnBlutooth(true);
		}

	}

	/**
	 * Referral data is saved to sd card and and the file name is returned
	 * 
	 * @return
	 */
	private String getPrintData() {
		String pickup = "Unknown";
		if (_pickUpLabel.getText().length() > 0) {
			if (!(_pickUpLabel.getText().toString()
					.equalsIgnoreCase("optional"))) {
				pickup = _pickUpLabel.getText().toString();

			}
		}

		String paidAt = "Unknown";
		if (_paidAtLabel.getText().length() > 0) {
			if (!(_paidAtLabel.getText().toString()
					.equalsIgnoreCase("mandatory"))) {
				paidAt = _paidAtLabel.getText().toString();
			}
		}

		String paidWith = "CASH";
		// if (_paidAtsuburbDetails.getPaidWith()!=null) {
		// paidWith = _paidAtsuburbDetails.getPaidWith();
		// }

		String taxiNumber = "Unknown";
		if (_paidAtsuburbDetails.getTaxiNumber() != null) {
			taxiNumber = _paidAtsuburbDetails.getTaxiNumber();
		}

		String invoiceDescription = "";
		String abn = "Unknown";
		String driverCompanyName = "Unknown";

		Double fareEntered = 0.0;
		try {
			fareEntered = Double
					.parseDouble(_fareEditText.getText().toString());
		} catch (NumberFormatException e) {
			fareEntered = 0.0;
		}
		double limit = _paidAtsuburbDetails
				.getShowCompanyDetailsIfValueExceeds();
		if (limit < fareEntered) {
			driverCompanyName = _paidAtsuburbDetails.getDriverCompanyName();
			abn = _paidAtsuburbDetails.getDriverABN();
			boolean returnInVoice = true;
			Log.e("locationid", "location id = "
					+ IngogoApp.getSharedApplication().getLocalityId() + "");
			for (String id : _paidAtsuburbDetails
					.getLocalitiesRequiringCompanyDetails()) {
				if (id.equalsIgnoreCase(IngogoApp.getSharedApplication()
						.getLocalityId() + "")) {
					returnInVoice = true;
				}
			}
			if (returnInVoice) {
				invoiceDescription = "\n\n" + "Driver's TAX INVOICE\n"
						+ driverCompanyName + "\n" + "ABN " + abn;

			}
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String currentDate = dateFormat.format(new Date());
		String content = "\n~ingogo cash receipt~" + "\n" + currentDate
				+ " at " + _timeTextView.getText().toString() + "\nPick up: "
				+ pickup + "\nPaid at: " + paidAt + "\nPaid with: " + paidWith
				+ "\nTaxi number: " + taxiNumber + invoiceDescription
				+ "\n\n~TOTAL PAID (inc GST) " + "$"
				+ _fareEditText.getText().toString() + "~" + "\n\n~Save "
				+ IngogoApp.getSharedApplication().getSavingsPercentage()
				+ "%~ get the ingogo App for\niPhone & Android. Watch your\n"
				+ "cab approach live on a GPS map!\n\n\n";
		// +"\n\n\n"
		;
		// File sdcard = Environment.getExternalStorageDirectory();
		// File file = new File(sdcard, "CashReceipt.txt");
		File file = getFileWithName("CashReceipt.txt");

		if (file.exists()) {
			file.delete();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
			buf.append(content);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "CashReceipt.txt";

	}

	public void onPickUpClick(View view) {
		_pickUpsuburbListDialog = new IGSuburbListDialog(this, true); // true
																		// given
																		// to
																		// force
																		// show
																		// keyboard
		_pickUpsuburbListDialog.getSuburbListView().setOnItemClickListener(
				new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						_pickUpLabel.setText(_pickUpsuburbListDialog
								.getNewSuburbList().get(position));
						IngogoApp.getSharedApplication().setSelectedSuburbName(
								_pickUpsuburbListDialog.getNewSuburbList().get(
										position));
						_pickUpsuburbListDialog.dismiss();
					}
				});
		invokeSuburbParser();

	}

	public void onPaidAtClick(View view) {
		_paidAtsuburbListDialog = new IGSuburbListDialog(this, true); // true
																		// given
																		// to
																		// force
																		// show
																		// keyboard
		_paidAtsuburbListDialog.getSuburbListView().setOnItemClickListener(
				new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						_paidAtLabel.setText(_paidAtsuburbListDialog
								.getNewSuburbList().get(position));
						_paidAtsuburbListDialog.dismiss();
						updatePrintButtonState();
					}
				});
		_paidAtsuburbListDialog
				.setListAdapterFromSuburbNameArray(_paidAtsuburbDetails
						.getSuburbs());
		_paidAtsuburbListDialog.show();

	}

	private void invokeSuburbParser() {

		if (_progressDialog != null) {
			if (!_progressDialog.isShowing()) {
				_progressDialog = IGUtility.showProgressDialog(this);
			}
		} else {
			_progressDialog = IGUtility.showProgressDialog(this);

		}

		IGSuburbParser parser = new IGSuburbParser();
		try {
			parser.getSerializedSuburbs(IngogoApp.getSharedApplication()
					.getLocalityName().toLowerCase(), this);
		} catch (Exception e) {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility
					.showDialog(
							"",
							"GPS location not established. Unable to select a Pickup LocationEnsure GPS is enabled and if problem persists contact ingogo support.",
							this);
			e.printStackTrace();
		}
	}

	@Override
	public void readSuburbsSuccessfully(
			HashMap<String, ArrayList<IGSuburbModel>> serializedSuburbs) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;
		_pickUpsuburbListDialog.setListAdapter(serializedSuburbs.get(IngogoApp
				.getSharedApplication().getLocalityName().toUpperCase()));
		_pickUpsuburbListDialog.show();

	}

	@Override
	public void failedToReadSuburbs() {
		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;
	}

	@Override
	public void connectedResult(String message) {
		IGCashReceiptActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				IGUtility.dismissProgressDialog(_blutoothProgressDialog);

			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (calledPrintFunction) {
					printFile(getPrintData(), null, null, false);
				}
			}
		}).start();

		IGCashReceiptActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (calledPrintFunction) {
					showVerifyPrintAlert(getString(R.string.receipt_not_printed_alert));
				}

			}
		});

	}

	private void showVerifyPrintAlert(String message) {
		if (_verifyPrintDialog != null) {
			if (!_verifyPrintDialog.isShowing()) {
				createVerifyPrintDialog(message);
				_verifyPrintDialog.show();
			}

		} else {
			createVerifyPrintDialog(message);
			_verifyPrintDialog.show();
		}

	}

	private void createVerifyPrintDialog(String message) {
		_verifyPrintDialog = new AlertDialog.Builder(this)
				.setTitle(null)
				.setMessage(message)
				.setPositiveButton("Try Again",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								printFile(getPrintData(), null, null, false);
								_verifyPrintDialog.dismiss();
								showVerifyPrintAlert(getString(R.string.receipt_not_printed_alert));

							}
						}).setNegativeButton("Done", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						goToJobsActivity();
					}
				}).create();
		_verifyPrintDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);

	}

	private void goToJobsActivity() {
		// If driver is in accept status,then he should be navigated to job
		// details page.If checkDriverStatus flag is set to true,then in jobs
		// activity
		// findCurrentDriverStateApi is called and navigate to jobs activity if
		// the state is accept.
		IGJobsActivity.checkDriverStatus = true;
		Intent intent = new Intent(this, IGJobsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	@Override
	public void retrieveSuburbCompleted(IGGetPaidAtResponseBean suburbDetails) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_paidAtsuburbDetails = suburbDetails;
		if (_paidAtsuburbDetails.getSuburbs() != null
				&& _paidAtsuburbDetails.getSuburbs().size() == 1) {
			_paidAtLabel.setText(_paidAtsuburbDetails.getSuburbs().get(0));
		}
		showKeyboard();

	}

	@Override
	public void retrieveSuburbFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		Log.e("retrievePaidAtFailed", errorMessage);
		IGUtility.showDialogOk("", errorMessage, this);
	}

}
