package com.ingogo.android.activities.payments;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCompleteOfflineJobApi;
import com.ingogo.android.webservices.interfaces.IGCompleteOfflineListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

public class IGPaymentFailureNewActivity extends IGBaseActivity implements
		IGCompleteOfflineListener, IGExceptionApiListener {

	private Button _btnPayOffline;
	private TextView _failureReasonTv, _totalFareTv, _baseFeeTv, _serviceFeeTv,
			_totalPaymentDueTv;
	private ProgressDialog _progressDialog;
	private String _jobID, _PaymentDue, _FailureReasonText, _totalFare,
			_baseFee, _serviceFee, _totalPaymentDue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_failed_new);
		getBundleExtras();
		initWithViews();
		setUpViews();
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

	private void getBundleExtras() {
		Intent intent = getIntent();
		HashMap<String, String> data_map = (HashMap) intent.getExtras().get(
				IGConstants.kPaymentDetails);

		_jobID = data_map.get(IGConstants.kJobId);
		_PaymentDue = data_map.get(IGConstants.kPaymentDue);
		_FailureReasonText = (String) intent.getExtras().get(
				IGConstants.kErrorMessage);
		_totalFare = data_map.get(IGConstants.kFareEntered);
		_baseFee = (String) intent.getExtras().get(IGConstants.kbaseFee);
		_serviceFee = (String) intent.getExtras().get(IGConstants.kServiceFee);
		_totalPaymentDue = (String) intent.getExtras().get(
				IGConstants.kTripCharge);

	}

	private void initWithViews() {
		_btnPayOffline = (Button) findViewById(R.id.bPayOffline);
		_failureReasonTv = (TextView) findViewById(R.id.failureReasonTV);
		_totalFareTv = (TextView) findViewById(R.id.totalFareTV);
		_baseFeeTv = (TextView) findViewById(R.id.baseFeeTV);
		_serviceFeeTv = (TextView) findViewById(R.id.serviceFeeTV);
		_totalPaymentDueTv = (TextView) findViewById(R.id.totalPaymentDueTV);
	}

	private void setUpViews() {
		_failureReasonTv.setText(_FailureReasonText);
		_totalFareTv.setText("$ " + _totalFare);
		_baseFeeTv.setText("$ " + _baseFee);
		_serviceFeeTv.setText("$ " + _serviceFee);
		_totalPaymentDueTv.setText("$ " + _totalPaymentDue);
	}

	/**
	 * Successful response is received by this method, when calling web service
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		super.onResponseReceived(response, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);

		// complete offline web service.
		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			goToJobsActivity();
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
			// TODO: Get the error message and show it in the error textview
		}

		Log.e("Payment activity errorResponse", "" + errorResponse);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onFailedToGetResponse(errorResponse, apiID);

	}

	/**
	 * To navigate to jobs activity by clearing all the activities between
	 * payment activity and jobs activity.
	 */
	private void goToJobsActivity() {
		Intent intent = new Intent(IGPaymentFailureNewActivity.this,
				IGJobsActivity.class);
		IGJobsActivity.checkDriverStatus = true;
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
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

	public void onPayOfflineButtonClick(View v) {
		Log.d("PAYMENTFAILURE", "Payoffline clicked");
		callCompleteOfflineJobApi();

	}

	private void callCompleteOfflineJobApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			// Disable the button to prevent multiple click.
			_btnPayOffline.setEnabled(false);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCompleteOfflineJobApi _completeOfflineApi = new IGCompleteOfflineJobApi(
					this,this, _jobID);
			_completeOfflineApi.completeOffline();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	@Override
	public void completeOfflineSuccess(String sucessString, String totalPaid) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_btnPayOffline.setEnabled(true);
		IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
		goToJobsActivity();

	}

	@Override
	public void completeOfflineFailed(String errorMessage,
			boolean isHandleDriverStaleState) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_btnPayOffline.setEnabled(true);
		if(isHandleDriverStaleState) {
			super.onNullResponseRecieved();
			return;
		}
		IGUtility.showDialog("", errorMessage, this);

	}
}
