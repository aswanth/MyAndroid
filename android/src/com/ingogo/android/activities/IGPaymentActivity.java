/*
 * Package Name : com.ingogo.android.app
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : This activity displays the payment details.
 */

package com.ingogo.android.activities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGPaymentConfirmationActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCompleteOfflineJobApi;
import com.ingogo.android.webservices.IGCreditDetailsApi;
import com.ingogo.android.webservices.interfaces.IGCompleteOfflineListener;

public class IGPaymentActivity extends IGBaseActivity implements
		IGCompleteOfflineListener {

	private EditText _fareText;
	private TextView _bookingFee;
	private TextView _bidAmount;
	private TextView _bidCredit;
	private TextView _totalPaymentDue;
	private TextView _surcharge;
	private String _job;
	private Button _processButton;
	private ImageButton _payOfflineButton;
	private ProgressDialog _progressDialog;
	String _bidCreditString;
	String _balanceString;
	String _bidAmountString;
	String _bookingFeeString;
	String _passengerNameString;
	String _passengerMobile;
	String _dropOffAtAddress;
	String _pickUpAddress;
	String _currentFare = IGConstants.zeroBalance;
	String _defaultCreditCardPercentange = IGConstants.zeroBalance;

	private Double _minimumFare = 0.0;
	private Double _maximumFare = 0.0;
	private Double _confirmationValue = 0.0;
	DecimalFormat _decimalFormat;

	private String _bookingType;
	private boolean _disableTakePaymentOption = false;

	// JSONArray _creditArray;
	private boolean _responsePendingInPayment;
	List<JSONObject> _cardDetailsList = new Vector<JSONObject>();
	public static boolean _isNavigateFromMenu;
	private AlertDialog _alertNavigateFromMenu;
	// public static boolean _isPayOfflineVisible;
	HashMap<String, Object> _jobDetails;

	private boolean isResponsePendingInPayment() {
		return _responsePendingInPayment;
	}

	private void setResponsePendingInPayment(boolean responsePendingInPayment) {
		_responsePendingInPayment = responsePendingInPayment;
	}

	/**
	 * @param savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.payment);
		initViews();
		IGUpdatePositionPollingTask.ignoreStaleState = true;
		// To get the job id.
		Bundle extras = getIntent().getExtras();
		_job = extras.getString(IGConstants.kJobId);

		_jobDetails = (HashMap) extras.get(IGConstants.kJobDetails);
		_disableTakePaymentOption = extras.getBoolean(
				"disableTakePaymentOption", false);
		_decimalFormat = new DecimalFormat(IGConstants.zeroBalance);

		JSONObject jObj;
		try {
			jObj = new JSONObject(
					(String) _jobDetails.get(IGConstants.kDetails));
			processCreditDetailsResponse(jObj);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
			callCreditDetailsApi();
		}

	}

	protected void onResume() {
		IGUpdatePositionPollingTask.setIgnoreStaleState(true);
		super.onResume();
		showAlertForJobInProgress();
		// Update the view.
		_processButton.setClickable(true);
		setUpViews();
		totalPaymentDue();

		// When the app enters back ground and a the progress dialogue is shown
		// then a flag is set true in IngogoApp. So if that
		// flag is true then show the progress dialog on onResume.
		if (isResponsePendingInPayment()) {
			setResponsePendingInPayment(false);
			_progressDialog = IGUtility.showProgressDialog(this);
		}
		showKeyboard();

	}

	private void showKeyboard() {
		_fareText.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(_fareText, InputMethodManager.SHOW_FORCED);
			}
		}, 500);

		_fareText.setSelection(_fareText.getText().length());
	}

	private void showAlertForJobInProgress() {
		if (_isNavigateFromMenu) {
			_isNavigateFromMenu = false;
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("");
			adb.setMessage(getString(R.string.jobs_inprogress_alert));
			adb.setCancelable(false);
			adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					showKeyboard();
				}
			});
			_alertNavigateFromMenu = adb.create();
			_alertNavigateFromMenu
					.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			_alertNavigateFromMenu.show();
		}
	}

	@Override
	protected void onPause() {
		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
		IngogoApp.getSharedApplication().setMeterFare(
				_fareText.getText().toString());
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_fareText.getWindowToken(), 0);
		if (_progressDialog != null) {

			// When the app enters back ground and a the progress dialogue is
			// shown then a flag is set true in IngogoApp. So if that
			// flag is true then show the progress dialog on onResume.
			if (_progressDialog.isShowing())
				setResponsePendingInPayment(true);
			_progressDialog.dismiss();
			_progressDialog = null;

		}
		super.onPause();
	}

	/**
	 * All button,textView,editText instances are initiated.
	 */
	private void initViews() {
		_fareText = (EditText) findViewById(R.id.fareEditText);
		_bookingFee = (TextView) findViewById(R.id.bookingFeeTv);
		_bidAmount = (TextView) findViewById(R.id.bidAmountTv);
		_bidCredit = (TextView) findViewById(R.id.bidCreditTv);
		_totalPaymentDue = (TextView) findViewById(R.id.totalFareTv);
		_surcharge = (TextView) findViewById(R.id.surchargeTV);
		_processButton = (Button) findViewById(R.id.received_button);
		_payOfflineButton = (ImageButton) findViewById(R.id.payOfflineBtn);
		_payOfflineButton.setVisibility(View.VISIBLE);
		_bidAmount.setText("");
		_bidCredit.setText("");
		_surcharge.setText("");
		_totalPaymentDue.setText("");
		// _fareText.setPadding(0, 5, 0, 0);

		hideHailBookingOptions();

	}

	private void hideHailBookingOptions() {
		Bundle extras = getIntent().getExtras();

		if (extras.getBoolean("hideHailBookOption", false)) {

			findViewById(R.id.bookingFeeLayout).setVisibility(View.GONE);
			findViewById(R.id.bidAmountLayout).setVisibility(View.GONE);

			RelativeLayout totalLayout = (RelativeLayout) findViewById(R.id.totalFareLayout);
			RelativeLayout surChargeLayout = (RelativeLayout) findViewById(R.id.surchargeLayout);

			totalLayout.setPadding(0, 80, 0, 0);
			surChargeLayout.setPadding(0, 10, 0, 0);

		}

	}

	/**
	 * Click on the process button is received in this method.
	 * 
	 * @param view
	 */
	public void onReceivedButtonClick(View view) {
		_processButton.setClickable(false);
		Double paymentDue = 0.0;
		try {
			paymentDue = Double.parseDouble(_totalPaymentDue.getText()
					.toString());
		} catch (NumberFormatException e) {
			paymentDue = 0.0;
		}
		if (paymentDue > _maximumFare) {
			String _maximumFareString = (String.valueOf(_decimalFormat
					.format(_maximumFare)));
			String message = this
					.getText(R.string.payment_due_max_limit_reached_message_section_1)
					+ _maximumFareString
					+ ". "
					+ this.getText(R.string.payment_due_max_limit_reached_message_section_2);
			showOKAlertWithMessage(message);
			_processButton.setClickable(true);
		} else {
			if (paymentDue > _confirmationValue) {
				showPaymentConfirmationAlertWithMessage(this
						.getString(R.string.payment_confirmation_msg));

			} else {
				// navigates to the Swipe Card page.
				confirmClickAction();

			}

		}

	}

	public void confirmClickAction() {
		Log.d("received button click", "received button click");

		// A hash map with booking fee, bid extra, balance, payment due,
		// meter
		// fare, credit card details and job id is passed to confirmation
		// page.
		HashMap<String, Object> paymentDetails = new HashMap<String, Object>();
		paymentDetails.put(IGConstants.kBookingFee, _bookingFee.getText()
				.toString());
		paymentDetails.put(IGConstants.kBidExtra, _bidAmount.getText()
				.toString());
		paymentDetails.put(IGConstants.kBalance, _bidCredit.getText()
				.toString());
		paymentDetails.put(IGConstants.kPaymentDue, _totalPaymentDue.getText()
				.toString());
		paymentDetails.put(IGConstants.kFareEntered, _fareText.getText()
				.toString());
		paymentDetails.put(IGConstants.kJobId, "" + _job);
		paymentDetails.put(IGConstants.kConfirmationValue, _confirmationValue);
		paymentDetails.put(IGConstants.kMinimumDue, _minimumFare);
		paymentDetails.put(IGConstants.kMaximumDue, _maximumFare);
		paymentDetails.put(IGConstants.kCreditPercentage,
				findCreditPercentageFromCreditArray());
		if (_balanceString == null) {
			_balanceString = IGConstants.zeroBalance;
		}
		paymentDetails.put(IGConstants.kCreditBalance, _balanceString);
		paymentDetails.put(IGApiConstants.kCardDetails,
				_cardDetailsList.toString());

		Log.i("paymentdetails", "" + paymentDetails);

		Intent intent = new Intent(this, IGPaymentConfirmationActivity.class);
		intent.putExtra(IGConstants.kBookingType, _bookingType);
		intent.putExtra(IGConstants.kPaymentDetails, paymentDetails);
		if (_bookingType.equals(IGConstants.kBookingTypeHail)) {
			String passengerId = (String) getIntent().getExtras().get(
					IGConstants.kPassengerID);
			intent.putExtra(IGConstants.kPassengerID, passengerId);
		} else {
			intent.putExtra(IGConstants.kJobDetails, _jobDetails);

		}
		IGPaymentConfirmationActivity.clearAccountList();
		startActivity(intent);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		// Disable the logout, jobs and account info activities on login page.
		if (IngogoApp.getSharedApplication().isLoggedIn()) {
			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.LOGOUT,
					IGConstants.ORDER_NONE, getString(R.string.logout_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.TRIP_HISTORY_SUMMARY,
					IGConstants.ORDER_NONE,
					getString(R.string.trip_history_summary_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PRINT_REFFERAL, IGConstants.ORDER_NONE,
					getString(R.string.print_referal_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.CASH_RECEIPT, IGConstants.ORDER_NONE,
					getString(R.string.cash_receipt_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PAYMENT_OPTION, IGConstants.ORDER_NONE,
					getString(R.string.payment_option_menu_title));

			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.JOBS,
					IGConstants.ORDER_NONE, getString(R.string.jobs_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.HELP,
					IGConstants.ORDER_NONE, getString(R.string.help_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PRACTICE_SWIPE, IGConstants.ORDER_NONE,
					getString(R.string.practice_swipe_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.REPRINT_RECEIPT, IGConstants.ORDER_NONE,
					getString(R.string.reprint_receipt_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.ACCOUNT_SETTINGS, IGConstants.ORDER_NONE,
					getString(R.string.account_settings_title));
		}
		if (_disableTakePaymentOption) {
			menu.findItem(menuEnumerator.PAYMENT_OPTION).setEnabled(false);
		}

		return true;
	}

	private void showOKAlertWithMessage(String message) {
		Dialog dlg = new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(IGConstants.OKMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.show();
	}

	private void showPaymentConfirmationAlertWithMessage(String message) {
		Dialog dlg = new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(IGConstants.ConfirmMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// confirm Action.
								confirmClickAction();
							}
						})
				.setNegativeButton(IGConstants.CancelMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								_processButton.setClickable(true);
							}
						}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.show();
	}

	private void setFareText(String text) {
		// boolean isVisible = _isPayOfflineVisible;
		_fareText.setText(text);
		// if (isVisible) {
		// _payOfflineButton.setVisibility(View.VISIBLE);
		//
		// } else {
		// _payOfflineButton.setVisibility(View.GONE);
		//
		// }
		// _isPayOfflineVisible = isVisible;
		// if (_fareText.getText().toString()
		// .equalsIgnoreCase(IGConstants.zeroBalance)) {
		// _payOfflineButton.setVisibility(View.GONE);
		// _isPayOfflineVisible = false;
		//
		// }
	}

	/**
	 * Set up data in the view.
	 */
	private void setUpViews() {

		if (!IngogoApp.getSharedApplication().getMeterFare()
				.equals(IGConstants.zeroBalance)) {

			setFareText(IngogoApp.getSharedApplication().getMeterFare());

			if (_cardDetailsList.size() > 0 && _cardDetailsList != null) {
				_processButton.setEnabled(true);
			}
		} else {
			setFareText(IGConstants.zeroBalance);

		}

		if ((_bidAmountString == null)) {
			_bidAmount.setText(IGConstants.zeroBalance);

		} else {
			_bidAmount.setText(_bidAmountString);
		}

		if (_bookingFeeString != null) {
			_bookingFee.setText(_bookingFeeString);
		} else {
			_bookingFee.setText(IGConstants.zeroBalance);
		}

		if (_balanceString != null) {
			_bidCredit.setText("(" + _balanceString + ")");
		} else {
			_bidCredit.setText("(" + IGConstants.zeroBalance + ")");
		}

		// Populate the fare text field with the total amount to be
		// paid by the passenger.
		totalPaymentDue();

		String fareString = _fareText.getText().toString();

		if ((fareString != null) && !(fareString.equals("."))
				&& !(fareString.equals(""))) {
			// change the status of the process payment button
			// with respect to the value of the fare entered.
			receivedButtonState();

		} else {
			_processButton.setEnabled(false);
		}

		// Any text change in fare editText is caught by this listener
		_fareText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				// When 1 is entered, it will be shown as '00.01'
				if (!s.toString().equals(_currentFare)) {
					_fareText.removeTextChangedListener(this);

					String cleanString = s.toString().replace(".", "");

					double parsed = Double.parseDouble(cleanString);

					DecimalFormat decimalFormat = new DecimalFormat(
							IGConstants.zeroBalance);
					String formated = (String.valueOf(decimalFormat
							.format(parsed / 100)));

					// The formatted text will contain a ','. For converting
					// this to double for calculation
					// purpose the comma is removed
					_currentFare = formated.replace(",", "");
					if (_currentFare.equals("") || _currentFare.equals("0")) {
						_currentFare = IGConstants.zeroBalance;
					}
					_fareText.setText(_currentFare);
					_fareText.setSelection(_currentFare.length());

					_fareText.addTextChangedListener(this);
					totalPaymentDue();
				}

				// totalPaymentDue();

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				_fareText.setGravity(Gravity.LEFT);
				_fareText.setPadding(0, 10, 5, 0);
				_fareText.setCursorVisible(true);
			}

			@Override
			public void afterTextChanged(Editable s) {
				// skipButtonState();
				Log.i("afterTextChanged--" + s, ""
						+ _fareText.getText().toString());
				String fareString = _fareText.getText().toString();

				if ((fareString != null) && !(fareString.equals("."))
						&& !(fareString.equals(""))) {
					// change the status of the process payment button
					// with respect to the value of the fare entered.
					receivedButtonState();

				} else {
					_processButton.setEnabled(false);
				}

			}
		});

		_fareText
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
									_fareText.getWindowToken(), 0);
							Log.i("faretext", "" + _fareText.getText());

						}

						String fareString = _fareText.getText().toString();

						if ((fareString != null) && !(fareString.equals("."))
								&& !(fareString.equals(""))) {
							// change the status of the process payment button
							// with respect to the value of the fare entered.
							receivedButtonState();

						} else {
							_processButton.setEnabled(false);
						}

						// Returning false allows other listeners to react to
						// the press.
						return false;

					}

				});
		// skipButtonState();
	}

	/**
	 * To enable or disable the process button based on the validation
	 */
	private void receivedButtonState() {

		if (_balanceString == null) {
			_balanceString = IGConstants.zeroBalance;
		}
		if (_fareText.getText().length() > 0) {
			if (Float.parseFloat(_fareText.getText().toString()) > 0
					&& Float.parseFloat(_balanceString) > 0) {
				_processButton.setEnabled(true);
			} else if (Float.parseFloat(_fareText.getText().toString()) > 0
					&& Double
							.parseDouble(_totalPaymentDue.getText().toString()) > _minimumFare) {
				_processButton.setEnabled(true);
			} else {
				_processButton.setEnabled(false);
			}
		} else {
			_processButton.setEnabled(false);

		}

	}

	/**
	 * To calculate the total payment due and set the total amount in the
	 * _totalPaymentDue text view.
	 */
	private void totalPaymentDue() {

		double payableAmount = 0;

		String fareAmount = _fareText.getText().toString();
		String bookingFee = _bookingFee.getText().toString();
		String bidAmount = _bidAmount.getText().toString();

		if (fareAmount.length() != 0) {
			if (fareAmount.contentEquals(".")) {
				fareAmount = "0.";
			}
			payableAmount += Double.parseDouble(fareAmount);

		}

		if (bookingFee.length() != 0) {
			payableAmount += Double.parseDouble(bookingFee);
		}

		if (bidAmount.length() != 0) {
			payableAmount += Double.parseDouble(bidAmount);
		}
		double balance = 0.0;
		try {
			balance = Double.parseDouble(_balanceString);
		} catch (NumberFormatException e) {
			balance = 0.0;
		} catch (NullPointerException e) {
			balance = 0.0;
		}

		if (balance >= payableAmount) {
			_surcharge.setText(IGConstants.zeroBalance);
		} else {
			DecimalFormat decimalFormat = new DecimalFormat(
					IGConstants.zeroBalance);
			String surcharge = (String.valueOf(decimalFormat
					.format(findCreditPercentageFromCreditArray()
							* (payableAmount - balance) / 100)));
			_surcharge.setText(surcharge);
		}

		String surCharge = _surcharge.getText().toString();

		double surchargeAmount = 0;
		if (surCharge.length() != 0) {
			surchargeAmount += Double.parseDouble(surCharge);
		}

		double totalPaymentDue = payableAmount - balance + surchargeAmount;
		if (totalPaymentDue > 0) {
			DecimalFormat decimalFormat = new DecimalFormat(
					IGConstants.zeroBalance);
			String totalFare = (String.valueOf(decimalFormat
					.format(totalPaymentDue)));
			_totalPaymentDue.setText(totalFare);
		} else {
			_totalPaymentDue.setText(IGConstants.zeroBalance);
		}

		Double paymentDue = 0.0;
		try {
			paymentDue = Double.parseDouble(_totalPaymentDue.getText()
					.toString());
		} catch (NumberFormatException e) {
			paymentDue = 0.0;
		}
		// if (paymentDue < _maximumFare) {
		// _payOfflineButton.setVisibility(View.GONE);
		//
		// }

	}

	public void onClickPayOfflineButtonClicked(View view) {
		callCompleteOfflineJobApi();
		// bookings/completeOffline w/s is called and on success we move back to
		// job list page
	}

	private void callCompleteOfflineJobApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			// Disable the button to prevent multiple click.
			_payOfflineButton.setEnabled(false);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCompleteOfflineJobApi _completeOfflineApi = new IGCompleteOfflineJobApi(
					this, this, _job + "");
			_completeOfflineApi.completeOffline();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * Successful response is received by this method, when calling web service
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		Log.e("Payment activity response ", "" + response);
		super.onResponseReceived(response, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInPayment(false);

		// complete offline web service.
		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {

			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			goToJobsActivity();

		} else if (apiID == IGApiConstants.kCreditDetailsWebServiceId) {

			JSONObject responseObject = (JSONObject) response
					.get(IGConstants.kDataKey);
			processCreditDetailsResponse(responseObject);

			showKeyboard();
		}

	}

	/**
	 * Function to get the credit percentage from the credit details array.
	 * Loops through the credit array, selects the credit percentage from the
	 * default card.
	 * 
	 * @param creditArray
	 * @return
	 */
	private double findCreditPercentageFromCreditArray() {
		if (_cardDetailsList != null && _cardDetailsList.size() > 0) {
			try {
				return _cardDetailsList.get(0).getDouble(
						IGApiConstants.kCreditPercentage);
			} catch (JSONException e) {

				e.printStackTrace();
				return 0;
			}

		} else {
			return Double.parseDouble(_defaultCreditCardPercentange);
		}
	}

	private class CardDetailsComperator implements Comparator<JSONObject> {

		@Override
		public int compare(JSONObject firstCardDetails,
				JSONObject secondCardDetails) {
			try {
				return firstCardDetails.getString("cardNickname")
						.compareToIgnoreCase(
								secondCardDetails.getString("cardNickname"));
			} catch (JSONException e) {
				e.printStackTrace();
				return 0;
			}

		}

	}

	private void sortCreditDetails(JSONArray creditArray) {
		_cardDetailsList = new ArrayList<JSONObject>();
		if (creditArray.length() > 1) {
			boolean defaultCardPresent = false;
			try {
				for (int i = 0; i < creditArray.length(); i++) {

					_cardDetailsList.add(creditArray.getJSONObject(i));
					if (_cardDetailsList.get(i).getBoolean("isDefault")) {
						_cardDetailsList.remove(i);
						_cardDetailsList.add(0, creditArray.getJSONObject(i));
						defaultCardPresent = true;
					}
				}

				// If no card is selected as the default card then sort the
				// cards alphabetically and show the first card as default.
				if (!defaultCardPresent) {
					Collections.sort(_cardDetailsList,
							new CardDetailsComperator());
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				_cardDetailsList.add(creditArray.getJSONObject(0));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * To process the response got by calling credit details web service
	 * 
	 * @param resp
	 */
	private void processCreditDetailsResponse(JSONObject resp) {

		if (resp != null) {
			if (resp.has(IGApiConstants.kCardDetails)) {
				try {
					sortCreditDetails(resp
							.getJSONArray(IGApiConstants.kCardDetails));
				} catch (JSONException e) {
					_cardDetailsList = null;
					e.printStackTrace();
				}
			}

			if (resp.has(IGConstants.kBooking)) {
				try {
					JSONObject creditResp = (JSONObject) resp
							.getJSONObject(IGConstants.kBooking);

					Log.e("CREDIT RESP", "" + creditResp);

					try {
						_pickUpAddress = getFullAddressFromObject(creditResp
								.getJSONObject(IGConstants.kpickupFrom));
					} catch (JSONException e) {
						e.printStackTrace();
						_pickUpAddress = null;
					}

					try {
						_dropOffAtAddress = getFullAddressFromObject(creditResp
								.getJSONObject(IGConstants.kDropOffAt));
					} catch (JSONException e) {
						e.printStackTrace();
						_dropOffAtAddress = null;
					}

					if (creditResp.has(IGConstants.kMobileNumber)) {
						_passengerMobile = creditResp
								.getString(IGConstants.kMobileNumber);
					} else {
						_passengerMobile = null;
					}

					if (creditResp.has(IGConstants.kName)) {
						_passengerNameString = creditResp
								.getString(IGConstants.kName);
					} else {
						_passengerNameString = null;
					}

					if (creditResp.has(IGConstants.kBookingType)) {
						_bookingType = creditResp
								.getString(IGConstants.kBookingType);
					} else {
						_bookingType = null;
					}

					DecimalFormat decimalFormat = new DecimalFormat(
							IGConstants.zeroBalance);
					if (creditResp.has(IGConstants.kBookingFee)) {
						Double bookingFee = Double.parseDouble(creditResp
								.getString(IGConstants.kBookingFee));
						_bookingFeeString = (String.valueOf(decimalFormat
								.format(bookingFee)));

					} else {
						_bookingFeeString = null;
					}

					if (resp != null && resp.has(IGConstants.kApplyBid)) {
						boolean applyBidResponse = resp
								.getBoolean(IGConstants.kApplyBid);

						if (applyBidResponse == true) {

							if (creditResp.has(IGConstants.kBidExtra)) {

								Double bidAmount = Double
										.parseDouble(creditResp
												.getString(IGConstants.kBidExtra));

								_bidAmountString = (String
										.valueOf(decimalFormat
												.format(bidAmount)));

							}

							Log.i("bid Amt String", "" + _bidAmountString);
						} else {
							_bidAmountString = null;
						}
					} else {
						_bidAmountString = null;
					}

					if (creditResp.has(IGConstants.kBalance)) {
						Double bidCredit = 0.0;

						if (_cardDetailsList != null
								&& _cardDetailsList.size() > 0
								|| _bookingType
										.equals(IGConstants.kBookingTypeHail)) {
							bidCredit = Double.parseDouble(creditResp
									.getString(IGConstants.kBalance));
						}
						Log.i("available credit", "" + bidCredit);
						_balanceString = (String.valueOf(decimalFormat
								.format(bidCredit)));

					} else {
						_balanceString = null;
					}
					// if (_bookingType.equals(IGConstants.kBookingTypeHail)) {
					// _balanceString = IGConstants.zeroBalance;
					// _bookingFeeString = IGConstants.zeroBalance;
					// _bidAmountString = IGConstants.zeroBalance;
					// }

				} catch (JSONException e1) {
					e1.printStackTrace();
				}

			}

			if (resp.has(IGApiConstants.kminTotalDueValue)) {
				try {
					_minimumFare = resp
							.getDouble(IGApiConstants.kminTotalDueValue);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (resp.has(IGApiConstants.kconfirmationValue)) {
				try {
					_confirmationValue = resp
							.getDouble(IGApiConstants.kconfirmationValue);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (resp.has(IGApiConstants.kmaxTotalDueValue)) {
				try {
					_maximumFare = resp
							.getDouble(IGApiConstants.kmaxTotalDueValue);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			_defaultCreditCardPercentange = String.valueOf(IngogoApp
					.getSharedApplication().getCreditPercentage());

		} else {
			callCreditDetailsApi();
		}
		setUpViews();

	}

	/**
	 * To call credit details web service.
	 */
	private void callCreditDetailsApi() {

		if (IGUtility.isNetworkAvailable(this)) {
			// if (_progressDialog != null && !_progressDialog.isShowing()) {
			_progressDialog = IGUtility.showProgressDialog(this);

			IGCreditDetailsApi creditApi = new IGCreditDetailsApi(this,
					Integer.parseInt(_job));
			creditApi.getCreditDetails();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * Failure response is got in this method
	 */
	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		Log.e("Payment activity errorResponse", "" + errorResponse);

		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInPayment(false);

		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			// Enable the payoffline button which is disabled earlier.
			_payOfflineButton.setEnabled(true);
		} else if (apiID == IGApiConstants.kCreditDetailsWebServiceId) {
			calculatordataFetchFailed = true;
			handleDriverStaleState();
			return;
		}

		super.onFailedToGetResponse(errorResponse, apiID);
	}

	/**
	 * Format the address with required data got from the web service.
	 * 
	 * @param addrObj
	 * @return formatted address
	 */
	private String getFullAddressFromObject(JSONObject addrObj) {
		String addr = "";

		try {

			String buildingName = addrObj.getString(IGConstants.kBuildingName);
			if (buildingName.equalsIgnoreCase("null")) {
				buildingName = "";
			} else {
				addr += "" + buildingName + ", ";
			}

			String unitNumber = addrObj.getString(IGConstants.kUnitNumber);
			if (unitNumber.equalsIgnoreCase("null")) {
				unitNumber = "";
			} else {
				if (buildingName.equals("")) {
					addr += "" + unitNumber;

				} else {
					addr += "" + unitNumber;
				}
			}

			String streetNumber = addrObj.getString(IGConstants.kStreetNumber);
			if (streetNumber.equalsIgnoreCase("null")) {
				streetNumber = "";
			} else {
				if (buildingName.equals("") && unitNumber.equals("")) {
					addr += "" + streetNumber;
				} else if ((!buildingName.equals("")) && unitNumber.equals("")) {
					addr += "" + streetNumber;
				} else {
					addr += "/" + streetNumber;
				}
			}

			String addressLine1 = addrObj.getString(IGConstants.kAddressLine1);
			if (addressLine1.equalsIgnoreCase("null")) {
				addressLine1 = "";
			} else if (buildingName.equals("") && unitNumber.equals("")
					&& streetNumber.equals("")) {
				addr += "" + addressLine1;
			} else {
				addr += " " + addressLine1;
			}

			String addressLine2 = addrObj.getString(IGConstants.kAddressLine2);
			if (addressLine2.equalsIgnoreCase("null")) {
				addressLine2 = "";
			} else {
				if (addressLine1.equals("")) {
					addr += " " + addressLine2;
				} else if (buildingName.equals("") && unitNumber.equals("")
						&& streetNumber.equals("") && addressLine1.equals("")) {
					addr += "" + addressLine2;
				} else {
					addr += ", " + addressLine2;
				}
			}

			String addressLine3 = addrObj.getString(IGConstants.kAddressLine3);
			if (addressLine3.equalsIgnoreCase("null")) {
				addressLine3 = "";
			}
			if (addressLine2.equals("")) {
				addr += " " + addressLine3;
			} else if (buildingName.equals("") && unitNumber.equals("")
					&& streetNumber.equals("") && addressLine1.equals("")
					&& addressLine2.equals("")) {
				addr += "" + addressLine3;
			} else {
				addr += ", " + addressLine3;
			}

			String suburb = addrObj.getString(IGConstants.kSuburb);
			if (suburb.equalsIgnoreCase("null")) {
				suburb = "";
			} else {
				if (addressLine1.equals("") && addressLine2.equals("")
						&& addressLine3.equalsIgnoreCase("")
						&& unitNumber.equals("") && streetNumber.equals("")) {
					addr += "" + suburb;
				} else if (addressLine1.equals("") || addressLine2.equals("")
						|| addressLine3.equals("") || unitNumber.equals("")
						|| streetNumber.equals("")) {
					addr += ", " + suburb;
				} else {
					addr += ", " + suburb;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return addr;
	}

	/**
	 * To navigate to jobs activity by clearing all the activities between
	 * payment activity and jobs activity.
	 */
	private void goToJobsActivity() {
		Intent intent = new Intent(IGPaymentActivity.this, IGJobsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		IGJobsActivity.checkDriverStatus = true;
		startActivity(intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (_bookingType.equals(IGConstants.kBookingTypeHail)) {
			return;
		}
	}

	@Override
	public void completeOfflineSuccess(String sucessString, String totalPaid) {
		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInPayment(false);
		_payOfflineButton.setEnabled(true);
		IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
		goToJobsActivity();

	}

	@Override
	public void completeOfflineFailed(String errorMessage,
			boolean isHandleDriverStaleState) {
		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInPayment(false);
		_payOfflineButton.setEnabled(true);
		if (isHandleDriverStaleState) {
			super.onNullResponseRecieved();
			return;
		}
		IGUtility.showDialog("", errorMessage, this);

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInPayment(false);
		_payOfflineButton.setEnabled(true);
		super.onNullResponseRecieved();

	}

}