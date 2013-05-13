package com.ingogo.android.activities.payments;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGPaymentDetailModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.EllipsizingTextView;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGPaymentDetailApi;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentDetailApiListener;

public class IGPaymentHistoryDetailActivity extends IGBaseActivity implements
		IGPaymentDetailApiListener, IGExceptionApiListener {

	private TextView _jobDateTime, _toAddress, _meterFare,
			_baseFee, _serviceFee, _serviceCredit, _passengerPaid,
			_settlingAmount, _ccShare, _pointsRevenue;
	private TextView _shareHeading;
	EllipsizingTextView _fromAddress;
	private LinearLayout _settlmentDetail, _ccShareDetail,
			_pointsRevenueDetail;
	private String _bookingId;
	private ProgressDialog _progressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_history_detail_layout);
		getBundleExtras();
		initWithViews();
		callPaymentDetailFetchApi();
	}

	private void getBundleExtras() {
		Intent intent = getIntent();
		_bookingId = (String) intent.getExtras().get(IGConstants.kBookingId);

	}

	private void initWithViews() {
		// TODO Auto-generated method stub
		_jobDateTime = (TextView) findViewById(R.id.trip_timing);
		_fromAddress = (EllipsizingTextView) findViewById(R.id.from_address);
		_fromAddress.setMaxLines(4);
		_toAddress = (TextView) findViewById(R.id.to_address);
		_meterFare = (TextView) findViewById(R.id.meter_fare);
		_baseFee = (TextView) findViewById(R.id.base_fee);
		_serviceFee = (TextView) findViewById(R.id.service_fee);
		_serviceCredit = (TextView) findViewById(R.id.service_credit);
		_passengerPaid = (TextView) findViewById(R.id.passenger_payment);
		_settlingAmount = (TextView) findViewById(R.id.settlement_fee);
		_ccShare = (TextView) findViewById(R.id.cc_share);
		_pointsRevenue = (TextView) findViewById(R.id.points_revenue);
		// Layouts
		_settlmentDetail = (LinearLayout) findViewById(R.id.settlement_detail);
		_ccShareDetail = (LinearLayout) findViewById(R.id.cc_share_detail);
		_pointsRevenueDetail = (LinearLayout) findViewById(R.id.points_revenue_detail);
		_shareHeading = (TextView) findViewById(R.id.cc_share_label);
	}

	private void callPaymentDetailFetchApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGPaymentDetailApi paymentDetailApi = new IGPaymentDetailApi(this,
					this);
			paymentDetailApi.getPaymentDetail(IngogoApp.getSharedApplication()
					.getUserId(), IngogoApp.getSharedApplication()
					.getPassword(), _bookingId);
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
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

	@Override
	public void paymentDetailFetchingCompleted(
			IGPaymentDetailModel paymentDetail) {
		setupViews(paymentDetail);

		IGUtility.dismissProgressDialog(_progressDialog);
	}

	private void setupViews(IGPaymentDetailModel paymentDetail) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy - hh:mm a");
		Date dat = null;
		try {
			dat = IGUtility.getDateFromTimeStamp(paymentDetail.getWhen());

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		_jobDateTime.setText(sdf.format(dat));

		_fromAddress.setText(paymentDetail.getPickupAddress() + " , "
				+ paymentDetail.getPickupSuburb());

		_toAddress.setText(paymentDetail.getDropoffSuburb());

		if (paymentDetail.getBookingStatus().trim()
				.equalsIgnoreCase(IGConstants.kCompletedOffline)) {
			_meterFare.setText(IGConstants.driverOffline);
			_passengerPaid.setText(IGConstants.driverOffline);
		} else {
			setAmount(_meterFare, paymentDetail.getMeterAmount());
			setAmount(_passengerPaid, paymentDetail.getPassengerPaid());
		}
		setAmount(_baseFee, paymentDetail.getBaseFee());
		setAmount(_serviceFee, paymentDetail.getServiceFee());
		setAmount(_serviceCredit, paymentDetail.getServiceCredit());
		setAmount(_settlingAmount, paymentDetail.getSettlingAmount());
		setAmount(_ccShare, paymentDetail.getShareOfCreditCardFees());
		setAmount(_pointsRevenue, paymentDetail.getPointRevenue());

		/**
		 * Change Share CC label for payment done by corporate account.
		 */
		if(paymentDetail.isPaidByCorporateAccount()) {
			_shareHeading.setText(getResources().getString(R.string.payment_history_detail_surcharge_share_label));
		}
		// TODO: Don't show payment details if the passenger has paid offline.
		if (paymentDetail.getPaidOffline()) {
			_ccShareDetail.setVisibility(View.INVISIBLE);
			_pointsRevenueDetail.setVisibility(View.INVISIBLE);
			_settlmentDetail.setVisibility(View.INVISIBLE);
		}

	}

	@Override
	public void paymentDetailFetchingFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialogOk("", errorMessage, this);

	}

	private void setAmount(TextView textView, String amount) {

		if (null == amount || amount.contains("null")) {
			textView.setText(IGConstants.zeroBalance);
		} else {
			if (amount.contains("-")) {
				String amt = amount.replaceAll("-", "");
				textView.setText("$ "
						+ "("
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amt)) + ")");
			} else {
				textView.setText("$ "
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amount)));
			}
		}
	}

}
