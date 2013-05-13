package com.ingogo.android.activities;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.ingogo.android.R;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGAccountInfoApi;
import com.ingogo.android.webservices.IGUpdateAccountApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IGAccountInfoRecordedOrNonRecordedActivity extends IGBaseActivity {

	private EditText _companyName;
	private EditText  _abn;
	private String _companyNameString, _abnString;
	private ProgressDialog _progressDialog;
	private ImageButton _saveButton;
	private long _touchDownTime;
	private boolean _isAccountRegistered;
	private RelativeLayout _loadAndGoLayout;
	private RelativeLayout _noLoadAndGoAccounts;
	private long _bankAccountId = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.account_info_recorded_or_nonrecorded);
		getBundleExtras();
		callCreditDetailsApi();
		intiViews();
		setUpViews();
	}

	private void getBundleExtras() {
		_isAccountRegistered = getIntent().getBooleanExtra("accountRegitered", false);
		
	}

	private void intiViews() {
		_companyName = (EditText)findViewById(R.id.companyNameTextField);
		_abn = (EditText)findViewById(R.id.ABNTextField);
		_saveButton = (ImageButton)findViewById(R.id.save_button);
		_saveButton.setEnabled(false);
		_loadAndGoLayout = (RelativeLayout)findViewById(R.id.LoadAndGoLayout);
		_noLoadAndGoAccounts = (RelativeLayout)findViewById(R.id.noLoadAndGoLayout);
		initializeStrings();
		
	}
	
	/**
	 * Initialize the strings as empty strings.
	 */
	private void initializeStrings() {
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

	@Override
	protected void onResume() {
		super.onResume();
		IGUpdatePositionPollingTask.ignoreStaleState = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUpdatePositionPollingTask.ignoreStaleState = false;
	}
	
	public void saveButtonClicked ( View view ) {
		if (IGUtility.isNetworkAvailable(this)) {
			_saveButton.setEnabled(false);
			_progressDialog = IGUtility.showProgressDialog(this);
			
			_companyNameString = _companyName.getText().toString();
			_abnString = _abn.getText().toString();
			
			IGUpdateAccountApi update = new IGUpdateAccountApi(this,
					_companyNameString, _abnString, null,
					null, null,_bankAccountId);
			update.updateAccountInfo();

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}
	
	public void onViewButtonClicked ( View view ) {
		Intent accountInfoIntent = new Intent( this, IGAccountInfoRecordedListActivity.class);
		startActivity(accountInfoIntent);
		
	}
	
	/**
	 * Successful response is received by this method, when calling web service
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		Log.w("Account info Recorded Or Non-Recorded activity response ", "" + response);
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

	private void setUpAccountInfo() {
	
		_companyName.setText(_companyNameString);
		_abn.setText(_abnString);
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
		dlg.show();
	}
	
	
	/**
	 * Function to filter text field entries.
	 */
	public void setUpViews() {
		if(_isAccountRegistered) {
			_noLoadAndGoAccounts.setVisibility(View.GONE);
			_loadAndGoLayout.setVisibility(View.VISIBLE);
		} else {
			_noLoadAndGoAccounts.setVisibility(View.VISIBLE);
			_loadAndGoLayout.setVisibility(View.GONE);
		}

		InputFilter company_name_filter[] = new InputFilter[2];
		InputFilter abn_filter[] = new InputFilter[2];
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
		_companyName.setFilters(company_name_filter);
		_abn.setFilters(abn_filter);
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
		if ( _companyName.getText().toString().trim().length() != 0
				&& _abn.getText().length() == 11) {
			_saveButton.setEnabled(true);
		} else {
			_saveButton.setEnabled(false);
		}
	}
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {

        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            _touchDownTime = SystemClock.elapsedRealtime();
            break;

        case MotionEvent.ACTION_UP:
            // to avoid drag events
            if (SystemClock.elapsedRealtime() - _touchDownTime <= 150) {

                EditText[] textFields = this.getFields();
                if (textFields != null && textFields.length > 0) {

                    boolean clickIsOutsideEditTexts = true;

                    for (EditText field : textFields) {
                        if (isPointInsideView((int) ev.getRawX(),
                                (int) ev.getRawY(), field)) {
                            clickIsOutsideEditTexts = false;
                            break;
                        }
                    }

                    if (clickIsOutsideEditTexts) {
                        IGUtility.hideKeyboard(IGAccountInfoRecordedOrNonRecordedActivity.this);
                    }
                } else {
                    IGUtility.hideKeyboard(IGAccountInfoRecordedOrNonRecordedActivity.this);
                }
            }
            break;
        }

        return super.dispatchTouchEvent(ev);
    }
	
	private boolean isPointInsideView(int rawX, int rawY, EditText field)
    {
        Rect fieldRect = new Rect();
        field.getGlobalVisibleRect(fieldRect);
        if (fieldRect.contains(rawX, rawY)) {
            return true;
        }
        return false;
    }

    private EditText[] getFields()
    {
        return new EditText[] { _companyName, _abn};
    }


}
