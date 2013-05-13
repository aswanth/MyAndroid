package com.ingogo.android.activities.payments;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import IDTech.MSR.uniMag.UniMagReader;
import IDTech.MSR.uniMag.UniMagReaderMsg;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.activities.IGPaymentBaseActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.cardreader.helpers.CardInfo;
import com.ingogo.android.cardreader.helpers.CardInfoParseException;
import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGCreditCardInformation;
import com.ingogo.android.model.IGCreditCardModel;
import com.ingogo.android.model.IGReceiptInformationModel;
import com.ingogo.android.model.IGSuburbModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGCustomScrollLayout;
import com.ingogo.android.utilities.IGCustomScrollLayout.Listener;
import com.ingogo.android.utilities.IGSuburbListDialog;
import com.ingogo.android.utilities.IGSuburbParser;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCompleteOfflineJobApi;
import com.ingogo.android.webservices.IGProcessPaymentApi;
import com.ingogo.android.webservices.IGProcessPaymentForUnknownPassengerApi;
import com.ingogo.android.webservices.IGSwipeInititaliseApi;
import com.ingogo.android.webservices.beans.response.IGInitialiseUnknownPassengerResponseBean;
import com.ingogo.android.webservices.beans.response.IGProcessPaymentForUnknownPassengerResponseBean;
import com.ingogo.android.webservices.beans.response.IGSwipeInititaliseResponseBean;
import com.ingogo.android.webservices.interfaces.IGCompleteOfflineListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGInitialiseUnknownPassengerApiListener;
import com.ingogo.android.webservices.interfaces.IGProcessPaymentApiListener;
import com.ingogo.android.webservices.interfaces.IGProcessPaymentForUnknownPassengerApiListener;
import com.ingogo.android.webservices.interfaces.IGSuburbsReadsFileListener;
import com.ingogo.android.webservices.interfaces.IGSwipeInititaliseApiListener;

/**
 * 
 * @author suslov
 * 
 */
