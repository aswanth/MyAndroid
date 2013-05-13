package com.ingogo.android.activities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGFindPassengerActivity;
import com.ingogo.android.activities.payments.IGPaymentCompletedActivity;
import com.ingogo.android.activities.payments.IGPaymentFailureActivity;
import com.ingogo.android.activities.payments.IGPaymentHistorySummaryActivity;
import com.ingogo.android.activities.payments.IGPaymentSucessActivity;
import com.ingogo.android.activities.payments.IGPaymentsSwipeActivity;
import com.ingogo.android.activities.payments.IGSwipeCalculatorActivity;
import com.ingogo.android.activities.payments.IGSwipePracticeActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.app.IngogoApp.jobStatusEnum;
import com.ingogo.android.cardreader.helpers.LinePrintHelper;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.logger.analytics.QBAnalytics;
import com.ingogo.android.model.IGAddress;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.model.IGJobAvailableModel;
import com.ingogo.android.model.IGReceiptInformationModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGBluetoothHelper;
import com.ingogo.android.utilities.IGCustomDialog;
import com.ingogo.android.utilities.IGCustomProgressDialog;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGBaseWebserviceThreadPool;
import com.ingogo.android.webservices.IGCreditDetailsApi;
import com.ingogo.android.webservices.IGFindCurrentDriverStateApi;
import com.ingogo.android.webservices.IGLogoutApi;
import com.ingogo.android.webservices.IGResponseListener;
import com.ingogo.android.webservices.IGTakePaymentApi;
import com.ingogo.android.webservices.IGVerifyPasswordApi;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGFindCurrentDriverStateApiListener;
import com.ingogo.android.webservices.interfaces.IGTakePaymentApiListener;

