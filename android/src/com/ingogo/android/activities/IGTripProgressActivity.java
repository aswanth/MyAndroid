package com.ingogo.android.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.model.IGEarningTargetModel;
import com.ingogo.android.model.IGJobTargetModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.beans.response.IGTargetProgressResponseBean;

public class IGTripProgressActivity extends IGBaseActivity {

	private IGTargetProgressResponseBean _targetProgress;
	private TextView _commissionHeading;
	private ProgressBar _jobProgressbar, _earningsProgressBar;

	private TextView _jobTargetMinTV, _jobTargetHalfTV, _jobTargetFullTV,
			_jobTargetQuarterTV, _jobTargetThreeQuarterTV;
	private TextView _jobTargetCommissionMinTV, _jobTargetCommissionHalfTV,
			_jobTargetCommissionFullTV, _jobTargetCommissionThreeQuarterTV,
			_jobTargetCommissionQuarterTV;

	private TextView _earningsMinTV, _earningsHalfTV, _earningsFullTV,
			_earningsQuarterTV, _earningsThreeQuarterTV;

	private TextView _earningsCommissionMinTV, _earningsCommissionHalfTV,
			_earningsCommissionFullTV, _earningsCommissionQuarterTV,
			_earningsCommissionThreeQuarterTV;