public class IGSwipeCalculatorActivity extends IGPaymentBaseActivity implements
		IGSwipeInititaliseApiListener, IGProcessPaymentApiListener,
		IGInitialiseUnknownPassengerApiListener, UniMagReaderMsg, Listener,
		IGProcessPaymentForUnknownPassengerApiListener,
		IGSuburbsReadsFileListener, IGCompleteOfflineListener,
		IGExceptionApiListener {

	private ProgressDialog _progressDialog;
	private EditText _fareEntered;
	private TextView _totalPaymentDueTV, _creditCardSurchargeTV,
			_totalPaymentDueMsgTv, _ingogoCreditTV;
	private RelativeLayout _ingogoCreditLayout;
	private Button _payOfflineButton;

	String _currentFare = IGConstants.zeroBalance;
	DecimalFormat _decimalFormat;
	private String _creditCardSurchargeString = "";
	static private String _jobID = "";
	static private String _passengerid = "";

	static private Double _minimumFare = 0.0;
	static private Double _maximumFare = 0.0;
	static private Double _confirmationValue = 0.0;
	static private Double _creditPercentage = 0.0;
	static private Double _ingogoCreditBalance = 0.0;

	static private boolean _isUnknownPassenger = true;
	public static boolean _isNavigateFromMenu;
	private AlertDialog _alertNavigateFromMenu;

	private static final int DISABLED_ALPHA = 110;
	private static final int ENABLED_ALPHA = 255;
	private boolean _processPaymentInProgress;
	private boolean _unknownPassengerApi;
	private boolean _showKeyboard = true;
	private boolean _isConfirmAlertShown;
	private boolean _isKeyboardShown = false;
	List<JSONObject> _creditCardList;
	private boolean _initUnimagInProgress;
	private String _unimagMessage;
	Handler _unimagHandler = new Handler();
	private UniMagReader unimagReader = null;
	private String _cardDataString;
	private boolean _isUnimagReaderConnected;
	private String _encryptedCardDataString;
	Handler _initialisationHandler = new Handler();

	// Used to remove the dialog on pause
	private Dialog _activeDialog;

	public static interface swipeStatus {
		int initialising = 1;
		int badswipe = 2;
		int cardnotready = 3;
		int defaultStatus = 4;
		int pleaseSwipe = 5;

	}

	private boolean _isBadSwipe;
	private static int _screenStatus;
	private CardInfo _cardInfo;
	private ImageButton _intialiseCardReader, _confirmBtn;
	private boolean _isIntialiseReaderClicked;
	private boolean _calledProcessPaymentApi;
	List<JSONObject> _cardDetailsList = new Vector<JSONObject>();

	Handler payOfflineHandler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			_payOfflineButton.getBackground().setAlpha(255);
			_payOfflineButton.setEnabled(true);
			IngogoApp.sPaymentOfflineButtonEnableFlagInSwipeCalculator = true;
		}
	};

	private TextView _pickUpLabel;
	private String _pickUpSuburbString;
	private IGSuburbListDialog _suburbListDialog;
	private Button _themeButton;
	private boolean _isButtonsEnabled;
	private TextView _totalFareLabel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.swipe_or_no_registered_card_calculator);
		_isUnimagReaderConnected = false;
		_decimalFormat = new DecimalFormat(IGConstants.zeroBalance);
		_passengerid = null;
		_processPaymentInProgress = false;
		_isBadSwipe = false;

		initViews();
		_isConfirmAlertShown = false;

		int status = _screenStatus;
		_intialiseCardReader = (ImageButton) findViewById(R.id.intCardReaderBtn);
		if (status == swipeStatus.badswipe) {
			_totalPaymentDueMsgTv.setText((String) this
					.getText(R.string.bad_swipe_read));
			_isBadSwipe = true;
			_calledProcessPaymentApi = false;
		}
		setUpViews();
		getIntentExtras();
		payOfflineButtonState();
		if (!isDeviceConnected()) {
			if (_jobID != null) {

			}

		}
		/*
		 * If minimum fare and maximum fare get from intent extras no need to
		 * all any api. If we get only job id from intent extras we need to call
		 * swipe initialise api to get other values like minimum fare, maximum
		 * fare, confirmation value etc. If we do not get job id from intent
		 * extras we need to call UnknownPassengerApi to create a job
		 */

		if (_minimumFare > 0 && _maximumFare > 0) {
			return;
		}
		if (_jobID != null) {
			if (_isUnknownPassenger) {
				updatePaymentBaseValues();

			} else {
				callSwipeInitialiseApi();

			}
			return;
		}

		// callUnknownPassengerApi();
		updatePaymentBaseValues();

		IngogoApp.setSwipeScreenCreatedTime(getTimeString());
	}

	/**
	 * Solution to race condition of drivers pressing offline whilst swiping
	 * card.
	 * */
	private void payOfflineButtonState() {
		if (!(IngogoApp.getCurrentActivityOnTop() instanceof IGSwipeCalculatorActivity)) {
			_payOfflineButton.getBackground().setAlpha(DISABLED_ALPHA);
			_payOfflineButton.setEnabled(false);
			IngogoApp.getSharedApplication()
					.setSwipeCalculatorActivityCreatedTime(
							System.currentTimeMillis());
			payOfflineHandler.postDelayed(runnable, 15000);
		} else if (!IngogoApp.sPaymentOfflineButtonEnableFlagInSwipeCalculator) {
			_payOfflineButton.getBackground().setAlpha(DISABLED_ALPHA);
			_payOfflineButton.setEnabled(false);
			payOfflineHandler.removeCallbacks(runnable);
			long differenceInTime = 15000 - (System.currentTimeMillis() - IngogoApp
					.getSharedApplication()
					.getSwipeCalculatorActivityCreatedTime());
			payOfflineHandler.postDelayed(runnable, differenceInTime);

		}
	}

	private void setButtonStatesForCardReaderInitialisation(boolean isEnabled) {
		_themeButton.setEnabled(isEnabled);
		if (isEnabled) {
			_themeButton.getBackground().setAlpha(225);

		} else {
			_themeButton.getBackground().setAlpha(110);

		}
		_isButtonsEnabled = isEnabled;

	}

	private void updatePaymentBaseValues() {

		_unknownPassengerApi = true;

		_minimumFare = IngogoApp.getSharedApplication().getMinTotalDueValue();
		_maximumFare = IngogoApp.getSharedApplication().getMaxTotalDueValue();
		_confirmationValue = IngogoApp.getSharedApplication()
				.getConfirmationValue();
		_creditPercentage = IngogoApp.getSharedApplication()
				.getCreditPercentage();
		_ingogoCreditBalance = 0.0;

		DecimalFormat decimalFormat = new DecimalFormat(IGConstants.zeroBalance);
		String credit = IGConstants.zeroBalance;
		try {
			credit = (String
					.valueOf(decimalFormat.format(_ingogoCreditBalance)));
		} catch (Exception e) {
			credit = IGConstants.zeroBalance;
		}

		if (credit != null) {
			_ingogoCreditTV.setText("(" + credit + ")");
		} else {
			_ingogoCreditTV.setText("(" + IGConstants.zeroBalance + ")");
		}

		if (!IngogoApp.getSharedApplication().getMeterFare()
				.equals(IGConstants.zeroBalance)) {
			_fareEntered.setText(IngogoApp.getSharedApplication()
					.getMeterFare());
			calculateSurcharge();
			updateTripCharge();

		} else {
			_fareEntered.setText(IGConstants.zeroBalance);
		}

		showKeyboard();

	}

	String getTimeString() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date(Calendar.getInstance().getTimeInMillis());
		String dateString = fmt.format(date);
		return dateString;
	}

	@Override
	protected void onResume() {

		if (unimagReader != null) {
			unimagReader.registerListen();
		}
		IGUpdatePositionPollingTask.ignoreStaleState = true;
		showAlertForJobInProgress();
		super.onResume();
		_payOfflineButton.setClickable(true);
		_confirmBtn.setClickable(true);
		_isIntialiseReaderClicked = false;
		_calledProcessPaymentApi = false;
		_confirmBtn.setVisibility(View.GONE);
		/*
		 * To set the speaker phone off.
		 */
		getApplicationContext();
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setMode(AudioManager.MODE_NORMAL);
		audioManager.setSpeakerphoneOn(false);

		/*
		 * Reload the saved value from user defaults. Used to reload the value
		 * in theme switching.
		 */
		if (!IngogoApp.getSharedApplication().getMeterFare()
				.equals(IGConstants.zeroBalance)) {

			_fareEntered.setText(IngogoApp.getSharedApplication()
					.getMeterFare());

		} else {
			_fareEntered.setText(IGConstants.zeroBalance);

		}
		/*
		 * Initialise the swipe when card reader is connected. Other wise show
		 * the message card reader not ready, with initialise card reader
		 * button.
		 */
		if (_confirmBtn.getVisibility() == View.GONE) {
			if (_cardInfo == null) {
				if (_screenStatus != swipeStatus.cardnotready) {
					onSwipeInit();
				} else {
					setButtonStatesForCardReaderInitialisation(true);
					if (_confirmBtn.getVisibility() == View.GONE) {
						_intialiseCardReader.setVisibility(View.VISIBLE);
						_intialiseCardReader.setEnabled(true);
					}
					_totalPaymentDueMsgTv.setText((String) this
							.getText(R.string.card_reader_not_ready));

				}

			}
		}
		if (_showKeyboard) {
			showKeyboard();
		} else {
			_isConfirmAlertShown = true;
		}

		setUpEditTextListeners();
		setUpLayoutListerner();

		String dateString2 = getTimeString();

	}

	private void setUpLayoutListerner() {
		IGCustomScrollLayout _scrollView = (IGCustomScrollLayout) findViewById(R.id.scrollView);

		// Disable Scrolling by setting up an OnTouchListener to do nothing
		_scrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		_scrollView.setListener(this);

	}

	private String returnStringExtraForKey(String key) {
		Intent intent = getIntent();
		if (null != intent.getStringExtra(key)
				&& !intent.getStringExtra(key).equals("")
				&& !intent.getStringExtra(key).equals("null")) {
			return intent.getStringExtra(key);
		}
		return null;
	}

	private void getIntentExtras() {

		Intent intent = getIntent();
		if (intent.getExtras() != null) {
			_jobID = returnStringExtraForKey(IGConstants.kJobId);
			_passengerid = returnStringExtraForKey(IGConstants.kPassengerID);
			_minimumFare = intent.getDoubleExtra(IGConstants.kMinimumDue, 0);
			_maximumFare = intent.getDoubleExtra(IGConstants.kMaximumDue, 0);
			String _fare = returnStringExtraForKey(IGConstants.kFareEntered);
			if (_fare != null) {
				this._fareEntered.setEnabled(false);
				this._fareEntered.setClickable(false);
				this._fareEntered.setFocusable(false);

				_showKeyboard = false;
			}
			if (getIntent().getExtras().get(IGConstants.kPaymentDetails) != null) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> paymentDetails = (HashMap) getIntent()
						.getExtras().get(IGConstants.kPaymentDetails);

				String jsonArray = (String) paymentDetails
						.get(IGApiConstants.kCardDetails);
				try {
					JSONArray jarray = new JSONArray(jsonArray);
					Log.i("jarray", "" + jarray);
					for (int i = 0; i < jarray.length(); i++) {

						_cardDetailsList.add(jarray.getJSONObject(i));
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			_confirmationValue = intent.getDoubleExtra(
					IGConstants.kConfirmationValue, 0);
			_creditPercentage = intent.getDoubleExtra(
					IGConstants.kCreditPercentage, 0);
			_ingogoCreditBalance = intent.getDoubleExtra(
					IGConstants.kCreditBalance, 0);

			if (getIntent().getExtras().get(IGConstants.kFareDetails) != null) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> paymentDetails = (HashMap) getIntent()
						.getExtras().get(IGConstants.kFareDetails);
				if (paymentDetails.get(IGConstants.kMinimumDue) != null) {
					_minimumFare = (Double) paymentDetails
							.get(IGConstants.kMinimumDue);

				}
				if (paymentDetails.get(IGConstants.kMaximumDue) != null) {

					_maximumFare = (Double) paymentDetails
							.get(IGConstants.kMaximumDue);
				}
				if (paymentDetails.get(IGConstants.kConfirmationValue) != null) {
					_confirmationValue = (Double) paymentDetails
							.get(IGConstants.kConfirmationValue);

				}
				if (paymentDetails.get(IGConstants.kCreditPercentage) != null) {
					_creditPercentage = (Double) paymentDetails
							.get(IGConstants.kCreditPercentage);

				}
				if (paymentDetails.get(IGConstants.kCreditBalance) != null) {
					_ingogoCreditBalance = Double
							.valueOf((String) paymentDetails
									.get(IGConstants.kCreditBalance));

				}

			}
			DecimalFormat decimalFormat = new DecimalFormat(
					IGConstants.zeroBalance);
			String credit = IGConstants.zeroBalance;
			try {
				credit = (String.valueOf(decimalFormat
						.format(_ingogoCreditBalance)));
			} catch (Exception e) {
				// TODO: handle exception
				credit = IGConstants.zeroBalance;
			}

			if (credit != null) {
				_ingogoCreditTV.setText("(" + credit + ")");
			} else {
				_ingogoCreditTV.setText("(" + IGConstants.zeroBalance + ")");
			}

			_isUnknownPassenger = intent.getBooleanExtra("isUnknownPassenger",
					true);
			if (_isUnknownPassenger) {
				_ingogoCreditLayout.setVisibility(View.GONE);
			} else {
				_ingogoCreditLayout.setVisibility(View.VISIBLE);

			}

		}

	}

	private double findCreditPercentageFromCreditArray() {
		if (_cardDetailsList != null && _cardDetailsList.size() > 0) {
			try {
				return _cardDetailsList.get(0).getDouble(
						IGApiConstants.kCreditPercentage);
			} catch (JSONException e) {
				return 0;
			}

		} else {
			if (_creditPercentage != null) {
				return _creditPercentage;

			}
			return 0.0;
		}
	}

	private void setUpEditTextListeners() {
		_fareEntered
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {

					/*
					 * Respond to soft keyboard events, look for the DONE press
					 * on the fare entered field.
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
							imm.hideSoftInputFromWindow(
									_fareEntered.getWindowToken(), 0);

							// Perform Login
							Double paymentDue = 0.0;
							try {
								paymentDue = Double
										.parseDouble(_totalPaymentDueTV
												.getText().toString());
							} catch (NumberFormatException e) {
								paymentDue = 0.0;
							}
							if (getFareEntered() == 0) {
								showOKAlertWithMessage(
										getResources().getString(
												R.string.enter_fare_amt_zero),
										false);
							} else if (paymentDue > _maximumFare) {
								String _maximumFareString = (String
										.valueOf(_decimalFormat
												.format(_maximumFare)));
								String message = IGSwipeCalculatorActivity.this
										.getText(R.string.payment_due_max_limit_reached_message_section_1)
										+ _maximumFareString
										+ ". "
										+ IGSwipeCalculatorActivity.this
												.getText(R.string.payment_due_max_limit_reached_message_section_2);
								showOKAlertWithMessage(message, false);

							} else if (paymentDue > _confirmationValue) {
								if (!_isConfirmAlertShown) {
									showPaymentConfirmationAlertWithMessage(
											IGSwipeCalculatorActivity.this
													.getString(R.string.payment_confirmation_msg),
											false);
								}

							} else if (_ingogoCreditBalance == 0
									&& getFareEntered() != 0) {
								if (paymentDue < _minimumFare) {
									String _minFareString = (String
											.valueOf(_decimalFormat
													.format(_minimumFare)));
									String message = IGSwipeCalculatorActivity.this
											.getText(R.string.payment_due_below_min_limit)
											+ _minFareString
											+ ". "
											+ IGSwipeCalculatorActivity.this
													.getText(R.string.payment_due_max_limit_reached_message_section_2);
									showOKAlertWithMessage(message, false);

								}
							}

						}
						// _encryptedCardDataString =
						// "02b600801f2c1f008383252a333731342a2a2a2a2a2a2a313030345e414d45582f494e474f474f5e313330352a2a2a2a2a2a2a2a3f2a3b333731342a2a2a2a2a2a2a313030343d313330352a2a2a2a2a2a2a2a3f2a9ba7238c1ac54da6ebe6200b34fe37ef4514bbe284365502ef59c85697ef6f76f4eecabcd4b439b026487de3755fc7ff552d3f410bc83c40df358cd62429c2d3576c02e84a7b012d1b9d4b9a68abec5754313232333030323030629949013600000001f72df503";
						// callUnknownIngogoPassengerApi();
						// Returning false allows other listeners to react to
						// the press.
						return false;

					}

				});
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

		resetUnimagReader();

		restartActivty();
	}

	/**
	 * Function to restart the activity to apply the new theme.
	 */
	private void restartActivty() {

		Intent intent = getIntent();
		intent.putExtra(IGConstants.kJobId, _jobID);
		intent.putExtra(IGConstants.kPassengerID, _passengerid);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	/**
	 * to initialize a reader
	 */
	private void onSwipeInit() {
		IngogoApp.setSwipeInitialisationStartedTime(getTimeString());

		onUnimagSwipeInit();
	}

	/**
	 * to initialize a unimag reader
	 */
	private void onUnimagSwipeInit() {
		if (!_initUnimagInProgress) {

			_cardInfo = null;
			if (!_isBadSwipe) {
				if (isDeviceConnected()) {

					_totalPaymentDueMsgTv.setText("Initialising reader ...");
					_initUnimagInProgress = true;
					_initialisationHandler
							.removeCallbacks(initialiseReaderRunnable);
					_initialisationHandler.postDelayed(
							initialiseReaderRunnable, 400);

				} else {
					setButtonStatesForCardReaderInitialisation(true);

					_totalPaymentDueMsgTv.setText((String) this
							.getText(R.string.card_reader_not_ready));
					_intialiseCardReader.setVisibility(View.VISIBLE);
					_intialiseCardReader.setEnabled(true);
					IGUtility.dismissProgressDialog(_progressDialog);

					if (_isIntialiseReaderClicked) {
						_isIntialiseReaderClicked = false;
						Dialog dlg = new AlertDialog.Builder(this)
								.setTitle("")
								.setMessage(
										"Attach card reader and then select the Initialise Card Reader")
								.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												_intialiseCardReader
														.setEnabled(true);
											}
										}).create();
						dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
						dlg.setCancelable(false);
						dlg.show();
					}
				}
				_totalPaymentDueMsgTv.setVisibility(View.VISIBLE);
				if (_screenStatus == swipeStatus.pleaseSwipe
						|| _screenStatus == swipeStatus.defaultStatus) {
					if (_screenStatus != swipeStatus.defaultStatus) {
						_totalPaymentDueMsgTv.setText("Swipe card ...");
					}

				} else {
					_screenStatus = swipeStatus.initialising;

				}
			} else {
				_screenStatus = swipeStatus.badswipe;
				if (isDeviceConnected()) {
					startSwipe();

				} else {
					setButtonStatesForCardReaderInitialisation(true);

					_totalPaymentDueMsgTv.setText((String) this
							.getText(R.string.card_reader_not_ready));
					_intialiseCardReader.setVisibility(View.VISIBLE);
					_intialiseCardReader.setEnabled(true);
					IGUtility.dismissProgressDialog(_progressDialog);

				}
			}

		} else {

			if (!_isBadSwipe) {

				_totalPaymentDueMsgTv.setText("Initialising reader ...");
			}
			startSwipe();

		}

	}

	/**
	 * on click initilaise card button
	 * 
	 * @param v
	 */
	public void onClickInitialiseCardButton(View v) {
		_intialiseCardReader.setEnabled(false);
		_isIntialiseReaderClicked = true;
		_calledProcessPaymentApi = false;

		onSwipeInit();
	}

	private void showKeyboard() {
		if (isDeviceConnected()) {
			_fareEntered.postDelayed(new Runnable() {

				@Override
				public void run() {
					_fareEntered.setRawInputType(Configuration.KEYBOARD_12KEY);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(_fareEntered,
							InputMethodManager.SHOW_FORCED);
				}
			}, 750);

		} else {
			_fareEntered.setRawInputType(Configuration.KEYBOARD_12KEY);
			this.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
		_fareEntered.setSelection(_fareEntered.getText().length());
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_fareEntered.getWindowToken(), 0);

	}

	private void showAlertForJobInProgress() {
		if (_isNavigateFromMenu) {
			_isNavigateFromMenu = false;
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle("");
			adb.setMessage(getString(R.string.jobs_inprogress_alert));
			adb.setCancelable(false);
			adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					showKeyboard();
				}
			});
			_alertNavigateFromMenu = adb.create();
			_alertNavigateFromMenu
					.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			_alertNavigateFromMenu.show();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		if (unimagReader != null) {
			unimagReader.release();
		}
		setButtonStatesForCardReaderInitialisation(true);

		super.onDestroy();

	}

	@Override
	protected void onPause() {
		_initialisationHandler.removeCallbacks(initialiseReaderRunnable);

		if (unimagReader != null) {
			// you should stop swipe card and unregister when the application go
			// to background
			unimagReader.stopSwipeCard();
			unimagReader.unregisterListen();
		}
		hideKeyboard();

		IGUtility.dismissProgressDialog(_progressDialog);
		if (_activeDialog != null) {
			_activeDialog.dismiss();
		}
		if (_alertNavigateFromMenu != null) {
			_alertNavigateFromMenu.dismiss();
		}

		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
		IngogoApp.getSharedApplication().setMeterFare(
				_fareEntered.getText().toString());
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_fareEntered.getWindowToken(), 0);
		super.onPause();
	}

	void initViews() {
		_payOfflineButton = (Button) findViewById(R.id.payOfflineBtn);
		_fareEntered = (EditText) findViewById(R.id.fareEditText);
		_totalPaymentDueTV = (TextView) findViewById(R.id.totalFareTv);
		_creditCardSurchargeTV = (TextView) findViewById(R.id.creditCardSurchargeTv);
		_totalPaymentDueMsgTv = (TextView) findViewById(R.id.totalPaymentDueMsgTv);
		_ingogoCreditTV = (TextView) findViewById(R.id.bidCreditTv);
		_confirmBtn = (ImageButton) findViewById(R.id.confirmButton);
		_themeButton = (Button) findViewById(R.id.themeButton);

		_ingogoCreditLayout = (RelativeLayout) findViewById(R.id.bidCreditLayout);
		_totalPaymentDueMsgTv
				.setText(R.string.total_payment_due_condition_check_msg);
		_totalFareLabel = (TextView) findViewById(R.id.totalFareLabel);

		_fareEntered.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// When 1 is entered, it will be shown as '00.01'
				if (!s.toString().equals(_currentFare)) {

					_fareEntered.removeTextChangedListener(this);

					String cleanString = s.toString().replace(".", "");

					double parsed = 00.00;
					try {
						parsed = Double.parseDouble(cleanString);
					} catch (Exception e) {
						// TODO: handle exception
					}

					String formated = (String.valueOf(_decimalFormat
							.format(parsed / 100)));

					// The formatted text will contain a ','. For converting
					// this to double for calculation
					// purpose the comma is removed
					_currentFare = formated.replace(",", "");
					if (_currentFare.equals("") || _currentFare.equals("0")) {
						_currentFare = IGConstants.zeroBalance;
					}
					_fareEntered.setText(_currentFare);
					_fareEntered.setSelection(_currentFare.length());

					_fareEntered.addTextChangedListener(this);
					_isConfirmAlertShown = false;

				}

				calculateSurcharge();
				updateTripCharge();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		_pickUpLabel = (TextView) findViewById(R.id.pickupTv);
		if (IngogoApp.getCurrentActivityOnTop() instanceof IGSwipeCalculatorActivity) {
			String selectedSuburbName = IngogoApp.getSharedApplication()
					.getSelectedSuburbName();
			if (selectedSuburbName != null && !selectedSuburbName.equals("")) {
				_pickUpLabel.setText(IngogoApp.getSharedApplication()
						.getSelectedSuburbName());
			}
		} else {
			Bundle extras = getIntent().getExtras();
			if (extras == null
					|| extras.getString(IGConstants.KSuburbName) == null
					|| extras.getString(IGConstants.KSuburbName).equals("")) {
				IngogoApp.getSharedApplication().setSelectedSuburbName("");
			} else {
				String suburbName = extras.getString(IGConstants.KSuburbName);
				_pickUpLabel.setText(suburbName);
				IngogoApp.getSharedApplication().setSelectedSuburbName(
						suburbName);
			}
		}

	}

	@Override
	public void onBackPressed() {
		_screenStatus = swipeStatus.defaultStatus;
		if (_jobID == null) {
			if (_isButtonsEnabled) {
				super.onBackPressed();

			}
		}
	}

	void updateTripCharge() {

		Double fareEntered = 0.0;
		try {
			fareEntered = Double.parseDouble(_fareEntered.getText().toString());
		} catch (NumberFormatException e) {
			fareEntered = 0.0;
		}
		Double creditCardSurcharge = 0.0;
		try {
			creditCardSurcharge = Double
					.parseDouble(_creditCardSurchargeString);
		} catch (NumberFormatException e) {
			creditCardSurcharge = 0.0;
		}

		double totalPaymentDue = fareEntered - _ingogoCreditBalance
				+ creditCardSurcharge;

		if (totalPaymentDue > 0) {
			_totalPaymentDueTV.setText(""
					+ _decimalFormat.format(totalPaymentDue));
			_totalPaymentDueMsgTv.setVisibility(View.VISIBLE);
			_confirmBtn.setVisibility(View.GONE);

			if (!isDeviceConnected()
					|| _screenStatus == swipeStatus.cardnotready) {
				_screenStatus = swipeStatus.cardnotready;
				_intialiseCardReader.setVisibility(View.VISIBLE);
				_intialiseCardReader.setEnabled(true);
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueMsgTv.setText((String) this
						.getText(R.string.card_reader_not_ready));
			} else {
				_intialiseCardReader.setVisibility(View.GONE);

				_totalPaymentDueMsgTv.setVisibility(View.VISIBLE);
			}

		} else {
			_totalPaymentDueTV.setText(IGConstants.zeroBalance);
			_confirmBtn.setVisibility(View.GONE);
			if (fareEntered > 0) {
				_totalPaymentDueMsgTv.setVisibility(View.GONE);
				_confirmBtn.setVisibility(View.VISIBLE);
				_intialiseCardReader.setVisibility(View.GONE);
			} else if (!isDeviceConnected()
					|| _screenStatus == swipeStatus.cardnotready) {
				_screenStatus = swipeStatus.cardnotready;
				_intialiseCardReader.setVisibility(View.VISIBLE);
				_intialiseCardReader.setEnabled(true);
				_totalPaymentDueMsgTv.setVisibility(View.VISIBLE);
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueMsgTv.setText((String) this
						.getText(R.string.card_reader_not_ready));
			} else {
				_intialiseCardReader.setVisibility(View.GONE);

				_totalPaymentDueMsgTv.setVisibility(View.VISIBLE);
			}

		}

		_creditCardSurchargeTV.setText(""
				+ _decimalFormat.format(creditCardSurcharge));

	}

	/**
	 * calculate the surcharge and set the text
	 */
	private void calculateSurcharge() {
		Double fareEntered = 0.0;
		try {
			fareEntered = Double.parseDouble(_fareEntered.getText().toString());
		} catch (NumberFormatException e) {
			fareEntered = 0.0;
		}
		if (_ingogoCreditBalance >= fareEntered) {
			_creditCardSurchargeString = IGConstants.zeroBalance;

			_creditCardSurchargeTV.setText(IGConstants.zeroBalance);
		} else {
			DecimalFormat decimalFormat = new DecimalFormat(
					IGConstants.zeroBalance);
			String surcharge = (String.valueOf(decimalFormat
					.format(findCreditPercentageFromCreditArray()
							* (fareEntered - _ingogoCreditBalance) / 100)));
			_creditCardSurchargeString = surcharge;
			_creditCardSurchargeTV.setText(surcharge);
		}
	}

	// sus
	void setUpViews() {
		Double surcharge = 0.0;
		try {
			surcharge = Double.parseDouble(_creditCardSurchargeString);
		} catch (NumberFormatException e) {
			surcharge = 0.0;
		}
		String formattedString = (String.valueOf(_decimalFormat
				.format(surcharge)));
		_creditCardSurchargeTV.setText(formattedString);

	}

	private void showOKAlertWithMessage(String message, final boolean afterSwipe) {
		setButtonStatesForCardReaderInitialisation(true);

		_activeDialog = new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(IGConstants.OKMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								_payOfflineButton.setClickable(true);
								if (afterSwipe)
									onSwipeInit();
							}
						}).create();
		_activeDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (!isFinishing())
			_activeDialog.show();
	}

	private void showPaymentConfirmationAlertWithMessage(String message,
			final boolean afterSwipe) {
		_activeDialog = new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(IGConstants.ConfirmMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								_isConfirmAlertShown = true;
								if (afterSwipe == true) {
									onSwipePerformed();
								}

							}
						})
				.setNegativeButton(IGConstants.CancelMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								_payOfflineButton.setClickable(true);
								if (afterSwipe == true) {
									onSwipeInit();
								}
							}
						}).create();
		_activeDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (!isFinishing())
			_activeDialog.show();
	}

	public void onConfirmButtonClick(View view) {
		_confirmBtn.setClickable(false);
		callProcessPaymentApi();
	}

	private void callIngogoPassengerPaymentApi() {
		if (IGUtility.isNetworkAvailable(IGSwipeCalculatorActivity.this)) {
			_pickUpSuburbString = _pickUpLabel.getText().toString();
			if (_pickUpSuburbString.equals("optional")) {
				_pickUpSuburbString = "";
			}
			_processPaymentInProgress = true;
			_progressDialog = IGUtility
					.showProgressDialog(IGSwipeCalculatorActivity.this);
			_unknownPassengerApi = false;
			IGProcessPaymentApi api = new IGProcessPaymentApi(
					IGSwipeCalculatorActivity.this,
					IGSwipeCalculatorActivity.this);
			_totalPaymentDueMsgTv.setText("Processing payment...");

			if (_encryptedCardDataString != null) {
				if (_encryptedCardDataString.length() > 4) {
					api.processPayment(_jobID, _fareEntered.getText()
							.toString(), _totalPaymentDueTV.getText()
							.toString(), getCreditCardInformation(),
							_encryptedCardDataString, _pickUpSuburbString);
				} else {
					api.processPayment(_jobID, _fareEntered.getText()
							.toString(), _totalPaymentDueTV.getText()
							.toString(), getCreditCardInformation(),
							_pickUpSuburbString);
				}
			} else {
				api.processPayment(_jobID, _fareEntered.getText().toString(),
						_totalPaymentDueTV.getText().toString(),
						getCreditCardInformation(), _pickUpSuburbString);
			}

			_payOfflineButton.setClickable(true);

		} else {
			_totalPaymentDueMsgTv.setText("Swipe card ...");
			_confirmBtn.setClickable(true);
			_payOfflineButton.setClickable(true);
			onSwipeInit();

			IGUtility.showDialogOk(
					IGSwipeCalculatorActivity.this.getText(
							R.string.network_error_title).toString(),
					IGSwipeCalculatorActivity.this.getText(
							R.string.ReachabilityMessage).toString(),
					IGSwipeCalculatorActivity.this);

		}
	}

	private void callUnknownIngogoPassengerApi() {
		if (IGUtility.isNetworkAvailable(IGSwipeCalculatorActivity.this)) {
			_pickUpSuburbString = _pickUpLabel.getText().toString();
			if (_pickUpSuburbString.equals("optional")) {
				_pickUpSuburbString = "";
			}
			_processPaymentInProgress = true;
			_progressDialog = IGUtility
					.showProgressDialog(IGSwipeCalculatorActivity.this);
			_unknownPassengerApi = false;
			IGProcessPaymentForUnknownPassengerApi api = new IGProcessPaymentForUnknownPassengerApi(
					IGSwipeCalculatorActivity.this,
					IGSwipeCalculatorActivity.this);
			_totalPaymentDueMsgTv.setText("Processing payment...");

			if (_encryptedCardDataString != null) {
				if (_encryptedCardDataString.length() > 4) {
					api.processPaymentForUnknownPassenger(_jobID, _fareEntered
							.getText().toString(), _totalPaymentDueTV.getText()
							.toString(), getCreditCardInformation(),
							_encryptedCardDataString, _pickUpSuburbString);
				} else {
					api.processPaymentForUnknownPassenger(_jobID, _fareEntered
							.getText().toString(), _totalPaymentDueTV.getText()
							.toString(), getCreditCardInformation(),
							_pickUpSuburbString);
				}
			} else {
				api.processPaymentForUnknownPassenger(_jobID, _fareEntered
						.getText().toString(), _totalPaymentDueTV.getText()
						.toString(), getCreditCardInformation(),
						_pickUpSuburbString);
			}

			_payOfflineButton.setClickable(true);

		} else {
			_totalPaymentDueMsgTv.setText("Swipe card ...");
			_confirmBtn.setClickable(true);
			_payOfflineButton.setClickable(true);
			onSwipeInit();

			IGUtility.showDialogOk(
					IGSwipeCalculatorActivity.this.getText(
							R.string.network_error_title).toString(),
					IGSwipeCalculatorActivity.this.getText(
							R.string.ReachabilityMessage).toString(),
					IGSwipeCalculatorActivity.this);

		}

	}

	private void callProcessPaymentApi() {
		if (_isUnknownPassenger) {
			callUnknownIngogoPassengerApi();
		} else {
			callIngogoPassengerPaymentApi();
		}

	}

	// pass empty card details
	private IGCreditCardInformation getCreditCardInformation() {
		if (_totalPaymentDueTV.getText().toString()
				.equals(IGConstants.zeroBalance)
				|| _cardInfo == null) {
			IGCreditCardInformation creditCardInfo = new IGCreditCardInformation();

			creditCardInfo.setCardNumber("");
			creditCardInfo.setExpiryMonth("");
			creditCardInfo.setExpiryYear("");
			creditCardInfo.setCardName("");

			return creditCardInfo;
		} else {
			IGCreditCardInformation creditCardInfo = new IGCreditCardInformation();
			creditCardInfo.setCardNumber(_cardInfo.getAccountNumber());
			creditCardInfo.setExpiryMonth(_cardInfo.getExpirationMonth() + "");
			creditCardInfo.setExpiryYear(_cardInfo.getExpirationYear() + "");
			creditCardInfo.setCardName(_cardInfo.getAccountName());
			return creditCardInfo;
		}

	}

	public void onClickPayOfflineButtonClicked(View view) {
		if (!isDeviceConnected()) {
			if (_jobID != null) {
				// Exception newExp = new Exception("Swipe Pay offline: "
				// + "Booking Id = " + _jobID + " ");
				// IGUtility.logExceptionInQLogger(newExp);
			}

		}
		if (_jobID != null) {

			callCompleteOfflineJobApi();
		} else {
			IngogoApp.getSharedApplication().setComingFromPayOffline(true);
			;
			goToJobsActivity();
		}
		// bookings/completeOffline w/s is called and on success we move back to
		// job list page
	}

	private void callSwipeInitialiseApi() {

		_unknownPassengerApi = false;
		_processPaymentInProgress = false;
		// Disable the button to prevent multiple click.
		_progressDialog = IGUtility.showProgressDialog(this);
		IGSwipeInititaliseApi swipeInitialiseApi = new IGSwipeInititaliseApi(
				this, this);
		swipeInitialiseApi.initialiseSwipe(_passengerid);
		_unknownPassengerApi = false;
		_processPaymentInProgress = false;

	}

	// private void callUnknownPassengerApi() {
	// _processPaymentInProgress = false;
	// _unknownPassengerApi = true;
	//
	// _progressDialog = IGUtility.showProgressDialog(this);
	// IGInitialiseUnknownPassengerPaymentApi igIntialiseUnknownApi = new
	// IGInitialiseUnknownPassengerPaymentApi(
	// IGSwipeCalculatorActivity.this, IGSwipeCalculatorActivity.this);
	// igIntialiseUnknownApi.getUnknownPassengerPaymentStatus();
	//
	// }

	private void callCompleteOfflineJobApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			// Disable the button to prevent multiple click.
			_payOfflineButton.setEnabled(false);
			_payOfflineButton.getBackground().setAlpha(DISABLED_ALPHA);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCompleteOfflineJobApi _completeOfflineApi = new IGCompleteOfflineJobApi(
					this, this, _jobID);
			_completeOfflineApi.completeOffline();
		} else {

			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);

		}
	}

	/**
	 * To navigate to jobs activity by clearing all the activities between swipe
	 * calculator activity and jobs activity.
	 */
	private void goToJobsActivity() {

		Intent intent = new Intent(IGSwipeCalculatorActivity.this,
				IGJobsActivity.class);
		IGJobsActivity.checkDriverStatus = true;
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		_screenStatus = swipeStatus.defaultStatus;

		finish();
	}

	/**
	 * Successful response is received by this method, when calling web service
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		Log.e("Payment response ", "" + response);
		super.onResponseReceived(response, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);
		_screenStatus = swipeStatus.defaultStatus;

		// complete offline web service.
		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {

			JSONObject resp = (JSONObject) response.get(IGConstants.kDataKey);
			String sucessString = null;
			String totalPaid = null;
			try {
				sucessString = resp
						.getString(IGConstants.kReceiptInformationKey);
				totalPaid = resp.getString(IGConstants.kTotalPaidKey);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (sucessString != null && !sucessString.equals("null")) {

				if (totalPaid == null || totalPaid.trim().equals("null")) {
					totalPaid = "00.00";
				} else {
					Double total = Double.valueOf(totalPaid);
					totalPaid = (String.valueOf(_decimalFormat.format(total)));
				}
				if (_isUnknownPassenger) {
					Intent paymentCompletedIntent = new Intent(
							IGSwipeCalculatorActivity.this,
							IGPaymentCompletedActivity.class);
					paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
							totalPaid);
					paymentCompletedIntent.putExtra(
							IGConstants.kPaymentSuccess,
							"Your payment was successful !!!");
					paymentCompletedIntent.putExtra(IGConstants.kBookingId,
							_jobID);
					startActivity(paymentCompletedIntent);

				} else {
					Intent paymentCompletedIntent = new Intent(
							IGSwipeCalculatorActivity.this,
							IGPaymentSucessActivity.class);
					paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
							totalPaid);
					paymentCompletedIntent.putExtra(IGConstants.kJobId, _jobID);
					paymentCompletedIntent.putExtra(
							IGConstants.kPaymentSuccess, sucessString);
					startActivity(paymentCompletedIntent);
				}

			} else {
				IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
				IngogoApp.getSharedApplication().setComingFromPayOffline(true);
				goToJobsActivity();
			}
		}

	}

	/**
	 * Failure response is got in this method
	 */
	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {

		if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			// Enable the payoffline button which is disabled earlier.
			_payOfflineButton.setEnabled(true);
			_payOfflineButton.getBackground().setAlpha(ENABLED_ALPHA);
		}

		Log.e("Payment activity errorResponse", "" + errorResponse);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onFailedToGetResponse(errorResponse, apiID);

	}

	@Override
	public void swipeInitialiseCompleted(
			IGSwipeInititaliseResponseBean initialiseDetails) {
		IGUtility.dismissProgressDialog(_progressDialog);

		if (initialiseDetails.getMinTotalDueValue() != null) {
			_minimumFare = initialiseDetails.getMinTotalDueValue();

		}
		if (initialiseDetails.getMaxTotalDueValue() != null) {
			_maximumFare = initialiseDetails.getMaxTotalDueValue();

		}
		if (initialiseDetails.getConfirmationValue() != null) {
			_confirmationValue = initialiseDetails.getConfirmationValue();

		}
		if (initialiseDetails.getCreditPercentage() != null) {
			_creditPercentage = initialiseDetails.getCreditPercentage();

		}

		if (initialiseDetails.getCardDetails() != null) {
			List<IGCreditCardModel> cardDetails = initialiseDetails
					.getCardDetails();
			if (cardDetails.size() > 1) {
				if (cardDetails.get(0).getCreditPercentage() != null) {
					_creditPercentage = Double.valueOf(cardDetails.get(0)
							.getCreditPercentage());
				}

			}
		}
		if (initialiseDetails.getBalance() != null) {
			_ingogoCreditBalance = initialiseDetails.getBalance();
			DecimalFormat decimalFormat = new DecimalFormat(
					IGConstants.zeroBalance);
			String credit = IGConstants.zeroBalance;
			try {
				credit = (String.valueOf(decimalFormat
						.format(_ingogoCreditBalance)));
			} catch (Exception e) {
				credit = IGConstants.zeroBalance;
			}

			if (credit != null) {
				_ingogoCreditTV.setText("(" + credit + ")");
			} else {
				_ingogoCreditTV.setText("(" + IGConstants.zeroBalance + ")");
			}

		} else {
			_ingogoCreditBalance = 0.0;
		}

		// boolean isVisible = _isPayOfflineVisible;
		if (!IngogoApp.getSharedApplication().getMeterFare()
				.equals(IGConstants.zeroBalance)) {
			_fareEntered.setText(IngogoApp.getSharedApplication()
					.getMeterFare());
			calculateSurcharge();
			updateTripCharge();

		} else {
			_fareEntered.setText(IGConstants.zeroBalance);
		}

		showKeyboard();

	}

	@Override
	public void swipeInitialiseFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (!IngogoApp.getSharedApplication().getMeterFare()
				.equals(IGConstants.zeroBalance)) {
			_fareEntered.setText(IngogoApp.getSharedApplication()
					.getMeterFare());
			calculateSurcharge();
			updateTripCharge();

		} else {
			_fareEntered.setText(IGConstants.zeroBalance);
		}
		calculatordataFetchFailed = true;
		showSwipeInitializeFailureDialog(errorMessage);

	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		handleErrorResponse(errorResponse);

	}

	private void handleErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (_processPaymentInProgress) {
			// for process payment api
			String errorHeading = getText(R.string.payment_failure_message)
					.toString();

			loadFailurePageWithMessage(errorHeading,
					getText(R.string.payment_unspecified_failure_error_string)
							.toString());
			_processPaymentInProgress = false;
		} else if (_unknownPassengerApi) {
			// for unknown passenger api
			showUnknownPassengerFailureDialog((String) errorResponse
					.get(IGApiConstants.kApiFailedMsgKey));

		} else {
			// for swipe initialise api
			showSwipeInitializeFailureDialog((String) errorResponse
					.get(IGApiConstants.kApiFailedMsgKey));
		}
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {

		handleErrorResponse(errorResponse);

	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {

		handleErrorResponse(errorResponse);

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (_processPaymentInProgress) {
			String errorHeading = getText(R.string.payment_failure_message)
					.toString();
			loadFailurePageWithMessage(errorHeading,
					getText(R.string.payment_unspecified_failure_error_string)
							.toString());
			_processPaymentInProgress = false;
		} else if (_unknownPassengerApi) {
			showUnknownPassengerFailureDialog("Internal server error.Please try again later");

		} else {

			showSwipeInitializeFailureDialog("Internal server error.Please try again later");

		}

	}

	@Override
	public void processPaymentCompleted(IGBookingModel bookingModel,
			String receiptInformationPageText,
			IGReceiptInformationModel receiptInformation) {

		IGUtility.dismissProgressDialog(_progressDialog);
		_confirmBtn.setClickable(true);
		_payOfflineButton.setClickable(true);

		if (_isUnknownPassenger) {
			Intent paymentCompletedIntent = new Intent(
					IGSwipeCalculatorActivity.this,
					IGPaymentCompletedActivity.class);
			paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
					_totalPaymentDueTV.getText().toString());
			// paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
			// "Your payment was successful !!!");
			paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
					receiptInformationPageText);
			paymentCompletedIntent.putExtra(IGConstants.kBookingId, _jobID);
			paymentCompletedIntent.putExtra(IGConstants.kReceiptInformationKey,
					receiptInformation);
			startActivity(paymentCompletedIntent);
			//finish();
			_screenStatus = swipeStatus.defaultStatus;

		} else {
			Intent paymentCompletedIntent = new Intent(
					IGSwipeCalculatorActivity.this,
					IGPaymentSucessActivity.class);
			paymentCompletedIntent.putExtra(IGConstants.kJobId, _jobID);
			paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
					_totalPaymentDueTV.getText().toString());
			paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
					"Your payment was successful !!!");
			startActivity(paymentCompletedIntent);
		//	finish();

			_screenStatus = swipeStatus.defaultStatus;

		}

	}

	@Override
	public void processPaymentFailed(String errorContent) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_payOfflineButton.setClickable(true);
		_confirmBtn.setClickable(true);
		_processPaymentInProgress = false;
		String errorHeading = getText(R.string.payment_failure_message)
				.toString();
		String errorMessage = getText(
				R.string.payment_unspecified_failure_error_string).toString();

		if (errorContent.contains("DECLINED")
				|| (errorContent.contains("Payment declined"))) {
			errorHeading = "TRANSACTION DECLINED BY CARD HOLDER's BANK";
			errorMessage = "Transaction declined by card holders bank. Try another card.";
		} else if (errorContent.contains("EXPIRED_CARD")
				|| (errorContent.contains("Expired card"))) {
			errorHeading = "EXPIRED CARD";
			errorMessage = "Card has expired. Try another card";
		} else if (errorContent.contains("INSUFFICIENT_FUNDS")
				|| (errorContent.contains("Insufficient funds"))) {
			errorHeading = "INSUFFICIENT FUNDS";
			errorMessage = "Card has insufficient funds available. Try another card";
		} else if (errorContent
				.contains(IGConstants.kPickupRefutedErrorMessage)
				|| (errorContent.contains("Pickup Refuted"))) {
			errorMessage = getText(R.string.pickup_refuted_error_message)
					.toString();
		}

		loadFailurePageWithMessage(errorHeading, errorMessage);

	}

	private void loadFailurePageWithMessage(String errorHeading,
			String errorMessage) {
		_payOfflineButton.setClickable(true);

		Intent failureIntent = new Intent(IGSwipeCalculatorActivity.this,
				IGPaymentFailureActivity.class);
		failureIntent.putExtra(IGApiConstants.kIsSwipeFailure, true);
		failureIntent.putExtra(IGConstants.kJobId, _jobID);

		failureIntent.putExtra(IGConstants.kPassengerID, _passengerid);
		failureIntent.putExtra("isUnknownPassenger", _isUnknownPassenger);

		failureIntent.putExtra(IGConstants.kMaximumDue, _maximumFare);
		failureIntent.putExtra(IGConstants.kMinimumDue, _minimumFare);
		failureIntent.putExtra(IGConstants.kConfirmationValue,
				_confirmationValue);
		failureIntent
				.putExtra(IGConstants.kCreditBalance, _ingogoCreditBalance);
		failureIntent
				.putExtra(IGConstants.kCreditPercentage, _creditPercentage);

		failureIntent.putExtra(IGConstants.kFareEntered, _fareEntered.getText()
				.toString());
		failureIntent.putExtra(IGConstants.kTotalDueAmount, _totalPaymentDueTV
				.getText().toString());
		failureIntent.putExtra("cardinfo", getCreditCardInformation());
		if (_encryptedCardDataString == null) {
			failureIntent.putExtra("encryptedCardDataString", "");
		} else {
			failureIntent.putExtra("encryptedCardDataString",
					_encryptedCardDataString);
		}
		failureIntent.putExtra(IGConstants.kErrorMessage, errorMessage);
		failureIntent.putExtra(IGConstants.kErrorHeading, errorHeading);
		failureIntent.putExtra(IGConstants.kBookingType,
				IGConstants.kBookingTypeHail);

		failureIntent.putExtra(IGConstants.kCreditCardCount, 0);
		failureIntent.putExtra(IGConstants.KSuburbName, _pickUpLabel.getText()
				.toString());

		startActivity(failureIntent);
	//	finish();
		_screenStatus = swipeStatus.defaultStatus;

	}

	double getFareEntered() {
		Double fareEntered = 0.0;
		try {
			fareEntered = Double.parseDouble(_fareEntered.getText().toString());
		} catch (NumberFormatException e) {
			fareEntered = 0.0;
		}
		return fareEntered;
	}

	private void onSwipePerformedForNonEncrptedData() {
		int creditCardType = IGUtility.getCardID(_cardInfo.getAccountNumber());
		if (creditCardType == IGUtility.VISA
				|| creditCardType == IGUtility.MASTERCARD
				|| creditCardType == IGUtility.AMERICAN_EXPRESS) {
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			month++;

			String yearString = year + "";
			if (yearString.length() > 3) {
				yearString = yearString.substring(2, 4);
			}
			year = Integer.parseInt(yearString);

			if (_cardInfo.getExpirationYear() > year) {
				_totalPaymentDueMsgTv.setText("Processing payment...");

				callProcessPaymentApi();

			} else if (year == _cardInfo.getExpirationYear()) {
				if (_cardInfo.getExpirationMonth() >= month) {
					_totalPaymentDueMsgTv.setText("Processing payment...");

					callProcessPaymentApi();
				} else {

					_activeDialog = IGUtility.showDialog("",
							"Card has expired. Please try another card.", this);

					_payOfflineButton.setClickable(true);

					onSwipeInit();
				}

			} else {
				if (!isFinishing()) {
					_activeDialog = IGUtility.showDialog("",
							"Card has expired. Please try another card.", this);
				}
				_payOfflineButton.setClickable(true);

				onSwipeInit();
			}

		} else {

			_activeDialog = IGUtility
					.showDialog(
							"",
							"Only Visa and Mastercard are accepted. Please try another card.",
							this);

			_payOfflineButton.setClickable(true);
			onSwipeInit();
		}
	}

	/**
	 * called after a swipe is success
	 */
	private void onSwipePerformed() {
		if (null == _fareEntered.getText()) {
			onSwipeInit();
			return;
		}

		if (_encryptedCardDataString != null) {
			if (_encryptedCardDataString.length() > 0) {
				callProcessPaymentApi();

			} else {
				onSwipePerformedForNonEncrptedData();
			}

		} else {
			onSwipePerformedForNonEncrptedData();
		}

	}

	@Override
	public void initialiseUnknownPassengerCompleted(
			IGInitialiseUnknownPassengerResponseBean initialiseDetails) {

		IGUtility.dismissProgressDialog(_progressDialog);
		_jobID = initialiseDetails.getBookingSummary().getBookingId();
		if (initialiseDetails.getMinTotalDueValue() != null) {
			_minimumFare = initialiseDetails.getMinTotalDueValue();

		}
		if (initialiseDetails.getMaxTotalDueValue() != null) {
			_maximumFare = initialiseDetails.getMaxTotalDueValue();

		}
		if (initialiseDetails.getConfirmationValue() != null) {
			_confirmationValue = initialiseDetails.getConfirmationValue();

		}
		if (initialiseDetails.getCreditPercentage() != null) {
			_creditPercentage = initialiseDetails.getCreditPercentage();

		}
		if (initialiseDetails.getCardDetails() != null) {
			List<IGCreditCardModel> cardDetails = initialiseDetails
					.getCardDetails();
			if (cardDetails.size() > 1) {
				_creditPercentage = Double.valueOf(cardDetails.get(0)
						.getCreditPercentage());
			}
		}
		if (initialiseDetails.getBalance() != null) {
			_ingogoCreditBalance = initialiseDetails.getBalance();
			DecimalFormat decimalFormat = new DecimalFormat(
					IGConstants.zeroBalance);
			String credit = IGConstants.zeroBalance;
			try {
				credit = (String.valueOf(decimalFormat
						.format(_ingogoCreditBalance)));
			} catch (Exception e) {
				credit = IGConstants.zeroBalance;
			}

			if (credit != null) {
				_ingogoCreditTV.setText("(" + credit + ")");
			} else {
				_ingogoCreditTV.setText("(" + IGConstants.zeroBalance + ")");
			}

		} else {
			_ingogoCreditBalance = 0.0;
		}

		if (!IngogoApp.getSharedApplication().getMeterFare()
				.equals(IGConstants.zeroBalance)) {
			_fareEntered.setText(IngogoApp.getSharedApplication()
					.getMeterFare());
			calculateSurcharge();
			updateTripCharge();

		} else {
			_fareEntered.setText(IGConstants.zeroBalance);
		}

		showKeyboard();

	}

	@Override
	public void initialiseUnknownPassengerFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		showUnknownPassengerFailureDialog(errorMessage);

	}

	private void showUnknownPassengerFailureDialog(String message) {
		Dialog dlg = new AlertDialog.Builder(this)
				.setTitle(null)
				.setMessage(message)
				.setPositiveButton("Try Again",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// callUnknownPassengerApi();
							}
						}).setNegativeButton("No", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						goToJobsActivity();
					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.show();
	}

	private void showSwipeInitializeFailureDialog(String message) {
		Dialog dlg = new AlertDialog.Builder(this)
				.setTitle(null)
				.setMessage(message)
				.setPositiveButton("Try Again",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// callUnknownPassengerApi();
							}
						})
				.setNegativeButton("Payoffline", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						callCompleteOfflineJobApi();
					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.show();
	}

	public static void clearCachedValues() {
		_jobID = null;
		_passengerid = null;
		_minimumFare = 0.0;
		_maximumFare = 0.0;
		_confirmationValue = 0.0;
		_creditPercentage = 0.0;
		_ingogoCreditBalance = 0.0;
		_screenStatus = swipeStatus.defaultStatus;

	}

	private void resetUnimagReader() {
		if (unimagReader != null) {
			unimagReader.stopSwipeCard();
			unimagReader.unregisterListen();
			unimagReader.release();
			unimagReader = null;
		}
	}

	private void startSwipe() {
		if (unimagReader != null) {
			if (unimagReader.startSwipeCard()) {

				Log.e("UNIMAG", "UNIMAG " + "to startSwipeCard called");
			} else {
				if (unimagReader.isSwipeCardRunning()) {

					if (!_isBadSwipe) {

						if (isDeviceConnected()) {
							_initUnimagInProgress = false;
							_initialisationHandler
									.postDelayed(onSwipeInit, 400);

							if (_screenStatus == swipeStatus.pleaseSwipe
									|| _screenStatus == swipeStatus.defaultStatus) {
								if (_screenStatus != swipeStatus.defaultStatus) {
									_totalPaymentDueMsgTv
											.setText("Swipe card ...");
								}

							} else {
								_screenStatus = swipeStatus.initialising;

							}
						} else {

							_unimagMessage = (String) IGSwipeCalculatorActivity.this
									.getText(R.string.card_reader_not_ready);
							_unimagHandler.post(doUpdateStatus);
						}

					} else {
						_screenStatus = swipeStatus.badswipe;
						if (isDeviceConnected()) {

							_initUnimagInProgress = false;
							_initialisationHandler
									.postDelayed(onSwipeInit, 400);
						} else {

							_unimagMessage = (String) IGSwipeCalculatorActivity.this
									.getText(R.string.card_reader_not_ready);
							_unimagHandler.post(doUpdateStatus);
						}

					}

				} else {
					resetUnimagReader();
					setButtonStatesForCardReaderInitialisation(true);

					_unimagMessage = (String) IGSwipeCalculatorActivity.this
							.getText(R.string.card_reader_not_ready);
					_unimagHandler.post(doUpdateStatus);

				}

			}
		} else {
			_cardInfo = null;
			if (!_isBadSwipe) {
				if (isDeviceConnected()) {
					_totalPaymentDueMsgTv.setText("Initialising reader ...");
					_initUnimagInProgress = true;
					setButtonStatesForCardReaderInitialisation(false);
					if (unimagReader == null)
						unimagReader = new UniMagReader(
								IGSwipeCalculatorActivity.this,
								IGSwipeCalculatorActivity.this);
					unimagReader.registerListen();
					unimagReader.setTimeoutOfSwipeCard(10000);
					String fileNameWithPath = IGUtility.getXMLFileFromRaw();
					if (!IGUtility.isFileExist(fileNameWithPath)) {
						fileNameWithPath = null;
					}
					unimagReader.setXMLFileNameWithPath(fileNameWithPath);
					unimagReader.loadingConfigurationXMLFile(true);

				} else {
					setButtonStatesForCardReaderInitialisation(true);

					_totalPaymentDueMsgTv
							.setText((String) IGSwipeCalculatorActivity.this
									.getText(R.string.card_reader_not_ready));
					IGUtility.dismissProgressDialog(_progressDialog);

					if (_isIntialiseReaderClicked) {
						_isIntialiseReaderClicked = false;
						Dialog dlg = new AlertDialog.Builder(
								IGSwipeCalculatorActivity.this)
								.setTitle("")
								.setMessage(
										"Attach card reader and then select the Initialise Card Reader")
								.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												_intialiseCardReader
														.setEnabled(true);
												if (_confirmBtn.getVisibility() != View.VISIBLE) {
													_intialiseCardReader
															.setVisibility(View.VISIBLE);

												}
											}
										}).create();
						dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
						dlg.setCancelable(false);
						dlg.show();
					}
				}
				_totalPaymentDueMsgTv.setVisibility(View.VISIBLE);
				if (_screenStatus == swipeStatus.pleaseSwipe
						|| _screenStatus == swipeStatus.defaultStatus) {
					if (_screenStatus != swipeStatus.defaultStatus) {
						_totalPaymentDueMsgTv.setText("Swipe card ...");
					}

				} else {
					_screenStatus = swipeStatus.initialising;

				}
			} else {
				_screenStatus = swipeStatus.badswipe;
			}
		}

	}

	@Override
	public boolean getUserGrant(int type, String message) {
		Log.e("UNIMAG", "UNIMAG getUserGrant  " + " type = " + type
				+ " message = " + message);
		return true;
	}

	public void onSoftKeyboardShown(boolean isShowing) {
		_isKeyboardShown = isShowing;

	}

	private void getEncryptionStatus(int commandID, byte[] cmdReturn) {
		switch (commandID) {
		case UniMagReaderMsg.cmdEnableTDES:
			if (0 == cmdReturn[0]) {
				Log.e("CARDREADER", "CARDREADER - ENABLE TDES TIMED OUT");
				unimagReader.sendCommandEnableTDES();

			} else if (6 == cmdReturn[0]) {
				Log.e("CARDREADER", "CARDREADER - ENABLE TDES Succeed");

			} else {
				Log.e("CARDREADER", "CARDREADER - ENABLE TDES Failed");

			}
			break;
		default:
			break;
		}

	}

	@Override
	public void onReceiveMsgAutoConfigProgress(int arg0) {
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgAutoConfigProgress"
				+ "flagOfCardData = " + arg0);

	}

	@Override
	public void onReceiveMsgCardData(byte flagOfCardData, byte[] cardData) {

		byte flag = (byte) (flagOfCardData & 0x04);
		_cardDataString = null;
		_encryptedCardDataString = null;

		if (flag == 0x00) {
			Log.e("UNIMAG", "Demo Info " + "no need of decryption");

			_cardDataString = new String(cardData);
			_unimagHandler.post(onSwipeSuccess);

		}
		if (flag == 0x04) {
			// You need to dencrypt the data here first.
			String hexString = IGUtility.getHexStringFromBytes(cardData);
			if (IGUtility.isTheEncodedCardDataSuccess(hexString)) {
				hexString = hexString.replace("<", "");
				hexString = hexString.replace(">", "");

				Log.e("UNIMAG", "Demo Info " + "need decryption"
						+ "updated hex string = " + hexString);

				_encryptedCardDataString = new String(hexString);
				_unimagHandler.post(onSwipeSuccess);

			} else {
				_unimagHandler.post(onBadRead);

			}
		}

	}

	@Override
	public void onReceiveMsgCommandResult(int commandID, byte[] cmdReturn) {
		Log.e("UNIMAG", "UNIMAG onReceiveMsgCommandResult " + " commandID= "
				+ commandID + " cmdReturn=" + cmdReturn + " cmdHexString = "
				+ IGUtility.getHexStringFromBytes(cmdReturn) + " status = "
				+ cmdReturn[0]);
		// _unimagHandler.post(startSwipe);
		getEncryptionStatus(commandID, cmdReturn);

	}

	@Override
	public void onReceiveMsgConnected() {
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgConnected");
		// unimagReader.sendCommandEnableTDES();

		IngogoApp.setInitialCardCheck(false);
		IngogoApp.setPrimaryCardReaderAttached(true);

		_unimagHandler.post(startSwipe);

	}

	@Override
	public void onReceiveMsgDisconnected() {
		IngogoApp.setPrimaryCardReaderAttached(true);
		IngogoApp.setInitialCardCheck(true);
		_isUnimagReaderConnected = false;

		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgDisconnected");
		_initUnimagInProgress = false;

		_screenStatus = swipeStatus.defaultStatus;
		_calledProcessPaymentApi = false;
		_unimagMessage = (String) this.getText(R.string.card_reader_not_ready);

		resetUnimagReader();
		_unimagHandler.post(onDisconnected);

	}

	@Override
	public void onReceiveMsgFailureInfo(int arg0, String message) {
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgFailureInfo" + "index = "
				+ arg0 + "message = " + message);

	}

	@Override
	public void onReceiveMsgSDCardDFailed(String message) {
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgSDCardDFailed" + "message = "
				+ message);

	}

	@Override
	public void onReceiveMsgTimeout(String message) {
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgTimeout" + "message = "
				+ message);
		if (!_isUnimagReaderConnected) {
			_unimagHandler.post(showReaderNotDetectedDialog);

		} else {
			_unimagHandler.post(startSwipe);
		}

	}

	@Override
	public void onReceiveMsgToConnect() {
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgToConnect");

	}

	@Override
	public void onReceiveMsgToSwipeCard() {
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgToSwipeCard");

		_isUnimagReaderConnected = true;

		_calledProcessPaymentApi = false;

		if (!_isBadSwipe) {
			if (isDeviceConnected()) {
				IngogoApp.setInitialCardCheck(false);
				IngogoApp.setPrimaryCardReaderAttached(true);
				_unimagHandler.post(onReadyToSwipe);
			}
		}

	}

	private Runnable onSwipeSuccess = new Runnable() {
		public void run() {

			IngogoApp.setSwipeRecordedTime(getTimeString());

			try {
				_cardInfo = new CardInfo(_cardDataString);
			} catch (CardInfoParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				Log.e("UNIMAG", "UNIMAG" + " " + e.getMessage());
			}
			_isBadSwipe = false;
			_payOfflineButton.setClickable(false);
			_payOfflineButton.getBackground().setAlpha(ENABLED_ALPHA);
			_payOfflineButton.setEnabled(true);
			if (!_calledProcessPaymentApi) {
				_calledProcessPaymentApi = true;

				_screenStatus = swipeStatus.defaultStatus;

				// //////////////////////

				Double paymentDue = 0.0;
				try {
					paymentDue = Double.parseDouble(_totalPaymentDueTV
							.getText().toString());
				} catch (NumberFormatException e) {
					paymentDue = 0.0;
				}
				if (_isKeyboardShown) {
					showOKAlertWithMessage(
							getResources().getString(
									R.string.keyboard_shown_text), true);
					return;
				}
				if (getFareEntered() == 0) {
					showOKAlertWithMessage(
							getResources().getString(
									R.string.enter_fare_amt_zero), true);
					return;
				}

				if (paymentDue > _maximumFare) {
					String _maximumFareString = (String.valueOf(_decimalFormat
							.format(_maximumFare)));
					String message = IGSwipeCalculatorActivity.this
							.getText(R.string.payment_due_max_limit_reached_message_section_1)
							+ _maximumFareString
							+ ". "
							+ IGSwipeCalculatorActivity.this
									.getText(R.string.payment_due_max_limit_reached_message_section_2);
					showOKAlertWithMessage(message, true);
					_payOfflineButton.setClickable(true);

					return;
				}
				if (paymentDue > _confirmationValue) {
					_payOfflineButton.setClickable(true);

					if (!_isConfirmAlertShown) {
						showPaymentConfirmationAlertWithMessage(
								IGSwipeCalculatorActivity.this
										.getString(R.string.payment_confirmation_msg),
								true);
					} else {
						onSwipePerformed();

					}

					return;

				}
				if (_ingogoCreditBalance == 0 && getFareEntered() != 0) {
					if (paymentDue < _minimumFare) {
						String _minFareString = (String.valueOf(_decimalFormat
								.format(_minimumFare)));
						String message = IGSwipeCalculatorActivity.this
								.getText(R.string.payment_due_below_min_limit)
								+ _minFareString
								+ ". "
								+ IGSwipeCalculatorActivity.this
										.getText(R.string.payment_due_max_limit_reached_message_section_2);
						showOKAlertWithMessage(message, true);
						_payOfflineButton.setClickable(true);

						return;
					}
				}
				onSwipePerformed();

			} else {
				onSwipeInit();

			}

		}
	};

	private Runnable onDisconnected = new Runnable() {
		public void run() {
			try {
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueMsgTv.setText(_unimagMessage);
				if (_confirmBtn.getVisibility() != View.VISIBLE) {
					_intialiseCardReader.setVisibility(View.VISIBLE);
				}
				_screenStatus = swipeStatus.cardnotready;
				_intialiseCardReader.setEnabled(true);
				_isBadSwipe = false;

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	private Runnable onReadyToSwipe = new Runnable() {
		public void run() {
			try {

				_totalPaymentDueMsgTv.setText("Swipe card ...");
				IngogoApp.setSwipeInitialisationCompleteTime(getTimeString());
				_intialiseCardReader.setVisibility(View.GONE);
				_intialiseCardReader.setEnabled(false);
				_screenStatus = swipeStatus.pleaseSwipe;
				setButtonStatesForCardReaderInitialisation(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	private Runnable doUpdateStatus = new Runnable() {
		public void run() {
			try {
				setButtonStatesForCardReaderInitialisation(true);

				_intialiseCardReader.setVisibility(View.VISIBLE);
				_intialiseCardReader.setEnabled(true);
				_totalPaymentDueMsgTv.setText(_unimagMessage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	private Runnable startSwipe = new Runnable() {
		public void run() {
			startSwipe();
		}
	};

	private Runnable onBadRead = new Runnable() {
		public void run() {
			if (isDeviceConnected()) {
				_totalPaymentDueMsgTv
						.setText((String) IGSwipeCalculatorActivity.this
								.getText(R.string.bad_swipe_read));
				_isBadSwipe = true;
				_calledProcessPaymentApi = false;
				_screenStatus = swipeStatus.badswipe;
				_payOfflineButton.setClickable(true);
				if (_isKeyboardShown) {

					showOKAlertWithMessage(
							getResources().getString(
									R.string.keyboard_shown_text), true);
				} else {
					onSwipeInit();
				}

			} else {
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueMsgTv
						.setText((String) IGSwipeCalculatorActivity.this
								.getText(R.string.card_reader_not_ready));
				if (_confirmBtn.getVisibility() != View.VISIBLE) {
					_intialiseCardReader.setVisibility(View.VISIBLE);

				}

				_intialiseCardReader.setEnabled(true);
				IngogoApp.setPrimaryCardReaderAttached(true);
				IngogoApp.setInitialCardCheck(true);

				_initUnimagInProgress = false;
				_screenStatus = swipeStatus.cardnotready;
				_calledProcessPaymentApi = false;
			}
		}
	};

	private Runnable showReaderNotDetectedDialog = new Runnable() {
		public void run() {
			showReaderNotDetectedDialog();
		}
	};

	private Runnable initialiseReaderRunnable = new Runnable() {
		public void run() {
			setButtonStatesForCardReaderInitialisation(false);

			if (unimagReader == null)
				unimagReader = new UniMagReader(IGSwipeCalculatorActivity.this,
						IGSwipeCalculatorActivity.this);
			unimagReader.registerListen();
			unimagReader.setTimeoutOfSwipeCard(10000);
			String fileNameWithPath = IGUtility.getXMLFileFromRaw();
			if (!IGUtility.isFileExist(fileNameWithPath)) {
				fileNameWithPath = null;
			}
			unimagReader.setXMLFileNameWithPath(fileNameWithPath);
			unimagReader.loadingConfigurationXMLFile(true);
		}
	};

	private void showReaderNotDetectedDialog() {
		Dialog dlg = new AlertDialog.Builder(IGSwipeCalculatorActivity.this)
				.setTitle("")
				.setMessage(
						"Unable to detect ingogo certified card reader. Please attach the ingogo card reader and try again.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						resetUnimagReader();

						_screenStatus = swipeStatus.defaultStatus;
						_isUnimagReaderConnected = false;
						_initUnimagInProgress = false;
						_unimagMessage = (String) IGSwipeCalculatorActivity.this
								.getText(R.string.card_reader_not_ready);
						_unimagHandler.post(onDisconnected);
					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.setCancelable(false);
		dlg.show();
	}

	@Override
	public void processPaymentForUnknownPassengerCompleted(
			IGProcessPaymentForUnknownPassengerResponseBean details) {

		IGUtility.dismissProgressDialog(_progressDialog);
		_confirmBtn.setClickable(true);
		_payOfflineButton.setClickable(true);
		if (!details.isCanTakePayment()) {
			goToJobsActivity();
		} else {
			if (_isUnknownPassenger) {
				Intent paymentCompletedIntent = new Intent(
						IGSwipeCalculatorActivity.this,
						IGPaymentCompletedActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
						_totalPaymentDueTV.getText().toString());
				// paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
				// "Your payment was successful !!!");
				try {
					if (details.getBookingSummary().getBookingId() != null) {
						_jobID = details.getBookingSummary().getBookingId();

					}
				} catch (Exception e) {

				}
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						details.getReceiptInformationPageText());
				paymentCompletedIntent.putExtra(IGConstants.kBookingId, details
						.getBookingSummary().getBookingId());
				paymentCompletedIntent.putExtra(
						IGConstants.kReceiptInformationKey,
						details.getReceiptInformation());
				startActivity(paymentCompletedIntent);
			//	finish();
				_screenStatus = swipeStatus.defaultStatus;

			} else {
				Intent paymentCompletedIntent = new Intent(
						IGSwipeCalculatorActivity.this,
						IGPaymentSucessActivity.class);
				try {
					if (details.getBookingSummary().getBookingId() != null) {
						_jobID = details.getBookingSummary().getBookingId();

					}
				} catch (Exception e) {

				}

				paymentCompletedIntent.putExtra(IGConstants.kJobId, details
						.getBookingSummary().getBookingId());
				paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
						_totalPaymentDueTV.getText().toString());
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						"Your payment was successful !!!");
				startActivity(paymentCompletedIntent);
			//	finish();

				_screenStatus = swipeStatus.defaultStatus;

			}
		}

	}

	@Override
	public void processPaymentForUnknownPassengerFailed(String errorContent,
			IGProcessPaymentForUnknownPassengerResponseBean details) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_confirmBtn.setClickable(true);
		if (details != null) {
			try {
				if (details.getBookingSummary().getBookingId() != null) {
					_jobID = details.getBookingSummary().getBookingId();

				}
			} catch (Exception e) {

			}

		}
		_payOfflineButton.setClickable(true);

		_processPaymentInProgress = false;
		if (details != null) {
			if (!details.isCanTakePayment()) {
				goToJobsActivity();
			} else {
				setErrorHeadingAndMessageAndCallFailurePage(errorContent);

			}
		} else {
			setErrorHeadingAndMessageAndCallFailurePage(errorContent);

		}
	}

	private void setErrorHeadingAndMessageAndCallFailurePage(String errorContent) {
		String errorHeading = getText(R.string.payment_failure_message)
				.toString();
		String errorMessage = getText(
				R.string.payment_unspecified_failure_error_string).toString();

		if (errorContent.contains("DECLINED")
				|| (errorContent.contains("Payment declined"))) {
			errorHeading = "TRANSACTION DECLINED BY CARD HOLDER's BANK";
			errorMessage = "Transaction declined by card holders bank. Try another card.";
		} else if (errorContent.contains("EXPIRED_CARD")
				|| (errorContent.contains("Expired card"))) {
			errorHeading = "EXPIRED CARD";
			errorMessage = "Card has expired. Try another card";
		} else if (errorContent.contains("INSUFFICIENT_FUNDS")
				|| (errorContent.contains("Insufficient funds"))) {
			errorHeading = "INSUFFICIENT FUNDS";
			errorMessage = "Card has insufficient funds available. Try another card";
		}

		loadFailurePageWithMessage(errorHeading, errorMessage);
	}

	private Runnable onSwipeInit = new Runnable() {
		public void run() {
			onSwipeInit();
		}
	};

	public void onPickUpClick(View view) {
		if (_progressDialog == null) {
			_progressDialog = IGUtility.showProgressDialog(this);

		} else if (!_progressDialog.isShowing()) {
			_progressDialog = IGUtility.showProgressDialog(this);

		}
		_suburbListDialog = new IGSuburbListDialog(this, true);
		_suburbListDialog.getSuburbListView().setOnItemClickListener(
				new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						_pickUpLabel.setText(_suburbListDialog
								.getNewSuburbList().get(position));
						IngogoApp.getSharedApplication().setSelectedSuburbName(
								_suburbListDialog.getNewSuburbList().get(
										position));
						_suburbListDialog.hideSoftKeyboard();
						_suburbListDialog.dismiss();
						_totalFareLabel.setFocusable(true);
						_totalFareLabel.setFocusableInTouchMode(true);
						_totalFareLabel.requestFocus();
					}
				});
		invokeSuburbParser();
	}

	private void invokeSuburbParser() {
		IGSuburbParser parser = new IGSuburbParser();
		parser.getSerializedSuburbs(IngogoApp.getSharedApplication()
				.getLocalityName().toLowerCase(), this);
	}

	@Override
	public void readSuburbsSuccessfully(
			HashMap<String, ArrayList<IGSuburbModel>> serializedSuburbs) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;
		_suburbListDialog.setListAdapter(serializedSuburbs.get(IngogoApp
				.getSharedApplication().getLocalityName().toUpperCase()));
		_suburbListDialog.show();

	}

	@Override
	public void failedToReadSuburbs() {
		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;
	}

	@Override
	public void completeOfflineSuccess(String sucessString, String totalPaid) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_payOfflineButton.setEnabled(true);
		_payOfflineButton.getBackground().setAlpha(ENABLED_ALPHA);
		_screenStatus = swipeStatus.defaultStatus;
		// complete offline web service.

		if (sucessString != null && !sucessString.equals("null")) {

			if (totalPaid == null || totalPaid.trim().equals("null")) {
				totalPaid = "00.00";
			} else {
				Double total = Double.valueOf(totalPaid);
				totalPaid = (String.valueOf(_decimalFormat.format(total)));
			}
			if (_isUnknownPassenger) {
				Intent paymentCompletedIntent = new Intent(
						IGSwipeCalculatorActivity.this,
						IGPaymentCompletedActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
						totalPaid);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						"Your payment was successful !!!");
				paymentCompletedIntent.putExtra(IGConstants.kBookingId, _jobID);
				startActivity(paymentCompletedIntent);

			} else {
				Intent paymentCompletedIntent = new Intent(
						IGSwipeCalculatorActivity.this,
						IGPaymentSucessActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
						totalPaid);
				paymentCompletedIntent.putExtra(IGConstants.kJobId, _jobID);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						sucessString);
				startActivity(paymentCompletedIntent);
			}

		} else {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			IngogoApp.getSharedApplication().setComingFromPayOffline(true);
			goToJobsActivity();
		}

	}

	@Override
	public void completeOfflineFailed(String errorMessage,
			boolean isHandleDriverStaleState) {
		_payOfflineButton.setEnabled(true);
		_payOfflineButton.getBackground().setAlpha(ENABLED_ALPHA);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (isHandleDriverStaleState) {
			super.onNullResponseRecieved();
			return;
		}
		IGUtility.showDialog("", errorMessage, this);

	}

}
