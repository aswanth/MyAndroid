/*
 * Package Name : com.ingogo.android.activities
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : This activity displays the login page.
 */

package com.ingogo.android.activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.adapters.IGMobileNumberOrLicenseNumberAdapter;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.logger.analytics.QBAnalytics;
import com.ingogo.android.poll.IGUpdatePositionListener;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGCustomProgressDialog;
import com.ingogo.android.utilities.IGCustomScrollLayout;
import com.ingogo.android.utilities.IGCustomScrollLayout.Listener;
import com.ingogo.android.utilities.IGGPSService;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.utilities.IGMobileNumberOrLicenseNumberFile;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGForgotPasswordApi;
import com.ingogo.android.webservices.IGLoginApi;
import com.ingogo.android.webservices.IGVersionApi;
import com.ingogo.android.webservices.beans.response.IGLoginResponseBean;
import com.ingogo.android.webservices.interfaces.IGLoginApiListener;

public class IGSignupActivity extends IGBaseActivity implements
		IGUpdatePositionListener, Listener, IGLoginApiListener {

	private EditText _userIdText;
	private EditText _passwordText;
	private ImageButton _loginButton;
	private String _userIdString;
	private String _passwordString;
	private ProgressDialog _progressDialog;
	private static final int AVAILABLE_JOBS_RESULT = 100;
	private IGUpdatePositionPollingTask _positionPollingTask = null;
	private ProgressDialog _updateProgress;
	private EditText _plateNo;
	private String _plateNoString;
	private NotificationManager mNotificationManager;
	private ListView _mobileNumberListView, _licenseNumberListView;
	private ArrayList<String> _mobileNumberList = null;
	private ArrayList<String> _licenseNumberList = null;
	private IGCustomScrollLayout _scrollView;
	private IGMobileNumberOrLicenseNumberAdapter _mobileNumberAdapter = null;
	private IGMobileNumberOrLicenseNumberAdapter _licenseNumberAdapter = null;
	// Map<String, Object> loginResponse;

	/**
	 * This flag is set to true when the progress dialog for the update shown.
	 * It is set to false when downloading for the update version of the
	 * application completes.
	 * **/
	private boolean _isUpdateProgressShown = false;

	/**
	 * This flag is set to true when alert dialog for the update shown. It is
	 * set to false when alert dialog dismisses.
	 * **/
	private boolean _isAlertDialogShown = false;
	private Handler _versionHandler = new Handler();

	private Runnable _versionRunnable = new Runnable() {

		@Override
		public void run() {
			Log.i("timer task running", "timer task running");
			if (IGUtility.isNetworkAvailable(IGSignupActivity.this) == true) {
				checkForUpdate();
				if (_versionHandler != null && _versionRunnable != null) {
					_versionHandler.removeCallbacks(_versionRunnable);
					_versionHandler = null;
					_versionRunnable = null;
				}

			} else {

				_versionHandler.postDelayed(this, 5000);
			}
		}

	};
	private IGLoginResponseBean _responseBean;

	/**
	 * @param savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.login);
		_scrollView = (IGCustomScrollLayout) findViewById(R.id.scrollView);
		_scrollView.setListener(this);
		initViews();
		setUpViews();
		IngogoApp.getSharedApplication().setComingFromPayOffline(false);

		// Check whether the GPS is enabled. If yes start listening GPS,
		// otherwise display an alert.
		performGpsCheck();

		/**
		 * Used for debug purpose only.
		 * **/
		if (IngogoApp.isDebugMode()) {
			_userIdText.setText(IGConstants.defaultUserId);
			_passwordText.setText(IGConstants.defaultPassword);
		}
		loginButtonState();
		IngogoApp.getSharedApplication().setLoggedIn(false);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.LOGOUT,
				IGConstants.ORDER_NONE, getString(R.string.logout_title));
		menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.JOBS,
				IGConstants.ORDER_NONE, getString(R.string.jobs_menu_title));
		menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.HELP,
				IGConstants.ORDER_NONE, getString(R.string.help_title));
		menu.addSubMenu(IGConstants.GROUP_ZERO,
				menuEnumerator.TRIP_HISTORY_SUMMARY, IGConstants.ORDER_NONE,
				getString(R.string.trip_history_summary_title));
		menu.addSubMenu(IGConstants.GROUP_ZERO,
				menuEnumerator.ACCOUNT_SETTINGS, IGConstants.ORDER_NONE,
				getString(R.string.account_settings_title));

		// Disable the logout, jobs and account info activities on login page.
		menu.findItem(menuEnumerator.LOGOUT).setEnabled(false);
		menu.findItem(menuEnumerator.JOBS).setEnabled(false);
		menu.findItem(menuEnumerator.TRIP_HISTORY_SUMMARY).setEnabled(false);
		menu.findItem(menuEnumerator.ACCOUNT_SETTINGS).setEnabled(false);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case menuEnumerator.LOGOUT:

			break;
		case menuEnumerator.JOBS:

			break;
		case menuEnumerator.HELP:
			goToHelpPage();
			break;
		case menuEnumerator.ACCOUNT_SETTINGS:

			break;

		default:
			break;
		}
		return true;
	}

	/**
	 * To check that GPS in the phone is enable or not. If enable, try to get
	 * the current location by starting the GPS service.Else prompt an alert
	 * .When click on the OK button on the alert finishes the application.
	 * **/
	private void performGpsCheck() {
		if (gpsCheck()) {

			// Start updating user location
			if (!IGUtility.isGPSServiceRunning()) {
				startService(new Intent(this, IGGPSService.class));
			}

		} else {
			// Dialog dlg = new AlertDialog.Builder(this)
			// .setTitle("")
			// .setMessage(getString(R.string.gps_enable_message))
			// .setPositiveButton("OK",
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			//
			// finish();
			// }
			// }).create();
			// dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			// dlg.show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		// Remove all tasks and credentials if our session has expired.
		Log.i("STATUS", "LOGIN ON RESUME===========================");
		if (extras != null) {

			// Stop the GPS listener.
			if (IGUtility.isGPSServiceRunning())
				stopService(new Intent(this, IGGPSService.class));

			// Stop polling location updates on log out.
			this.stopPollingLocationUpdates();

			// If the kHttpStatusForbidden status is passed in the extras then
			// remove the login credentials from the local.
			if (extras.getInt(IGApiConstants.kHttpStatusKey) == IGApiConstants.kHttpStatusForbidden) {

				Exception newExp = new Exception(
						" Authentication failure count > 3 "
								+ "For mobile number "
								+ IngogoApp.getSharedApplication().getUserId());
				IGUtility.logAuthExceptionInQLogger(newExp);
				this.removeLoginCredentials();
			}

			// For logout and executing the forbidden status clear the initial
			// fix flags
			// and the clear the notifications.
			// IGLocationListener.clearGPSData();
			IGJobDetailsActivity._isNavigateFromMenu = false;
			if (IGJobsActivity.getnotificationShowed()) {
				mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

				mNotificationManager.cancel(IGConstants.NotificationID);
				IGJobsActivity.setnotificationShowed(false);
			}
			return;

		}

		if (!_isAlertDialogShown && !_isUpdateProgressShown) {

			if (IGUtility.isNetworkAvailable(this)) {
				checkForUpdate();
			} else {
				if (_versionHandler == null) {
					_versionHandler = new Handler();
				}
				_versionHandler.postDelayed(_versionRunnable, 1000);
			}
		}

	}

	@Override
	protected void onPause() {
		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;

		if ((_updateProgress != null) && (_updateProgress.isShowing())) {
			_updateProgress.dismiss();
			_updateProgress = null;
		}

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		this.stopPollingLocationUpdates();
		super.onDestroy();
	}

	private void stopPollingLocationUpdates() {
		if (_positionPollingTask != null) {
			_positionPollingTask.stopPolling();
			_positionPollingTask = null;
		}
	}

	/**
	 * All text fields and button instances are initiated.
	 */
	private void initViews() {

		_userIdText = (EditText) findViewById(R.id.userIdTextField);
		_passwordText = (EditText) findViewById(R.id.passwordTextField);
		_loginButton = (ImageButton) findViewById(R.id.login_button);
		_loginButton.setEnabled(false);
		_plateNo = (EditText) findViewById(R.id.plateNo);
		_plateNo.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

		InputFilter filter = new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				return source.toString().toUpperCase();
			}
		};

		InputFilter[] FilterArray = new InputFilter[2];
		FilterArray[0] = new InputFilter.LengthFilter(7);
		FilterArray[1] = filter;
		_plateNo.setFilters(FilterArray);
		_mobileNumberListView = (ListView) findViewById(R.id.mobileNumberListView);
		_licenseNumberListView = (ListView) findViewById(R.id.licenseNumberListView);
		_mobileNumberList = IGMobileNumberOrLicenseNumberFile
				.readFromFile(IGConstants.kMobileNumberFile);
		_licenseNumberList = IGMobileNumberOrLicenseNumberFile
				.readFromFile(IGConstants.kLicenseNumberFile);
		if (_mobileNumberList != null) {
			_mobileNumberAdapter = new IGMobileNumberOrLicenseNumberAdapter(
					this, _mobileNumberList);
			_mobileNumberListView.setAdapter(_mobileNumberAdapter);
		}
		if (_licenseNumberList != null) {
			_licenseNumberAdapter = new IGMobileNumberOrLicenseNumberAdapter(
					this, _licenseNumberList);
			_licenseNumberListView.setAdapter(_licenseNumberAdapter);

		}

	}

	/**
	 * To call login web service.
	 * **/
	private void performLogin() {
		if (IGUtility.isNetworkAvailable(this)) {

			_progressDialog = IGUtility.showProgressDialog(this);
			IGLoginApi igLoginApi = new IGLoginApi(_userIdString,
					_passwordString, _plateNoString, this);
			igLoginApi.login();

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * When user tap on the login button, all text fields checked for validity
	 * and upon successful validation login web service is initiated.
	 * 
	 * @param view
	 */
	public void onLoginButtonClick(View view) {
		Log.i("Login", "Login button tapped");
		if (!IGUtility.isGPSServiceRunning()) {
			startService(new Intent(this, IGGPSService.class));
		}
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_plateNo.getWindowToken(), 0);

		this.performLogin();
	}

	/**
	 * Initialise and set up the views
	 */

	private void setUpViews() {
		/**
		 * Any text change in userIdField is caught by this listener
		 */
		_userIdText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				loginButtonState();
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
		/**
		 * Any text change in passwordField is caught by this listener
		 */
		_passwordText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				loginButtonState();

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

		_plateNo.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				loginButtonState();

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

		_plateNo.setImeOptions(EditorInfo.IME_ACTION_DONE);

		_plateNo.setOnEditorActionListener(new EditText.OnEditorActionListener() {

			/*
			 * Respond to soft keyboard events, look for the DONE press on the
			 * password field.
			 */

			@Override
			public boolean onEditorAction(TextView view, int keyCode,
					KeyEvent event) {
				if ((keyCode == EditorInfo.IME_ACTION_SEARCH
						|| keyCode == EditorInfo.IME_ACTION_DONE || event
						.getAction() == KeyEvent.ACTION_DOWN
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					// Done pressed. Hide the soft keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(_passwordText.getWindowToken(),
							0);

					// Perform Login
					if (validateFields()) {
						IGSignupActivity.this.performLogin();
					}

				}
				// Returning false allows other listeners to react to
				// the press.
				return false;

			}

		});

		_userIdText
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {

					/*
					 * Respond to soft keyboard events, look for the DONE press
					 * on the password field.
					 */

					@Override
					public boolean onEditorAction(TextView view, int keyCode,
							KeyEvent event) {
						if ((keyCode == EditorInfo.IME_ACTION_SEARCH
								|| keyCode == EditorInfo.IME_ACTION_NEXT || event
								.getAction() == KeyEvent.ACTION_DOWN
								&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

							_passwordText.requestFocus();
						}
						// Returning false allows other listeners to react to
						// the press.
						return false;

					}

				});

		_userIdText.setOnFocusChangeListener(new CommonFocusChangeListener());
		_plateNo.setOnFocusChangeListener(new CommonFocusChangeListener());
		_mobileNumberListView
				.setOnItemClickListener(new CommonItemClickListener(
						_mobileNumberListView));
		_licenseNumberListView
				.setOnItemClickListener(new CommonItemClickListener(
						_licenseNumberListView));
		_userIdText.setOnClickListener(new CommonClickListener());
		_plateNo.setOnClickListener(new CommonClickListener());

	}

	/**
	 * Used for validating userIdField and passwordField
	 * 
	 * @return
	 */
	private boolean validateFields() {
		_userIdString = _userIdText.getText().toString().trim();
		_passwordString = _passwordText.getText().toString();
		_plateNoString = _plateNo.getText().toString().trim();
		if (_userIdString.equals("")) {
			return false;
		} else if (_passwordString.equals("")) {
			return false;
		} else if (_plateNoString.equals("")) {
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
				} else {
					if (_passwordString.contains(" ")
							|| _passwordString.length() < 4
							|| _passwordString.length() > 10) {
						return false;
					} else {
						if (_plateNoString.length() >= 4
								&& _plateNoString.length() <= 7) {
							String maskedPrefixesString = IngogoApp
									.getSharedApplication()
									.getValidMaskedTaxiPlatePrefixes();

							if (_plateNoString.length() == 6
									&& maskedPrefixesString.length() > 0) {
								// /////////////////////////////////////

								ArrayList<String> maskedDriverLocality = new ArrayList<String>();
								String[] maskedTokens = maskedPrefixesString
										.split(",");
								for (String localityPrefix : maskedTokens) {
									maskedDriverLocality.add(localityPrefix);
								}
								if (null != maskedDriverLocality
										&& maskedDriverLocality.size() > 0) {

									// The taxi number plate can have prefixes
									// listed in the _supportedPrefixes
									for (String pattern : maskedDriverLocality) {

										if (pattern.contains("0000")) {
											int index = pattern.indexOf("0000");
											String patternString;
											Pattern taxiPlatePattern;
											if (index == 0) {
												patternString = "^"
														+ "[0-9]+"
														+ pattern.replace(
																"0000", "");
												taxiPlatePattern = Pattern
														.compile(patternString);
											} else {
												patternString = "^"
														+ pattern.replace(
																"0000", "")
														+ "[0-9]+";
												taxiPlatePattern = Pattern
														.compile(patternString);
											}

											if (taxiPlatePattern.matcher(
													_plateNoString).matches()) {
												return true;
											}

										}
										// String patternString = "^" + pattern
										// + "[0-9]+";
										// Pattern taxiPlatePattern = Pattern
										// .compile(patternString);
										//
										// if (taxiPlatePattern.matcher(
										// _plateNoString).matches()) {
										// return true;
										// } else {
										// String patternStringReverse = "^"
										// + "[0-9]+" + pattern;
										// Pattern taxiPlatePatternReverse =
										// Pattern
										// .compile(patternStringReverse);
										// if (taxiPlatePatternReverse
										// .matcher(_plateNoString)
										// .matches()) {
										// return true;
										// }
										// }
									}

									// if (validTaxiPlate) {
									//
									// }
								}
								// /////////////////////////////////////
							}
							String validPrefixesString = IngogoApp
									.getSharedApplication()
									.getValidTaxiPlatePrefixes();
							if (validPrefixesString.length() == 0) {
								return false;
							}
							ArrayList<String> driverLocality = new ArrayList<String>();
							String[] tokens = validPrefixesString.split(",");
							for (String localityPrefix : tokens) {
								driverLocality.add(localityPrefix);
							}
							if (null != driverLocality
									&& driverLocality.size() > 0) {
								boolean validTaxiPlate = false;
								// The taxi number plate can have prefixes
								// listed in the _supportedPrefixes
								for (String pattern : driverLocality) {
									String patternString = "^" + pattern
											+ "[0-9]+";
									Pattern taxiPlatePattern = Pattern
											.compile(patternString);

									if (taxiPlatePattern
											.matcher(_plateNoString).matches()) {
										validTaxiPlate = true;
									}
								}

								if (validTaxiPlate) {
									return true;
								}
								return false;
							}
						} else {
							return false;

						}
						return false;

					}
				}
			}

			return false;

		}
	}

	/**
	 * To remove the loggedIn details
	 */
	private void removeLoginCredentials() {
		IngogoApp.getSharedApplication().removeLoginCredentials();
		IngogoApp.getSharedApplication().setLoggedIn(false);
	}

	/**
	 * Process the response that got by calling the API for loggedIn
	 * 
	 * @param loginObj
	 */
	private void processLoginResponse() {

		// The job lists are saved only in the application context for the
		// same user logged in.For different user the job list must be
		// cleared.

		String userId = IngogoApp.getSharedApplication().getPreviousUserId();
		if (userId != null && !userId.equals(_userIdString)) {
			IngogoApp.getSharedApplication().setJobList(null);
		}
		IngogoApp.getSharedApplication().setUserId(_userIdString);

		IngogoApp.getSharedApplication().setPassword(_passwordString);
		IngogoApp.getSharedApplication().setLoggedIn(true);
		IGUtility.setDefaults(IGConstants.kPlateNumber, _plateNoString, this);
		IngogoApp.getSharedApplication().setBroadcastPositionInterval(
				Long.parseLong(_responseBean.getBroadcastPositionInterval()));

		IngogoApp.setConnectionTimeout(Integer.parseInt(_responseBean
				.getWebServiceTimeout()));
		Log.e("POLLING TASK",
				"polling interval" + _responseBean.getPollBookingsInterval());
		IngogoApp.getSharedApplication().setPollJobsInterval(
				Integer.parseInt(_responseBean.getPollBookingsInterval()));
		String jobReminderInterval = "60";
		jobReminderInterval = _responseBean.getJobReminderInterval();
		IngogoApp.getSharedApplication().setJobRemainderInterval(
				jobReminderInterval);

		// Enable flurry log if the value of "captureDiagnostics" flag is
		// true. If the value for "captureDiagnostics" is not specified then
		// consider it as true.
		boolean enableFlurryLog = true;

		// if (loginObj.has(IGConstants.enableFlurryLog)) {
		// Log.i("enableFlurryLog", "has enableFlurryLog");
		// try {

		enableFlurryLog = _responseBean.isCaptureDiagnostics();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		QLog.i("enableFlurryLog", "enableFlurryLog = " + enableFlurryLog);

		QBAnalytics.setFlurryEnabled(enableFlurryLog);
		if (enableFlurryLog == false) {
			QBAnalytics.endSession(this);
		}

		QLog.setRemoteLoggingEnabled(enableFlurryLog);
		updatePaymentBaseValues();

		IGMobileNumberOrLicenseNumberFile.writeToFile(
				IGConstants.kMobileNumberFile, _userIdString);
		IGMobileNumberOrLicenseNumberFile.writeToFile(
				IGConstants.kLicenseNumberFile, _plateNoString);
	}

	private void updatePaymentBaseValues() {

		IngogoApp.getSharedApplication().resetPaymentBaseValues();

		IngogoApp.getSharedApplication().setMinTotalDueValue(
				_responseBean.getMinTotalDueValue());
		IngogoApp.getSharedApplication().setMaxTotalDueValue(
				_responseBean.getMaxTotalDueValue());
		IngogoApp.getSharedApplication().setConfirmationValue(
				_responseBean.getConfirmationValue());
		IngogoApp.getSharedApplication().setCreditPercentage(
				_responseBean.getCreditPercentage());
	}

	private void startLocationUpdate() {
		if (_positionPollingTask != null) {
			_positionPollingTask.stopPolling();
			_positionPollingTask = null;
		}

		_positionPollingTask = new IGUpdatePositionPollingTask(this);
		_positionPollingTask.setupPollingProcess();
		_positionPollingTask.startPolling();

	}

	@Override
	public void positionUpdateSuccessfull(double latitude, double longitude) {
		// If the location is 0.0,0.0 ,then don't save the values, just return
		if ((latitude == 0.0) || (longitude == 0.0))
			return;
		IngogoApp.LATTITUDE = String.valueOf(latitude);
		IngogoApp.LONGITUDE = String.valueOf(longitude);
		Log.d("LOCATION_UPDATE", "Updated to : " + String.valueOf(latitude)
				+ " , " + String.valueOf(longitude));

	}

	@Override
	public void positionUpdateFailed(String errorMessage) {
		Log.e("LOCATION_UPDATE", errorMessage);
	}

	public void clearFields() {

		_userIdText.setText("");
		_passwordText.setText("");
		_plateNo.setText("");
	}

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		Log.e("response", "" + response);

		if (apiID == IGApiConstants.kLoginWebServiceId) {

			if (response != null && response.containsKey(IGConstants.kDataKey)) {
				// loginResponse = new HashMap<String, Object>();
				// loginResponse = response;
				moveToJobsScreen();
			} else {
				IGUtility.dismissProgressDialog(_progressDialog);

			}

		} else if (apiID == IGApiConstants.kForgotPasswordWebServiceId) {

			if (response != null) {
				IGUtility.dismissProgressDialog(_progressDialog);
				AlertDialog.Builder dlg = new AlertDialog.Builder(this);
				dlg.setTitle("");
				dlg.setCancelable(false);
				dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {

					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_SEARCH
								&& event.getRepeatCount() == 0) {
							return true;
						}
						return false;
					}

				});
				dlg.setMessage(getString(R.string.forgot_password_alert));
				dlg.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Intent intent = new Intent(
										IGSignupActivity.this,
										IGForgotPasswordActivity.class);
								intent.putExtra("mobile", _userIdText.getText()
										.toString()); // required for forgot
														// password

								startActivity(intent);
							}
						});
				AlertDialog al = dlg.create();
				al.setVolumeControlStream(AudioManager.STREAM_MUSIC);
				al.show();
			}

		} else if (apiID == IGApiConstants.kVersionWebServiceId) {
			if (_updateProgress != null) {
				_updateProgress.dismiss();
				_updateProgress = null;
			}

			try {
				final JSONObject dataObj = (JSONObject) response
						.get(IGConstants.kDataKey);
				final JSONObject versionObj = (JSONObject) dataObj
						.get(IGApiConstants.kAppVersion);
				if (IngogoApp.getVersionCode() < Float.parseFloat(versionObj
						.getString(IGApiConstants.kVersionNumber))) {
					Log.i("VERSION OBJ", "" + versionObj);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(IGConstants.kUpgrade);
					builder.setMessage(this
							.getString(R.string.upgrade_alert_message));
					builder.setIcon(R.drawable.icon);
					builder.setCancelable(false);
					builder.setPositiveButton(IGConstants.kUpgrade,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									try {
										_isAlertDialogShown = false;
										IGUpdateTask update = new IGUpdateTask();
										update.execute(versionObj
												.getString(IGApiConstants.kAppUrl));
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});

					if (IGUtility
							.getDefaults(IGConstants.kUpdatedVersion, this) != null) {
						if (IGUtility
								.getDefaults(IGConstants.kUpdatedVersion, this)
								.equals(versionObj
										.getString(IGApiConstants.kVersionNumber))) {
							float updatedTimeInMilliSec;
							if (IGUtility.getRetryTime(this) != 0.0f)
								updatedTimeInMilliSec = IGUtility
										.getRetryTime(this);
							else
								updatedTimeInMilliSec = 0.0f;
							float timeInterval = (float) (versionObj
									.getDouble(IGApiConstants.kRetryAfter) * 60.0 * 60.0 * 1000.0);
							float shouldRetryAfter = updatedTimeInMilliSec
									+ timeInterval;
							Calendar cal = Calendar.getInstance();
							float currentTimeInMilliSec = cal.getTimeInMillis();

							if (currentTimeInMilliSec < shouldRetryAfter) {
								builder.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												_isAlertDialogShown = false;
												dialog.dismiss();
											}
										});
							}
						}
					}
					/**
					 * Store the current version in shared prefences.
					 * **/
					IGUtility
							.setDefaults(
									IGConstants.kUpdatedVersion,
									versionObj
											.getString(IGApiConstants.kVersionNumber),
									this);

					Calendar cal = Calendar.getInstance();
					float time = cal.getTimeInMillis();
					Log.i("time", "" + time);
					IGUtility.setRetryTime(time, this);
					Log.i("String.valueOf(time)", "" + String.valueOf(time));
					AlertDialog alert = builder.create();
					alert.show();
					_isAlertDialogShown = true;

				} else {
					IngogoApp.setAppUpdateStatus(true);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		if (apiID == IGApiConstants.kVersionWebServiceId) {
			_isAlertDialogShown = false;
			_isUpdateProgressShown = false;
			if ((_updateProgress != null) && (_updateProgress.isShowing())) {
				_updateProgress.dismiss();
				_updateProgress = null;
			}
		}
		Log.e("errorResponse", "" + errorResponse);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (!isFinishing()) {
			super.onFailedToGetResponse(errorResponse, apiID);
		}
	}

	/**
	 * This method receives any click on forgot password text.
	 * **/
	public void onForgotPasswordClick(View view) {
		Log.e("Forgot password", "Forgot password link tapped");
		String userIdText = _userIdText.getText().toString().trim();
		if (userIdText.length() > 0) {
			IGUtility.dismissProgressDialog(_progressDialog);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGForgotPasswordApi forgotPassApi = new IGForgotPasswordApi(this,
					userIdText);
			forgotPassApi.requestNewPassword();

		} else {
			IGUtility.showDialogOk("",
					getString(R.string.userid_empty_message), this);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AVAILABLE_JOBS_RESULT) {
			finish();
		}
	}

	/**
	 * Method used for enable or disable the login button by validate the fields
	 * */

	private void loginButtonState() {
		if (validateFields()) {
			_loginButton.setEnabled(true);
		} else {
			_loginButton.setEnabled(false);
		}
	}

	/**
	 * Used to check Gps in the mobile phone is enable or not.
	 * **/
	private boolean gpsCheck() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	}

	/**
	 * From this method we get the initial latitude and longitude of the driver
	 * **/
	@Override
	public void initialFixObtained(double latitude, double longitude) {
		// If the location is 0.0,0.0 ,then don't save the values, just return

		if (!isTopActivityDifferentFromNewIntentActivity(IGConstants.kSignInActivityName)) {

			if ((latitude == 0.0) || (longitude == 0.0))
				return;
			IngogoApp.LATTITUDE = String.valueOf(latitude);
			IngogoApp.LONGITUDE = String.valueOf(longitude);

			Log.e("Initial fix obtained in login page",
					"Updated to : " + String.valueOf(latitude) + " , "
							+ String.valueOf(longitude));
			// IGLocationListener.setInitialFixObtained(true);
			if (_progressDialog != null) {
				if (_progressDialog.isShowing()) {
					_progressDialog.dismiss();
				}
			}
		}
	}

	/**
	 * To call IGVersion Api for update purpose.
	 * **/
	private void checkForUpdate() {
		_updateProgress = null;

		if (!isFinishing()) {
			_updateProgress = new ProgressDialog(this);
			_updateProgress.setIcon(R.drawable.icon);
			_updateProgress.setTitle(IGConstants.kCheckForUpdate);
			_updateProgress.setCancelable(false);
			_updateProgress
					.setOnKeyListener(new DialogInterface.OnKeyListener() {

						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_SEARCH
									&& event.getRepeatCount() == 0) {
								return true;
							}
							return false;
						}

					});
			_updateProgress.show();
		}
		IGVersionApi versionApi = new IGVersionApi(this);
		versionApi.getLatestAppVersion();

	}

	/**
	 * This AsyncTask helps us to download updated version of the application.
	 * **/
	private class IGUpdateTask extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (null != _updateProgress && _updateProgress.isShowing()) {
				_updateProgress.dismiss();
			}
			_isUpdateProgressShown = false;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(
					Uri.fromFile(new File(Environment
							.getExternalStorageDirectory() + "/ingogo.apk")),
					"application/vnd.android.package-archive");
			startActivity(intent);
			IGSignupActivity.this.finish();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (_updateProgress == null) {
				_updateProgress = new IGCustomProgressDialog(
						IGSignupActivity.this);
			}
			_updateProgress.setProgress(0);
			_updateProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			_updateProgress.setTitle(IGConstants.kDownloadUpdate);
			_updateProgress.setIcon(R.drawable.icon);
			_updateProgress
					.setOnKeyListener(new DialogInterface.OnKeyListener() {

						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_SEARCH
									&& event.getRepeatCount() == 0) {
								return true;
							}
							return false;
						}

					});
			_updateProgress.show();
			_isUpdateProgressShown = true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (_updateProgress != null)
				_updateProgress.setProgress(values[0]);

		}

		@Override
		protected String doInBackground(String... urls) {
			int count;
			try {
				URL url = new URL(urls[0]);
				Authenticator.setDefault(new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("qburst", "qburst"
								.toCharArray());
					}
				});
				URLConnection conexion = url.openConnection();
				conexion.connect();
				// this will be useful so that you can show a tipical 0-100%
				// progress bar
				int lenghtOfFile = conexion.getContentLength();

				// downlod the file
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(
						"mnt/sdcard/ingogo.apk");

				byte data[] = new byte[1024];

				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					// publishing the progress....
					publishProgress((int) (total * 100 / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
			}
			return null;
		}
	}

	private class CommonFocusChangeListener implements
			View.OnFocusChangeListener {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (v == _userIdText) {
				if (hasFocus && _mobileNumberList != null) {
					scrollToPosition(_scrollView.getHeight() / 5,
							_mobileNumberListView);
				} else {
					_mobileNumberListView.setVisibility(View.GONE);
				}

			} else if (v == _plateNo) {
				if (hasFocus && _licenseNumberList != null) {
					scrollToPosition((_scrollView.getHeight() * 3) / 4,
							_licenseNumberListView);
				} else {
					_licenseNumberListView.setVisibility(View.GONE);
				}

			}
		}

	}

	private class CommonItemClickListener implements OnItemClickListener {

		View _listView;

		public CommonItemClickListener(View listView) {
			this._listView = listView;

		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if (_listView == _mobileNumberListView) {
				_userIdText.setText(_mobileNumberList.get(position));
				_userIdText.setSelection(_mobileNumberList.get(position)
						.length());
				_mobileNumberListView.setVisibility(View.GONE);
				_passwordText.requestFocus();

			} else if (_listView == _licenseNumberListView) {
				_plateNo.setText(_licenseNumberList.get(position));
				_plateNo.setSelection(_licenseNumberList.get(position).length());
				_licenseNumberListView.setVisibility(View.GONE);
			}
		}

	}

	private class CommonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (v == _userIdText) {
				if (_mobileNumberList != null) {
					scrollToPosition(_scrollView.getHeight() / 5,
							_mobileNumberListView);
				}

			} else if (v == _plateNo) {

				if (_licenseNumberList != null) {
					scrollToPosition((_scrollView.getHeight() * 3) / 4,
							_licenseNumberListView);
				}
			}
		}

	}

	private void scrollToPosition(final int height, final View view) {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				_scrollView.scrollTo(0, height);
				view.setVisibility(View.VISIBLE);
			}

		}, 500);
	}

	@Override
	public void onSoftKeyboardShown(boolean isShowing) {
		if (isShowing) {
			Log.i("keyboard showing", "keyboard showing");
		} else {
			Log.i("dismiss keyboard", "dismiss keyboard");
			_mobileNumberListView.setVisibility(View.GONE);
			_licenseNumberListView.setVisibility(View.GONE);
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
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	private void moveToJobsScreen() {
		IGUtility.dismissProgressDialog(_progressDialog);

		IGSignupActivity.this.processLoginResponse();

		// Start updating user location
		startLocationUpdate();

		// Start jobs activity
		Intent intent = new Intent(IGSignupActivity.this, IGJobsActivity.class);
		startActivityForResult(intent, AVAILABLE_JOBS_RESULT);
		clearFields();

		AudioManager audioManager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);
		audioManager.setMode(AudioManager.MODE_IN_CALL);
		audioManager.setSpeakerphoneOn(true);
		IngogoApp.setPlayLoginAlert(true);

	}

	@Override
	public void loginSuccessfully(IGLoginResponseBean loginResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_responseBean = loginResponse;

		moveToJobsScreen();
	}

	@Override
	public void failedToLogin(String erroMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialog("", erroMessage, this);
	}

}