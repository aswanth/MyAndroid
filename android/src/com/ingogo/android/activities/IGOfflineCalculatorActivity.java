/**
 * 
 */
package com.ingogo.android.activities;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCompleteOfflineJobApi;
import com.ingogo.android.webservices.interfaces.IGCompleteOfflineListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

/**
 * @author dipu
 * 
 */
public class IGOfflineCalculatorActivity extends IGBaseActivity implements IGCompleteOfflineListener, IGExceptionApiListener {

	private ProgressDialog _progressDialog;
	private String _baseFeeString, _serviceFeeString;
	private TextView _baseFeeTV, _serviceFeeTV, _totalFareTV, _tripChargeMsgTv;
	private EditText _fareEntered;
	private boolean _applyBid, _hasCardDetails;
	String _jobId;
	String _currentFare = IGConstants.zeroBalance;
	DecimalFormat _decimalFormat;
	private Button _skipButton; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offline_payment);
		initViews();
		_decimalFormat = new DecimalFormat(IGConstants.zeroBalance);
		// To get the job id.
		Intent intent = getIntent();
		_jobId = intent.getExtras().getString(IGConstants.kJobId);
		Log.i("job id", "" + _jobId);

		HashMap<String, Object> jobDetails;
		jobDetails = (HashMap) intent.getExtras().get(IGConstants.kJobDetails);

		processBookingData(jobDetails);
		setUpViews();
	}

	@Override
	protected void onResume() {
		IGUpdatePositionPollingTask.setIgnoreStaleState(true);
		super.onResume();
		showKeyboard();
	}
	private void showKeyboard() {
		_fareEntered.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(_fareEntered, InputMethodManager.SHOW_FORCED);
			}
		}, 500);
		
		_fareEntered.setSelection(_fareEntered.getText().length());
	}
	void processBookingData(HashMap<String, Object> jobDetails) {
		JSONObject bookingDetails = null;

		try {
			bookingDetails = new JSONObject(
					(String) jobDetails.get(IGConstants.kDetails));

			if (bookingDetails != null
					&& bookingDetails.has(IGConstants.kBooking)) {

				// Apply bid is set true when the value of applyBid
				// in the response is true
				if (bookingDetails.has(IGConstants.kApplyBid)
						&& bookingDetails.getBoolean(IGConstants.kApplyBid)) {
					_applyBid = true;
				}

				if (bookingDetails.has(IGApiConstants.kCardDetails)
						&& bookingDetails.getJSONArray(
								IGApiConstants.kCardDetails).length() > 0) {
					_hasCardDetails = true;
				}

				JSONObject creditResp = (JSONObject) bookingDetails
						.getJSONObject(IGConstants.kBooking);
				if (creditResp != null) {
					if (creditResp.has(IGConstants.kBookingFee)) {
						Double bookingFee = Double.parseDouble(creditResp
								.getString(IGConstants.kBookingFee));
						_baseFeeString = (String.valueOf(_decimalFormat
								.format(bookingFee)));

					} else {
						_baseFeeString = IGConstants.zeroBalance;
					}

					if (creditResp.has(IGConstants.kBidExtra)) {
						Double bidCredit = Double.parseDouble(creditResp
								.getString(IGConstants.kBidExtra));
						Log.i("available credit", "" + bidCredit);

						// _applyBid is set to true when the bidCredit is
						// greater than zero
						// and the value of applyBid is true. We need to check
						// both these
						// params to ensure the applyBid feature as the
						// webservice is returning
						// wrong value for applyBid.
						if (bidCredit > 0 && _applyBid) {
							_serviceFeeString = (String.valueOf(_decimalFormat
									.format(bidCredit)));

						} else {
							_applyBid = false;
							_serviceFeeString = IGConstants.zeroBalance;
						}

					} else {
						_serviceFeeString = null;
					}

				} else {
					_baseFeeString = IGConstants.zeroBalance;
					_serviceFeeString = IGConstants.zeroBalance;
				}
			}
		} catch (JSONException e) {
			_baseFeeString = IGConstants.zeroBalance;
			_serviceFeeString = IGConstants.zeroBalance;
			e.printStackTrace();
		} catch (NullPointerException e) {
			_baseFeeString = IGConstants.zeroBalance;
			_serviceFeeString = IGConstants.zeroBalance;
		}
	}

	void initViews() {
		_baseFeeTV = (TextView) findViewById(R.id.bookingFeeTv);
		_serviceFeeTV = (TextView) findViewById(R.id.bidAmountTv);
		_totalFareTV = (TextView) findViewById(R.id.totalFareTv);
		_tripChargeMsgTv = (TextView) findViewById(R.id.tripChargeMsgTv);
		_fareEntered = (EditText) findViewById(R.id.fareEditText);
		_fareEntered.setPadding(0, 0, 5, 0);
		_fareEntered.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				// When 1 is entered, it will be shown as '00.01'
				if (!s.toString().equals(_currentFare)) {
					_fareEntered.removeTextChangedListener(this);

					String cleanString = s.toString().replace(".", "");

					double parsed = Double.parseDouble(cleanString);

					String formated = (String.valueOf(_decimalFormat
							.format(parsed / 100)));

					// The formatted text will contain a ','. For converting
					// this to double for calculation
					// purpose the comma is removed
					_currentFare = formated.replace(",", "");
					if (_currentFare.equals("") || _currentFare.equals("0")) {
						_currentFare = IGConstants.zeroBalance;
					}
					_fareEntered.setText(_currentFare);
					_fareEntered.setSelection(_currentFare.length());

					_fareEntered.addTextChangedListener(this);
				}
				updateTripCharge();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				_fareEntered.setGravity(Gravity.LEFT);
				_fareEntered.setPadding(0, 10, 5, 0);
				_fareEntered.setCursorVisible(true);
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		_fareEntered
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {

					/*
					 * Respond to soft keyboard events, look for the DONE press
					 * on the password field.
					 */

					@Override
					public boolean onEditorAction(TextView view, int keyCode,
							KeyEvent event) {

						if ((keyCode == EditorInfo.IME_ACTION_SEARCH
								|| keyCode == EditorInfo.IME_ACTION_DONE || event
								.getAction() == KeyEvent.ACTION_DOWN
								&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							// Done pressed. Hide the soft keyboard
							Log.i("STATUS", "Keyboard down");
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									_fareEntered.getWindowToken(), 0);

						}
						// Returning false allows other listeners to react to
						// the press.
						return false;

					}

				});
		_hasCardDetails = false;
		_applyBid = false;
		_skipButton = (Button)findViewById(R.id.skip_button);

	}

	void updateTripCharge() {
		Double fareEntered = 0.0;
		try {
			fareEntered = Double.parseDouble(_fareEntered.getText().toString());
		} catch (NumberFormatException e) {
			fareEntered = 0.0;
		}
		Double baseFee = Double.parseDouble(_baseFeeString);
		Double serviceFee = Double.parseDouble(_serviceFeeString);

		_totalFareTV.setText(""
				+ _decimalFormat.format(fareEntered + baseFee + serviceFee));
	}

	void setUpViews() {
		if (!IngogoApp.getSharedApplication().getMeterFare()
				.equals(IGConstants.zeroBalance)) {
			_fareEntered.setText(IngogoApp.getSharedApplication()
					.getMeterFare());
		} else {
			_fareEntered.setText(IGConstants.zeroBalance);
		}

		_baseFeeTV.setText(_baseFeeString);

		_serviceFeeTV.setText(_serviceFeeString);

		/*
		 * if (!_applyBid && !_hasCardDetails) {
		 * _tripChargeMsgTv.setText(R.string.trip_charge_condition_check_msg); }
		 * else { _tripChargeMsgTv.setText(R.string.trip_charge_msg); }
		 */
		
		_tripChargeMsgTv.setText(R.string.trip_charge_condition_check_msg);
		updateTripCharge();
	}

	/**
	 * Click on the process off line button is received in this method. If
	 * network is available then the complete offline webservice is activated.
	 * 
	 * @param view
	 */
	public void onSkipButtonClick(View view) {
		Log.d("CompleteOffline button tapped", "CompleteOffline button tapped");
		if (IGUtility.isNetworkAvailable(this)) {
			// Disable the button to prevent multiple click.
			_skipButton.setEnabled(false);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCompleteOfflineJobApi _completeOfflineApi = new IGCompleteOfflineJobApi(
					this,this, "" + _jobId);
			_completeOfflineApi.completeOffline();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		super.onResponseReceived(response, apiID);

		IGUtility.removeDefaults(IGConstants.kJobInProgress, this);

		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			// When the pay offline is success navigate to the Job list screen.
			Intent intent = new Intent(IGOfflineCalculatorActivity.this,
					IGJobsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		// TODO Auto-generated method stub
		super.onFailedToGetResponse(errorResponse, apiID);
	}

	@Override
	protected void onPause() {
		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
		IngogoApp.getSharedApplication().setMeterFare(
				_fareEntered.getText().toString());
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(
				_fareEntered.getWindowToken(), 0);
		if (_progressDialog != null) {

			if (_progressDialog.isShowing())
				_progressDialog.dismiss();
			_progressDialog = null;

		}
		super.onPause();
	}

	@Override
	public void completeOfflineSuccess(String sucessString, String totalPaid) {
		_skipButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
		Intent intent = new Intent(IGOfflineCalculatorActivity.this,
				IGJobsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
		
	}

	@Override
	public void completeOfflineFailed(String errorMessage, boolean isHandleDriverStaleState ) {
		_skipButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
		if(isHandleDriverStaleState) {
			super.onNullResponseRecieved();
			return;
		}
		IGUtility.showDialog("", errorMessage, this);
		
	}
	
	@Override
	public void onNullResponseRecieved() {
		_skipButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNullResponseRecieved();
	}

}
