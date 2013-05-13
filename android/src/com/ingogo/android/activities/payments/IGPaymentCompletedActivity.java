package com.ingogo.android.activities.payments;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.model.IGReceiptInformationModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGBluetoothDeviceListDialog;
import com.ingogo.android.utilities.IGBluetoothHelper;
import com.ingogo.android.utilities.IGBluetoothReceiveListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGSendReceiptApi;
import com.ingogo.android.webservices.interfaces.IGBluetoothDeviceSelectListener;
import com.ingogo.android.webservices.interfaces.IGPrintReceiptApiListener;
import com.ingogo.android.webservices.interfaces.IGSendReceiptApiListener;
import com.qburst.utilities.emailvalidator.EmailValidator;

public class IGPaymentCompletedActivity extends IGBluetoothBasePaymentActivity
		implements IGSendReceiptApiListener, IGPrintReceiptApiListener,
		IGBluetoothReceiveListener, IGBluetoothDeviceSelectListener {

	private TextView _totalPaidTv;
	private EditText _mobileNumberET, _emailAddressET;
	private Button _skipBtn, _sendReceiptBtn, _printReceiptBtn;
	private ProgressDialog _progressDialog;

	private String _mobileNumber, _emailAddress, _totalPaymentDue;
	private String _bookingId;

	private static String allowableStringMobile = "+";
	private static final int DISABLED_ALPHA = 110;
	private static final int ENABLED_ALPHA = 255;

	private boolean _mobileNumberFormat = false;
	private boolean _emailAddressFormat = false;
	private IGReceiptInformationModel _receiptInfo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_completed);

		getBundleExtras();
		initViews();
		setUpViews();
		IngogoApp.setSwipeCompletedScreenLoadedTime(getTimeString());

		HashMap<String, String> timeTracker = new HashMap<String, String>();
		timeTracker.put("swipeButtonTappedTime",
				IngogoApp.getSwipeButtonTappedTime());
		timeTracker.put("swipeScreenCreatedTime",
				IngogoApp.getSwipeScreenCreatedTime());
		timeTracker.put("swipeInitialisationStartedTime",
				IngogoApp.getSwipeInitialisationStartedTime());
		timeTracker.put("swipeInitialisationCompleteTime",
				IngogoApp.getSwipeInitialisationCompleteTime());
		timeTracker.put("swipeRecordedime",
				IngogoApp.getSwipeRecordedTime());
		timeTracker.put("swipeCompleteScreenLoadedTime",
				IngogoApp.getSwipeCompletedScreenLoadedTime());

		QLog.d("SWIPE PAY TIME TRACKER", timeTracker.toString());
		IngogoApp.clearTimeTrackerHistory();

	}
	
	String getTimeString() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date(Calendar.getInstance().getTimeInMillis());
		String dateString = fmt.format(date);
		return dateString;
	}

	@Override
	protected void onResume() {
		super.onResume();
		IGUpdatePositionPollingTask.setIgnoreStaleState(true);
		if (_printReceiptBtn != null)
			_printReceiptBtn.setEnabled(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
	}

	private void getBundleExtras() {
		Intent intent = getIntent();
		_bookingId = (String) intent.getExtras().get(IGConstants.kBookingId);
		_totalPaymentDue = (String) intent.getExtras().get(
				IGConstants.kTripCharge);

		if (intent.hasExtra(IGConstants.kReceiptInformationKey)) {
			_receiptInfo = (IGReceiptInformationModel) intent
					.getSerializableExtra(IGConstants.kReceiptInformationKey);
		}
	}

	private void initViews() {
		_totalPaidTv = (TextView) findViewById(R.id.totalPaidTV);
		_mobileNumberET = (EditText) findViewById(R.id.mobileNumET);
		_emailAddressET = (EditText) findViewById(R.id.emailET);
		_skipBtn = (Button) findViewById(R.id.skipBtn);
		_sendReceiptBtn = (Button) findViewById(R.id.sendReceiptBtn);
		_printReceiptBtn = (Button) findViewById(R.id.bPrintReceipt);

		_blutoothAdapter = BluetoothAdapter.getDefaultAdapter();
		_blutoothHelper = new IGBluetoothHelper(this, this);
		_dialog = new IGBluetoothDeviceListDialog(this, this);

	}

	private void setUpViews() {
		_totalPaidTv.setText(_totalPaymentDue);
		_skipBtn.setEnabled(true);
		_skipBtn.getBackground().setAlpha(ENABLED_ALPHA);
		setSendReceiptButtonState(false);
		mobileNumberFieldTextListner();
		emailAddressFieldTextListner();
	}

	private void emailAddressFieldTextListner() {
		_emailAddressET.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				_mobileNumberFormat = false;
				String emailString = _emailAddressET.getText().toString()
						.trim();
				if (!emailString.equals("")) {
					_mobileNumberET.setEnabled(false);
					setSendReceiptButtonState(true);
				} else {
					_mobileNumberET.setEnabled(true);
					setSendReceiptButtonState(false);
				}
				if (EmailValidator
						.isValid(_emailAddressET.getText().toString())) {
					_emailAddressFormat = true;
				} else {
					_emailAddressFormat = false;
				}
			}
		});
	}

	private void mobileNumberFieldTextListner() {
		_mobileNumberET.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String currentString = s.toString();
				try {
					if (currentString.substring(0, 1).equalsIgnoreCase("+")
							|| Character.isDigit(s.charAt(0))) {
						if (currentString.matches("^[0-9]*$")) {

							Pattern p = Pattern.compile("^\\+(?:[0-9] ?){12}$");
							Matcher m = p.matcher(currentString);
							boolean numberFound = m.matches();

							if (numberFound) {
							} else {

							}

						} else {
							InputFilter[] mobileFilter = new InputFilter[2];
							mobileFilter[0] = filterMobileNoSpecial;
							mobileFilter[1] = new InputFilter.LengthFilter(12);
							_mobileNumberET.setFilters(mobileFilter);
						}
						InputFilter[] mobileFilter = new InputFilter[2];
						mobileFilter[0] = filterMobileNoSpecial;
						mobileFilter[1] = new InputFilter.LengthFilter(12);
						_mobileNumberET.setFilters(mobileFilter);
					}
				} catch (StringIndexOutOfBoundsException e) {
					setMobileTextFilter();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				setMobileTextFilter();
			}

			@Override
			public void afterTextChanged(Editable s) {
				_emailAddressFormat = false;
				String mobileString = _mobileNumberET.getText().toString()
						.trim();
				if (!mobileString.equals("")) {
					_emailAddressET.setEnabled(false);
					setSendReceiptButtonState(true);
				} else {
					_emailAddressET.setEnabled(true);
					setSendReceiptButtonState(false);
				}
				checkMobileNumberFormat();
			}
		});
	}

	private void checkMobileNumberFormat() {
		String _mobileNoString = _mobileNumberET.getText().toString().trim();

		if (_mobileNoString.equals("") || _mobileNoString.length() < 10) {
			_mobileNumberFormat = false;
		} else if (_mobileNoString.contains("+")
				&& (_mobileNoString.length() != 12)) {
			_mobileNumberFormat = false;
		} else if (!_mobileNoString.contains("+")
				&& _mobileNoString.length() > 10) {
			_mobileNumberFormat = false;
		} else {
			if (_mobileNoString.length() == 10
					|| _mobileNoString.length() == 12) {
				Pattern p = null;
				if (_mobileNoString.length() == 10) {
					p = Pattern.compile("[0-9]*");
				} else {
					p = Pattern.compile("[+][0-9]*");
				}
				Matcher m = p.matcher(_mobileNoString);
				if (m.matches() == false) {
					_mobileNumberFormat = false;
				} else {
					_mobileNumberFormat = true;

				}
			}
		}
	}

	private void setMobileTextFilter() {
		InputFilter[] mobileFilter = new InputFilter[2];
		mobileFilter[0] = filterMobile;
		mobileFilter[1] = new InputFilter.LengthFilter(12);
		_mobileNumberET.setFilters(mobileFilter);
	}

	/**
	 * Filter to allow only mobile number without special character
	 */
	private static InputFilter filterMobileNoSpecial = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			for (int i = start; i < end;) {

				if (source.toString().length() <= 1
						|| source.toString().matches("^[a-zA-Z0-9]*$")) {
					if (Character.isDigit(source.charAt(i))) {
						return source;
					} else {
						if (dstart == 0
								&& new Character(source.charAt(i))
										.toString()
										.equalsIgnoreCase(allowableStringMobile)
								&& !dest.toString().contains(
										allowableStringMobile)) {
							return source;
						}
						return "";
					}
				} else {
					return "";
				}
			}
			return null;
		}
	};

	/**
	 * Filter to allow only mobile number with plus sign
	 */
	private InputFilter filterMobile = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {

			for (int i = start; i < end;) {

				if (source.toString().length() <= 1) {
					if (Character.isDigit(source.charAt(i))
							|| allowableStringMobile.contains(source)) {
						return source;
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
			return null;
		}
	};

	public void onSkipButtonClick(View view) {
		_skipBtn.setEnabled(false);
		_skipBtn.getBackground().setAlpha(DISABLED_ALPHA);
		goToJobsActivity();
	}

	/**
	 * To navigate to jobs activity by clearing all the activities between
	 * payment activity and jobs activity.
	 */
	private void goToJobsActivity() {

		// if (_dialog != null && _dialog.isShowing()) {
		// _dialog.cancel();
		// }
		Intent intent = new Intent(IGPaymentCompletedActivity.this,
				IGJobsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		IGJobsActivity.checkDriverStatus = true;
		startActivity(intent);
		finish();
		
	}

	public void onSendReceiptButtonClick(View view) {
		_mobileNumber = _mobileNumberET.getText().toString();
		_emailAddress = _emailAddressET.getText().toString();

		if (_mobileNumberFormat || _emailAddressFormat) {
			callSendReceiptApi();
		} else if (!_mobileNumberFormat && !_mobileNumber.equals("")) {
			IGUtility.showDialogOk("",
					getResources().getString(R.string.invalid_mobile_format),
					this);
		} else if (!_emailAddressFormat && !_emailAddress.equals("")) {
			IGUtility.showDialogOk("",
					getResources().getString(R.string.invalid_email_format),
					this);
		}
	}

	public void onPrintReceiptButtonClick(View view) {

		if (isPrinterConfigured()) {
			// callPrintReceiptApi(_bookingId);
			if (_receiptInfo != null) {
				_printReceiptBtn.setEnabled(false);
				writeReceipt(_receiptInfo);
				turnOnBlutooth(true);
			}
		} else {
			IGUtility.showDialogOk("",
					getResources().getString(R.string.printer_not_paired),
					IGPaymentCompletedActivity.this);
		}
	}

	private void callSendReceiptApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			// Disable the button to prevent multiple click.
			setSendReceiptButtonState(false);
			_progressDialog = IGUtility.showProgressDialog(this);

			if (_mobileNumber != null && _emailAddress.equals("")) {
				IGSendReceiptApi _sendReceiptApi = new IGSendReceiptApi(this,
						this);
				_sendReceiptApi.sendReceiptMobileNumber(_bookingId,
						_mobileNumber);
			} else if (_emailAddress != null && _mobileNumber.equals("")) {
				IGSendReceiptApi _sendReceiptApi = new IGSendReceiptApi(this,
						this);
				_sendReceiptApi.sendReceiptEmailAddress(_bookingId,
						_emailAddress);
			}

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	@Override
	public void sendReceiptCompleted(String statusResponse) {
		Log.d("SendReceiptCompleted", statusResponse);
		String completedResponse = null;
		if (_mobileNumber != null && _emailAddress.equals("")) {
			completedResponse = getResources().getString(
					R.string.send_receipt_moblie);
		} else if (_emailAddress != null && _mobileNumber.equals("")) {
			completedResponse = getResources().getString(
					R.string.send_receipt_email);
		}

		Dialog dlg = new AlertDialog.Builder(IGPaymentCompletedActivity.this)
				.setTitle("").setMessage(completedResponse)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent intent = new Intent(
								IGPaymentCompletedActivity.this,
								IGJobsActivity.class);
						IGJobsActivity.checkDriverStatus = true;
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
				}).create();
		dlg.setCancelable(false);
		dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true;
				}
				return false;
			}
		});
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.show();

		IGUtility.dismissProgressDialog(_progressDialog);
		setSendReceiptButtonState(true);
	}

	@Override
	public void sendReceiptFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		Log.d("SendReceiptFailed", errorMessage);
		IGUtility.showDialogOk("", errorMessage, this);
		setSendReceiptButtonState(true);
	}

	private void setSendReceiptButtonState(boolean state) {
		if (state) {
			_sendReceiptBtn.setEnabled(true);
			_sendReceiptBtn.getBackground().setAlpha(ENABLED_ALPHA);
		} else {
			_sendReceiptBtn.setEnabled(false);
			_sendReceiptBtn.getBackground().setAlpha(DISABLED_ALPHA);
		}
	}

	@Override
	public void onBackPressed() {
		return;
	}

	@Override
	protected void finishPrinting() {
		_printReceiptBtn.setEnabled(true);
	}
}
