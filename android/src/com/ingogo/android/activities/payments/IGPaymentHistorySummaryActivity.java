package com.ingogo.android.activities.payments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.activities.IGTripProgressActivity;
import com.ingogo.android.adapters.IGPaymentSummariesAdapter;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGPaymentSummaryModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGPaymentHistoryApi;
import com.ingogo.android.webservices.IGTargetProgressApi;
import com.ingogo.android.webservices.beans.response.IGPaymentHistoryResponseBean;
import com.ingogo.android.webservices.beans.response.IGPaymentSummaryBean;
import com.ingogo.android.webservices.beans.response.IGTargetProgressResponseBean;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentHistoryApiListener;
import com.ingogo.android.webservices.interfaces.IGTargetProgressApiListener;

public class IGPaymentHistorySummaryActivity extends IGBaseActivity implements
		IGExceptionApiListener, IGPaymentHistoryApiListener,
		IGTargetProgressApiListener {

	private ProgressDialog _progressDialog;
	private static final int INDEX_NOT_FOUND = -1;
	private ArrayList<IGPaymentSummaryModel> _paymentSummaries;
	private IGPaymentSummariesAdapter adapter = null;
	private ListView _paymentSummaryListView;
	private String _username, _password;
	private TextView _commissionHeadingTextView, _commissionTextView,
			_levelTextView;
	private IGTargetProgressResponseBean _targetProgress;

	private ProgressDialog _targetProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.payment_history_summary);
		initViews();

	}

	private void getTargetProgress() {
		IGTargetProgressApi api = new IGTargetProgressApi(this, this);
		api.getTargetProgress();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!IGBaseActivity._infoDialogShown) {
			IGUpdatePositionPollingTask.setIgnoreStaleState(true);
			getTripHistory();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
	}

	private void initViews() {
		_paymentSummaryListView = (ListView) findViewById(R.id.paymentSummaryListView);
		_levelTextView = (TextView) findViewById(R.id.ingogoLevelText);

		_commissionTextView = (TextView) findViewById(R.id.commissionText);
		_username = IngogoApp.getSharedApplication().getUserId();
		_password = IngogoApp.getSharedApplication().getPassword();
		_paymentSummaryListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View view,
							int position, long arg3) {
						//If the status is rejected just show an alert.
						if (position != 0 && view.getTag() != null) {
							String tag = view.getTag().toString();
							if (tag.equalsIgnoreCase("Rejected")) {
								IGUtility.showDialogOk("", getString(R.string.rejected_status_message), IGPaymentHistorySummaryActivity.this);
								return;

							}

						}

						Intent intent = new Intent(
								IGPaymentHistorySummaryActivity.this,
								IGPaymentDailySummaryActivity.class);

						if (position != 0) {

							intent.putExtra(IGConstants.kPaymentHistoryItem,
									_paymentSummaries.get(position - 1));

						} else {
							intent.putExtra(IGConstants.kPaymentStatus,
									IGConstants.kPending);
						}
						startActivity(intent);
					}

				});

	}

	private void getTripHistory() {
		if (IGUtility.isNetworkAvailable(this)) {
			if (_progressDialog != null && _progressDialog.isShowing()) {
				return;
			}
			_progressDialog = IGUtility.showProgressDialog(this);

			IGPaymentHistoryApi paymentHistoryApi = new IGPaymentHistoryApi(
					this, this);
			paymentHistoryApi.getPaymentHistorySummaries(_username, _password);
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	@Override
	public void paymentHistoryFetchingCompleted(
			IGPaymentHistoryResponseBean response) {
		IGUtility.dismissProgressDialog(_progressDialog);
		ArrayList<IGPaymentSummaryModel> paymentSummaries = this
				.getPaymentSummariesFromResponse(response);
		if (paymentSummaries != null) {
			sortPaymentSummaries(paymentSummaries);
			filterPaymentSummaries();
		}
		adapter = new IGPaymentSummariesAdapter(this, _paymentSummaries,
				response.getPendingAmount());
		_paymentSummaryListView.setAdapter(adapter);
		setUpCommissionValues(response);

	}

	private void setUpCommissionValues(IGPaymentHistoryResponseBean response) {
		if (response.getCurrentCommission() != null) {

			_commissionTextView.setText(IGUtility.getInPriceFormat(response
					.getCurrentCommission()) + "%");
		} else {
			_commissionTextView.setText("0%");
		}

		/**
		 * setting level information text.
		 */
		setColoredLevelInformationText(response.getCurrentCommssionLevel());
	}

	/**
	 * set the level information text with corresponding green color.
	 * 
	 * @param levelText
	 */
	private void setColoredLevelInformationText(String levelText) {
		_levelTextView.setText(levelText, TextView.BufferType.SPANNABLE);
		Spannable str = (Spannable) _levelTextView.getText();
		/*
		 * Setting green colour to the text after the first word,if the string
		 * contains multiple words.
		 */
		int spaceIndex = levelText.indexOf(' ');
		if (spaceIndex != INDEX_NOT_FOUND) {
			String firstWord = levelText.substring(0, spaceIndex);
			int startColorIndex = 0 + firstWord.length() + 1;
			str.setSpan(
					new ForegroundColorSpan(getResources().getColor(
							R.color.light_green_color)), startColorIndex,
					levelText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	private ArrayList<IGPaymentSummaryModel> getPaymentSummariesFromResponse(
			IGPaymentHistoryResponseBean respBean) {
		ArrayList<IGPaymentSummaryModel> paymentArray = null;

		if (respBean.getPaymentSummaryList() != null) {
			paymentArray = new ArrayList<IGPaymentSummaryModel>();
			for (IGPaymentSummaryBean payBean : respBean
					.getPaymentSummaryList()) {

				IGPaymentSummaryModel paymentSummary = new IGPaymentSummaryModel();
				paymentSummary.setAmount(String.valueOf(payBean.getAmount()));
				paymentSummary.setPaymentId(String.valueOf(payBean
						.getPaymentId()));
				paymentSummary.setSettled(String.valueOf(payBean.getDate()));
				paymentSummary.setStatus(String.valueOf(payBean.getStatus()));

				paymentArray.add(paymentSummary);
			}
		}
		return paymentArray;

	}

	private void filterPaymentSummaries() {
		if (_paymentSummaries.size() > 7) {
			_paymentSummaries = (ArrayList<IGPaymentSummaryModel>) _paymentSummaries
					.subList(0, 6);
		}
	}

	private void sortPaymentSummaries(
			ArrayList<IGPaymentSummaryModel> paymentSummaries) {
		_paymentSummaries = paymentSummaries;
		Collections.sort(_paymentSummaries,
				new Comparator<IGPaymentSummaryModel>() {

					@Override
					public int compare(IGPaymentSummaryModel lhs,
							IGPaymentSummaryModel rhs) {
						return (new Long(rhs.getSettled()).compareTo(new Long(
								lhs.getSettled())));

					}

				});
	}

	@Override
	public void paymentHistoryFetchingFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialogOk("", errorMessage, this);

	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNetWorkUnavailableResponse(errorResponse);
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onRequestTimedoutResponse(errorResponse);
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onInternalServerErrorResponse(errorResponse);

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNullResponseRecieved();

	}

	/**
	 * Earn percentage button click
	 * 
	 * @param v
	 */
	public void onEarnPercentageButtonClick(View v) {

		_targetProgressDialog = IGUtility.showProgressDialog(this);
		getTargetProgress();

	}

	private void goToTripProgressActivity() {
		Intent progressIntent = new Intent(
				IGPaymentHistorySummaryActivity.this,
				IGTripProgressActivity.class);
		progressIntent.putExtra(IGConstants.kTargetProgress, _targetProgress);
		startActivity(progressIntent);
	}

	@Override
	public void targetProgressFetchingCompleted(
			IGTargetProgressResponseBean response) {
		_targetProgress = response;
		IGUtility.dismissProgressDialog(_targetProgressDialog);
		goToTripProgressActivity();

	}

	@Override
	public void targetProgressFetchingFailed(String errorMessge) {
		IGUtility.dismissProgressDialog(_targetProgressDialog);
		IGUtility.showDialog("", errorMessge, this);
	}

}
