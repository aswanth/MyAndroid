package com.ingogo.android.activities;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ingogo.android.R;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGAccountInfoApi;
import com.ingogo.android.webservices.IGUpdateAccountApi;

public class IGAccountInfoActivity extends IGBaseActivity {

	private EditText _accountName, _accountBSB, _accountNumber, _companyName,
			_abn;
	private ProgressDialog _progressDialog;
	private String _accountNameString, _accountNumberString, _bsbString,
			_companyNameString, _abnString;
	private ImageButton _saveButton;
	private long _bankAccountId = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.account_info);

		// Call the account info api to populate the account info page.
		callCreditDetailsApi();

		// Initialize the edit texts and strings.
		initViews();

		// Add validations for edit texts.
		validateTextFields();

	}

	@Override
	protected void onResume() {
		IGUpdatePositionPollingTask.ignoreStaleState = true;
		super.onResume();
	}

	/**
	 * Initialise the edit texts and the strings.
	 */
	private void initViews() {
		_accountName = (EditText) findViewById(R.id.accountNameTextField);
		_accountBSB = (EditText) findViewById(R.id.bsbTextField);
		_accountNumber = (EditText) findViewById(R.id.accountNoTextField);
		_companyName = (EditText) findViewById(R.id.companyNameTextField);
		_abn = (EditText) findViewById(R.id.ABNTextField);
		_saveButton = (ImageButton) findViewById(R.id.save_button);
		_saveButton.setEnabled(false);

		initializeStrings();

	}

	/**
	 * Initialize the strings as empty strings.
	 */
	private void initializeStrings() {
		// Initialize the strings.
		_accountNameString = "";
		_accountNumberString = "";
		_bsbString = "";
		_abnString = "";
		_companyNameString = "";
	}

	/**
	 * To call account info web service.
	 */
	private void callCreditDetailsApi() {

		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGAccountInfoApi accountInfo = new IGAccountInfoApi(this);
			accountInfo.getAccountDetails();

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * OnclickListener for the Submit Button
	 * 
	 * @param view
	 */
	public void saveButtonClicked(View view) {
		if (IGUtility.isNetworkAvailable(this)) {
			_saveButton.setEnabled(false);
			_progressDialog = IGUtility.showProgressDialog(this);
			_accountNameString = _accountName.getText().toString();
			_accountNumberString = _accountNumber.getText().toString();
			_bsbString = _accountBSB.getText().toString();
			_companyNameString = _companyName.getText().toString();
			_abnString = _abn.getText().toString();
			int accountNumberLength = _accountNumber.getText().toString()
					.length();
			if (accountNumberLength == 5) {
				_accountNumber.setText("0000" + _accountNumberString);
				_accountNumberString = _accountNumber.getText().toString();
			} else if (accountNumberLength == 6) {
				_accountNumber.setText("000" + _accountNumberString);
				_accountNumberString = _accountNumber.getText().toString();
			} else if (accountNumberLength == 7) {
				_accountNumber.setText("00" + _accountNumberString);
				_accountNumberString = _accountNumber.getText().toString();
			} else if (accountNumberLength == 8) {
				_accountNumber.setText("0" + _accountNumberString);
				_accountNumberString = _accountNumber.getText().toString();
			}

			IGUpdateAccountApi update = new IGUpdateAccountApi(this,
					_companyNameString, _abnString, _accountNameString,
					_accountNumberString, _bsbString,_bankAccountId);
			update.updateAccountInfo();

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * Function to filter text field entries.
	 */
	public void validateTextFields() {

		InputFilter company_name_filter[] = new InputFilter[2];
		InputFilter abn_filter[] = new InputFilter[2];
		InputFilter accnt_name_filter[] = new InputFilter[2];
		InputFilter accnt_bsb_filter[] = new InputFilter[2];
		InputFilter accnt_num_filter[] = new InputFilter[2];

		company_name_filter[0] = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {

				String destTxt = dest.toString();
				String resultingTxt = destTxt.substring(0, dstart)
						+ source.subSequence(start, end)
						+ destTxt.substring(dend);

				for (int i = start; i < end; i++) {
					if (!resultingTxt
							.matches("^[[0-9]*[a-z]*[A-Z]*\\s*\\�*\\.*\\<*\\+*\\�*\\�*"
									+ "\\(*\\&*\\!*\\$*\\)*\\;*\\-*\\/*\\|*\\,*\\%*\\_*\\>*\\?*"
									+ "\\:*\\#*\\@*\\'*\\=*\\\"*\\~*\\`*\\{*\\}*\\\\*\\�*]*$")) {
						return "";
					}
				}
				return null;
			}
		};
		company_name_filter[1] = new InputFilter.LengthFilter(50);

		abn_filter[0] = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (!Character.isDigit(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		abn_filter[1] = new InputFilter.LengthFilter(11);

		accnt_name_filter[0] = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {

				String destTxt = dest.toString();
				String resultingTxt = destTxt.substring(0, dstart)
						+ source.subSequence(start, end)
						+ destTxt.substring(dend);

				for (int i = start; i < end; i++) {
					if (!resultingTxt
							.matches("^[[0-9]*[a-z]*[A-Z]*\\s*\\�*\\.*\\<*\\+*"
									+ "\\(*\\&*\\!*\\$*\\)*\\;*\\-*\\/*\\|*\\,*\\%*\\_*\\>*\\?*"
									+ "\\:*\\#*\\@*\\'*\\=*\\\"*\\~*\\`*\\{*\\}*\\\\*\\�*]*$")) {
						return "";
					}
				}
				return null;
			}
		};
		accnt_name_filter[1] = new InputFilter.LengthFilter(32);

		accnt_bsb_filter[0] = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (!Character.isDigit(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		accnt_bsb_filter[1] = new InputFilter.LengthFilter(6);

		accnt_num_filter[0] = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (!Character.isDigit(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		accnt_num_filter[1] = new InputFilter.LengthFilter(9);

		_accountName.setFilters(accnt_name_filter);
		_accountBSB.setFilters(accnt_bsb_filter);
		_accountNumber.setFilters(accnt_num_filter);
		_companyName.setFilters(company_name_filter);
		_abn.setFilters(abn_filter);
		_accountName.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				saveButtonState();
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

		_accountBSB.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				saveButtonState();
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

		_accountNumber.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				saveButtonState();
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

		_companyName.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				saveButtonState();
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
		_abn.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				saveButtonState();
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
	 * Function to enable/disable save button
	 */
	private void saveButtonState() {
		if (_accountName.getText().toString().trim().length() != 0
				&& _accountBSB.getText().length() == 6
				&& _accountNumber.getText().length() >= 5
				&& _companyName.getText().toString().trim().length() != 0
				&& _abn.getText().length() == 11) {
			_saveButton.setEnabled(true);
		} else {
			_saveButton.setEnabled(false);
		}
	}

	private void setUpAccountInfo() {
		_accountName.setText(_accountNameString);
		_accountNumber.setText(_accountNumberString);
		_accountBSB.setText(_bsbString);
		_companyName.setText(_companyNameString);
		_abn.setText(_abnString);
	}

	/**
	 * Return the details of the given key from the given json object.
	 * 
	 * @param key
	 * @param accountInfo
	 * @return
	 */
	private String getDetailsFor(String key, JSONObject accountInfo) {
		if (accountInfo.has(key)) {

			// When the account details are not entered it
			// is returned as null string. So ensure that the returned
			// value is not the null string.
			try {
				if ((accountInfo.getString(key) != null)
						&& !(accountInfo.getString(key)
								.equals(IGConstants.kNull))) {
					return accountInfo.getString(key);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return "";
			}
		}
		return "";
	}

	private void showAlert() {
		Dialog dlg = new AlertDialog.Builder(this)
				.setMessage(
						getResources().getString(
								R.string.account_updated_message))
				.setPositiveButton(IGConstants.OKMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								finish();
							}
						}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (!isFinishing())
			dlg.show();
	}

	/**
	 * Successful response is received by this method, when calling web service
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		Log.w("Account info activity response ", "" + response);
		super.onResponseReceived(response, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (apiID == IGApiConstants.kAccountInfoWebServiceId) {
			Log.i("ACCOUNT INFO API RESPONSE", "" + response);
			if (response != null && response.containsKey(IGConstants.kDataKey)) {
				if ((JSONObject) response.get(IGConstants.kDataKey) != null) {
					JSONObject resp = (JSONObject) response
							.get(IGConstants.kDataKey);
					if ((resp != null)
							&& (resp.has(IGApiConstants.kJSONAccountInfoKey))) {
						try {

							JSONObject accountInfo = resp
									.getJSONObject(IGApiConstants.kJSONAccountInfoKey);

							if (accountInfo != null) {
								_accountNameString = getDetailsFor(
										IGApiConstants.kJSONAccountNameKey,
										accountInfo);
								_accountNumberString = getDetailsFor(
										IGApiConstants.kJSONAccountNumberKey,
										accountInfo);
								_bsbString = getDetailsFor(
										IGApiConstants.kJSONBSBKey, accountInfo);
								_companyNameString = getDetailsFor(
										IGApiConstants.kJSONCompanyNameKey,
										accountInfo);
								_abnString = getDetailsFor(
										IGApiConstants.kJSONABNKey, accountInfo);
								if (accountInfo
										.has(IGApiConstants.kJSONBankAccountIDKey)
										&& !accountInfo
												.isNull(IGApiConstants.kJSONBankAccountIDKey)) {
									_bankAccountId = accountInfo
											.getLong(IGApiConstants.kJSONBankAccountIDKey);
								}
							}
						} catch (JSONException e) {
							// If the get account details api call gets any
							// exceptions
							// then all the strings will be empty, as they are
							// initialsed.
							// get account details w/s is called from onCreate.
							e.printStackTrace();

						}
					}

				}
			}
			setUpAccountInfo();
			_saveButton.setEnabled(false);
		} else if (apiID == IGApiConstants.kUpdateAccountWebServiceId) {
			Log.i("UPDATE ACCOUNT INFO API RESPONSE", "" + response);
			showAlert();
		}

	}

	/**
	 * Failure response is got in this method
	 */
	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		super.onFailedToGetResponse(errorResponse, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (apiID == IGApiConstants.kUpdateAccountWebServiceId) {
			_saveButton.setEnabled(true);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUpdatePositionPollingTask.ignoreStaleState = false;
		finish();
	}

}