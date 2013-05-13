package com.ingogo.android.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGContactInfoModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGMaintainContactInfoApi;
import com.ingogo.android.webservices.IGRetrieveContactInfoApi;
import com.ingogo.android.webservices.interfaces.IGMaintainContactInfoApiListener;
import com.ingogo.android.webservices.interfaces.IGRetreiveContactInfoApiListener;
import com.qburst.utilities.emailvalidator.EmailValidator;

public class IGAddressDetailsActivity extends IGBaseActivity implements
		IGRetreiveContactInfoApiListener, IGMaintainContactInfoApiListener {

	private ProgressDialog _progressDialog;
	private EditText _streetNoET, _streetNameET, _suburbET, _stateET,
			_postcodeET, _emailET;
	ImageButton _saveBtn;
	private static IGContactInfoModel contactInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.address_detail);
		initViews();

		if (contactInfo == null) {
			_progressDialog = IGUtility
					.showProgressDialog(IGAddressDetailsActivity.this);
			IGRetrieveContactInfoApi retrieveContacts = new IGRetrieveContactInfoApi(
					this, this);
			retrieveContacts.retreiveContactInfo(null);
		} else {
			setUpViews(contactInfo);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IGUpdatePositionPollingTask.ignoreStaleState = true;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;
	}

	
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		clearViews();
		if (contactInfo == null) {
			_progressDialog = IGUtility
					.showProgressDialog(IGAddressDetailsActivity.this);
			IGRetrieveContactInfoApi retrieveContacts = new IGRetrieveContactInfoApi(
					this, this);
			retrieveContacts.retreiveContactInfo(null);
		} else {
			setUpViews(contactInfo);
		}
	}

	/**
	 * Initialise the edit texts and the strings.
	 */
	private void initViews() {

		_streetNoET = (EditText) findViewById(R.id.streetNoTextField);
		_streetNameET = (EditText) findViewById(R.id.streetNameTextField);
		_suburbET = (EditText) findViewById(R.id.suburbTextField);
		_stateET = (EditText) findViewById(R.id.stateTextField);
		_postcodeET = (EditText) findViewById(R.id.postCodeTextField);
		_emailET = (EditText) findViewById(R.id.emailTextField);
		_saveBtn = (ImageButton) findViewById(R.id.save_button);
		_saveBtn.setEnabled(false);
		
		_streetNoET.addTextChangedListener(new EnterAddressTextWatcher());
		_streetNameET.addTextChangedListener(new EnterAddressTextWatcher());
		_suburbET.addTextChangedListener(new EnterAddressTextWatcher());
		_stateET.addTextChangedListener(new EnterAddressTextWatcher());
		_postcodeET.addTextChangedListener(new EnterAddressTextWatcher());
		_emailET.addTextChangedListener(new EnterAddressTextWatcher());
		
		_suburbET.setOnEditorActionListener(
		        new EditText.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_NEXT) {
		        	_stateET.requestFocus();
		            return true;
		        }
		        return false;
		    }
		});
		
		_stateET.setOnEditorActionListener(
		        new EditText.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_NEXT) {
		        	_postcodeET.requestFocus();
		            return true;
		        }
		        return false;
		    }
		});

	}
	
	/**
	 * Text Watcher to enable/disable the done button
	 */
	private class EnterAddressTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			doneButtonState();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}
	
	/**
	 * Enable /Disable done button based on validation of views
	 */
	private void doneButtonState() {
		
		if(_streetNoET.getText().toString().trim().length() == 0 
				|| _streetNameET.getText().toString().trim().length() == 0
				|| _suburbET.getText().toString().trim().length() == 0
				|| _stateET.getText().toString().trim().length() == 0
				|| _postcodeET.getText().toString().trim().length() == 0
				|| _emailET.getText().toString().trim().length() == 0){
			_saveBtn.setEnabled(false);
		} else {
			_saveBtn.setEnabled(true);
		}
		
	}

	private void setUpViews(IGContactInfoModel contactInfo) {

		_streetNoET.setText(contactInfo.getAddressLine1());
		_streetNameET.setText(contactInfo.getAddressLine2());
		_suburbET.setText(contactInfo.getSuburb());
		_stateET.setText(contactInfo.getState());
		_postcodeET.setText(contactInfo.getPostcode());
		_emailET.setText(contactInfo.getEmailAddress());

		doneButtonState();
	}
	
	private void clearViews() {

		_streetNoET.setText("");
		_streetNameET.setText("");
		_suburbET.setText("");
		_stateET.setText("");
		_postcodeET.setText("");
		_emailET.setText("");

		doneButtonState();
	}

	/**
	 * OnclickListener for the Submit Button
	 * 
	 * @param view
	 */
	public void saveButtonClicked(View view) {

		if (EmailValidator.isValid(_emailET.getText().toString())) {
			
			Dialog dlg = new AlertDialog.Builder(this).setTitle(this.getText(R.string.confirm_title))
					.setMessage(this.getText(R.string.confirm_message))
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							saveAction();
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							
						}
					}).create();
			dlg.setCancelable(false);
			dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			dlg.show();
			
		} else {
			IGUtility.showDialogOk("Error", this.getText(R.string.email_error)
					.toString(), this);
		}

	}

	public void saveAction() {

		if (IGUtility.isNetworkAvailable(this)) {
			_saveBtn.setEnabled(false);

			contactInfo = new IGContactInfoModel();
			contactInfo.setAddressLine1(_streetNoET.getText().toString());
			contactInfo.setAddressLine2(_streetNameET.getText().toString());
			contactInfo.setSuburb(_suburbET.getText().toString());
			contactInfo.setState(_stateET.getText().toString());
			contactInfo.setPostcode(_postcodeET.getText().toString());
			contactInfo.setEmailAddress(_emailET.getText().toString());

			_progressDialog = IGUtility
					.showProgressDialog(IGAddressDetailsActivity.this);
			IGMaintainContactInfoApi retrieveContacts = new IGMaintainContactInfoApi(
					this, this);
			retrieveContacts.maintainContactInfo(contactInfo);

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	@Override
	public void retreiveContactInfoCompleted(IGContactInfoModel contactInfo) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);

		if (contactInfo != null)
			setUpViews(contactInfo);
	}

	@Override
	public void retreiveContactInfoFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);

		IGUtility.showDialogOk("Error", errorMessage, this);
	}

	@Override
	public void maintainContactInfoCompleted(
			IGContactInfoModel contactInformation) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		contactInfo = null;
		finish();
	}

	@Override
	public void maintainContactInfoFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialogOk("Error", errorMessage, this);
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

		contactInfo = new IGContactInfoModel();
		contactInfo.setAddressLine1(_streetNoET.getText().toString());
		contactInfo.setAddressLine2(_streetNameET.getText().toString());
		contactInfo.setSuburb(_suburbET.getText().toString());
		contactInfo.setState(_stateET.getText().toString());
		contactInfo.setPostcode(_postcodeET.getText().toString());
		contactInfo.setEmailAddress(_emailET.getText().toString());

		Intent intent = getIntent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		contactInfo = null;
		finish();
	}
	
	public static void clearContactInfo() {
		contactInfo = null;
	}

}