	private TextView _earningsValueTV, _jobCompletedTV, _nextCommissionTV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_progress);
		getBundleExtras();
		initViews();
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

	/**
	 * To initialise all the view used in this class.
	 */

	private void initViews() {

		_jobProgressbar = (ProgressBar) findViewById(R.id.jobProgressBar);
		_earningsProgressBar = (ProgressBar) findViewById(R.id.moneyProgressBar);
		_commissionHeading = (TextView) findViewById(R.id.commissionHeading);

		_earningsFullTV = (TextView) findViewById(R.id.earningFullTV);
		_earningsThreeQuarterTV = (TextView) findViewById(R.id.earningThreeQuarterTV);
		_earningsHalfTV = (TextView) findViewById(R.id.earningHalfTV);
		_earningsQuarterTV = (TextView) findViewById(R.id.earningQuarterTV);
		_earningsMinTV = (TextView) findViewById(R.id.earningMinTV);

		_earningsCommissionFullTV = (TextView) findViewById(R.id.earningCommissionFullTV);
		_earningsCommissionHalfTV = (TextView) findViewById(R.id.earningCommissionHalfTV);
		_earningsCommissionMinTV = (TextView) findViewById(R.id.earningCommissionMinTV);
		_earningsCommissionQuarterTV = (TextView) findViewById(R.id.earningCommissionQuarterTV);
		_earningsCommissionThreeQuarterTV = (TextView) findViewById(R.id.earningCommissionThreeQuarterTV);

		_jobTargetFullTV = (TextView) findViewById(R.id.jobFullTV);
		_jobTargetHalfTV = (TextView) findViewById(R.id.jobHalfTV);
		_jobTargetMinTV = (TextView) findViewById(R.id.jobMinTV);
		_jobTargetQuarterTV = (TextView) findViewById(R.id.jobQuarterTV);
		_jobTargetThreeQuarterTV = (TextView) findViewById(R.id.jobThreeQuarterTV);

		_jobTargetCommissionFullTV = (TextView) findViewById(R.id.jobCommissionFullTV);
		_jobTargetCommissionHalfTV = (TextView) findViewById(R.id.jobCommissionHalfTV);
		_jobTargetCommissionMinTV = (TextView) findViewById(R.id.jobCommissionMinTV);
		_jobTargetCommissionQuarterTV = (TextView) findViewById(R.id.jobCommissionQuarterTV);
		_jobTargetCommissionThreeQuarterTV = (TextView) findViewById(R.id.jobCommissionThreeQuarterTV);

		_earningsValueTV = (TextView) findViewById(R.id.earningText);
		_jobCompletedTV = (TextView) findViewById(R.id.completedJobsValue);
		_nextCommissionTV = (TextView) findViewById(R.id.nextCommissionTV);

	}

	/**
	 * To Set up all the views used in the class.
	 */
	private void setUpViews() {
		_commissionHeading.setText(getCommissionHeading());
		_nextCommissionTV.setText(getNextMonthCommission()+"%");
		plotJobTargetValues();
		plotEarningTargetValues();
		_jobProgressbar.setProgress(_targetProgress.getJobsDisplayPercentile());
		_earningsProgressBar.setProgress(_targetProgress
				.getPaymentsDisplayPercentile());
		_earningsValueTV.setText("$"
				+ IGUtility.getInPriceFormat(_targetProgress
						.getPaymentsThisMonth()) + " ");
		_jobCompletedTV.setText(_targetProgress.getJobsCompletedThisMonth()
				+ " ");

	}

	/**
	 * To get the next month commission
	 * 
	 * @return
	 */
	private String getNextMonthCommission() {
		if (_targetProgress.getJobsDisplayPercentile() >= _targetProgress
				.getPaymentsDisplayPercentile()) {
			return IGUtility.getInPriceFormat(getCurrentJobCommission());
		} else {
			return IGUtility.getInPriceFormat(getCurrentPaymentCommission());
		}
	}

	private String getCurrentPaymentCommission() {

		ArrayList<IGEarningTargetModel> earningTargetsArray = _targetProgress
				.getEarningsTarget();
		int currentDisplayPercentile = _targetProgress
				.getPaymentsDisplayPercentile();
		int diff = 100;
		String commission = "0";
		for (IGEarningTargetModel earningTarget : earningTargetsArray) {
			if (currentDisplayPercentile >= earningTarget.getDisplayPercentile()) {
				if (Math.abs(currentDisplayPercentile
						- earningTarget.getDisplayPercentile()) < diff) {
					diff = Math.abs(currentDisplayPercentile
							- earningTarget.getDisplayPercentile());
					commission = earningTarget.getCommissionPercentage();
				}
			}

		}
		return commission;
	}

	private String getCurrentJobCommission() {
		ArrayList<IGJobTargetModel> jobTargetsArray = _targetProgress
				.getJobsTargets();
		int currentDisplayPercentile = _targetProgress
				.getJobsDisplayPercentile();
		int diff = 100;
		String commission = "0";
		for (IGJobTargetModel jobTarget : jobTargetsArray) {
			if (currentDisplayPercentile >= jobTarget.getDisplayPercentile()) {
				if (Math.abs(currentDisplayPercentile
						- jobTarget.getDisplayPercentile()) < diff) {
					diff = Math.abs(currentDisplayPercentile
							- jobTarget.getDisplayPercentile());
					commission = jobTarget.getCommissionPercentage();
				}
			}

		}
		return commission;

	}

	/**
	 * plotting the earning values.
	 */
	private void plotEarningTargetValues() {
		ArrayList<IGEarningTargetModel> earningTargetsArray = _targetProgress
				.getEarningsTarget();
		for (IGEarningTargetModel earningTarget : earningTargetsArray) {
			TextView earningValueTV = getEarningTargetValueTextViewForDisplayPercentage(earningTarget
					.getDisplayPercentile());
			if (earningValueTV != null) {
				earningValueTV.setText("$" + earningTarget.getPayments());
			}
			TextView earningsCommissionTV = getEarningCommissionTextViewForDisplayPercentage(earningTarget
					.getDisplayPercentile());
			if (earningsCommissionTV != null) {
				earningsCommissionTV.setText(earningTarget
						.getCommissionPercentage() + "%");
			}
		}
	}

	/**
	 * for getting the Earning commission for plotting the commission values
	 * according to the display percentile.
	 * 
	 * @param displayPercentile
	 * @return
	 */
	private TextView getEarningCommissionTextViewForDisplayPercentage(
			int displayPercentile) {
		// TODO Auto-generated method stub
		switch (displayPercentile) {
		case 0:
			return _earningsCommissionMinTV;
		case 25:
			return _earningsCommissionQuarterTV;
		case 50:
			return _earningsCommissionHalfTV;
		case 75:
			return _earningsCommissionThreeQuarterTV;
		case 100:
			return _earningsCommissionFullTV;

		default:
			return null;
		}
	}

	/**
	 * For getting the Earning text view to plot according to the display
	 * percentile.
	 * 
	 * @param displayPercentile
	 * @return
	 */
	private TextView getEarningTargetValueTextViewForDisplayPercentage(
			int displayPercentile) {

		switch (displayPercentile) {
		case 0:
			return _earningsMinTV;
		case 25:
			return _earningsQuarterTV;
		case 50:
			return _earningsHalfTV;
		case 75:
			return _earningsThreeQuarterTV;
		case 100:
			return _earningsFullTV;

		default:
			return null;
		}
	}

	/**
	 * plot up job values steps for progress bar.
	 */
	private void plotJobTargetValues() {
		ArrayList<IGJobTargetModel> jobTargetsArray = _targetProgress
				.getJobsTargets();
		for (IGJobTargetModel jobTarget : jobTargetsArray) {
			TextView jobValueTV = getJobValueTextViewForDisplayPercentage(jobTarget
					.getDisplayPercentile());
			if (jobValueTV != null) {
				jobValueTV.setText(jobTarget.getJobsCompleted());
			}
			TextView jobCommissionTV = getJobCommissionTextViewForDisplayPercentage(jobTarget
					.getDisplayPercentile());
			if (jobCommissionTV != null) {
				jobCommissionTV.setText(jobTarget.getCommissionPercentage()
						+ "%");
			}
		}
	}

	/**
	 * get the job commission text view to plot the commission value according
	 * to display percentile.
	 * 
	 * @param displayPercentile
	 * @return
	 */
	private TextView getJobCommissionTextViewForDisplayPercentage(
			int displayPercentile) {
		switch (displayPercentile) {
		case 0:
			return _jobTargetCommissionMinTV;
		case 25:
			return _jobTargetCommissionQuarterTV;
		case 50:
			return _jobTargetCommissionHalfTV;
		case 75:
			return _jobTargetCommissionThreeQuarterTV;
		case 100:
			return _jobTargetCommissionFullTV;

		default:
			return null;
		}
	}

	/**
	 * get the job value text view to plot the the job value according to to
	 * display percentile.
	 * 
	 * @param displayPercentile
	 * @return
	 */
	private TextView getJobValueTextViewForDisplayPercentage(
			int displayPercentile) {
		switch (displayPercentile) {
		case 0:
			return _jobTargetMinTV;
		case 25:
			return _jobTargetQuarterTV;
		case 50:
			return _jobTargetHalfTV;
		case 75:
			return _jobTargetThreeQuarterTV;
		case 100:
			return _jobTargetFullTV;

		default:
			return null;
		}

	}

	/**
	 * To get the commission heading.
	 * 
	 * @return
	 */
	private String getCommissionHeading() {
		String commissionHeading = getString(R.string.commission_text);
		String commissionPercentage = "0";
		if (_targetProgress.getCurrentCommissionPercentage() == null
				|| _targetProgress.getCurrentCommissionPercentage()
						.equalsIgnoreCase("0")) {
			commissionPercentage = "0";
		}
		commissionPercentage = IGUtility.getInPriceFormat(_targetProgress
				.getCurrentCommissionPercentage());
		commissionHeading = commissionHeading + " " + commissionPercentage
				+ "%";
		return commissionHeading;
	}

	/**
	 * to the bundle extras.
	 */
	private void getBundleExtras() {
		_targetProgress = (IGTargetProgressResponseBean) getIntent()
				.getExtras().getSerializable(IGConstants.kTargetProgress);
	}

}