public class IGBaseActivity extends Activity implements IGResponseListener,
		IGExceptionApiListener, IGTakePaymentApiListener,
		IGFindCurrentDriverStateApiListener {

	private ProgressDialog _progressDialog = null;
	private IGJob _job = new IGJob();
	private AlertDialog alert;
	private ProgressDialog _igCustomProgressDialog = null;
	private static int _asyncTaskInitialDelay = 1000;
	private static int _asyncTaskTimePeriod = 5000;
	private static String _commaString = ", ";
	private static String _slashString = "/";
	private DeviceBroadcast _deviceBroadcast;

	private static final String NORMAL_BOOKING = "BOOKING";
	private static final int SINGLE_LINE_LENGTH = 32;
	private static final int SECONDPART_LENGTH = 10;
	private static final String SPACESTRING = "                      ";
	protected static final char LINE_FEED = 0x0A;
	protected static final String ACCOUNT_DETAIL_KEY = "accountDetails";

	private EditText _passwordText;
	private Button _continueBtn;
	private int currentThemeId;
	private boolean jobProgressStatus = false;
	protected boolean calculatordataFetchFailed = false;

	protected boolean isIngogoPay;
	AlertDialog _authenticationDialog;
	Dialog _dialog;

	public static boolean _infoDialogShown = false;

	private boolean _hasCreditCards;
	JSONObject _bookingDetails;
	boolean _navigateToCalculator = false;
	private MenuItem _currentSelectedMenuItem;
	private AlertDialog _unavailableDialog;

	public static interface menuEnumerator {
		public static final int LOGOUT = 0;
		public static final int TRIP_HISTORY_SUMMARY = 1;
		public static final int PRINT_REFFERAL = 2;
		public static final int CASH_RECEIPT = 3;
		public static final int PAYMENT_OPTION = 4;
		public static final int JOBS = 5;
		public static final int HELP = 6;
		public static final int PRACTICE_SWIPE = 7;
		public static final int REPRINT_RECEIPT = 8;
		public static final int ACCOUNT_SETTINGS = 9;

		public static final int ACCOUNT_INFO = 10;
		public static final int PRINTER_CONFIG = 11;
		public static final int MAINTAIN_ADDRESS = 12;

	}

	private Handler _handler = new Handler();
	private Handler _handlerForNetworkCheck = new Handler();
	private Handler _handlerForLogout = new Handler();

	private Runnable _timerTaskRunnable = new Runnable() {

		@Override
		public void run() {
			Log.i("timer task running", "timer task running");

			if (IGUtility.isNetworkAvailable(IGBaseActivity.this) == true) {
				invokeDriverStateApiForException();

			} else {

				_handler.postDelayed(this, _asyncTaskTimePeriod);
			}
		}
	};

	private Runnable _logoutRunnable = new Runnable() {

		@Override
		public void run() {

			if (alert != null && alert.isShowing()) {

				Exception newExp = new Exception(
						"No network response for 5 mins in Current Driver State for driver"
								+ IngogoApp.getSharedApplication().getUserId());
				IGUtility.logAuthExceptionInQLogger(newExp);
				alert.dismiss();
				alert = null;
				// performLogout();
				_handlerForLogout.removeCallbacks(_logoutRunnable);
				_unavailableDialog = new AlertDialog.Builder(
						IGBaseActivity.this)
						.setTitle(null)
						.setMessage(
								"Please try again as your device lost it's mobile connection. If problem persists contact ingogo support.")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

									}
								}).create();
				_unavailableDialog
						.setVolumeControlStream(AudioManager.STREAM_MUSIC);
				_unavailableDialog.show();

				// IGUtility.showDialogOk(
				// null,
				// IGBaseActivity.this.getText(
				// R.string.locality_api_failed).toString(),
				// IGBaseActivity.this);
			}

		}
	};

	protected void callTakePaymentApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGTakePaymentApi api = new IGTakePaymentApi(this, this);
			api.getTakePaymentStatus();
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	private Runnable _checkNetworkAvailable = new Runnable() {

		@Override
		public void run() {
			Log.i("network availability check thread running",
					"network availability check thread running");
			if (IGUtility.isNetworkAvailable(IGBaseActivity.this)) {
				IGUtility.dismissProgressDialog(_igCustomProgressDialog);
				_igCustomProgressDialog = null;

			} else {
				_handlerForNetworkCheck.postDelayed(this, 5000);
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (IngogoApp.getThemeID() == 1) {
			this.setTheme(R.style.dayTheme);
		} else {
			this.setTheme(R.style.nightTheme);
		}
		currentThemeId = IngogoApp.getThemeID();
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Log.i("AIR PLANE MODE initial", "" + isAirplaneModeOn(this));
		_deviceBroadcast = new DeviceBroadcast();
		Intent intent = this.registerReceiver(_deviceBroadcast,
				new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		if (intent != null) {
			_deviceBroadcast.onReceive(this, intent);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		QBAnalytics.setAnalyticsEnabled(true);
		QBAnalytics.startSession(this);
		QBAnalytics.logPageViews();

		if (IngogoApp.getCurrentActivityOnTop() != null) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("Class Name", IngogoApp.getCurrentActivityOnTop()
					.getClass().getName());
			QBAnalytics.logEvent(IGConstants.kAnalyticsPageView, params);
			QLog.d("Class Name", IngogoApp.getCurrentActivityOnTop().getClass()
					.getName());
		}

	}

	@Override
	protected void onStop() {
		QBAnalytics.endSession(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (_deviceBroadcast != null) {
			this.unregisterReceiver(_deviceBroadcast);

		}
		super.onDestroy();
	}

	/**
	 * Gets the state of Airplane Mode. If the 'Airplane' mode is enabled then
	 * return true otherwise return false.
	 * 
	 * @param context
	 * @return true if enabled.
	 */
	private static boolean isAirplaneModeOn(Context context) {

		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;

	}

	@Override
	protected void onResume() {
		if (!(this instanceof IGPaymentsSwipeActivity)) {
			IngogoApp.sPaymentOfflineButtonEnableFlag = false;
		}

		if (!(this instanceof IGSwipeCalculatorActivity)) {
			IngogoApp.sPaymentOfflineButtonEnableFlagInSwipeCalculator = false;
		}

		if (currentThemeId != IngogoApp.getThemeID()) {
			restartActivty();
		}
		// Check whether 'Airplane' mode is enabled.
		// If enabled then disable it.
		if (isAirplaneModeOn(this) == true) {
			// Disable the 'Airplane' mode.
			Settings.System.putInt(this.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0);

			// Post an intent to reload
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", 1);
			sendBroadcast(intent);
			if (!IGUtility.isNetworkAvailable(this)) {
				_igCustomProgressDialog = IGCustomProgressDialog.show(this, "",
						getString(R.string.network_fetching_message));
				_igCustomProgressDialog
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
				if (_handlerForNetworkCheck == null) {
					_handlerForNetworkCheck = new Handler();
				}
				_handlerForNetworkCheck.postDelayed(_checkNetworkAvailable, 0);
			}
		}

		if (!IGUtility.getTopActivity().equals(IGConstants.kJobsActivityName)) {
			IGJobsActivity.stopJobRemainderAlertRunnable();
		}
		AudioManager audioManager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);
		IngogoApp.setCurrentActivityOnTop(this);

		if (!IGSignupActivity.class.isInstance(this)
				&& !IGSwipePracticeActivity.class.isInstance(this)
				&& !IGSwipeCalculatorActivity.class.isInstance(this)
				&& !IGPaymentsSwipeActivity.class.isInstance(this)
				&& !IGPaymentSucessActivity.class.isInstance(this)
				&& !IGPaymentCompletedActivity.class.isInstance(this)
				&& !IGPaymentFailureActivity.class.isInstance(this)) {

			audioManager.setMode(AudioManager.MODE_IN_CALL);
			audioManager.setSpeakerphoneOn(true);
		} else {
			audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(false);
		}

		calculatordataFetchFailed = false;
		super.onResume();

	}

	protected void onPause() {
		if (alert != null && alert.isShowing()) {
			alert.getCurrentFocus();
			alert.dismiss();
			alert = null;
		}

		if (_authenticationDialog != null && _authenticationDialog.isShowing()) {
			_authenticationDialog.getCurrentFocus();
			_authenticationDialog.dismiss();
			_authenticationDialog = null;
		}

		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;
		hideSoftKeyboard();
		super.onPause();

	}

	private void hideSoftKeyboard() {
		if (getWindow().getCurrentFocus() != null) {

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
					.getApplicationWindowToken(), 0);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		// Add the log out, jobs and account infooptions to the menu button if
		// the user is
		// logged in.
		if (IngogoApp.getSharedApplication().isLoggedIn()) {
			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.LOGOUT,
					IGConstants.ORDER_NONE, getString(R.string.logout_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.TRIP_HISTORY_SUMMARY,
					IGConstants.ORDER_NONE,
					getString(R.string.trip_history_summary_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PRINT_REFFERAL, IGConstants.ORDER_NONE,
					getString(R.string.print_referal_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.CASH_RECEIPT, IGConstants.ORDER_NONE,
					getString(R.string.cash_receipt_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PAYMENT_OPTION, IGConstants.ORDER_NONE,
					getString(R.string.payment_option_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.JOBS,
					IGConstants.ORDER_NONE, getString(R.string.jobs_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.HELP,
					IGConstants.ORDER_NONE, getString(R.string.help_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PRACTICE_SWIPE, IGConstants.ORDER_NONE,
					getString(R.string.practice_swipe_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.REPRINT_RECEIPT, IGConstants.ORDER_NONE,
					getString(R.string.reprint_receipt_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.ACCOUNT_SETTINGS, IGConstants.ORDER_NONE,
					getString(R.string.account_settings_title));
			// SubMenu fileMenu =
			// menu.addSubMenu(getString(R.string.account_settings_title));
			// fileMenu.addSubMenu(menuEnumerator.ACCOUNT_SETTINGS, 1,
			// IGConstants.ORDER_NONE, getString(R.string.account_info_title));
			// fileMenu.addSubMenu(menuEnumerator.ACCOUNT_SETTINGS, 2,
			// IGConstants.ORDER_NONE,
			// getString(R.string.maintain_address_menu_title));
			// fileMenu.addSubMenu(menuEnumerator.ACCOUNT_SETTINGS, 3,
			// IGConstants.ORDER_NONE,
			// getString(R.string.printer_configure_title));
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		String userId = IngogoApp.getSharedApplication().getUserId();
		String password = IngogoApp.getSharedApplication().getPassword();
		String plateNumber = IGUtility.getDefaults(IGConstants.kPlateNumber,
				this);
		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager
				.getRunningTasks(1);

		ComponentName componentInfo = taskInfo.get(0).topActivity;
		Log.i("Current top activity", "" + componentInfo.getShortClassName());
		if (getWindow().getCurrentFocus() != null) {

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
					.getApplicationWindowToken(), 0);
		}
		_currentSelectedMenuItem = item;
		switch (item.getItemId()) {
		case menuEnumerator.LOGOUT:
			logoutFromServer(userId, password);
			break;
		case menuEnumerator.JOBS:
			invokeCurrentDriverStateAPI(userId, password, plateNumber);
			break;
		// case menuEnumerator.ACCOUNT_INFO:
		// if (!(componentInfo.getShortClassName()
		// .equals(IGConstants.kAccountInfoActivityName))) {
		// showPasswordVerificationPopUp();
		// }
		// break;
		case menuEnumerator.PRINT_REFFERAL:
			if (!(componentInfo.getShortClassName()
					.equals(IGConstants.kPrintReferralActivityName))) {
				goToPrintReferralPage();
			}
			break;
		case menuEnumerator.HELP:
			if (!(componentInfo.getShortClassName()
					.equals(IGConstants.kHelpActivityName))) {
				goToHelpPage();
			}
			break;
		case menuEnumerator.TRIP_HISTORY_SUMMARY:
			if (!componentInfo.getShortClassName().equals(
					IGConstants.kPaymentDailyHistoryActivityName)
					&& !componentInfo.getShortClassName().equals(
							IGConstants.kPaymentDetailsActivityName)) {
				goToTripHistorySummaryPage();
			}
			break;
		case menuEnumerator.PAYMENT_OPTION:
			if (getWindow().getDecorView().findViewById(android.R.id.content) != null) {
				registerForContextMenu(getWindow().getDecorView().findViewById(
						android.R.id.content));
				openContextMenu(getWindow().getDecorView().findViewById(
						android.R.id.content));
			}

			break;
		case menuEnumerator.CASH_RECEIPT:
			if (!componentInfo.getShortClassName().equals(
					IGConstants.kCashReceiptActivityName)) {
				goToCashReceiptPage();
			}
			break;
		case menuEnumerator.PRACTICE_SWIPE:
			if (!componentInfo.getShortClassName().equals(
					IGConstants.kSwipePracticeActivityName)) {
				goToSwipePracticePage();
			}
			break;
		// case menuEnumerator.MAINTAIN_ADDRESS:
		// if (!componentInfo.getShortClassName().equals(
		// IGConstants.kAddressDetailsActivityName)) {
		// goToAddressDetailsPage();
		// }
		// break;
		// case menuEnumerator.PRINTER_CONFIG:
		// if (!componentInfo.getShortClassName().equals(
		// IGConstants.kPrinterConfigActivityName)) {
		// initialisePrinterConfig();
		// }
		// break;
		case menuEnumerator.REPRINT_RECEIPT:
			reprintReceipt();
			break;
		case menuEnumerator.ACCOUNT_SETTINGS:
			// TODO insert action
			if (getWindow().getDecorView().findViewById(android.R.id.content) != null) {
				registerForContextMenu(getWindow().getDecorView().findViewById(
						android.R.id.content));
				openContextMenu(getWindow().getDecorView().findViewById(
						android.R.id.content));
			}
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		ActivityManager activityManager = (ActivityManager) IGBaseActivity.this
				.getSystemService(ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager
				.getRunningTasks(1);

		ComponentName componentInfo = taskInfo.get(0).topActivity;
		switch (item.getItemId()) {
		case R.id.menu_account_info:
			if (!(componentInfo.getShortClassName()
					.equals(IGConstants.kAccountInfoActivityName)) && !(componentInfo.getShortClassName()
							.equals(IGConstants.kAccountInfoRecordedOrNonRecordedActivityName)) && !(componentInfo.getShortClassName()
									.equals(IGConstants.kAccountInfoRecordedListActivityName))) {
				showPasswordVerificationPopUp();
			}
			return true;
		case R.id.menu_maintain_address:
			if (!componentInfo.getShortClassName().equals(
					IGConstants.kAddressDetailsActivityName)) {
				goToAddressDetailsPage();
			}
			return true;
		case R.id.menu_printer_config:
			if (!componentInfo.getShortClassName().equals(
					IGConstants.kPrinterConfigActivityName)) {
				initialisePrinterConfig();
			}
			return true;
		case R.id.ingogo_pay:
			if (!componentInfo.getShortClassName().equals(
					IGConstants.kFindPassengerActivityName)) {
				jobProgressStatus = true;
				isIngogoPay = true;
				callTakePaymentApi();
			}
			return true;
		case R.id.swipe_pay:
			if (!componentInfo.getShortClassName().equals(
					IGConstants.kSwipeCalculatorActivityName)) {
				IGBaseWebserviceThreadPool.getSharedInstance()
						.shutDownThreadPool();
				jobProgressStatus = true;
				isIngogoPay = false;
				Intent swipePay = new Intent(this,
						IGSwipeCalculatorActivity.class);
				IGSwipeCalculatorActivity.clearCachedValues();
				swipePay.putExtra("isUnknownPassenger", true);

				swipePay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// swipePay.putExtra("isUnknownPassenger", true);
				IngogoApp.getSharedApplication().setMeterFare("00.00");
				startActivity(swipePay);
			}
			return true;
		}
		return super.onContextItemSelected(item);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		if (_currentSelectedMenuItem.getItemId() == menuEnumerator.ACCOUNT_SETTINGS) {

			IGBaseActivity.this.getMenuInflater().inflate(
					R.menu.account_settings_context, menu);
		} else {
			IGBaseActivity.this.getMenuInflater().inflate(
					R.menu.payment_option, menu);
		}

	}

	private void goToTripHistorySummaryPage() {
		Intent intent = new Intent(this, IGPaymentHistorySummaryActivity.class);
		startActivity(intent);

	}

	private void goToSwipePracticePage() {
		Intent intent = new Intent(this, IGSwipePracticeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void goToCashReceiptPage() {
		Intent intent = new Intent(this, IGCashReceiptActivity.class);
		startActivity(intent);
	}

	private void goToAddressDetailsPage() {
		IGAddressDetailsActivity.clearContactInfo();
		Intent intent = new Intent(this, IGAddressDetailsActivity.class);
		startActivity(intent);
	}

	/**
	 * Shows a custom pop up dialog with an option to enter the password.
	 */
	private void showPasswordVerificationPopUp() {

		IGCustomDialog.Builder customBuilder = new IGCustomDialog.Builder(this);
		customBuilder.setTitle((String) getResources().getText(
				R.string.verify_pwd_title));
		_dialog = customBuilder.create();
		_dialog.setCancelable(true);
		_dialog.show();

		_continueBtn = (Button) _dialog.findViewById(R.id.verifyPwdBtn);
		_passwordText = (EditText) _dialog.findViewById(R.id.verifyPwdEditText);

		_passwordText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

				Log.i("PWD ENTERD", "" + _passwordText.getText().toString());
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
	 * Function to determine the states of the continue button of the verify
	 * password pop up
	 */
	private void continueButtonState() {
		if (validateField()) {
			_continueBtn.setEnabled(true);
		} else {
			_continueBtn.setEnabled(false);
		}
	}

	/**
	 * Function to validate the password field in the verify password pop up
	 * 
	 * @return true if the password entered is a valid one.
	 */
	private boolean validateField() {
		String passwordString = _passwordText.getText().toString();
		if (passwordString.equals("")) {
			return false;
		}

		if (passwordString.contains(" ") || passwordString.length() < 4
				|| passwordString.length() > 10) {
			return false;
		}

		return true;

	}

	/**
	 * Function to execute the continue button action. Continue btn of the
	 * verify password pop up.
	 * 
	 * @param view
	 */
	public void continueBtnClicked(View view) {

		String password = _passwordText.getText().toString();
		Log.i("PWD ENTERD", "" + password);

		// Call the verify password api
		IGVerifyPasswordApi verifyPwdApi = new IGVerifyPasswordApi(password,
				this);
		verifyPwdApi.verifyPwd();

		_dialog.cancel();
		_progressDialog = IGUtility.showProgressDialog(this);

	}

	/**
	 * Function to navigate to the account info activity.
	 */
	public void navigateToAccountInfo( JSONObject jObject ) {
		boolean isLoadAndGo = false;
		try {
			isLoadAndGo = jObject.getBoolean("usesLoadAndGo");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		boolean isAccountRegistered = false;
		try {
			isAccountRegistered = jObject.getBoolean("accountRegistered");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager
				.getRunningTasks(1);

		ComponentName componentInfo = taskInfo.get(0).topActivity;

		Log.i("TOP ACTIVITY", "getClassName:" + componentInfo.getClassName());
		Log.i("TOP ACTIVITY",
				"getPaIGUtilityckageName:" + componentInfo.getPackageName());
		Log.i("TOP ACTIVITY",
				"getShortClassName:" + componentInfo.getShortClassName());
		Log.i("TOP ACTIVITY", "getClass:" + componentInfo.getClass());
		Intent accountIntent = null;
		if(isLoadAndGo) {
			if (!(componentInfo.getShortClassName()
					.equals(IGConstants.kAccountInfoRecordedOrNonRecordedActivityName)) && !(componentInfo.getShortClassName()
							.equals(IGConstants.kAccountInfoRecordedListActivityName))) {
				accountIntent = new Intent(this, IGAccountInfoRecordedOrNonRecordedActivity.class);
				accountIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				accountIntent.putExtra("accountRegitered", isAccountRegistered);
				startActivity(accountIntent);
			}
			return;
		} 
		if (!(componentInfo.getShortClassName()
				.equals(IGConstants.kAccountInfoActivityName))) {
			accountIntent = new Intent(this, IGAccountInfoActivity.class);
			accountIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(accountIntent);
		} 
	}

	/**
	 * Function to process the logout webservice. Performs logging out locally.
	 * Clears the active Ingogo notification.
	 */
	private void performLogout() {
		// stop calling currentLocationApi
		stopPositionLocationUpdateApi();
		// Set the previous id for next login,we have to compare with the new
		// id.If both are same clear the job list which we stored in the
		// application context
		IngogoApp.getSharedApplication().setPreviousUserId(
				IngogoApp.getSharedApplication().getUserId());
		// Remove all locally stored Data.
		IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
		IngogoApp.getSharedApplication().removeLoginCredentials();
		IngogoApp.getSharedApplication().setLoggedIn(false);

		// The driver is available in the web service,after logged in. Therefore
		// driver is set to available,after logged out.IGUtility
		IngogoApp.setAvailable(true);

		// Navigate to Login activity.
		// Set the status flag as off line to clear the locally saved data
		// from login activity.
		Intent intent = new Intent(this, IGSignupActivity.class);
		intent.putExtra(IGConstants.kStatus, IGConstants.driverOffline);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);

	}

	/**
	 * Checks whether the current activity is jobs activity, if not navigates to
	 * the jobs activity.
	 */
	private void goToJobsActivity() {

		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager
				.getRunningTasks(1);

		ComponentName componentInfo = taskInfo.get(0).topActivity;

		if (!(componentInfo.getShortClassName()
				.equals(IGConstants.kJobsActivityName))) {
			Intent intent = new Intent(this, IGJobsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}

	/*
	 * Function to redirect to help page.
	 */
	public void goToHelpPage() {
		Intent intent = new Intent(this, IGHelpActivity.class);
		startActivity(intent);
	}

	/**
	 * 
	 */
	public void goToPrintReferralPage() {
		Intent intent = new Intent(this, IGPrintReferralActivity.class);
		startActivity(intent);
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
		ViewGroup parent = (ViewGroup) view.getParent();
		parent.setEnabled(false);
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

	private void goToSwipeCalculatorPage(Intent intent, String passengerId) {
		// TODO Auto-generated method stub

		if (isTopActivityDifferentFromNewIntentActivity(IGConstants.kSwipeCalculatorActivityName)) {
			loadSwipeCalculatorPage(intent, passengerId);
		} else if (calculatordataFetchFailed == true) {
			loadSwipeCalculatorPage(intent, passengerId);

		} else {
			showAlertForJobInProgress();
		}
	}

	private void loadSwipeCalculatorPage(Intent intent, String passengerId) {
		IngogoApp.getSharedApplication().setMeterFare("00.00");
		IGSwipeCalculatorActivity._isNavigateFromMenu = true;
		intent = new Intent(this, IGSwipeCalculatorActivity.class);
		IGSwipeCalculatorActivity.clearCachedValues();

		/*
		 * if the passengerid is valid, then it is considers as a ingogo
		 * passnger booking. else non ingogo passenger booking. For navigating
		 * to corresponding payment success page after payment.
		 */
		boolean isUnknownIngogoPassenger = true;
		if (passengerId != null) {
			isUnknownIngogoPassenger = false;
		}

		intent.putExtra(IGConstants.kJobId, _job.getId());
		intent.putExtra("isUnknownPassenger", isUnknownIngogoPassenger);
		intent.putExtra(IGConstants.kPassengerID, passengerId);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}

	private boolean isValidMobileNumber(String mobileNumber) {

		if (mobileNumber.equals("") || mobileNumber.length() < 10) {

			return false;
		} else if (mobileNumber.contains("+") && (mobileNumber.length() != 12)) {

			return false;

		} else if (!mobileNumber.contains("+") && mobileNumber.length() > 10) {

			return false;

		} else {
			if (mobileNumber.length() == 10 || mobileNumber.length() == 12) {
				Pattern p = null;
				if (mobileNumber.length() == 10) {
					p = Pattern.compile("[0-9]*");
				} else {
					p = Pattern.compile("[+][0-9]*");
				}
				Matcher m = p.matcher(mobileNumber);
				if (m.matches() == false) {
					return false;

				} else {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isTopActivityDifferentFromNewIntentActivity(
			String shortClassName) {
		ActivityManager activityManager = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager
				.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		Log.i("Current top activity", "" + componentInfo.getShortClassName());
		if (!(componentInfo.getShortClassName().equals(shortClassName))) {
			return true;

		}
		return false;

	}

	private void goToNormalCalculatorPage(Intent intent, String passengerId) {
		// TODO Auto-generated method stub

		if (isTopActivityDifferentFromNewIntentActivity(IGConstants.kPaymentActivityName)) {
			loadNormalCalculatorPage(intent, passengerId);
		} else if (calculatordataFetchFailed == true) {
			loadNormalCalculatorPage(intent, passengerId);

		} else {
			showAlertForJobInProgress();
		}

	}

	private void loadNormalCalculatorPage(Intent intent, String passengerId) {
		IngogoApp.getSharedApplication().setMeterFare("00.00");
		intent = new Intent(this, IGPaymentActivity.class);
		intent.putExtra(IGConstants.kJobId, _job.getId());
		intent.putExtra(IGConstants.kPassengerID, passengerId);
		intent.putExtra("disableTakePaymentOption", true);
		intent.putExtra("hideHailBookOption", true);

		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		IGPaymentActivity._isNavigateFromMenu = true;
		startActivity(intent);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		if (apiID != IGApiConstants.kCreditDetailsWebServiceId
				&& apiID != IGApiConstants.kCompletedJobWebServiceId) {

			IGUtility.dismissProgressDialog(_progressDialog);

		}
		if (response != null) {
			Log.d("API Response",
					String.valueOf(apiID) + ": " + response.toString());
			// If the response received contains the data key then the contents
			// of the
			// data key is processed. Otherwise the exceptional conditions are
			// handled.
			if (response.containsKey(IGConstants.kDataKey)) {
				JSONObject jObject = (JSONObject) response
						.get(IGConstants.kDataKey);
				try {
					JSONObject msgObject = null;
					msgObject = jObject.getJSONObject("responseMessages");

					if (apiID == IGApiConstants.kLogoutWebServiceId) {
						performLogout();
					} else if (apiID == IGApiConstants.kVerifyPwdWebServiceId) {
						// TODO: Navigate only if the resp code is ok

						navigateToAccountInfo( jObject );

					} 
//					else if (apiID == IGApiConstants.kFindCurrentDriverStateWebServiceId) {
//						processDriverStaleStateResponse(response);
//					} 
					else if (apiID == IGApiConstants.kCreditDetailsWebServiceId) {
						// if
						// (isTopActivityDifferentFromNewIntentActivity(IGConstants.kJobDetailsActivityName))
						// {
						// if
						// (isTopActivityDifferentFromNewIntentActivity(IGConstants.kDriversMapActivityName))
						// {
						_bookingDetails = jObject;
						Log.i("CREDIT RESP ON COMPLETE JOB", ""
								+ _bookingDetails);
						processBookingDetailsResponse(jObject);
						if (_navigateToCalculator) {
							goToCalculator();
							return;
						}
						IGUtility.dismissProgressDialog(_progressDialog);

						// }
						// else {
						// IGUtility
						// .dismissProgressDialog(_progressDialog);
						//
						// }
						// }
						// // else {
						// // IGUtility.dismissProgressDialog(_progressDialog);
						// //

					} else if (apiID == IGApiConstants.kCompletedJobWebServiceId) {
						if (isTopActivityDifferentFromNewIntentActivity(IGConstants.kJobDetailsActivityName)) {
							if (isTopActivityDifferentFromNewIntentActivity(IGConstants.kDriversMapActivityName)) {
								IngogoApp.setJobStatus(jobStatusEnum.COMPLETED);
								_navigateToCalculator = true;
								callCreditDetailsApi();
							} else {
								IGUtility
										.dismissProgressDialog(_progressDialog);

							}
						} else {
							IGUtility.dismissProgressDialog(_progressDialog);

						}

					}

					// If the response received has the info messages then the
					// info message is
					// showed as an alert.
					if (msgObject != null) {
						JSONArray infoMessagesObj = msgObject
								.getJSONArray(IGConstants.kInformationMessages);
						if (infoMessagesObj != null) {
							for (int i = 0; i < infoMessagesObj.length(); i++) {
								JSONObject infoMessageObj = infoMessagesObj
										.getJSONObject(i);
								String infoCode = IGConstants.CodeHeader
										+ infoMessageObj
												.getString(IGConstants.kCode);
								String infoDesc = IGConstants.ContentHeader
										+ infoMessageObj
												.getString(IGApiConstants.kJSONMessageKey);
								String infoMessageString = infoCode
										+ _commaString + infoDesc;
								IGUtility.showDialogOk(IGConstants.kInfo,
										infoMessageString, this);
							}
						}
					}
				} catch (JSONException e) {

					e.printStackTrace();
					handleDriverStaleState();
				}
			} else {
				// The response received doesnot contains the data key and if
				// the user is logged in
				// then the handleDriverStaleState function is called. Otherwise
				// show an
				// alert with the system unavailable message.
				if (IngogoApp.getSharedApplication().isLoggedIn()) {
					handleDriverStaleState();
				} else {

					IGUtility.showDialogOk(
							IGApiConstants.kErrorMsgKey,
							getResources().getText(
									R.string.system_unavailable_message)
									.toString(), IGBaseActivity.this);
				}
			}

		} else {
			handleDriverStaleState();
		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {

		IGUtility.dismissProgressDialog(_progressDialog);
		/**
		 * If the user is not logged in when the response is received then do
		 * not process the response
		 */
		if ((apiID != IGApiConstants.kLoginWebServiceId)
				&& (apiID != IGApiConstants.kVersionWebServiceId)
				&& (apiID != IGApiConstants.kResetPasswordWebServiceId)) {
			if (!(IngogoApp.getSharedApplication().isLoggedIn())
					|| (IngogoApp.getSharedApplication().getUserId() == null)) {
				return;
			}
		}
		String[] errorMessages = parseErrorMessages(errorResponse);
		if (errorMessages != null) {
			for (String errorMessageString : errorMessages) {

				if (errorMessageString.trim().equals(
						IGApiConstants.kExceptionKey)) {
					// If the response contains the exception key and
					// the user is logged in then the handleDriverStaleState
					// function is called, otherwise an alert with system
					// unavailable alert is showed.
					if (IngogoApp.getSharedApplication().isLoggedIn()) {
						handleDriverStaleState();
					} else {
						IGUtility.showDialogOk(
								IGApiConstants.kErrorMsgKey,
								getResources().getString(
										R.string.system_unavailable_message),
								this);
					}
				} else {

					if (errorMessageString.trim().equalsIgnoreCase(
							IGConstants.kUnspecifiedFailureMessage.trim())) {
						errorMessageString = getResources()
								.getString(
										R.string.payment_unspecified_failure_error_string);
					}
					// Whenever the server returns unexpected_error as the error
					// message
					// an alert is showed with application_error_message, all
					// other error messages are
					// showes as such.
					if (errorMessageString.contains(getResources().getString(
							R.string.unexpected_error))) {
						IGUtility.showDialogOk(
								IGApiConstants.kErrorMsgKey,
								getResources().getString(
										R.string.application_error_message),
								this);

					} else {
						if (apiID == IGApiConstants.kVerifyPwdWebServiceId) {

							// If the verify password webservice fails then
							// a alert with retry option is shown.
							showAlertWithRetryOption(errorMessageString);
							return;
						}
						// If the web service polled is complete job, then show
						// the error message returned from server and then
						// navigate to the job list screen on tapping the ok
						// button of the alert.
						if (apiID == IGApiConstants.kCompletedJobWebServiceId) {
							Dialog dlg = new AlertDialog.Builder(this)
									.setTitle("Error")
									.setMessage(errorMessageString)
									.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													goToJobsActivity();
												}
											}).create();
							dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
							dlg.show();
							return;
						}
						if (!isFinishing()) {
							IGUtility.showDialogOk("Error", errorMessageString,
									this);
						}

					}
				}
			}
			return;
		}
		// If the response does not contains an error message and the driver is
		// logged in
		// then handleDriverStaleState function is called. If driver is logged
		// off then
		// system_unavailable_message is showed.
		if (IngogoApp.getSharedApplication().isLoggedIn()) {

			handleDriverStaleState();
		} else {
			IGUtility.showDialogOk(IGApiConstants.kErrorMsgKey, getResources()
					.getString(R.string.system_unavailable_message), this);
		}

	}

	/**
	 * Function to show an alert with cancel and retry option
	 * 
	 * @param errorMessageString
	 */
	private void showAlertWithRetryOption(String errorMessageString) {
		Dialog dlg = new AlertDialog.Builder(IGBaseActivity.this)
				.setTitle(IGApiConstants.kErrorMsgKey)
				.setMessage(errorMessageString)
				.setPositiveButton(getResources().getText(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						})
				.setNegativeButton(getResources().getText(R.string.retry),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								showPasswordVerificationPopUp();

							}
						}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.show();
	}

	/**
	 * Handles the driver's stale state. Shows an alert message with data time
	 * out error message. Calls checkNetworkAvailability.
	 */
	public void handleDriverStaleState() {

		if (alert == null) {
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			Context appContext = IngogoApp.getSharedApplication()
					.getApplicationContext();
			alertbox.setMessage(appContext
					.getString(R.string.data_time_out_error_message));
			alertbox.setTitle("Error");
			alertbox.setCancelable(false);
			alertbox.setOnKeyListener(new DialogInterface.OnKeyListener() {
				// To disable the search button.
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_SEARCH
							&& event.getRepeatCount() == 0) {
						return true;
					}
					return false;
				}

			});

			alert = alertbox.create();
			alert.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			if (!isFinishing())
				alert.show();
		}
		if (_handler == null) {
			_handler = new Handler();
		}

		_handler.postDelayed(_timerTaskRunnable, _asyncTaskInitialDelay);

		if (_handlerForLogout == null) {
			_handlerForLogout = new Handler();
		}

		_handlerForLogout.postDelayed(_logoutRunnable, IGConstants.logoutDelay);

	}

	/**
	 * Function for parsing the error message received from server. Returns the
	 * error message string.
	 * 
	 * @param response
	 * @return messages
	 */
	protected String[] parseErrorMessages(Map<String, Object> response) {
		String[] messages = null;
		if (null != response && response.size() > 0) {
			JSONObject jObject = (JSONObject) response
					.get(IGApiConstants.kErrorMsgKey);
			JSONObject msgObject = null;
			try {
				if (jObject.has(IGApiConstants.kResponseMessages)
						&& null != jObject
								.getJSONObject(IGApiConstants.kResponseMessages)) {
					msgObject = jObject
							.getJSONObject(IGApiConstants.kResponseMessages);
					if (msgObject != null) {
						JSONArray messagesObj = msgObject
								.getJSONArray(IGApiConstants.kErrorMessages);
						if (messagesObj != null) {
							messages = new String[messagesObj.length()];
							for (int i = 0; i < messagesObj.length(); i++) {
								JSONObject messageObj = messagesObj
										.getJSONObject(i);

								String desc = messageObj
										.getString(IGApiConstants.kJSONMessageKey);
								String errorMessageString = desc;

								messages[i] = errorMessageString;
							}
						}
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return messages;

	}

	/**
	 * Resets the application. Navigates to the login activity if the apiId is
	 * not the kLoginWebServiceId.
	 * 
	 * @param apiId
	 */
	private void resetApplication(int apiId) {
		if (apiId != IGApiConstants.kLoginWebServiceId) {
			Intent intent = new Intent(IGBaseActivity.this,
					IGSignupActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(IGApiConstants.kHttpStatusKey,
					IGApiConstants.kHttpStatusForbidden);
			startActivity(intent);
		}
	}

	private void authenticationFailureOperations(int apiID) {

		Log.i("authentication failure count", ""
				+ IngogoApp.getSharedApplication().getAuthFaileureCount());
		if (IngogoApp.getSharedApplication().getAuthFaileureCount() < 3) {
			return;
		}

		IngogoApp.getSharedApplication().setAuthFailureCount(0);
		//resetApplication(apiID);
	}

	/**
	 * Function to perform the authentication failure actions. If the response
	 * contains the error message then an alert is displayed with the error
	 * message and resetApplication function is called when the user responds to
	 * the alert. Otherwise the resetApplication function is called without an
	 * alert.
	 * 
	 * @param response
	 * @param apiId
	 */

	public void onAuthenticationFailure(Map<String, Object> response,
			final int apiId) {

		IGUtility.dismissProgressDialog(_progressDialog);
		if (null != alert && alert.isShowing()) {
			alert.dismiss();
		}
		if (null != IGUtility.getCurrentProgressDialog()
				&& IGUtility.getCurrentProgressDialog().isShowing()) {
			IGUtility.dismissProgressDialog(IGUtility
					.getCurrentProgressDialog());
		}

		// Donot show the alerts for polling tasks.
		if (apiId == IGApiConstants.kJobsWebServiceId
				|| apiId == IGApiConstants.kIncomingMessageWebServiceId
				|| apiId == IGApiConstants.kUpdateCurrentPositionWebServiceId) {
			return;
		}

		String[] messages = parseErrorMessages(response);
		if (_authenticationDialog != null && _authenticationDialog.isShowing()) {
			authenticationFailureOperations(apiId);
			return;
		}

		if (messages != null) {
			String username = IngogoApp.getSharedApplication().getUserId();
			if (username == null) {
				username = "";
			}
			if (messages.length > 0) {

				QLog.e("Authentication Failure", messages[0]
						+ " For mobile number" + username);
			} else {
				QLog.e("Authentication Failure", "Authentication Failure"
						+ " For mobile number" + username);

			}
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setMessage(messages[0]);
			alertbox.setTitle(IGApiConstants.kErrorMsgKey);
			alertbox.setNeutralButton(IGApiConstants.kStatusOK,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							authenticationFailureOperations(apiId);

							// Stop everything, logout and restart application.
							// IGBaseActivity.this.resetApplication(apiId);

						}
					});

			_authenticationDialog = alertbox.create();
			_authenticationDialog
					.setVolumeControlStream(AudioManager.STREAM_MUSIC);

			if (!isFinishing())
				_authenticationDialog.show();
			return;

		}
		authenticationFailureOperations(apiId);
		// resetApplication(apiId);

	}

	/**
	 * Performs driver logout. Shows the progress dialog. Calls the logout web
	 * service.
	 * 
	 * @param username
	 * @param password
	 */
	public void logoutFromServer(String username, String password) {

		_progressDialog = IGUtility.showProgressDialog(this);
		IGLogoutApi igLogOutApi = new IGLogoutApi(this);
		igLogOutApi.logout(username, password);
	}

	/**
	 * Finds the driver's current state. Shows the progress dialog. Calls the
	 * findCurrentDriverState web service. *
	 * 
	 * @param username
	 * @param password
	 * @param plateNumber
	 */
	public void invokeCurrentDriverStateAPI(String username, String password,
			String plateNumber) {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGFindCurrentDriverStateApi igFindCurrentDriverStateApi = new IGFindCurrentDriverStateApi(
					this, this);
			igFindCurrentDriverStateApi.findCurrentDriverState(username,
					password, plateNumber, false);
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);

		}

	}

	/**
	 * 
	 */
	private void showAlertForJobInProgress() {
		AlertDialog alertDialog;

		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("");
		adb.setMessage(getString(R.string.jobs_inprogress_alert));
		adb.setCancelable(false);
		adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();

			}
		});
		alertDialog = adb.create();
		alertDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (!isFinishing())
			alertDialog.show();

	}

	/**
	 * Navigates to the job details page. Adds _job as an external intent
	 * parameter and finishes all the activities between the current activity
	 * and job details activity.
	 * 
	 * @param intent
	 */
	private void goToJobDetailsPage(Intent intent) {
		if (isTopActivityDifferentFromNewIntentActivity(IGConstants.kJobDetailsActivityName)) {
			IGJobDetailsActivity._isNavigateFromMenu = true;
			intent = new Intent(this, IGJobDetailsActivity.class);
			intent.putExtra(IGConstants.kJob, _job);
			intent.putExtra("isCollectButtonEnabled", false);

			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else {
			;
			showAlertForJobInProgress();
		}
	}

	private void goToCompleteJobPage(Intent intent) {
		if (isTopActivityDifferentFromNewIntentActivity(IGConstants.kCompleteJobActivityName)) {

			// IGCompleteJobActivity._isNavigateFromMenu = true;
			// intent = new Intent(this, IGCompleteJobActivity.class);
			// intent.putExtra(IGConstants.kJob,
			// Integer.parseInt(_job.getId()));
			// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// startActivity(intent);
			gotoPayment();

		} else {
			IGUtility.dismissProgressDialog(_progressDialog);

			if (!jobProgressStatus) {
				showAlertForJobInProgress();

			}
			jobProgressStatus = false;

		}
	}

	private void gotoPayment() {
		_navigateToCalculator = true;
		callCreditDetailsApi();
		return;

	}

	/**
	 * To call credit details web service.
	 */
	private void callCreditDetailsApi() {

		if (IGUtility.isNetworkAvailable(this)) {
			if (_progressDialog != null && !_progressDialog.isShowing()) {
				_progressDialog = IGUtility.showProgressDialog(this);
			}
			IGCreditDetailsApi creditApi = new IGCreditDetailsApi(this,
					Integer.parseInt(_job.getId()));
			creditApi.getCreditDetails();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	private void goToCalculator() {

		IngogoApp.getSharedApplication().removeStoredMeterFare();
		Intent intent;

		// If the btn tapped is ingogo payment then navigate to the
		// calculator screen otherwise to the offline calculator screen.
		IGUtility.dismissProgressDialog(_progressDialog);

		if (isCorporateAccountPresent(_bookingDetails)) {
			if (!isTopActivityDifferentFromNewIntentActivity(IGConstants.kPaymentActivityName)) {
				showAlertForJobInProgress();
				return;
			}

			intent = new Intent(IGBaseActivity.this, IGPaymentActivity.class);
		} else {
			if (_hasCreditCards) {
				if (!isTopActivityDifferentFromNewIntentActivity(IGConstants.kPaymentActivityName)) {
					showAlertForJobInProgress();
					return;
				}

				intent = new Intent(IGBaseActivity.this,
						IGPaymentActivity.class);
			} else {
				if (!isTopActivityDifferentFromNewIntentActivity(IGConstants.kPaymentsSwipeActivityName)) {
					showAlertForJobInProgress();
					return;
				}
				intent = new Intent(IGBaseActivity.this,
						IGPaymentsSwipeActivity.class);
			}
		}

		intent.putExtra(IGConstants.kJobId, _job.getId());

		if (_bookingDetails != null) {

			HashMap<String, Object> jobDetails = new HashMap<String, Object>();
			jobDetails.put(IGConstants.kDetails, _bookingDetails.toString());

			intent.putExtra(IGConstants.kJobDetails, jobDetails);
		}
		startActivity(intent);
		finish();
	}

	private void processBookingDetailsResponse(JSONObject responseObject) {

		// Check whether the passenger has atleast one card
		// registered with ingogo.
		if (responseObject.has(IGApiConstants.kCardDetails)) {
			try {
				if (responseObject.getJSONArray(IGApiConstants.kCardDetails)
						.length() > 0) {
					_hasCreditCards = true;
				} else {
					_hasCreditCards = false;
				}
			} catch (JSONException e) {
				_hasCreditCards = false;
				e.printStackTrace();
			}
		}

	}

	/**
	 * Navigates to the time to pick up page. Adds _job as an external intent
	 * parameter and finishes all the activities between the current activity
	 * and time to pick up page.
	 * 
	 * @param intent
	 */
	private void goToTimeToPickUpPage(Intent intent) {
		intent = new Intent(this, IGTimeToPickUpActivity.class);
		intent.putExtra(IGConstants.kJob, _job);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * When exception like server down occur,then findDriverState web service is
	 * invoked in this method.
	 */
	private void invokeDriverStateApiForException() {
		// final String username = IngogoApp.getSharedApplication().getUserId();
		// final String password =
		// IngogoApp.getSharedApplication().getPassword();
		// final String plateNumber = IGUtility.getDefaults(
		// IGConstants.kPlateNumber, IGBaseActivity.this);
		// IGFindCurrentDriverStateApi igFindCurrentDriverStateApi = new
		// IGFindCurrentDriverStateApi(
		// IGBaseActivity.this);
		// igFindCurrentDriverStateApi.findCurrentDriverState(username,
		// password,
		// plateNumber);
		try {

			final String username = IngogoApp.getSharedApplication()
					.getUserId();
			final String password = IngogoApp.getSharedApplication()
					.getPassword();
			final String plateNumber = IGUtility.getDefaults(
					IGConstants.kPlateNumber, IGBaseActivity.this);
			IGFindCurrentDriverStateApi igFindCurrentDriverStateApi = new IGFindCurrentDriverStateApi(
					IGBaseActivity.this, IGBaseActivity.this);
			igFindCurrentDriverStateApi.findCurrentDriverState(username,
					password, plateNumber, true);

		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

	// Stop polling current position web service call ,when the driver is logged
	// out.
	private void stopPositionLocationUpdateApi() {
		if (IGUpdatePositionPollingTask._handler != null
				&& IGUpdatePositionPollingTask._positionPoller != null) {
			IGUpdatePositionPollingTask._handler
					.removeCallbacks(IGUpdatePositionPollingTask._positionPoller);
			IGUpdatePositionPollingTask._positionPoller = null;
			IGUpdatePositionPollingTask._handler = null;
		}
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		// IGUtility.dismissProgressDialog(_progressDialog);
		if (_unavailableDialog != null && _unavailableDialog.isShowing()) {
			return;
		}
		if (alert != null && alert.isShowing()) {
			invokeDriverStateApiForException();
			return;
		}
		handleDriverStaleState();
		// IGUtility.showDialog(null,
		// (String)errorResponse.get(IGApiConstants.kApiFailedMsgKey), this);

	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		// IGUtility.dismissProgressDialog(_progressDialog);
		if (_unavailableDialog != null && _unavailableDialog.isShowing()) {
			return;
		}
		if (alert != null && alert.isShowing()) {
			invokeDriverStateApiForException();
			return;
		}
		handleDriverStaleState();

	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		// IGUtility.dismissProgressDialog(_progressDialog);
		if (_unavailableDialog != null && _unavailableDialog.isShowing()) {
			return;
		}
		if (alert != null && alert.isShowing()) {
			invokeDriverStateApiForException();
			return;
		}
		handleDriverStaleState();

	}

	@Override
	public void onNullResponseRecieved() {
		// IGUtility.dismissProgressDialog(_progressDialog);
		if (_unavailableDialog != null && _unavailableDialog.isShowing()) {
			return;
		}
		if (alert != null && alert.isShowing()) {
			invokeDriverStateApiForException();
			return;
		}
		handleDriverStaleState();

	}

	@Override
	public void takePaymentCompleted(boolean status) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (status) {

			if (isIngogoPay) {
				Intent ingogPay = new Intent(this,
						IGFindPassengerActivity.class);
				IGFindPassengerActivity.clearCachedPassengerInfo();
				startActivity(ingogPay);
			} else {
				Intent swipePay = new Intent(this,
						IGSwipeCalculatorActivity.class);
				IGSwipeCalculatorActivity.clearCachedValues();
				swipePay.putExtra("isUnknownPassenger", true);

				swipePay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// swipePay.putExtra("isUnknownPassenger", true);
				IngogoApp.getSharedApplication().setMeterFare("00.00");
				startActivity(swipePay);
			}

		} else {
			Dialog dlg = new AlertDialog.Builder(this)
					.setTitle("Error")
					.setMessage(
							"Job in progress. Follow existing system prompts")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									_progressDialog = IGUtility
											.showProgressDialog(IGBaseActivity.this);
									invokeDriverStateApiForException();
								}
							}).create();
			dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			if (!isFinishing())
				dlg.show();

		}
	}

	@Override
	public void takePaymentFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);

		Dialog dlg = new AlertDialog.Builder(this).setTitle("Error")
				.setMessage(errorMessage)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						_progressDialog = IGUtility
								.showProgressDialog(IGBaseActivity.this);
						invokeDriverStateApiForException();
					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (!isFinishing())
			dlg.show();
		return;
	}

	private class DeviceBroadcast extends BroadcastReceiver {
		private boolean _deviceConnected = false;

		@Override
		public void onReceive(Context aContext, Intent aIntent) {
			int headSetState = aIntent.getIntExtra("state", 0);
			// int hasMicrophone = aIntent.getIntExtra("microphone", 0);
			// if (headSetState == 1 && hasMicrophone == 1) {
			if (headSetState == 1) {

				_deviceConnected = true;

			} else {
				_deviceConnected = false;
				IngogoApp.setPrimaryCardReaderAttached(true);
				IngogoApp.setInitialCardCheck(true);

			}
			// AudioManager audioManager = (AudioManager)
			// getSystemService(getApplicationContext().AUDIO_SERVICE);
			//
			// if (_deviceConnected) {
			// Log.e("CONNECTED_READER", "CONNECTED_READER");
			//
			// if (!IGUtility.getTopActivity().equals(
			// IGConstants.kSwipeCardActivityName)
			// && !IGUtility.getTopActivity().equals(
			// IGConstants.kSwipeCalculatorActivityName)
			// && !IGUtility.getTopActivity().equals(
			// IGConstants.kSwipePracticeActivityName)
			// && !IGUtility.getTopActivity().equals(
			// IGConstants.kPaymentsSwipeActivityName)) {
			//
			// // IGJobsActivity.stopJobRemainderAlertRunnable();
			// audioManager.setMode(AudioManager.MODE_IN_CALL);
			// audioManager.setSpeakerphoneOn(true);
			// } else {
			// Log.e("IN_CARD_ACTIVTY", "IN_CARD_ACTIVTY");
			// audioManager.setMode(AudioManager.MODE_NORMAL);
			// audioManager.setSpeakerphoneOn(false);
			//
			// }
			//
			// } else {
			// Log.e("DISCONNECTED_READER", "DISCONNECTED_READER");
			//
			// audioManager.setMode(AudioManager.MODE_IN_CALL);
			// audioManager.setSpeakerphoneOn(true);
			//
			// }
		}

		public boolean isDeviceConnected() {
			return _deviceConnected;
		}
	}

	public boolean isDeviceConnected() {
		return _deviceBroadcast.isDeviceConnected();
	}

	public static void writeReceipt(IGReceiptInformationModel receiptInfo) {

		boolean displayDriverCompanyDetails = receiptInfo
				.isDisplayDriverCompanyDetails();

		String _driverDetail = "";

		if (displayDriverCompanyDetails) {
			_driverDetail = "\n~Driver's TAX INVOICE~\n"
					+ receiptInfo.getDriverCompanyName()
					+ receiptInfo.getDriverABN().trim();
		}

		String pickup = "";
		if (receiptInfo.getPickUp() != null) {
			if (receiptInfo.getPickUp().length() > 0) {
				pickup = "\nPick up: " + receiptInfo.getPickUp();
			}

		}

		String paidwith = "";
		if (receiptInfo.getPaidWith() != null) {
			if (receiptInfo.getPaidWith().length() > 0) {
				paidwith = "\nPaid with: " + receiptInfo.getPaidWith();
			}
		}

		String _content = "\n~ingogo trip receipt~" + "\nview e-receipt at:\n"
				+ receiptInfo.getReceiptUrl() + "\n\n" + receiptInfo.getDate()
				+ " at " + receiptInfo.getTime() + pickup + "\nPaid at: "
				+ receiptInfo.getPaidAt() + "\nTaxi Number: "
				+ receiptInfo.getTaxiPlate() + paidwith + "\n" + _driverDetail
				+ "\n~Fare~ (inc GST)      " + "~"
				+ receiptInfo.getFareNetPaid() + "~"
				+ "\n\n~ingogo TAX INVOICE~\ningogo Pty Ltd\nABN "
				+ IGConstants.ABN + "\nCard fee            "
				+ receiptInfo.getCcSurcharge() + "\n~TOTAL PAID~          "
				+ "~" + receiptInfo.getTotalPaid() + "~"
				+ "\n(inc GST)           " + receiptInfo.getGstOnTotalPaid()
				+ "\n\n~Save "
				+ IngogoApp.getSharedApplication().getSavingsPercentage()
				+ "%~ get the ingogo App for\niPhone & Android. Watch your\n"
				+ "cab approach live on a GPS map!\n\n\n";

//		File sdcard = Environment.getExternalStorageDirectory();
//		File file = new File(sdcard, "receipt.txt");
		 File file = getFileWithName("receipt.txt");

		if (file.exists()) {
			file.delete();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
			buf.append(_content);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printFile(final IGBluetoothHelper bluetoothHelper) {
		// Find the directory for the SD Card using the API
		// *Don't* hardcode "/sdcard"
//		File sdcard = Environment.getExternalStorageDirectory();
//
//		// Get the text file
//		File file = new File(sdcard, "receipt.txt");
		 File file = getFileWithName("receipt.txt");


		// Read text from file

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				/*
				 * if (line.length() > SINGLE_LINE_LENGTH &&
				 * !(line.contains("~_"))) {
				 * 
				 * printLargeReceiptItem(line, bluetoothHelper); continue; }
				 */
				String printingData = LinePrintHelper.getPrinterFeed(line);
				String data = printingData + LINE_FEED;
				bluetoothHelper.write(data.getBytes());
			}
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}

		if (file.exists()) {
			file.delete();
		}
		Dialog dlg = new AlertDialog.Builder(IGBaseActivity.this)
				.setTitle("Verify Print")
				.setMessage(
						"If a receipt has not been printed, ensure the device is powered on and try again")
				.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						IGBaseActivity.this.finishReprintReceipt();
						IGBaseActivity.this.finishPrinting();
					}
				}).create();
		dlg.show();
		// Find the view by its id
	}

	private void printLargeReceiptItem(String item,
			IGBluetoothHelper bluetoothHelper) {
		String firstPart = item.substring(0, SINGLE_LINE_LENGTH);
		String secondPart = item.substring(SINGLE_LINE_LENGTH, item.length());
		String printingData = LinePrintHelper.getPrinterFeed(firstPart);
		String data = printingData + LINE_FEED;
		bluetoothHelper.write(data.getBytes());
		if (secondPart.length() > SECONDPART_LENGTH) {
			processLargeSecondPart(secondPart, bluetoothHelper);
		} else {

			printSecondPartWithSpace(secondPart, bluetoothHelper);

		}
	}

	private void processLargeSecondPart(String item,
			IGBluetoothHelper bluetoothHelper) {
		do {
			String firstPart = item.substring(0, SECONDPART_LENGTH);
			item = item.substring(SECONDPART_LENGTH, item.length());
			printSecondPartWithSpace(firstPart, bluetoothHelper);
		} while (item.length() > SECONDPART_LENGTH);

		if (item.length() > 0) {
			printSecondPartWithSpace(item, bluetoothHelper);

		}
	}

	private void printSecondPartWithSpace(String secondPart,
			IGBluetoothHelper bluetoothHelper) {
		secondPart = SPACESTRING + secondPart;
		String printingData = LinePrintHelper.getPrinterFeed(secondPart);
		String data = printingData + LINE_FEED;
		bluetoothHelper.write(data.getBytes());
	}

	public void initialisePrinterConfig() {
		Intent intent = new Intent(this, IGPrinterConfigActivity.class);
		startActivity(intent);
	}

	public void reprintReceipt() {
		Intent intent = new Intent(this, IGReprintReceiptActivity.class);
		startActivity(intent);
	}

	protected void finishReprintReceipt() {

	}

	protected void finishPrinting() {

	}

	protected boolean isCorporateAccountPresent(JSONObject bookingObject) {
		boolean isPresent = false;
		if (bookingObject != null) {
			if (bookingObject.has(ACCOUNT_DETAIL_KEY)) {
				try {
					JSONArray array = bookingObject
							.getJSONArray(ACCOUNT_DETAIL_KEY);
					if (array.length() > 0) {
						isPresent = true;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {

				}
			}

		}
		return isPresent;

	}

	@Override
	public void findCurrentDriverStateCompleted(
			ArrayList<IGJobAvailableModel> bookingSummaries,
			IGReceiptInformationModel receiptInformation) {

		processDriverStaleStateResponse(bookingSummaries, receiptInformation);
	}

	/**
	 * Function to process findDriverCurrentState web service. If the booking
	 * summaries array is null then navigates the driver to booking list page
	 * other wise call processBookingSumamriesArray web service.
	 * 
	 ** @param response
	 */
	private void processDriverStaleStateResponse(
			final ArrayList<IGJobAvailableModel> bookingSummaries,
			final IGReceiptInformationModel receiptInformation) {

		if (alert != null && alert.isShowing()) {
			alert.getCurrentFocus();
			alert.dismiss();
			alert = null;
		}

		_handlerForLogout.removeCallbacks(_logoutRunnable);

		/**
		 * If the user is not logged in when the response is received then do
		 * not process the response
		 */
		if (!(IngogoApp.getSharedApplication().isLoggedIn())
				|| (IngogoApp.getSharedApplication().getUserId() == null)) {
			IGUtility.dismissProgressDialog(_progressDialog);
			return;
		}

		if (receiptInformation != null) {
			IGUtility.dismissProgressDialog(_progressDialog);

			try {
				final Dialog dialog = new Dialog(this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.last_payment_info_dialog);
				dialog.setCancelable(false);
				ImageButton printBtn = (ImageButton) dialog
						.findViewById(R.id.printReceiptButton);
				printBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(IGBaseActivity.this,
								IGReprintReceiptActivity.class);
						intent.putExtra(IGConstants.kReceiptInformationKey,
								receiptInformation);
						startActivity(intent);

					}
				});
				ImageButton continueBtn = (ImageButton) dialog
						.findViewById(R.id.continueButton);
				continueBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						_infoDialogShown = false;
						processBookingSummary(bookingSummaries);
					}
				});
				TextView amountTv = (TextView) dialog
						.findViewById(R.id.amountTv);
				amountTv.setText(receiptInformation.getTotalPaid());
				TextView timeTv = (TextView) dialog.findViewById(R.id.timeTv);
				timeTv.setText(receiptInformation.getDate() + " at "
						+ receiptInformation.getTime());

				_infoDialogShown = true;
				dialog.show();
				return;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		processBookingSummary(bookingSummaries);
	}

	protected void processBookingSummary(
			ArrayList<IGJobAvailableModel> bookingSummaries) {

		Log.d("bookingSummaryarrayLength", "" + bookingSummaries.size());

		// If a job is in progress then the value for kJobInProgress
		// stored in
		// shared preference will be true, otherwise the kJobInProgress
		// has
		// to be removed from the shared preference.
		if (bookingSummaries.size() == 0) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			IGUtility.dismissProgressDialog(_progressDialog);
			goToJobsActivity();
		} else {
			processBookingSumamriesArray(bookingSummaries);
		}
	}

	/**
	 * Process the booking details array. If the driver status is
	 * accepted/collected then call goToJobDetails page. If the driver status is
	 * paymentDue then call payment page.
	 * 
	 * @param bookingSummaryArray
	 */
	private void processBookingSumamriesArray(
			ArrayList<IGJobAvailableModel> bookingSummaries) {

		IGJobAvailableModel jobDetails = null;

		if (bookingSummaries.size() == 1) {
			jobDetails = bookingSummaries.get(0);
		} else {
			for (int i = 0; i < bookingSummaries.size(); i++) {
				if (bookingSummaries.get(i).getBookingStatus().trim()
						.equals(IGConstants.kPaymentDue)) {
					jobDetails = bookingSummaries.get(i);
					break;
				}
			}
		}

		// Process all the details regarding the current available jobs.
		parseJobDetails(jobDetails);
		String driverStatus = "";
		if (jobDetails.getBookingStatus() != null) {
			driverStatus = jobDetails.getBookingStatus();
		} else {
			driverStatus = "";
		}

		boolean hasRegisteredCards = false;
		if (jobDetails.isHasRegisteredCard()) {
			hasRegisteredCards = jobDetails.isHasRegisteredCard();
		} else {
			hasRegisteredCards = false;
		}

		String passengerId = null;
		if (jobDetails.getPassengerId() != null) {
			passengerId = jobDetails.getPassengerId();
		}

		/*
		 * Navigate to corresponding pages according to booking type. ie
		 * according normal booking or payment booking
		 */
		if (jobDetails.getBookingType() != null) {
			String bookingType = jobDetails.getBookingType();

			if (bookingType.equals(NORMAL_BOOKING)) {
				processNormalBookingWithStatus(driverStatus, passengerId);
			} else {
				IGUtility.dismissProgressDialog(_progressDialog);
				processPaymentBookingWithStatus(driverStatus,
						hasRegisteredCards, passengerId);
			}
		} else {
			processNormalBookingWithStatus(driverStatus, passengerId);
		}

	}

	/**
	 * Function to parse the job details. Saves job id, passenger name, booking
	 * time stamp, pick up address, drop off address, bid extra and bid interval
	 * if they are not null.
	 * 
	 * @param jObject
	 */
	private void parseJobDetails(IGJobAvailableModel jobDetail) {

		if (jobDetail.getBookingId() != null) {
			_job.setId(jobDetail.getBookingId());
		}
		if (jobDetail.getPassengerName() != null) {
			_job.setPassengerName(jobDetail.getPassengerName());
		}
		if (jobDetail.getBooked() != 0) {
			_job.setTimeStamp(jobDetail.getBooked());
		}
		if (jobDetail.getPickupFrom() != null) {
			_job.setPickupFrom(getFullAddressFromObject(jobDetail
					.getPickupFrom()));
		}

		if (jobDetail.getDropOffAt() != null) {
			// If suburb field is null then the drop off address is set as
			// empty .
			String dropOffAddr = jobDetail.getDropOffAt().getSuburb();
			if (dropOffAddr == null
					|| dropOffAddr.equalsIgnoreCase(IGConstants.kNull)) {
				dropOffAddr = "";
			}
			_job.setDropOffTo(dropOffAddr);
		}

		String extraPayString = jobDetail.getBidExtra();
		try {
			_job.setExtraOffer(Float.parseFloat(extraPayString));
		} catch (NumberFormatException ex) {
			_job.setExtraOffer(0.0f);
		} catch (NullPointerException e) {
			_job.setExtraOffer(0.0f);
		}

		if (jobDetail.getBidInterval() != null) {
			try {
				_job.setTimeWithin(Integer.parseInt(jobDetail.getBidInterval()));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		if (jobDetail.getPassengerMobileNumber() != null) {
			_job.setPassengermobileNumber(jobDetail.getPassengerMobileNumber());
		}

	}

	/**
	 * Function for pick up address formatting. Formats the given address object
	 * in the following format building name, unit number/street number address
	 * line 1, address line 2, address line 3, suburb. If any of the fields
	 * mentioned in the above format is missing then the missing fields are
	 * avoided and address is returned.
	 * 
	 * @param igAddress
	 * @return formatted full address
	 */
	private String getFullAddressFromObject(IGAddress igAddress) {
		String addressString = "";

		// If the addrObj contains building name then it is added to the
		// address string
		// and a ',' is added at the end.
		String buildingName = igAddress.getBuildingName();
		if (buildingName == null
				|| buildingName.equalsIgnoreCase(IGConstants.kNull)) {
			buildingName = "";
		} else {
			addressString += "" + buildingName + _commaString;
		}

		// If the addrObj contains unit number then it is added to the
		// address string.
		String unitNumber = igAddress.getUnitNumber();
		if (unitNumber == null
				|| unitNumber.equalsIgnoreCase(IGConstants.kNull)) {
			unitNumber = "";
		} else {
			addressString += "" + unitNumber;
		}

		// If the addrObj contains the street number then it is added to the
		// address string according to the following rules:
		// If building name and unit number are empty or only unit number is
		// empty
		// then street number is added to the address string.
		// Other wise a slash is added to the address string and then
		// the street number is added.
		String streetNumber = igAddress.getStreetNumber();
		if (streetNumber == null
				|| streetNumber.equalsIgnoreCase(IGConstants.kNull)) {
			streetNumber = "";
		} else {
			if (buildingName.equals("") && unitNumber.equals("")) {
				addressString += "" + streetNumber;
			} else if ((!buildingName.equals("")) && unitNumber.equals("")) {
				addressString += "" + streetNumber;
			} else {
				addressString += _slashString + streetNumber;
			}
		}

		// If the addrObj contains the addressLine1 then it is added to the
		// address string.
		String addressLine1 = igAddress.getAddressLine1();
		if (addressLine1 == null
				|| addressLine1.equalsIgnoreCase(IGConstants.kNull)) {
			addressLine1 = "";
		} else {
			addressString += " " + addressLine1;
		}

		// If the addrObj contains address line 2 then it is added to the
		// address string
		// If addressString is not empty then a comma is added before adding
		// the
		// address line 2.
		String addressLine2 = igAddress.getAddressLine2();
		if (addressLine2 == null
				|| addressLine2.equalsIgnoreCase(IGConstants.kNull)) {
			addressLine2 = "";
		} else {
			if (addressLine1.equals("")) {
				addressString += " " + addressLine2;
			} else if (buildingName.equals("") && unitNumber.equals("")
					&& streetNumber.equals("") && addressLine1.equals("")) {
				addressString += "" + addressLine2;
			} else {
				addressString += _commaString + addressLine2;
			}
		}

		// If the addrObj contains address line 3 then it is added to the
		// address string
		// If addressString is not empty then a comma is added before adding
		// the
		// address line 3.
		String addressLine3 = igAddress.getAddressLine3();
		if (addressLine3 == null
				|| addressLine3.equalsIgnoreCase(IGConstants.kNull)) {
			addressLine3 = "";
		}
		if (addressLine2.equals("")) {
			addressString += " " + addressLine3;
		} else if (buildingName.equals("") && unitNumber.equals("")
				&& streetNumber.equals("") && addressLine1.equals("")
				&& addressLine2.equals("")) {
			addressString += "" + addressLine3;
		} else {
			addressString += _commaString + addressLine3;
		}

		// If the addrObj contains suburb then it is added to the address
		// string
		// If addressString is not empty then a comma is added before adding
		// the
		// suburb.
		String suburb = igAddress.getSuburb();
		if (suburb == null || suburb.equalsIgnoreCase(IGConstants.kNull)) {
			suburb = "";
		} else {
			if (addressLine1.equals("") && addressLine2.equals("")
					&& addressLine3.equalsIgnoreCase("")
					&& unitNumber.equals("") && streetNumber.equals("")) {
				addressString += "" + suburb;
			} else if (addressLine1.equals("") || addressLine2.equals("")
					|| addressLine3.equals("") || unitNumber.equals("")
					|| streetNumber.equals("")) {
				addressString += _commaString + suburb;
			} else {
				addressString += _commaString + suburb;
			}
		}
		return addressString;
	}

	private void processNormalBookingWithStatus(String driverStatus,
			String passengerId) {
		// TODO Auto-generated method stub
		Intent intent = null;

		// When a job is in progress then the value of kJobInProgress
		// stored in shared preference should be true.
		// Set the status of the job in _job to pass it to the
		// job details activity.
		if (driverStatus.equalsIgnoreCase(IGApiConstants.kAcceptKey)) {
			IGUtility.setDefaults(IGConstants.kJobInProgress,
					IGConstants.kTrue, this);
			_job.setStatus(IGConstants.kJobAccepted);
			IGUtility.dismissProgressDialog(_progressDialog);

			goToJobDetailsPage(intent);
			return;
		} else if (driverStatus.equalsIgnoreCase(IGApiConstants.kCollectKey)) {
			IGUtility.setDefaults(IGConstants.kJobInProgress,
					IGConstants.kTrue, this);

			_job.setStatus(IGConstants.kJobCollected);
			IngogoApp.setJobStatus(jobStatusEnum.COLLECTED);
			goToCompleteJobPage(intent);
			return;
		} else if (driverStatus.equalsIgnoreCase(IGApiConstants.kConfirmKey)) {
			IGUtility.setDefaults(IGConstants.kJobInProgress,
					IGConstants.kTrue, this);

			_job.setStatus(IGConstants.kJobCollected);

			IngogoApp.setJobStatus(jobStatusEnum.COLLECTED);
			goToCompleteJobPage(intent);
			return;
		} else if (driverStatus.equalsIgnoreCase(IGApiConstants.kPaymentDue)) {
			IGUtility.setDefaults(IGConstants.kJobInProgress,
					IGConstants.kTrue, this);

			IngogoApp.setJobStatus(jobStatusEnum.PAYMENT_DUE);
			// IGCompleteJobActivity._isPaymentDue = true;
			goToCompleteJobPage(intent);
			return;
		}
	}

	private void processPaymentBookingWithStatus(String driverStatus,
			boolean hasRegisteredCards, String passengerId) {
		IGUtility.dismissProgressDialog(_progressDialog);
		Intent intent = null;
		if (hasRegisteredCards) {
			IGUtility.setDefaults(IGConstants.kJobInProgress,
					IGConstants.kTrue, this);
			goToNormalCalculatorPage(intent, passengerId);
			return;
		} else {
			IGUtility.setDefaults(IGConstants.kJobInProgress,
					IGConstants.kTrue, this);
			goToSwipeCalculatorPage(intent, passengerId);
			return;
		}

	}

	@Override
	public void findCurrentDriverStateFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (_unavailableDialog != null && _unavailableDialog.isShowing()) {
			return;
		}
		if (alert != null && alert.isShowing()) {
			invokeDriverStateApiForException();
			return;
		}

		if (errorMessage != null) {
			if (errorMessage.trim().equals(IGApiConstants.kExceptionKey)) {
				if (IngogoApp.getSharedApplication().isLoggedIn()) {
					handleDriverStaleState();
				} else {
					IGUtility.showDialogOk(
							IGApiConstants.kErrorMsgKey,
							getResources().getString(
									R.string.system_unavailable_message), this);
				}
			} else {
				if (errorMessage.trim().equalsIgnoreCase(
						IGConstants.kUnspecifiedFailureMessage.trim())) {
					errorMessage = getResources().getString(
							R.string.payment_unspecified_failure_error_string);
				}

				if (errorMessage.contains(getResources().getString(
						R.string.unexpected_error))) {
					IGUtility.showDialogOk(
							IGApiConstants.kErrorMsgKey,
							getResources().getString(
									R.string.application_error_message), this);

				} else {
					if (!isFinishing()) {
						// invokeDriverStateApiForException();
						IGUtility.showDialogOk("Error", errorMessage, this);
					}
				}
			}
			return;
		}

		if (IngogoApp.getSharedApplication().isLoggedIn()) {
			handleDriverStaleState();
		} else {
			IGUtility.showDialogOk(IGApiConstants.kErrorMsgKey, getResources()
					.getString(R.string.system_unavailable_message), this);
		}
	}

	@Override
	public void onAuthenticationErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (null != alert && alert.isShowing()) {
			alert.dismiss();
		}
		if (null != IGUtility.getCurrentProgressDialog()
				&& IGUtility.getCurrentProgressDialog().isShowing()) {
			IGUtility.dismissProgressDialog(IGUtility
					.getCurrentProgressDialog());
		}

		// String[] messages = parseErrorMessages(response);
		String messages = null;
		if (errorResponse.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			messages = errorResponse.get(IGApiConstants.kApiFailedMsgKey)
					.toString();
		}
		if (_authenticationDialog != null && _authenticationDialog.isShowing()) {
			// authenticationFailureOperations(2255);
			return;
		}

		if (messages != null) {
			String username = IngogoApp.getSharedApplication().getUserId();
			if (username == null) {
				username = "";
			}
			if (messages.length() > 0) {

				QLog.e("Authentication Failure", messages
						+ " For mobile number" + username);
			} else {
				QLog.e("Authentication Failure", "Authentication Failure"
						+ " For mobile number" + username);

			}
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setMessage(messages);
			alertbox.setTitle(IGApiConstants.kErrorMsgKey);
			alertbox.setNeutralButton(IGApiConstants.kStatusOK,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// authenticationFailureOperations(2255);

							// Stop everything, logout and restart application.
							// IGBaseActivity.this.resetApplication(apiId);

						}
					});

			_authenticationDialog = alertbox.create();
			_authenticationDialog
					.setVolumeControlStream(AudioManager.STREAM_MUSIC);

			if (!isFinishing())
				_authenticationDialog.show();
			return;

		}
		// authenticationFailureOperations(2255);

	}
	
	protected static File getFileWithName(String fileName) {
		ContextWrapper contextWrapper = new ContextWrapper(IngogoApp
				.getSharedApplication().getApplicationContext());
		File directory = contextWrapper.getDir("MyFileStorage",
				Context.MODE_PRIVATE);
		File file = new File(directory, fileName);
		return file;
	}

}