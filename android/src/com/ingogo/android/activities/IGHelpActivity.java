/*
 * Package Name : com.ingogo.android.activities
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : This activity displays help Screen, which is accessible from all other activities.
 */

package com.ingogo.android.activities;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGCustomDialog;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGSupportApi;

public class IGHelpActivity extends IGBaseActivity {
	private TextView _callTextView;
	private TextView _smsTextView;
	private TextView _emailTextView;
	private TextView _callPromptView;
	private TextView _smsPromptView;
	private TextView _emailPromptView;
	private ProgressDialog _progressDialog;
	private ImageButton _reportAnIssueButton;
	private EditText _mobileNumberText;
	private Button _continueBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.help);
		initViews();
		setupViews();
		callSupportApi();
	}
	
	private void setupViews() {
		_reportAnIssueButton.setVisibility(View.VISIBLE);
	
	}
	
	public void onClickReportIssueButton(View v) {
		if (IngogoApp.getSharedApplication().isLoggedIn()) {
			Intent reportAnIssueIntent =  new Intent(this, IGReportAnIssueActivity.class);
			startActivity(reportAnIssueIntent);		
		} else {
			showMobileNumberNeededPopUp();

		}

	}
	
	/**
	 * Shows a custom pop up dialog with an option to enter the mobileNumber.
	 */
	private void showMobileNumberNeededPopUp() {

		IGCustomDialog.Builder customBuilder = new IGCustomDialog.Builder(this);
		customBuilder.setTitle("Mobile Number");
		_dialog = customBuilder.create(R.layout.verify_mobilenumber);
		_dialog.setCancelable(true);
		_dialog.show();

		_continueBtn = (Button) _dialog.findViewById(R.id.continueBtn);
		_mobileNumberText = (EditText) _dialog.findViewById(R.id.mobileEditText);

		_mobileNumberText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				Log.i("PWD ENTERD", "" + _mobileNumberText.getText().toString());
				continueButtonState();

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});

	}
	
	/**
	 * Function to execute the continue button action. Continue btn of the
	 * verify password pop up.
	 * 
	 * @param view
	 */
	public void continueBtnClicked(View view) {

		String mobileNumber = _mobileNumberText.getText().toString();
		Log.i("_mobileNumberText", "" + mobileNumber);

		Intent reportAnIssueIntent =  new Intent(IGHelpActivity.this, IGReportAnIssueActivity.class);
		reportAnIssueIntent.putExtra("mobileNumber", mobileNumber);
		startActivity(reportAnIssueIntent);	
		_dialog.cancel();



	}
	
	/**
	 * Function to determine the states of the continue button of the verify
	 * password pop up
	 */
	private void continueButtonState() {
		if (validateFields()) {
			_continueBtn.setEnabled(true);
		} else {
			_continueBtn.setEnabled(false);
		}
	}
	
	/**
	 * Used for validating userIdField and passwordField
	 * 
	 * @return
	 */
	private boolean validateFields() {
		String _userIdString = _mobileNumberText.getText().toString().trim();
		if (_userIdString.equals("")) {
			return false;
		} else {
			if (_userIdString.length() == 10 || _userIdString.length() == 12) {
				Pattern p = null;
				if (_userIdString.length() == 10) {
					p = Pattern.compile("[0-9]*");
				} else {
					p = Pattern.compile("[+][1-9][0-9]*");
				}
				Matcher m = p.matcher(_userIdString);
				if (m.matches() == false) {
					return false;
				} 
				return true;
			}

			return false;

		}
	}

	/**
	 * To call supportApi web service to get the details for CALL,SMS and EMAIL.
	 */
	private void callSupportApi() {

		// If network is available,call supportApi. Otherwise prompt an alert
		// indicated that there is no network.
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGSupportApi supportApi = new IGSupportApi(this);
			supportApi.getSupport();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * Function to initiate different Views.
	 */

	private void initViews() {
		_callTextView = (TextView) findViewById(R.id.CalltextView);
		_smsTextView = (TextView) findViewById(R.id.SMStextView);
		_emailTextView = (TextView) findViewById(R.id.EmailtextView);
		_callPromptView = (TextView) findViewById(R.id.support_phone_prompt);
		_smsPromptView = (TextView) findViewById(R.id.support_sms_prompt);
		_emailPromptView = (TextView) findViewById(R.id.support_email_prompt);
		_reportAnIssueButton = (ImageButton) findViewById(R.id.reportAnIssueButton);

	}

	/**
	 * Used to set the details for CALL,SMS,EMAIL that got from the web service
	 * 
	 * @param responseObj
	 */
	private void applyJSONResponse(JSONObject responseObj) {
		try {
			JSONArray supportArray = responseObj
					.getJSONArray(IGApiConstants.kSupport);
			JSONObject supportObj = (JSONObject) supportArray.get(0);
			_callTextView.setText(supportObj
					.getString(IGApiConstants.kPhoneInfo));
			_smsTextView.setText(supportObj.getString(IGApiConstants.kSmsInfo));
			_emailTextView.setText(supportObj
					.getString(IGApiConstants.kEmailInfo));
			_callPromptView.setText(supportObj
					.getString(IGApiConstants.kPhonePrompt));
			_smsPromptView.setText(supportObj
					.getString(IGApiConstants.kSmsPrompt));
			_emailPromptView.setText(supportObj
					.getString(IGApiConstants.kEmailPrompt));

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setMessage("Server Error");
			alertbox.setNeutralButton("Ok",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					});
		}

	}

	/**
	 * Successful response from web service is got in this method.
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {

		super.onResponseReceived(response, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (response != null) {
			applyJSONResponse((JSONObject) response.get(IGConstants.kDataKey));
		}

	}

	/**
	 * Any failure response from web service is got in this method.
	 */

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onFailedToGetResponse(errorResponse, apiID);

	}

	@Override
	protected void onPause() {
		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;
		super.onPause();
		finish();
	}

}
