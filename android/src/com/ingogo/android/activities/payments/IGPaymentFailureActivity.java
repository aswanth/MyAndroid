package com.ingogo.android.activities.payments;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGCreditCardInformation;
import com.ingogo.android.model.IGReceiptInformationModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGPayOfflineOverlay;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCompleteOfflineJobApi;
import com.ingogo.android.webservices.IGPaymentJobApi;
import com.ingogo.android.webservices.IGProcessPaymentApi;
import com.ingogo.android.webservices.IGProcessPaymentForUnknownPassengerApi;
import com.ingogo.android.webservices.beans.response.IGProcessPaymentForUnknownPassengerResponseBean;
import com.ingogo.android.webservices.interfaces.IGCompleteOfflineListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentJobApiListener;
import com.ingogo.android.webservices.interfaces.IGProcessPaymentApiListener;
import com.ingogo.android.webservices.interfaces.IGProcessPaymentForUnknownPassengerApiListener;

public class IGPaymentFailureActivity extends IGBaseActivity implements
		IGProcessPaymentApiListener,
		IGProcessPaymentForUnknownPassengerApiListener,
		IGPaymentJobApiListener, IGCompleteOfflineListener,
		IGExceptionApiListener {

	Dialog _dialog;
	private TextView _tvwPaymentDueAmount, _tvwFailureReason,
			_tvFailureReasonHeading;
	private int _FailureReason, _creditCardCount;
	private String _FailureReasonText, _jobID, _totalAmtToBePaidIncAllCharges;
	private Button _btnTryAgain, _btnPayOffline, _btnTryAnotherCard,
			_btnTryAgainH, _swipeButton, _swipeCardBottom, _btnPayOfflineHalf;
	private String _FareEntered, _baseFee, _serviceFee, _tripCharge;
	private String _CardID;
	private String _PinNumber;
	private ProgressDialog _progressDialog;
	private IGCreditCardInformation _creditCardInfo;
	private boolean _isPaymentViaSwipe;
	private String _bookingType;
	private boolean _hailBookingType = false;
	private boolean _isUnknownPassenger;
	DecimalFormat _decimalFormat;
	private String _encryptedCardDataString;

	private String _balance;
	List<JSONObject> _cardDetailsList = new Vector<JSONObject>();
	private HashMap<String, Object> _dataMap;
	private String _suburbName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_failed_layout);
		_decimalFormat = new DecimalFormat(IGConstants.zeroBalance);
		_creditCardInfo = new IGCreditCardInformation();
		Log.d("PAYMENTFAILURE", "Paymentfailure loaded");
		getBundleExtras();
		initWithViews();
		setUpViews();

		IngogoApp.setSwipeFailedScreenLoadedTime(getTimeString());

		HashMap<String, String> timeTracker = new HashMap<String, String>();
		timeTracker.put("swipeButtonTappedTime",
				IngogoApp.getSwipeButtonTappedTime());
		timeTracker.put("swipeScreenCreatedTime",
				IngogoApp.getSwipeScreenCreatedTime());
		timeTracker.put("swipeInitialisationStartedTime",
				IngogoApp.getSwipeInitialisationStartedTime());
		timeTracker.put("swipeInitialisationCompleteTime",
				IngogoApp.getSwipeInitialisationCompleteTime());
		timeTracker.put("swipeRecordedTime", IngogoApp.getSwipeRecordedTime());
		timeTracker.put("swipeFailedScreenLoadedTime",
				IngogoApp.getSwipeFailedScreenLoadedTime());

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
	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
	}

	@Override
	public void onBackPressed() {
		Log.d("PAYMENTFAILURE", "Back button pressed, navigation not allowed");
		return;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return false;
	}

	private void setUpViews() {

		_tvwPaymentDueAmount.setText("$" + _totalAmtToBePaidIncAllCharges);

		// TODO: Rewrite this part to accommodate changes in
		// COnfirmationActivity
		if (_FailureReason == IGConstants.paymentErrorTypes.kUnspecifiedFailure) {
			Log.d("PAYMENTFAILURE", "Online payment allowed");
			// tryAgainOption = true;
			if (_creditCardCount > 1) {
				_btnTryAnotherCard.setVisibility(View.VISIBLE);
				_btnTryAgainH.setVisibility(View.VISIBLE);

				_btnTryAgain.setVisibility(View.GONE);

				_swipeCardBottom.setVisibility(View.VISIBLE);
				_btnPayOfflineHalf.setVisibility(View.VISIBLE);

				_btnPayOffline.setVisibility(View.GONE);

			} else {
				_btnPayOffline.setVisibility(View.VISIBLE);
				_btnPayOfflineHalf.setVisibility(View.GONE);
				_btnTryAnotherCard.setVisibility(View.GONE);
				_btnTryAgainH.setVisibility(View.GONE);
				_swipeCardBottom.setVisibility(View.GONE);

				_btnTryAgain.setVisibility(View.VISIBLE);
				_btnTryAgain.setEnabled(true);

				_swipeButton.setVisibility(View.VISIBLE);

				_btnTryAgainH.setVisibility(View.VISIBLE);
				_btnTryAgain.setVisibility(View.GONE);

			}

		} else {

			_btnTryAnotherCard.setVisibility(View.GONE);

			_btnTryAgainH.setVisibility(View.GONE);
		}

		_tvwFailureReason.setText(_FailureReasonText);
		if (getIntent().getExtras().getString(IGConstants.kErrorHeading) != null) {
			_tvFailureReasonHeading.setText(getIntent().getExtras().getString(
					IGConstants.kErrorHeading));
		}
		if (_isPaymentViaSwipe) {
			_swipeButton.setVisibility(View.VISIBLE);
			_btnTryAgainH.setVisibility(View.VISIBLE);
			_btnTryAnotherCard.setVisibility(View.GONE);
			_btnTryAgain.setVisibility(View.GONE);
			if (_creditCardCount == 0
					&& _totalAmtToBePaidIncAllCharges
							.equalsIgnoreCase(IGConstants.zeroBalance)) {
				_swipeButton.setVisibility(View.GONE);
				_btnTryAgain.setVisibility(View.VISIBLE);
				_btnTryAgainH.setVisibility(View.GONE);
			}
			if (_creditCardCount > 1) {
				_btnTryAnotherCard.setVisibility(View.VISIBLE);
				_btnTryAgainH.setVisibility(View.VISIBLE);
				_btnTryAgain.setVisibility(View.GONE);

				_swipeButton.setVisibility(View.GONE);
				_swipeCardBottom.setVisibility(View.VISIBLE);
				_btnPayOfflineHalf.setVisibility(View.VISIBLE);
				_btnPayOffline.setVisibility(View.GONE);
			}

		}
		if (_totalAmtToBePaidIncAllCharges
				.equalsIgnoreCase(IGConstants.zeroBalance)) {
			_swipeCardBottom.setVisibility(View.GONE);

			_swipeButton.setVisibility(View.GONE);
			_btnTryAgain.setVisibility(View.VISIBLE);
			_btnTryAgainH.setVisibility(View.GONE);
			_btnPayOfflineHalf.setVisibility(View.GONE);
			_btnPayOffline.setVisibility(View.VISIBLE);
			_btnTryAnotherCard.setVisibility(View.GONE);
			_btnTryAnotherCard.setVisibility(View.GONE);

		}

		if (_FailureReasonText.equals(getText(
				R.string.pickup_refuted_error_message).toString())) {

			_swipeCardBottom.setVisibility(View.GONE);
			_swipeButton.setVisibility(View.GONE);
			_btnTryAgain.setVisibility(View.GONE);
			_btnTryAgainH.setVisibility(View.GONE);
			_btnPayOfflineHalf.setVisibility(View.GONE);
			_btnPayOffline.setVisibility(View.VISIBLE);
			_btnTryAnotherCard.setVisibility(View.GONE);
		}
	}

	private void getBundleExtras() {
		Intent intent = getIntent();

		if (!intent.getBooleanExtra(IGApiConstants.kIsSwipeFailure, false)) {
			_isPaymentViaSwipe = false;
			HashMap<String, String> data_map = (HashMap<String, String>) intent
					.getExtras().get(IGConstants.kPaymentDetails);
			// Log.i("EXTRAS", "" + data_map.get(IGConstants.kPaymentDetails));

			_jobID = data_map.get(IGConstants.kJobId);
			_FareEntered = data_map.get(IGConstants.kFareEntered);
			_CardID = data_map.get(IGConstants.kCardNumber);
			_PinNumber = data_map.get(IGConstants.kPinNumber);
			_FailureReason = Integer.parseInt(intent.getExtras()
					.get(IGConstants.kErrorCode).toString());
			_FailureReasonText = (String) intent.getExtras().get(
					IGConstants.kErrorMessage);
			_creditCardCount = intent.getIntExtra(IGConstants.kCreditCardCount,
					0);
			_baseFee = intent.getStringExtra(IGConstants.kbaseFee);
			_serviceFee = intent.getStringExtra(IGConstants.kServiceFee);
			_tripCharge = intent.getStringExtra(IGConstants.kTripCharge);
			_totalAmtToBePaidIncAllCharges = intent
					.getStringExtra(IGConstants.kTotalDueAmount);
			_bookingType = intent.getExtras().getString(
					IGConstants.kBookingType);
			if (_bookingType.equals(IGConstants.kBookingTypeHail)) {
				_hailBookingType = true;
			} else {
				_hailBookingType = false;
			}

			if (intent.getExtras().containsKey(IGConstants.kBalance)) {
				_balance = intent.getExtras().getString(IGConstants.kBalance);
			}

			if (intent.getExtras().containsKey(IGApiConstants.kCardDetails)) {

				_dataMap = (HashMap) intent.getExtras().get(
						IGApiConstants.kCardDetails);
				String jsonArray = (String) _dataMap
						.get(IGApiConstants.kCardDetails);
				try {
					JSONArray jarray = new JSONArray(jsonArray);
					Log.i("jarray", "" + jarray);
					for (int i = 0; i < jarray.length(); i++) {

						_cardDetailsList.add(jarray.getJSONObject(i));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		} else {
			_isPaymentViaSwipe = true;
			_creditCardInfo = (IGCreditCardInformation) intent.getExtras()
					.getSerializable("cardinfo");
			_encryptedCardDataString = (String) intent.getExtras().getString(
					"encryptedCardDataString");
			_FareEntered = (String) intent.getExtras().get(
					IGConstants.kFareEntered);
			_tripCharge = _FareEntered;
			_baseFee = "00.00";
			_serviceFee = "00.00";
			_totalAmtToBePaidIncAllCharges = (String) intent.getExtras().get(
					IGConstants.kTotalDueAmount);
			try {
				_jobID = (String) intent.getExtras().get(IGConstants.kJobId);

			} catch (Exception e) {
				_jobID = null;
			}

			_FailureReasonText = (String) intent.getExtras().get(
					IGConstants.kErrorMessage);
			_creditCardCount = intent.getIntExtra(IGConstants.kCreditCardCount,
					0);

			if (intent.getExtras().containsKey(IGConstants.kBalance)) {
				_balance = intent.getExtras().getString(IGConstants.kBalance);
			}

			if (intent.getExtras().containsKey(IGApiConstants.kCardDetails)) {

				_dataMap = (HashMap) intent.getExtras().get(
						IGApiConstants.kCardDetails);
				String jsonArray = (String) _dataMap
						.get(IGApiConstants.kCardDetails);
				try {
					JSONArray jarray = new JSONArray(jsonArray);
					Log.i("jarray", "" + jarray);
					for (int i = 0; i < jarray.length(); i++) {

						_cardDetailsList.add(jarray.getJSONObject(i));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (intent.getExtras().containsKey(IGConstants.kBookingType)) {
				_bookingType = intent.getExtras().getString(
						IGConstants.kBookingType);
			}
		}
		_isUnknownPassenger = intent.getBooleanExtra("isUnknownPassenger",
				false);
		try {
			_suburbName = intent.getStringExtra(IGConstants.KSuburbName);
		} catch (NullPointerException e) {

		}

	}

	private void initWithViews() {
		_tvwPaymentDueAmount = (TextView) findViewById(R.id.payment_failure_due_amount);
		_tvwFailureReason = (TextView) findViewById(R.id.payment_failure_message);
		_btnTryAgain = (Button) findViewById(R.id.payment_failure_tryagain_button);
		_btnTryAnotherCard = (Button) findViewById(R.id.tryAnotherCardBtn);
		_btnTryAgainH = (Button) findViewById(R.id.tryAgainHBtn);
		_btnPayOffline = (Button) findViewById(R.id.payment_failure_offlinepayment_button);
		_swipeButton = (Button) findViewById(R.id.swipeAnotherCardBtn);
		_swipeCardBottom = (Button) findViewById(R.id.swipeAnotherCardBottomBtn);
		_btnPayOfflineHalf = (Button) findViewById(R.id.payment_failure_offlinepayment_half_button);
		_tvFailureReasonHeading = (TextView) findViewById(R.id.payment_failure_message_label);
	}

	private void callCompleteOfflineJobApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			// Disable the button to prevent multiple click.
			_btnPayOffline.setEnabled(false);
			_btnPayOfflineHalf.setEnabled(false);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCompleteOfflineJobApi _completeOfflineApi = new IGCompleteOfflineJobApi(
					this, this, _jobID);
			_completeOfflineApi.completeOffline();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * Call record payment api
	 */
	private void callPaymentApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGPaymentJobApi paymentApi = new IGPaymentJobApi(this, this);
			paymentApi.payment(_jobID, _totalAmtToBePaidIncAllCharges,
					_FareEntered, _CardID, _PinNumber, getIntent().getExtras()
							.getString(IGConstants.kPaymentMethod));
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);

		}

	}

	private void callUnknownIngogoPassengerApi() {

		if (IGUtility.isNetworkAvailable(IGPaymentFailureActivity.this)) {
			_progressDialog = IGUtility
					.showProgressDialog(IGPaymentFailureActivity.this);
			IGProcessPaymentForUnknownPassengerApi api = new IGProcessPaymentForUnknownPassengerApi(
					IGPaymentFailureActivity.this,
					IGPaymentFailureActivity.this);

			if (_encryptedCardDataString != null) {
				if (_encryptedCardDataString.length() > 4) {
					api.processPaymentForUnknownPassenger(_jobID, _FareEntered,
							_totalAmtToBePaidIncAllCharges, _creditCardInfo,
							_encryptedCardDataString, "");
				} else {
					api.processPaymentForUnknownPassenger(_jobID, _FareEntered,
							_totalAmtToBePaidIncAllCharges, _creditCardInfo, "");
				}
			} else {
				api.processPaymentForUnknownPassenger(_jobID, _FareEntered,
						_totalAmtToBePaidIncAllCharges, _creditCardInfo, "");
			}

		} else {

			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);

		}

	}

	private void callIngogoPassengerPaymentApi() {
		if (IGUtility.isNetworkAvailable(this)) {

			_progressDialog = IGUtility.showProgressDialog(this);

			IGProcessPaymentApi api = new IGProcessPaymentApi(this, this);
			api.processPayment(_jobID, _FareEntered,
					_totalAmtToBePaidIncAllCharges, _creditCardInfo,
					_encryptedCardDataString, "");
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	private void callProcessPaymentApi() {
		if (_isUnknownPassenger) {
			callUnknownIngogoPassengerApi();
		} else {
			callIngogoPassengerPaymentApi();
		}
	}

	/**
	 * To navigate to jobs activity by clearing all the activities between
	 * payment activity and jobs activity.
	 */
	private void goToJobsActivity() {

		if (_dialog != null && _dialog.isShowing()) {
			_dialog.cancel();
		}
		Intent intent = new Intent(IGPaymentFailureActivity.this,
				IGJobsActivity.class);
		IGJobsActivity.checkDriverStatus = true;
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	/**
	 * Triggered when the ok btn in the overlay is tapped
	 * 
	 * @param view
	 */
	public void overlayOkBtnClicked(View view) {

		goToJobsActivity();
	}

	/**
	 * Successful response is received by this method, when calling web service
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		Log.e("Payment response ", "" + response);
		super.onResponseReceived(response, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);

		// complete offline web service.
		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);

			JSONObject resp = (JSONObject) response.get(IGConstants.kDataKey);
			String sucessString = null;
			String totalPaid = null;
			try {
				sucessString = resp
						.getString(IGConstants.kReceiptInformationKey);
				totalPaid = resp.getString(IGConstants.kTotalPaidKey);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (sucessString != null && !sucessString.equals("null")) {

				if (totalPaid == null || totalPaid.trim().equals("null")) {
					totalPaid = "00.00";
				} else {
					Double total = Double.valueOf(totalPaid);
					totalPaid = (String.valueOf(_decimalFormat.format(total)));
				}

				if (_isUnknownPassenger) {
					Intent paymentCompletedIntent = new Intent(
							IGPaymentFailureActivity.this,
							IGPaymentCompletedActivity.class);

					paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
							totalPaid);
					paymentCompletedIntent.putExtra(
							IGConstants.kPaymentSuccess,
							"Your payment was successful !!!");
					paymentCompletedIntent.putExtra(IGConstants.kBookingId,
							_jobID);
					startActivity(paymentCompletedIntent);

				} else {
					Intent paymentCompletedIntent = new Intent(
							IGPaymentFailureActivity.this,
							IGPaymentSucessActivity.class);
					paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
							totalPaid);
					paymentCompletedIntent.putExtra(
							IGConstants.kPaymentSuccess, sucessString);
					paymentCompletedIntent.putExtra(IGConstants.kJobId, _jobID);
					startActivity(paymentCompletedIntent);
				}

			} else {

				if (!_hailBookingType && !_isPaymentViaSwipe) {
					IGPayOfflineOverlay.Builder customBuilder = new IGPayOfflineOverlay.Builder(
							this);
					_dialog = customBuilder.create();
					_dialog.setCancelable(false);
					_dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_SEARCH
									&& event.getRepeatCount() == 0) {
								return true;
							}
							return false;
						}

					});
					_dialog.setTitle(getResources().getString(
							R.string.pay_offline_overlay_title));

					TextView totalFare = (TextView) _dialog
							.findViewById(R.id.overlayTotalFare);
					TextView baseFee = (TextView) _dialog
							.findViewById(R.id.overlayBaseFee);
					TextView serviceFee = (TextView) _dialog
							.findViewById(R.id.overlayServiceFee);
					TextView tripCharge = (TextView) _dialog
							.findViewById(R.id.overlayTripCharge);

					totalFare.setText("$" + _FareEntered);
					baseFee.setText("$" + _baseFee);
					serviceFee.setText("$" + _serviceFee);
					tripCharge.setText("$" + _tripCharge);

					_dialog.show();
				} else {
					goToJobsActivity();
				}

			}

			return;
		}
		if (apiID == IGApiConstants.kPaymentWebServiceId) {
			JSONObject resp = (JSONObject) response.get(IGConstants.kDataKey);
			String sucessString = null;
			try {
				sucessString = resp.getString(IGConstants.kReceiptKey);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IGUtility.dismissProgressDialog(_progressDialog);
			// load the sucess page.

			if (_isUnknownPassenger) {
				Intent paymentCompletedIntent = new Intent(
						IGPaymentFailureActivity.this,
						IGPaymentCompletedActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
						_totalAmtToBePaidIncAllCharges);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						"Your payment was successful !!!");
				paymentCompletedIntent.putExtra(IGConstants.kBookingId, _jobID);
				startActivity(paymentCompletedIntent);

			} else {
				Intent paymentCompletedIntent = new Intent(
						IGPaymentFailureActivity.this,
						IGPaymentSucessActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
						_totalAmtToBePaidIncAllCharges);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						sucessString);
				paymentCompletedIntent.putExtra(IGConstants.kJobId, _jobID);

				startActivity(paymentCompletedIntent);
			}

			return;
		}
	}

	/**
	 * Failure response is got in this method
	 */
	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			// Enable the payoffline button which is disabled earlier.
			_btnPayOffline.setEnabled(true);
			_btnPayOfflineHalf.setEnabled(true);
		}

		Log.e("Payment activity errorResponse", "" + errorResponse);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (apiID != IGApiConstants.kPaymentWebServiceId) {
			super.onFailedToGetResponse(errorResponse, apiID);
		} else {
			String errorContent = errorResponse.toString();
			if (errorContent.contains(IGConstants.kPickupRefutedErrorMessage)
					|| (errorContent.contains("Pickup Refuted"))) {

				String errorMessage = getText(
						R.string.pickup_refuted_error_message).toString();

				_swipeCardBottom.setVisibility(View.GONE);
				_swipeButton.setVisibility(View.GONE);
				_btnTryAgain.setVisibility(View.GONE);
				_btnTryAgainH.setVisibility(View.GONE);
				_btnPayOfflineHalf.setVisibility(View.GONE);
				_btnPayOffline.setVisibility(View.VISIBLE);
				_btnTryAnotherCard.setVisibility(View.GONE);

				_tvwFailureReason.setText(errorMessage);
			}
		}

	}

	public void onPayOfflineButtonClick(View v) {
		callCompleteOfflineJobApi();

	}

	public void onTryAgainButtonClick(View v) {
		QLog.d("PAYMENT", "Try again payment button pressed for mobile number "
				+ IngogoApp.getSharedApplication().getUserId());
		if (_isPaymentViaSwipe) {
			callProcessPaymentApi();
		} else {
			callPaymentApi();

		}
	}

	public void onTryAnotherCardBtnClicked(View view) {

		Intent intent = new Intent(this, IGPaymentConfirmationActivity.class);
		HashMap<String, Object> _jobDetails = (HashMap) getIntent().getExtras()
				.get(IGConstants.kJobDetails);
		intent.putExtra(IGConstants.kJobDetails, _jobDetails);

		String passengerId = (String) getIntent().getExtras().get(
				IGConstants.kPassengerID);

		intent.putExtra(IGConstants.kPassengerID, passengerId);

		intent.putExtra(IGConstants.kBookingType, _bookingType);
		intent.putExtra(IGConstants.disableBackButton, true);

		HashMap<String, Object> paymentDetailsFromIntent = (HashMap) getIntent()
				.getExtras().get(IGConstants.kDetails);
		if (paymentDetailsFromIntent != null) {
			intent.putExtra(IGConstants.kPaymentDetails,
					paymentDetailsFromIntent);

		} else {
			HashMap<String, Object> paymentDetails = new HashMap<String, Object>();
			paymentDetails.put(IGConstants.kBookingFee, _baseFee);
			paymentDetails.put(IGConstants.kBidExtra, _serviceFee);
			if (_balance != null) {
				paymentDetails.put(IGConstants.kBalance, _balance);
			}
			paymentDetails.put(IGConstants.kPaymentDue,
					_totalAmtToBePaidIncAllCharges);
			paymentDetails.put(IGConstants.kFareEntered, _FareEntered);
			paymentDetails.put(IGConstants.kJobId, _jobID);
			paymentDetails.put(IGApiConstants.kCardDetails,
					_cardDetailsList.toString());
			try {
				paymentDetails.put(IGApiConstants.kminTotalDueValue,
						_jobDetails.get(IGApiConstants.kminTotalDueValue));
				paymentDetails.put(IGApiConstants.kmaxTotalDueValue,
						_jobDetails.get(IGApiConstants.kmaxTotalDueValue));
				paymentDetails.put(IGApiConstants.kconfirmationValue,
						_jobDetails.get(IGApiConstants.kconfirmationValue));
				paymentDetails.put(IGApiConstants.kCreditPercentage,
						_jobDetails.get(IGApiConstants.kCreditPercentage));
				paymentDetails.put(IGConstants.kCreditBalance,
						_jobDetails.get(IGConstants.kCreditBalance));
			} catch (Exception e) {

			}
			intent.putExtra(IGConstants.kPaymentDetails, paymentDetails);

		}

		startActivity(intent);

	}

	@Override
	public void processPaymentCompleted(IGBookingModel bookingModel,
			String receiptInformationPageText,
			IGReceiptInformationModel receiptInformation) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		if (_isUnknownPassenger) {
			Intent paymentCompletedIntent = new Intent(
					IGPaymentFailureActivity.this,
					IGPaymentCompletedActivity.class);
			paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
					_totalAmtToBePaidIncAllCharges);
			// paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
			// "Your payment was successful !!!");
			if (_jobID == null) {
				_jobID = bookingModel.getBookingId();
			}
			paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
					receiptInformationPageText);
			paymentCompletedIntent.putExtra(IGConstants.kBookingId, _jobID);
			paymentCompletedIntent.putExtra(IGConstants.kReceiptInformationKey,
					receiptInformation);
			startActivity(paymentCompletedIntent);
		} else {
			Intent sucessIntent = new Intent(IGPaymentFailureActivity.this,
					IGPaymentSucessActivity.class);
			if (_jobID == null) {
				_jobID = bookingModel.getBookingId();
			}
			sucessIntent.putExtra(IGConstants.kJobId, _jobID);

			sucessIntent.putExtra(IGConstants.kPaymentSuccess,
					"Your payment was successful !!!");
			sucessIntent.putExtra(IGConstants.kPaymentDue,
					_totalAmtToBePaidIncAllCharges);
			startActivity(sucessIntent);
		}

	}

	@Override
	public void processPaymentFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		refreshErrorMessage(errorMessage);
	}

	public void refreshErrorMessage(String errorContent) {
		String errorHeading = getText(R.string.payment_failure_message)
				.toString();
		String errorMessage = getText(
				R.string.payment_unspecified_failure_error_string).toString();

		if (errorContent.contains("DECLINED")
				|| (errorContent.contains("Payment declined"))) {
			errorHeading = "TRANSACTION DECLINED BY CARD HOLDER's BANK";
			errorMessage = "Transaction declined by card holders bank. Try another card.";
		} else if (errorContent.contains("EXPIRED_CARD")
				|| (errorContent.contains("Expired card"))) {
			errorHeading = "EXPIRED CARD";
			errorMessage = "Card has expired. Try another card";
		} else if (errorContent.contains("INSUFFICIENT_FUNDS")
				|| (errorContent.contains("Insufficient funds"))) {
			errorHeading = "INSUFFICIENT FUNDS";
			errorMessage = "Card has insufficient funds available. Try another card";
		} else if (errorContent
				.contains(IGConstants.kPickupRefutedErrorMessage)
				|| (errorContent.contains("Pickup Refuted"))) {
			errorMessage = getText(R.string.pickup_refuted_error_message)
					.toString();

			_swipeCardBottom.setVisibility(View.GONE);
			_swipeButton.setVisibility(View.GONE);
			_btnTryAgain.setVisibility(View.GONE);
			_btnTryAgainH.setVisibility(View.GONE);
			_btnPayOfflineHalf.setVisibility(View.GONE);
			_btnPayOffline.setVisibility(View.VISIBLE);
			_btnTryAnotherCard.setVisibility(View.GONE);
		}

		_tvFailureReasonHeading.setText(errorHeading);
		_tvwFailureReason.setText(errorMessage);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		refreshErrorMessage("");
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		refreshErrorMessage("");
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		refreshErrorMessage("");
	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		refreshErrorMessage("");
	}

	public void onSwipeAnotherCardBtnClicked(View v) {

		Intent swipeCardActivity = null;
		if (!_bookingType.equals(IGConstants.kBookingTypeBooking.toString())) {
			swipeCardActivity = new Intent(this,
					IGSwipeCalculatorActivity.class);
			IGSwipeCalculatorActivity.clearCachedValues();
			String passengerId = (String) getIntent().getExtras().get(
					IGConstants.kPassengerID);
			if (_isUnknownPassenger) {
				passengerId = null;
			}
			Double _minimumFare = getIntent().getDoubleExtra(
					IGConstants.kMinimumDue, 0);
			Double _maximumFare = getIntent().getDoubleExtra(
					IGConstants.kMaximumDue, 0);
			Double _confirmationValue = getIntent().getDoubleExtra(
					IGConstants.kConfirmationValue, 0);
			Double _creditPercentage = getIntent().getDoubleExtra(
					IGConstants.kCreditPercentage, 0);
			Double _ingogoCreditBalance = getIntent().getDoubleExtra(
					IGConstants.kCreditBalance, 0);
			swipeCardActivity.putExtra(IGConstants.kPassengerID, passengerId);
			swipeCardActivity.putExtra(IGConstants.kMaximumDue, _maximumFare);
			swipeCardActivity.putExtra(IGConstants.kMinimumDue, _minimumFare);
			swipeCardActivity.putExtra(IGConstants.kConfirmationValue,
					_confirmationValue);
			swipeCardActivity.putExtra(IGConstants.kCreditBalance,
					_ingogoCreditBalance);
			swipeCardActivity.putExtra(IGConstants.kCreditPercentage,
					_creditPercentage);
			HashMap<String, Object> map = (HashMap) getIntent().getExtras()
					.get(IGConstants.kPaymentDetails);
			swipeCardActivity.putExtra(IGConstants.kFareDetails, map);
		} else {
			swipeCardActivity = new Intent(this, IGPaymentsSwipeActivity.class);
			HashMap<String, Object> _jobDetails = (HashMap) getIntent()
					.getExtras().get(IGConstants.kJobDetails);
			swipeCardActivity.putExtra(IGConstants.kJobDetails, _jobDetails);
		}

		swipeCardActivity.putExtra(IGConstants.kTripCharge,
				_totalAmtToBePaidIncAllCharges);
		swipeCardActivity.putExtra(IGConstants.kFareEntered, _FareEntered);
		IngogoApp.getSharedApplication().setMeterFare(_FareEntered);
		swipeCardActivity.putExtra("isUnknownPassenger", _isUnknownPassenger);
		swipeCardActivity.putExtra(IGConstants.kJobId, _jobID);
		swipeCardActivity.putExtra(IGConstants.kCreditCardCount,
				_creditCardCount);

		HashMap<String, Object> paymentDetails = new HashMap<String, Object>();
		paymentDetails.put(IGApiConstants.kCardDetails,
				_cardDetailsList.toString());
		swipeCardActivity.putExtra(IGConstants.kPaymentDetails, paymentDetails);
		if (_balance != null) {
			swipeCardActivity.putExtra(IGConstants.kBalance, _balance);
		}
		swipeCardActivity.putExtra(IGConstants.kBookingType, _bookingType);
		swipeCardActivity.putExtra(IGConstants.kPaymentFailurePage, true);
		swipeCardActivity.putExtra(IGConstants.KSuburbName, _suburbName);
		startActivity(swipeCardActivity);

	}

	@Override
	public void processPaymentForUnknownPassengerCompleted(
			IGProcessPaymentForUnknownPassengerResponseBean details) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (!details.isCanTakePayment()) {
			goToJobsActivity();
		} else {
			if (_isUnknownPassenger) {
				Intent paymentCompletedIntent = new Intent(
						IGPaymentFailureActivity.this,
						IGPaymentCompletedActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
						_totalAmtToBePaidIncAllCharges);
				// paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
				// "Your payment was successful !!!");
				try {
					if (details.getBookingSummary().getBookingId() != null) {
						_jobID = details.getBookingSummary().getBookingId();

					}
				} catch (Exception e) {

				}
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						details.getReceiptInformationPageText());
				paymentCompletedIntent.putExtra(IGConstants.kBookingId, _jobID);
				paymentCompletedIntent.putExtra(
						IGConstants.kReceiptInformationKey,
						details.getReceiptInformation());
				startActivity(paymentCompletedIntent);
			} else {
				Intent sucessIntent = new Intent(IGPaymentFailureActivity.this,
						IGPaymentSucessActivity.class);
				try {
					if (details.getBookingSummary().getBookingId() != null) {
						_jobID = details.getBookingSummary().getBookingId();

					}
				} catch (Exception e) {

				}
				sucessIntent.putExtra(IGConstants.kJobId, _jobID);

				sucessIntent.putExtra(IGConstants.kPaymentSuccess,
						"Your payment was successful !!!");
				sucessIntent.putExtra(IGConstants.kPaymentDue,
						_totalAmtToBePaidIncAllCharges);
				startActivity(sucessIntent);
			}
		}

	}

	@Override
	public void processPaymentForUnknownPassengerFailed(String errorMessage,
			IGProcessPaymentForUnknownPassengerResponseBean details) {
		IGUtility.dismissProgressDialog(_progressDialog);
		refreshErrorMessage(errorMessage);

	}

	@Override
	public void completeOfflineSuccess(String sucessString, String totalPaid) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_btnPayOffline.setEnabled(false);
		_btnPayOfflineHalf.setEnabled(false);
		IGUtility.removeDefaults(IGConstants.kJobInProgress, this);

		if (sucessString != null && !sucessString.equals("null")) {
			if (totalPaid == null || totalPaid.trim().equals("null")) {
				totalPaid = "00.00";
			} else {
				Double total = Double.valueOf(totalPaid);
				totalPaid = (String.valueOf(_decimalFormat.format(total)));
			}

			if (_isUnknownPassenger) {
				Intent paymentCompletedIntent = new Intent(
						IGPaymentFailureActivity.this,
						IGPaymentCompletedActivity.class);

				paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
						totalPaid);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						"Your payment was successful !!!");
				paymentCompletedIntent.putExtra(IGConstants.kBookingId, _jobID);
				startActivity(paymentCompletedIntent);

			} else {
				Intent paymentCompletedIntent = new Intent(
						IGPaymentFailureActivity.this,
						IGPaymentSucessActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
						totalPaid);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						sucessString);
				paymentCompletedIntent.putExtra(IGConstants.kJobId, _jobID);
				startActivity(paymentCompletedIntent);
			}

		} else {

			if (!_hailBookingType && !_isPaymentViaSwipe) {
				IGPayOfflineOverlay.Builder customBuilder = new IGPayOfflineOverlay.Builder(
						this);
				_dialog = customBuilder.create();
				_dialog.setCancelable(false);
				_dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_SEARCH
								&& event.getRepeatCount() == 0) {
							return true;
						}
						return false;
					}

				});
				_dialog.setTitle(getResources().getString(
						R.string.pay_offline_overlay_title));

				TextView totalFare = (TextView) _dialog
						.findViewById(R.id.overlayTotalFare);
				TextView baseFee = (TextView) _dialog
						.findViewById(R.id.overlayBaseFee);
				TextView serviceFee = (TextView) _dialog
						.findViewById(R.id.overlayServiceFee);
				TextView tripCharge = (TextView) _dialog
						.findViewById(R.id.overlayTripCharge);

				totalFare.setText("$" + _FareEntered);
				baseFee.setText("$" + _baseFee);
				serviceFee.setText("$" + _serviceFee);
				tripCharge.setText("$" + _tripCharge);

				_dialog.show();
			} else {
				goToJobsActivity();
			}

		}

	}

	@Override
	public void completeOfflineFailed(String errorMessage,
			boolean isHandleDriverStaleState) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_btnPayOffline.setEnabled(true);
		_btnPayOfflineHalf.setEnabled(true);
		if (isHandleDriverStaleState) {
			super.onNullResponseRecieved();
			return;
		}
		IGUtility.showDialog("", errorMessage, this);

	}

	@Override
	public void paymentJobCompleted(String sucessString) {
		IGUtility.dismissProgressDialog(_progressDialog);

		if (_isUnknownPassenger) {
			Intent paymentCompletedIntent = new Intent(
					IGPaymentFailureActivity.this,
					IGPaymentCompletedActivity.class);
			paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
					_totalAmtToBePaidIncAllCharges);
			paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
					"Your payment was successful !!!");
			paymentCompletedIntent.putExtra(IGConstants.kBookingId, _jobID);
			startActivity(paymentCompletedIntent);

		} else {
			Intent paymentCompletedIntent = new Intent(
					IGPaymentFailureActivity.this,
					IGPaymentSucessActivity.class);
			paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
					_totalAmtToBePaidIncAllCharges);
			paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
					sucessString);
			paymentCompletedIntent.putExtra(IGConstants.kJobId, _jobID);

			startActivity(paymentCompletedIntent);
		}

		return;
	}

	@Override
	public void paymentJobFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);

		if (errorMessage.contains(IGConstants.kPickupRefutedErrorMessage)
				|| (errorMessage.contains("Pickup Refuted"))) {

			String errorMessageText = getText(
					R.string.pickup_refuted_error_message).toString();

			_swipeCardBottom.setVisibility(View.GONE);
			_swipeButton.setVisibility(View.GONE);
			_btnTryAgain.setVisibility(View.GONE);
			_btnTryAgainH.setVisibility(View.GONE);
			_btnPayOfflineHalf.setVisibility(View.GONE);
			_btnPayOffline.setVisibility(View.VISIBLE);
			_btnTryAnotherCard.setVisibility(View.GONE);

			_tvwFailureReason.setText(errorMessage);
		}
	}

}
