package com.ingogo.android.activities.payments;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.activities.IGPaymentActivity;
import com.ingogo.android.activities.IGPaymentBaseActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGPassengerInformationModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCreateBookingForPaymentApi;
import com.ingogo.android.webservices.IGFindAccountApi;
import com.ingogo.android.webservices.beans.response.IGCreateBookingForPaymentResponseBean;
import com.ingogo.android.webservices.interfaces.IGCreateBookingForPaymentApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGFindAccountApiListener;

public class IGFindPassengerActivity extends IGPaymentBaseActivity implements
		IGFindAccountApiListener, IGExceptionApiListener,
		IGCreateBookingForPaymentApiListener {

	private EditText _mobileNumberText;
	private ImageButton _findAccountButton, _confirmButton;
	private RelativeLayout _passengerInfoLayout;
	private ProgressDialog _progressDialog;
	static private String _mobileNumberString;
	static private IGPassengerInformationModel _passengerInfo;
	private TextView _nameTextView;
	private static final int DISABLED_ALPHA = 110;
	private static final int ENABLED_ALPHA = 255;
	private Runnable keyBoardRunnable = new Runnable() {
		
		@Override
		public void run() {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(_mobileNumberText,
					InputMethodManager.SHOW_FORCED);
			
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_passenger);
		initViews();
		setUpViews();
		IngogoApp.getSharedApplication().setMeterFare("00.00");

	}

	@Override
	protected void onResume() {
		super.onResume();
		IGUpdatePositionPollingTask.setIgnoreStaleState(true);
		Log.e("Mobile Number", "resume" + _mobileNumberString);
		_confirmButton.setClickable(true);
		refreshButttonStates();
		showKeyboard();
	}

	private void showKeyboard() {
		_mobileNumberText.postDelayed(keyBoardRunnable, 500);
		_mobileNumberText.setSelection(_mobileNumberText.getText().length());

	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
		// _mobileNumberString = _mobileNumberText.getText().toString();
		if(_mobileNumberText != null && keyBoardRunnable != null){
			_mobileNumberText.removeCallbacks(keyBoardRunnable);
		}
	}

	/**
	 * Initializing the views.
	 */
	private void initViews() {

		_findAccountButton = (ImageButton) findViewById(R.id.find_account_btn);
		_confirmButton = (ImageButton) findViewById(R.id.confirmBtn);
		_mobileNumberText = (EditText) findViewById(R.id.enter_no_EditText);
		_findAccountButton.setEnabled(false);
		_findAccountButton.setAlpha(DISABLED_ALPHA);
		_nameTextView = (TextView) findViewById(R.id.nameTV);
		_passengerInfoLayout = (RelativeLayout) findViewById(R.id.passenger_info);

		InputFilter[] mobileFilter = new InputFilter[2];
		mobileFilter[0] = IGUtility.filterMobile;
		mobileFilter[1] = new InputFilter.LengthFilter(12);
		_mobileNumberText.setFilters(mobileFilter);
	}

	/**
	 * Setting up of views
	 */
	private void setUpViews() {

		setButtonState();
		// Any text change in mobile number textField is listened.
		_mobileNumberText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				setButtonState();

				String currentString = s.toString();
				try {
					if (currentString.substring(0, 1).equalsIgnoreCase("+")
							|| Character.isDigit(s.charAt(0))) {
						InputFilter[] mobileFilter = new InputFilter[2];
						mobileFilter[0] = IGUtility.filterMobileNoSpecial;
						mobileFilter[1] = new InputFilter.LengthFilter(12);
						_mobileNumberText.setFilters(mobileFilter);
					}
				} catch (StringIndexOutOfBoundsException e) {
					InputFilter[] mobileFilter = new InputFilter[2];
					mobileFilter[0] = IGUtility.filterMobile;
					mobileFilter[1] = new InputFilter.LengthFilter(12);
					_mobileNumberText.setFilters(mobileFilter);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				setButtonState();
			}
		});

		if (_mobileNumberString != null && _passengerInfo != null) {
			modifyPassengerInfo();
		}
		if (_mobileNumberString != null) {
			_mobileNumberText.setText(_mobileNumberString);
		}
	}

	private void setButtonState() {
		String _mobileNoString = _mobileNumberText.getText().toString().trim();

		if (_mobileNoString.equals("") || _mobileNoString.length() < 10) {

			_findAccountButton.setEnabled(false);
			_findAccountButton.setAlpha(DISABLED_ALPHA);
		} else if (_mobileNoString.contains("+")
				&& (_mobileNoString.length() != 12)) {

			_findAccountButton.setEnabled(false);
			_findAccountButton.setAlpha(DISABLED_ALPHA);

		} else if (!_mobileNoString.contains("+")
				&& _mobileNoString.length() > 10) {

			_findAccountButton.setEnabled(false);
			_findAccountButton.setAlpha(DISABLED_ALPHA);

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
					_findAccountButton.setEnabled(false);
					_findAccountButton.setAlpha(DISABLED_ALPHA);

				} else {
					_findAccountButton.setEnabled(true);
					_findAccountButton.setAlpha(ENABLED_ALPHA);

				}
			}
		}
	}

	private void refreshButttonStates() {
		_findAccountButton.setAlpha(ENABLED_ALPHA);
		_findAccountButton.setAlpha(DISABLED_ALPHA);
		setButtonState();
	}

	/**
	 * To call Find Account web service.
	 * **/
	private void findAccount() {

		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGFindAccountApi igFindAccountApi = new IGFindAccountApi(
					IGFindPassengerActivity.this, IGFindPassengerActivity.this);
			igFindAccountApi.getFindAccountStatus(_mobileNumberText.getText()
					.toString());

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * When user tap on the Find Account button, the text field is checked for
	 * validity and upon successful validation Find Account web service is
	 * initiated.
	 * 
	 * @param view
	 */
	public void findAccountButtonClick(View view) {
		_findAccountButton.setEnabled(false);
		Log.i("Login", "Find Account button tapped");
		// Done pressed. Hide the soft keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_mobileNumberText.getWindowToken(), 0);
		this.findAccount();

	}

	@Override
	public void findAccountCompleted(IGPassengerInformationModel passengerInfo) {
		// TODO Auto-generated method stub
		_passengerInfo = passengerInfo;
		_mobileNumberString = _mobileNumberText.getText().toString();
		modifyPassengerInfo();
		IGUtility.dismissProgressDialog(_progressDialog);
		_findAccountButton.setEnabled(true);
	}

	private void modifyPassengerInfo() {

		// TODO Auto-generated method stub
		_passengerInfoLayout.setVisibility(View.VISIBLE);

		// _mobileNumberString.
		String passengerInfo = _passengerInfo.getInitial() + " "
				+ _passengerInfo.getSurname() + ", "
				+ formatString(_mobileNumberString);
		_nameTextView.setText(passengerInfo);
	}

	private String formatString(String string) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (char c : string.toCharArray()) {
			i++;
			sb.append(c);
			if (i % 3 == 1 && i > 1) {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	@Override
	public void findAccountFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		_mobileNumberText.setText("");
		_findAccountButton.setEnabled(false);
		IGUtility.showDialogOk("", errorMessage, IGFindPassengerActivity.this);
	}

	/*
	 * Action passenger confirm button call createBookingForTakingPayment api
	 */
	public void onConfirmButtonClick(View v) {
		if (IGUtility.isNetworkAvailable(this)) {
			_confirmButton.setClickable(false);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCreateBookingForPaymentApi api = new IGCreateBookingForPaymentApi(
					this, this);
			api.createBookingForPassenger(_passengerInfo.getPassengerId());

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/*
	 * Action passenger pay offline button button Go to job list page
	 */
	public void onClickPayOfflineButtonClicked(View v) {
		goToJobsActivity();
	}

	/**
	 * To navigate to jobs activity by clearing all the activities between
	 * SwipeCardActivity activity and jobs activity.
	 */
	private void goToJobsActivity() {
		IngogoApp.getSharedApplication().setComingFromPayOffline(true);

		Intent intent = new Intent(IGFindPassengerActivity.this,
				IGJobsActivity.class);
		IGJobsActivity.checkDriverStatus = true;
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/*
	 * Action for swipe payment button button
	 */

	public void onSwipePaymentButtonClick(View v) {
		IngogoApp.getSharedApplication().setMeterFare("00.00");

		Intent swipeCalculator = new Intent(IGFindPassengerActivity.this,
				IGSwipeCalculatorActivity.class);
		swipeCalculator.putExtra("isUnknownPassenger", true);

		IGSwipeCalculatorActivity.clearCachedValues();
		startActivity(swipeCalculator);
	}

	@Override
	public void createBookingForPaymentCompleted(
			IGCreateBookingForPaymentResponseBean response) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		_confirmButton.setClickable(true);
		IGBookingModel bookingSummary = response.getBookingSummary();
		IngogoApp.getSharedApplication().setMeterFare("00.00");

		if (bookingSummary.isHasRegisteredCard()) {
			Intent paymentActivity = new Intent(IGFindPassengerActivity.this,
					IGPaymentActivity.class);
			paymentActivity.putExtra(IGConstants.kJobId,
					bookingSummary.getBookingId());
			paymentActivity.putExtra("disableTakePaymentOption", true);
			paymentActivity.putExtra("hideHailBookOption", true);
			String passengerId = _passengerInfo.getPassengerId();
			paymentActivity.putExtra(IGConstants.kPassengerID, passengerId);
			startActivity(paymentActivity);
		} else {
			Intent swipeCalculator = new Intent(IGFindPassengerActivity.this,
					IGSwipeCalculatorActivity.class);
			IGSwipeCalculatorActivity.clearCachedValues();

			swipeCalculator.putExtra(IGConstants.kJobId,
					bookingSummary.getBookingId());

			swipeCalculator.putExtra(IGConstants.kMinimumDue,
					response.getMinTotalDueValue());
			swipeCalculator.putExtra(IGConstants.kMaximumDue,
					response.getMaxTotalDueValue());
			swipeCalculator.putExtra(IGConstants.kConfirmationValue,
					response.getConfirmationValue());
			swipeCalculator.putExtra(IGConstants.kCreditBalance,
					response.getBalance());
			swipeCalculator.putExtra(IGConstants.kCreditPercentage,
					response.getCreditPercentage());
			swipeCalculator.putExtra("isUnknownPassenger", false);

			startActivity(swipeCalculator);
		}
	}

	@Override
	public void createBookingForPaymentFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		_confirmButton.setClickable(true);
		IGUtility.showDialogOk("", errorMessage, this);
	}

	@Override
	public void onBackPressed() {
		clearCachedPassengerInfo();
		super.onBackPressed();
	}

	public static void clearCachedPassengerInfo() {
		// TODO Auto-generated method stub
		_mobileNumberString = null;
		_passengerInfo = null;
	}

}
