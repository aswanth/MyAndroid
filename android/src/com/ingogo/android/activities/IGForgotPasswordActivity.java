/*
 * Package Name : com.ingogo.android.activities
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : This activity displays forgot password Screen, which is accessible from the login screen.
 */

package com.ingogo.android.activities;

import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGResetPasswordApi;

public class IGForgotPasswordActivity extends IGBaseActivity {

	private String _userId = null;
	private String _passphrase = null;
	private EditText _newPasswordTextBox;
	private EditText _passPhraseTextBox;
	private ScrollView _scrollView;
	private String _passwordString = null;
	private ImageButton _updateButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.forgot_password);
		initViews();
		setUpViews();
		// Data from the intent launcher activity,passed through the intent. 
		_passphrase = (String) getIntent().getCharSequenceExtra(
				IGConstants.kPassphrase);
		_userId = (String) getIntent()
				.getCharSequenceExtra(IGConstants.kUserId);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// If pass phrase is not null,then the pass phrase editText is set with
		// the given data and disable the editText.
		if (_passphrase != null) {
			_passPhraseTextBox.setText(_passphrase);
			_passPhraseTextBox.setEnabled(false);
		}
	}

	/**
	 * Click on the update button is received in this method.
	 * 
	 * @param view
	 */
	public void onUpdateButtonClick(View view) {
		callResetPasswordApi();
	}

	/**
	 * Initialise the views
	 */
	private void initViews() {
		_newPasswordTextBox = (EditText) findViewById(R.id.passwordchangeTextField);
		_passPhraseTextBox = (EditText) findViewById(R.id.passPhraseEditTextField);
		_scrollView = (ScrollView) findViewById(R.id.scrollForPasswordPage);
		_updateButton = (ImageButton) findViewById(R.id.updateButton);
		// Initially disable the update button.
		_updateButton.setEnabled(false);
	}

	/**
	 * To call reset password web service.
	 */
	private void callResetPasswordApi() {

		if (IGUtility.isNetworkAvailable(this)) {
			IGResetPasswordApi resetPassApi = new IGResetPasswordApi(this);
			resetPassApi.requestNewPassword(_passPhraseTextBox.getText()
					.toString(), _newPasswordTextBox.getText().toString(),
					_userId);
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * Set up the views
	 */
	private void setUpViews() {

		/**
		 * Disable Scrolling by setting up an OnTouchListener to do nothing
		 * */
		_scrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		/**
		 * Any text change in password field is caught by this listener
		 * */

		_newPasswordTextBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				updateButtonState();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}
		});

	}

	/**
	 * Update button enable or disable based on password validation.
	 */
	private void updateButtonState() {
		if (validatePasswordField())
			_updateButton.setEnabled(true);
		else
			_updateButton.setEnabled(false);

	}

	/**
	 * Function to go to login page
	 */
	private void startLoginActivity() {
		Intent intent = new Intent(this, IGSignupActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);

	}

	/**
	 * Create dialog to ensure that password has been changed.On clicking 'OK'
	 * button to go to login activity.
	 */
	private void displaySuccessfulResponse() {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(getString(R.string.change_password_alert_message));
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				startLoginActivity();
				finish();
			}
		});
		alertbox.show();
	}

	/**
	 * Successful response got in this method by calling reset password web
	 * service.
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		Log.d("Reset Acc", response.toString());
		super.onResponseReceived(response, apiID);

		if (response != null) {
			displaySuccessfulResponse();
		}

	}

	/**
	 * Failure response got in this method by calling reset password web
	 * service.
	 */
	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		Log.e("Reset-Password", errorResponse.toString());
		super.onFailedToGetResponse(errorResponse, apiID);
	}

	/**
	 * 
	 * @return password validation
	 */
	private boolean validatePasswordField() {
		_passwordString = _newPasswordTextBox.getText().toString();
		if (_passwordString.contains(" ") || _passwordString.length() < 4
				|| _passwordString.length() > 10)
			return false;
		else
			return true;
	}
}
