package com.ingogo.android.activities.payments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;

public class IGPaymentSucessActivity extends IGBaseActivity {

	Button _done;
	TextView _totalPrice, _successText;
	TextView _paymentChargeView;
	String _gettotalprice, _getSuccessString;
	String _jobId;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_completion);

		getBundleExtras();
		initWithViews();

		_totalPrice.setText("$" + _gettotalprice);

		if (Double.parseDouble(_gettotalprice) == 0) {

			_paymentChargeView.setVisibility(View.INVISIBLE);
			_successText.setText(getResources().getString(
					R.string.payment_charge_with_no_due));

		} else {
			_paymentChargeView.setVisibility(View.VISIBLE);

			_successText.setText(_getSuccessString);
		}

		_done = (Button) findViewById(R.id.bDone);
		_done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent _gotoJobList = new Intent(IGPaymentSucessActivity.this,
						IGJobsActivity.class);
				startActivity(_gotoJobList);
			}
		});
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

	private void initWithViews() {
		_totalPrice = (TextView) findViewById(R.id.paidText);
		_successText = (TextView) findViewById(R.id.paymentSuccessfulText);
		_paymentChargeView = (TextView) findViewById(R.id.paymentChargeText);
	}

	private void getBundleExtras() {
		_gettotalprice = getIntent().getStringExtra(IGConstants.kPaymentDue);
		_getSuccessString = getIntent().getStringExtra(
				IGConstants.kPaymentSuccess);
		_jobId = getIntent().getStringExtra(IGConstants.kJobId);
	}

	public void onBackPressed() {
		return;
	}

}
