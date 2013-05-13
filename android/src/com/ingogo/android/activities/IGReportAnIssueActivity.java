package com.ingogo.android.activities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ingogo.android.R;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLogCache;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGIssueReasonsApi;
import com.ingogo.android.webservices.IGRegisterIssueApi;
import com.ingogo.android.webservices.IGSendDiagnosticsApi;
import com.ingogo.android.webservices.beans.response.IGIssueReasonsResponseBean;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGIssueReasonsApiListener;
import com.ingogo.android.webservices.interfaces.IGRegisterIssueApiListener;
import com.ingogo.android.webservices.interfaces.IGSendDiagnosticsListener;

public class IGReportAnIssueActivity extends IGBaseActivity implements
		IGExceptionApiListener, IGRegisterIssueApiListener,
		IGIssueReasonsApiListener, IGSendDiagnosticsListener {
	RadioGroup _issueRadioGroup;
	private ProgressDialog _progressDialog;
	EditText _issueDescriptionEditText;
	private HashMap<String, String> _reasons;
	Dialog _successDialog;
	private ProgressDialog _diagnosticProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.report_an_issue);
		initViews();
		callGetIssueListApi();
	}

	private void initViews() {
		_issueRadioGroup = (RadioGroup) findViewById(R.id.issueSelector);
		_issueDescriptionEditText = (EditText) findViewById(R.id.issue_note_edit_text);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.getString("description") != null) {
				_issueDescriptionEditText.setText(getIntent().getExtras()
						.getString("description"));

			}
		}
	}

	/**
	 * Add radio buttons with text - titleText and tag
	 * 
	 * @param titleText
	 * @param tag
	 */
	private void addRadioButtons(String titleText, int tag) {

		RadioButton rdbtn = new RadioButton(this);
		if (IngogoApp.getThemeID() == 1) {
			rdbtn.setTextColor(R.color.grey_color);

		} else {
			rdbtn.setTextColor(Color.rgb(255, 255, 255));

		}
		rdbtn.setButtonDrawable(R.drawable.radio_btn_selector);
		rdbtn.setId(tag);
		rdbtn.setTag(tag);

		rdbtn.setText(titleText);
		_issueRadioGroup.addView(rdbtn);
		updateTheSelectedChoice(tag);
	}

	/**
	 * check the box previously used or the last check box
	 * 
	 * @param tag
	 */
	private void updateTheSelectedChoice(int tag) {
		if (tag == _reasons.size() - 1) {

			try {
				_issueRadioGroup.check(getIntent().getExtras().getInt(
						"selection", tag));
			} catch (Exception e) {
				_issueRadioGroup.check(tag);
			}

		}
	}

	/**
	 * Return the reason key corresponding to the one selected
	 * 
	 * @return the key
	 */
	private String getSelectedCode() {

		try {
			int selectedLocation = _issueRadioGroup.getCheckedRadioButtonId();
			Iterator myVeryOwnIterator = _reasons.keySet().iterator();

			int i = 0;
			while (myVeryOwnIterator.hasNext()) {
				String key = (String) myVeryOwnIterator.next();
				if (i == selectedLocation) {
					return key;
				}
				i++;
			}
		} catch (NullPointerException e) {

		}
		return "";
	}

	public void onClickSendButton(View v) {
		Log.e("SELECTED INDEX = ", "GET SELECTED CODE = " + getSelectedCode());
		callReportIssueApi();
	}

	private boolean checkWhetherTheExtraIsNull(Intent intent, String key) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			if (extras.getString(key) != null) {
				return true;

			} else {
				return false;

			}
		}
		return false;

	}

	private String getStringExtra(Intent intent, String key) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			return extras.getString(key);
		}
		return null;
	}

	/**
	 * Call the api to retrieve the reason list to be populated as check boxes
	 */
	private void callGetIssueListApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGIssueReasonsApi api = new IGIssueReasonsApi(this, this);
			if (checkWhetherTheExtraIsNull(getIntent(), "mobileNumber")) {
				api.retrieveReasonsList(getStringExtra(getIntent(),
						"mobileNumber"));
			} else {
				api.retrieveReasonsList();
			}
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * call the api to update the issue based on the issue description entered
	 * and the type chosen on the check box
	 */
	private void callReportIssueApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGRegisterIssueApi api = new IGRegisterIssueApi(this, this);
			if (checkWhetherTheExtraIsNull(getIntent(), "mobileNumber")) {
				api.registerIssue(getStringExtra(getIntent(), "mobileNumber"),
						getSelectedCode(), _issueDescriptionEditText.getText()
								.toString());

			} else {
				api.registerIssue(getSelectedCode(), _issueDescriptionEditText
						.getText().toString());
			}
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * Button action to change the current theme If the current theme is day
	 * then set night as current theme and restart the activity. If the current
	 * theme is night then set day as current theme and restart the activity.
	 * 
	 * @param view
	 */
	public void changeTheme(View view) {
		if (IngogoApp.getThemeID() == 1) {
			IngogoApp.setThemeID(2);
		} else {
			IngogoApp.setThemeID(1);
		}

		restartActivty();
	}

	/**
	 * Function to restart the activity to apply the new theme.
	 */
	private void restartActivty() {

		Intent intent = getIntent();
		intent.putExtra("selection", _issueRadioGroup.getCheckedRadioButtonId());
		intent.putExtra("description", _issueDescriptionEditText.getText()
				.toString());
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	@Override
	public void retrieveReasonsCompleted(IGIssueReasonsResponseBean issueDetails) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_issueRadioGroup.removeAllViews();

		_reasons = issueDetails.getReasons();
		Iterator myVeryOwnIterator = _reasons.keySet().iterator();
		int i = 0;
		while (myVeryOwnIterator.hasNext()) {
			String key = (String) myVeryOwnIterator.next();
			String value = (String) _reasons.get(key);
			addRadioButtons(value, i);
			i++;
		}

	}

	@Override
	public void retrieveReasonsFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		Log.e("retrieveReasonsFailed", errorMessage);
		IGUtility.showDialogOk("", errorMessage, this);
		_issueRadioGroup.removeAllViews();

	}

	@Override
	public void registerIssueCompleted() {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialogOkWithGoBack("", IGReportAnIssueActivity.this
				.getText(R.string.report_an_issue_success_message).toString(),
				IGReportAnIssueActivity.this);

	}

	@Override
	public void registerIssueFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		Log.e("registerIssueFailed", errorMessage);
		IGUtility.showDialogOk("", errorMessage, this);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialog(
				"",
				IGReportAnIssueActivity.this.getText(
						R.string.report_an_issue_failure_message).toString(),
				IGReportAnIssueActivity.this);

	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialog(
				"",
				IGReportAnIssueActivity.this.getText(
						R.string.report_an_issue_failure_message).toString(),
				IGReportAnIssueActivity.this);

	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialog(
				"",
				IGReportAnIssueActivity.this.getText(
						R.string.report_an_issue_failure_message).toString(),
				IGReportAnIssueActivity.this);

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialog(
				"",
				IGReportAnIssueActivity.this.getText(
						R.string.report_an_issue_failure_message).toString(),
				IGReportAnIssueActivity.this);

	}

	public void onDiagnosticButtonClicked(View view) {
		invokeDiagnosticApi();
	}

	private void invokeDiagnosticApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			if (_diagnosticProgressDialog != null
					&& _diagnosticProgressDialog.isShowing()) {
				return;
			}
			_diagnosticProgressDialog = IGUtility
					.showProgressDialogWithMsg(
							this,
							"Sending diagnostics file to ingogo support. This may take a few minutes, please be patient...");
			IGSendDiagnosticsApi api = new IGSendDiagnosticsApi(this, this);
			if (checkWhetherTheExtraIsNull(getIntent(), "mobileNumber")) {
				api.sendDiagnostics(getStringExtra(getIntent(), "mobileNumber"));
			} else {
				api.sendDiagnostics(null);
			}

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	@Override
	public void successfullyySendDiagnostics(String mobileNumber) {
		IGUtility.dismissProgressDialog(_diagnosticProgressDialog);
		// If success, remove all cached logs. Don't need to remove the cached
		// files.
		QLogCache.getSharedInstance().removeAllCachedLog(mobileNumber);
		if (!isFinishing()) {
			IGUtility.showDialogOk("", "Diagnostics sent successfully.", this);
		}

	}

	@Override
	public void failedToSendDiagnostics(String errorMessage) {
		IGUtility.dismissProgressDialog(_diagnosticProgressDialog);
		if (!isFinishing()) {
			IGUtility.showDialogOk("", "Failed to send diagnostics.", this);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.dismissProgressDialog(_diagnosticProgressDialog);
	}

}
