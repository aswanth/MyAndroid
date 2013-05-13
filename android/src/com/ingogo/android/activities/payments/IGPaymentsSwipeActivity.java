package com.ingogo.android.activities.payments;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.ingogo.android.model.IGReceiptInformationModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGCustomScrollLayout;
import com.ingogo.android.utilities.IGCustomScrollLayout.Listener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCompleteOfflineJobApi;
import com.ingogo.android.webservices.IGProcessPaymentApi;
import com.ingogo.android.webservices.interfaces.IGCompleteOfflineListener;
import com.ingogo.android.webservices.interfaces.IGProcessPaymentApiListener;

/**
 * 
 * @author suslov
 * 
 */
public class IGPaymentsSwipeActivity extends IGPaymentBaseActivity implements
		IGProcessPaymentApiListener, UniMagReaderMsg, Listener,
		IGCompleteOfflineListener {
	private ProgressDialog _progressDialog;

	private EditText _fareEntered;
	private TextView _totalPaymentDueTV, _creditCardSurchargeTV,
			_totalPaymentDueTVMsgTv, _ingogoCreditTV;

	private Button _payOfflineButton;

	String _currentFare = IGConstants.zeroBalance;
	DecimalFormat _decimalFormat;
	private String _creditCardSurchargeString = "";
	List<JSONObject> _cardDetailsList = new Vector<JSONObject>();

	// sus
	private Double _minimumFare = 0.0;
	private Double _maximumFare = 0.0;
	private Double _confirmationValue = 0.0;
	private Double _ingogoCreditBalance = 0.0;

	private boolean _isUnknownPassenger;
	public static boolean _isNavigateFromMenu;
	private AlertDialog _alertNavigateFromMenu;
	boolean _showKeyboard = true;
	private boolean _isConfirmAlertShown;

	// public static boolean _isPayOfflineVisible;
	private static final int DISABLED_ALPHA = 110;
	private static final int ENABLED_ALPHA = 255;
	private boolean _processPaymentInProgress;

	private boolean _initUnimagInProgress;
	private String _unimagMessage;
	Handler _unimagHandler = new Handler();
	private UniMagReader unimagReader = null;
	private String _cardDataString;
	private boolean _isUnimagReaderConnected;
	private String _encryptedCardDataString;
	Handler _initialisationHandler = new Handler();

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
	private boolean _calledProcessPaymentApi;
	private String _bookingFeeString;
	private String _bidAmountString;
	private String _balanceString;
	private String _defaultCreditCardPercentange;
	static private String _job;
	private TextView _bidAmount;
	private TextView _bookingFee;
	private boolean _isIntialiseReaderClicked;
	private boolean _isKeyboardShown = false;
	static private HashMap<String, Object> _jobDetails;
	private Dialog _actitiveDialog;
	private boolean _isSwipeInactive = true;
	private Button _themeButton;
	private boolean _isButtonsEnabled;

	Handler payOfflineHandler = new Handler();
	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			_payOfflineButton.getBackground().setAlpha(ENABLED_ALPHA);
			_payOfflineButton.setEnabled(true);
			IngogoApp.sPaymentOfflineButtonEnableFlag = true;
			// new Handler().postDelayed(new Runnable() {
			//
			// @Override
			// public void run() {
			// _payOfflineButton.getBackground().setAlpha(ENABLED_ALPHA);
			// _payOfflineButton.setEnabled(true);
			// }
			// }, 2000);
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payments_swipe);
		_decimalFormat = new DecimalFormat(IGConstants.zeroBalance);
		_processPaymentInProgress = false;
		_isBadSwipe = false;
		int status = _screenStatus;
		initViews();
		payOfflineButtonState();
		_intialiseCardReader = (ImageButton) findViewById(R.id.intCardReaderBtn);
		if (status == swipeStatus.badswipe) {
			_totalPaymentDueTVMsgTv.setText("Bad read, swipe again...");
			_isBadSwipe = true;
			_calledProcessPaymentApi = false;
		}

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			_job = extras.getString(IGConstants.kJobId);
			_jobDetails = (HashMap<String, Object>) extras
					.get(IGConstants.kJobDetails);
			String _fare = extras.getString(IGConstants.kFareEntered);

			if (_fare != null) {
				_fareEntered.setEnabled(false);
				this._fareEntered.setEnabled(false);
				this._fareEntered.setClickable(false);
				this._fareEntered.setFocusable(false);
				_showKeyboard = false;
			} else {
				_showKeyboard = true;
			}
			JSONObject jObj;
			try {
				jObj = new JSONObject(
						(String) _jobDetails.get(IGConstants.kDetails));
				processCreditDetailsResponse(jObj);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		setUpViews();
		if (!isDeviceConnected()) {
			if (_job != null) {
				// Exception newExp = new Exception("Card reader not attached: "
				// + "Booking Id = " + _job + " ");
				// IGUtility.logExceptionInQLogger(newExp);
			}

		}

	}

	/**
	 * Solution to race condition of drivers pressing offline whilst swiping
	 * card.
	 * */
	private void payOfflineButtonState() {
		if (!(IngogoApp.getCurrentActivityOnTop() instanceof IGPaymentsSwipeActivity)) {
			_payOfflineButton.getBackground().setAlpha(DISABLED_ALPHA);
			_payOfflineButton.setEnabled(false);
			IngogoApp.getSharedApplication()
					.setPaymentSwipeActivityCreatedTime(
							System.currentTimeMillis());
			payOfflineHandler.postDelayed(runnable, 15000);
		} else if (!IngogoApp.sPaymentOfflineButtonEnableFlag) {
			_payOfflineButton.getBackground().setAlpha(DISABLED_ALPHA);
			_payOfflineButton.setEnabled(false);
			payOfflineHandler.removeCallbacks(runnable);
			long differenceInTime = 15000 - (System.currentTimeMillis() - IngogoApp
					.getSharedApplication()
					.getPaymentSwipeActivityCreatedTime());
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

	/**
	 * To process the response got by calling credit details web service
	 * 
	 * @param resp
	 */
	private void processCreditDetailsResponse(JSONObject resp) {

		if (resp != null) {
			if (resp.has(IGApiConstants.kCardDetails)) {
				try {
					sortCreditDetails(resp
							.getJSONArray(IGApiConstants.kCardDetails));
				} catch (JSONException e) {
					_cardDetailsList = null;
					e.printStackTrace();
				}
			}

			if (resp.has(IGConstants.kBooking)) {
				try {
					JSONObject creditResp = (JSONObject) resp
							.getJSONObject(IGConstants.kBooking);

					DecimalFormat decimalFormat = new DecimalFormat(
							IGConstants.zeroBalance);
					if (creditResp.has(IGConstants.kBookingFee)) {
						Double bookingFee = Double.parseDouble(creditResp
								.getString(IGConstants.kBookingFee));
						_bookingFeeString = (String.valueOf(decimalFormat
								.format(bookingFee)));

					} else {
						_bookingFeeString = null;
					}

					if (resp != null && resp.has(IGConstants.kApplyBid)) {
						boolean applyBidResponse = resp
								.getBoolean(IGConstants.kApplyBid);

						if (applyBidResponse == true) {

							if (creditResp.has(IGConstants.kBidExtra)) {

								Double bidAmount = Double
										.parseDouble(creditResp
												.getString(IGConstants.kBidExtra));

								_bidAmountString = (String
										.valueOf(decimalFormat
												.format(bidAmount)));

							}

						} else {
							_bidAmountString = null;
						}
					} else {
						_bidAmountString = null;
					}

					if (creditResp.has(IGConstants.kBalance)) {
						Double bidCredit = Double.parseDouble(creditResp
								.getString(IGConstants.kBalance));
						_ingogoCreditBalance = bidCredit;
						Log.i("available credit", "" + bidCredit);
						_balanceString = (String.valueOf(decimalFormat
								.format(bidCredit)));

					} else {
						_balanceString = null;
					}

				}

				catch (JSONException e1) {
					e1.printStackTrace();
				}

			}

			if (resp.has(IGApiConstants.kminTotalDueValue)) {
				try {
					_minimumFare = resp
							.getDouble(IGApiConstants.kminTotalDueValue);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (resp.has(IGApiConstants.kconfirmationValue)) {
				try {
					_confirmationValue = resp
							.getDouble(IGApiConstants.kconfirmationValue);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (resp.has(IGApiConstants.kmaxTotalDueValue)) {
				try {
					_maximumFare = resp
							.getDouble(IGApiConstants.kmaxTotalDueValue);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				_defaultCreditCardPercentange = resp
						.getString(IGApiConstants.kCreditPercentage);
			} catch (JSONException e) {
				_defaultCreditCardPercentange = IGConstants.zeroBalance;
				e.printStackTrace();
			}

		}
	}

	private void sortCreditDetails(JSONArray creditArray) {
		if (creditArray.length() > 1) {
			boolean defaultCardPresent = false;
			try {
				_cardDetailsList.clear();
				for (int i = 0; i < creditArray.length(); i++) {

					_cardDetailsList.add(creditArray.getJSONObject(i));
					if (_cardDetailsList.get(i).getBoolean("isDefault")) {
						_cardDetailsList.remove(i);
						_cardDetailsList.add(0, creditArray.getJSONObject(i));
						defaultCardPresent = true;
					}
				}

				// If no card is selected as the default card then sort the
				// cards alphabetically and show the first card as default.
				if (!defaultCardPresent) {
					Collections.sort(_cardDetailsList,
							new CardDetailsComperator());
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				_cardDetailsList.add(creditArray.getJSONObject(0));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private class CardDetailsComperator implements Comparator<JSONObject> {

		@Override
		public int compare(JSONObject firstCardDetails,
				JSONObject secondCardDetails) {
			try {
				return firstCardDetails.getString("cardNickname")
						.compareToIgnoreCase(
								secondCardDetails.getString("cardNickname"));
			} catch (JSONException e) {
				e.printStackTrace();
				return 0;
			}

		}

	}

	@Override
	protected void onResume() {
		if (unimagReader != null) {
			unimagReader.registerListen();
		}
		IGUpdatePositionPollingTask.ignoreStaleState = true;
		showAlertForJobInProgress();
		_isConfirmAlertShown = false;
		super.onResume();
		// //////////////////
		// set speaker phone off
		_isIntialiseReaderClicked = false;
		_calledProcessPaymentApi = false;
		_confirmBtn.setVisibility(View.GONE);

		totalPaymentDue();
		if (_confirmBtn.getVisibility() != View.VISIBLE) {
			if (_cardInfo == null) {
				if (_screenStatus != swipeStatus.cardnotready) {
					_calledProcessPaymentApi = false;
					onSwipeInit();
				} else {
					_screenStatus = swipeStatus.cardnotready;
					_intialiseCardReader.setVisibility(View.VISIBLE);
					_intialiseCardReader.setEnabled(true);
					_calledProcessPaymentApi = false;
					setButtonStatesForCardReaderInitialisation(true);

					_totalPaymentDueTVMsgTv.setText("Card reader not ready...");
					// _payOffline.setEnabled(true);

				}

			}
		}
		if (_showKeyboard) {
			try {
				showKeyboard();
			} catch (Exception e) {
			}
		} else {
			_isConfirmAlertShown = true;

		}
		setUpEditTextListeners();
		setUpLayoutListerner();
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
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	private void setUpEditTextListeners() {
		_fareEntered
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {

					/*
					 * Respond to soft keyboard events, look for the DONE press
					 * on the fare enterted field.
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
								String message = IGPaymentsSwipeActivity.this
										.getText(R.string.payment_due_max_limit_reached_message_section_1)
										+ _maximumFareString
										+ ". "
										+ IGPaymentsSwipeActivity.this
												.getText(R.string.payment_due_max_limit_reached_message_section_2);
								showOKAlertWithMessage(message, false);

							} else if (paymentDue > _confirmationValue) {
								showPaymentConfirmationAlertWithMessage(
										IGPaymentsSwipeActivity.this
												.getString(R.string.payment_confirmation_msg),
										false);

							} else if (_ingogoCreditBalance == 0
									&& getFareEntered() != 0) {
								if (paymentDue < _minimumFare) {
									String _minFareString = (String
											.valueOf(_decimalFormat
													.format(_minimumFare)));
									String message = IGPaymentsSwipeActivity.this
											.getText(R.string.payment_due_below_min_limit)
											+ _minFareString
											+ ". "
											+ IGPaymentsSwipeActivity.this
													.getText(R.string.payment_due_max_limit_reached_message_section_2);
									showOKAlertWithMessage(message, false);

								}
							}

						}
						// Returning false allows other listeners to react to
						// the press.
						return false;

					}

				});
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
									_totalPaymentDueTVMsgTv
											.setText("Swipe card ...");
								}

							} else {
								_screenStatus = swipeStatus.initialising;

							}
						} else {
							_unimagMessage = (String) IGPaymentsSwipeActivity.this
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
							_unimagMessage = (String) IGPaymentsSwipeActivity.this
									.getText(R.string.card_reader_not_ready);
							_unimagHandler.post(doUpdateStatus);
						}

					}

				} else {
					resetUnimagReader();
					_unimagMessage = (String) IGPaymentsSwipeActivity.this
							.getText(R.string.card_reader_not_ready);
					_unimagHandler.post(doUpdateStatus);

				}

			}
		} else {
			_cardInfo = null;
			if (!_isBadSwipe) {
				if (isDeviceConnected()) {
					_totalPaymentDueTVMsgTv.setText("Initialising reader ...");
					_initUnimagInProgress = true;
					setButtonStatesForCardReaderInitialisation(false);

					if (unimagReader == null)
						unimagReader = new UniMagReader(
								IGPaymentsSwipeActivity.this,
								IGPaymentsSwipeActivity.this);
					unimagReader.registerListen();
					unimagReader.setTimeoutOfSwipeCard(10000);
					String fileNameWithPath = IGUtility.getXMLFileFromRaw();
					if (!IGUtility.isFileExist(fileNameWithPath)) {
						fileNameWithPath = null;
					}
					unimagReader.setXMLFileNameWithPath(fileNameWithPath);
					unimagReader.loadingConfigurationXMLFile(true);

				} else {
					_totalPaymentDueTVMsgTv
							.setText((String) IGPaymentsSwipeActivity.this
									.getText(R.string.card_reader_not_ready));
					IGUtility.dismissProgressDialog(_progressDialog);

					if (_isIntialiseReaderClicked) {
						_isIntialiseReaderClicked = false;
						Dialog dlg = new AlertDialog.Builder(
								IGPaymentsSwipeActivity.this)
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
				_totalPaymentDueTVMsgTv.setVisibility(View.VISIBLE);
				if (_screenStatus == swipeStatus.pleaseSwipe
						|| _screenStatus == swipeStatus.defaultStatus) {
					if (_screenStatus != swipeStatus.defaultStatus) {
						_totalPaymentDueTVMsgTv.setText("Swipe card ...");
					}

				} else {
					_screenStatus = swipeStatus.initialising;

				}
			} else {
				_screenStatus = swipeStatus.badswipe;
			}
		}

	}

	/**
	 * to initialize a reader
	 */
	private void onSwipeInit() {
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

					_totalPaymentDueTVMsgTv.setText("Initialising reader ...");
					_initUnimagInProgress = true;

					_initialisationHandler
							.removeCallbacks(initialiseReaderRunnable);
					_initialisationHandler.postDelayed(
							initialiseReaderRunnable, 400);

				} else {
					setButtonStatesForCardReaderInitialisation(true);

					_screenStatus = swipeStatus.cardnotready;
					if (_confirmBtn.getVisibility() != View.VISIBLE) {
						_intialiseCardReader.setVisibility(View.VISIBLE);
						_totalPaymentDueTVMsgTv
								.setText("Card reader not ready...");
						_totalPaymentDueTVMsgTv.setVisibility(View.VISIBLE);
					}
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

				if (_screenStatus == swipeStatus.pleaseSwipe
						|| _screenStatus == swipeStatus.defaultStatus) {
					if (_screenStatus != swipeStatus.defaultStatus) {
						_totalPaymentDueTVMsgTv.setText("Swipe card ...");
					}

				} else {
					_screenStatus = swipeStatus.initialising;

				}

			} else {
				_screenStatus = swipeStatus.badswipe;
				if (isDeviceConnected()) {
					startSwipe();

				}
			}

			if (!isDeviceConnected()) {
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueTVMsgTv.setText("Card reader not ready...");

				return;

			}

		} else {
			if (!_isBadSwipe) {
				_totalPaymentDueTVMsgTv.setText("Initialising reader ...");
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

	private void showKeyboard() throws IllegalArgumentException {
		if (isDeviceConnected()) {
			_fareEntered.postDelayed(new Runnable() {

				@Override
				public void run() {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(_fareEntered,
							InputMethodManager.SHOW_FORCED);
				}
			}, 750);

		} else {
			try {
				this.getWindow()
						.setSoftInputMode(
								WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			} catch (Exception e) {

			}

		}
		_fareEntered.setSelection(_fareEntered.getText().length());
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
					// showKeyboard();
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

		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (_actitiveDialog != null) {
			_actitiveDialog.dismiss();
		}
		if (_alertNavigateFromMenu != null) {
			_alertNavigateFromMenu.dismiss();
		}

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
		_totalPaymentDueTVMsgTv = (TextView) findViewById(R.id.totalPaymentDueMsgTv);
		_ingogoCreditTV = (TextView) findViewById(R.id.bidCreditTv);
		_bidAmount = (TextView) findViewById(R.id.swipeBidAmountTV);
		_bookingFee = (TextView) findViewById(R.id.swipeBookingFeeTv);
		_confirmBtn = (ImageButton) findViewById(R.id.confirmBtn);
		_totalPaymentDueTVMsgTv
				.setText(R.string.total_payment_due_condition_check_msg);
		_themeButton = (Button) findViewById(R.id.themeButton);

		_isConfirmAlertShown = false;
	}

	public void onConfirmButtonClick(View view) {

		callProcessPaymentApi();
	}

	@Override
	public void onBackPressed() {
		// if(getIntent().getExtras()!=null) {
		// boolean isbackButtonEnabled =
		// getIntent().getBooleanExtra(IGConstants.isBackButtonEnabled, false);
		// if(isbackButtonEnabled) {
		// super.onBackPressed();
		// return;
		// }
		// }
		return;
	}

	private void setFareText(String text) {
		_fareEntered.setText(text);
	}

	/**
	 * To calculate the total payment due and set the total amount in the
	 * _totalPaymentDueTV text view.
	 */
	private void totalPaymentDue() {

		double payableAmount = 0;

		String fareAmount = _fareEntered.getText().toString();
		String bookingFee = _bookingFee.getText().toString();
		String bidAmount = _bidAmount.getText().toString();

		if (fareAmount.length() != 0) {
			if (fareAmount.contentEquals(".")) {
				fareAmount = "0.";
			}
			payableAmount += Double.parseDouble(fareAmount);

		}

		if (bookingFee.length() != 0) {
			payableAmount += Double.parseDouble(bookingFee);
		}

		if (bidAmount.length() != 0) {
			payableAmount += Double.parseDouble(bidAmount);
		}
		payableAmount = RoundTo2Decimals(payableAmount);
		double balance = 0.0;
		try {
			balance = Double.parseDouble(_balanceString);
		} catch (NumberFormatException e) {
			balance = 0.0;
		} catch (NullPointerException e) {
			balance = 0.0;
		}

		if (balance >= payableAmount) {
			_creditCardSurchargeTV.setText(IGConstants.zeroBalance);
		} else {
			DecimalFormat decimalFormat = new DecimalFormat(
					IGConstants.zeroBalance);
			String surcharge = (String.valueOf(decimalFormat
					.format(findCreditPercentageFromCreditArray()
							* (payableAmount - balance) / 100)));
			_creditCardSurchargeTV.setText(surcharge);
		}
		_creditCardSurchargeString = _creditCardSurchargeTV.getText()
				.toString();
		String surCharge = _creditCardSurchargeTV.getText().toString();

		double surchargeAmount = 0;
		if (surCharge.length() != 0) {
			surchargeAmount += Double.parseDouble(surCharge);
		}

		double totalPaymentDue = payableAmount - balance + surchargeAmount;
		if (totalPaymentDue > 0) {
			DecimalFormat decimalFormat = new DecimalFormat(
					IGConstants.zeroBalance);
			String totalFare = (String.valueOf(decimalFormat
					.format(totalPaymentDue)));
			_totalPaymentDueTV.setText(totalFare);
		} else {
			_totalPaymentDueTV.setText(IGConstants.zeroBalance);
		}

		totalPaymentDue = Double.parseDouble(_totalPaymentDueTV.getText()
				.toString());

		if (totalPaymentDue > 0) {
			_totalPaymentDueTV.setText(""
					+ _decimalFormat.format(totalPaymentDue));
			_totalPaymentDueTVMsgTv.setVisibility(View.VISIBLE);
			_confirmBtn.setVisibility(View.GONE);

			if (!isDeviceConnected()
					|| _screenStatus == swipeStatus.cardnotready) {
				_screenStatus = swipeStatus.cardnotready;
				_intialiseCardReader.setVisibility(View.VISIBLE);
				_intialiseCardReader.setEnabled(true);
				_totalPaymentDueTVMsgTv.setVisibility(View.VISIBLE);
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueTVMsgTv.setText("Card reader not ready...");
			} else {
				_intialiseCardReader.setVisibility(View.GONE);

				_totalPaymentDueTVMsgTv.setVisibility(View.VISIBLE);
			}

		} else {
			_totalPaymentDueTV.setText(IGConstants.zeroBalance);
			_confirmBtn.setVisibility(View.GONE);
			if (Double.parseDouble(fareAmount) > 0) {
				_totalPaymentDueTVMsgTv.setVisibility(View.GONE);
				_confirmBtn.setVisibility(View.VISIBLE);
				_intialiseCardReader.setVisibility(View.GONE);
			} else if (!isDeviceConnected()
					|| _screenStatus == swipeStatus.cardnotready) {
				_screenStatus = swipeStatus.cardnotready;
				_intialiseCardReader.setVisibility(View.VISIBLE);
				_intialiseCardReader.setEnabled(true);
				_totalPaymentDueTVMsgTv.setVisibility(View.VISIBLE);
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueTVMsgTv.setText("Card reader not ready...");

			} else {
				_intialiseCardReader.setVisibility(View.GONE);

				_totalPaymentDueTVMsgTv.setVisibility(View.VISIBLE);

			}
		}

	}

	double RoundTo2Decimals(double val) {
		DecimalFormat df2 = new DecimalFormat("###.##");
		return Double.valueOf(df2.format(val));
	}

	/**
	 * Function to get the credit percentage from the credit details array.
	 * Loops through the credit array, selects the credit percentage from the
	 * default card.
	 * 
	 * @param creditArray
	 * @return
	 */
	private double findCreditPercentageFromCreditArray() {
		if (_cardDetailsList != null && _cardDetailsList.size() > 0) {
			try {
				return _cardDetailsList.get(0).getDouble(
						IGApiConstants.kCreditPercentage);
			} catch (JSONException e) {

				e.printStackTrace();
				return 0;
			}

		} else {
			try {
				return Double.parseDouble(_defaultCreditCardPercentange);

			} catch (Exception e) {
				return 0;
			}
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

		if (!IngogoApp.getSharedApplication().getMeterFare()
				.equals(IGConstants.zeroBalance)) {

			setFareText(IngogoApp.getSharedApplication().getMeterFare());

		} else {
			setFareText(IGConstants.zeroBalance);

		}

		if (_bidAmountString == null) {
			_bidAmount.setText(IGConstants.zeroBalance);

		} else {
			_bidAmount.setText(_bidAmountString);
		}

		if (_bookingFeeString != null) {
			_bookingFee.setText(_bookingFeeString);
		} else {
			_bookingFee.setText(IGConstants.zeroBalance);
		}

		if (_balanceString != null) {
			_ingogoCreditTV.setText("(" + _balanceString + ")");
		} else {
			_ingogoCreditTV.setText("(" + IGConstants.zeroBalance + ")");
		}
		totalPaymentDue();

		// Populate the fare text field with the total amount to be
		// paid by the passenger.
		totalPaymentDue();

		// Any text change in fare editText is caught by this listener
		_fareEntered.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				// When 1 is entered, it will be shown as '00.01'
				try {
					if (!s.toString().equals(_currentFare)) {
						_fareEntered.removeTextChangedListener(this);

						String cleanString = s.toString().replace(".", "");

						double parsed = Double.parseDouble(cleanString);

						DecimalFormat decimalFormat = new DecimalFormat(
								IGConstants.zeroBalance);
						String formated = (String.valueOf(decimalFormat
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
					}
					_isConfirmAlertShown = false;
					totalPaymentDue();
				} catch (IllegalArgumentException e) {

				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// try {
				// _fareEntered.setGravity(Gravity.LEFT);
				// _fareEntered.setPadding(0, 10, 5, 0);
				// // _fareEntered.setCursorVisible(true);
				// } catch (IllegalArgumentException e) {
				//
				// }
			}

			@Override
			public void afterTextChanged(Editable s) {
				// Log.i("afterTextChanged--" + s, ""
				// + _fareEntered.getText().toString());

			}
		});

		_fareEntered
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {

					/*
					 * Respond to soft keyboard events, look for the DONE press
					 * on the password field.
					 */

					@Override
					public boolean onEditorAction(TextView view, int keyCode,
							KeyEvent event) {

						if ((keyCode == EditorInfo.IME_ACTION_SEARCH
								|| keyCode == EditorInfo.IME_ACTION_DONE || event
								.getAction() == KeyEvent.ACTION_DOWN
								&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							// Done pressed. Hide the soft keyboard
							Log.i("STATUS", "Keyboard down");
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									_fareEntered.getWindowToken(), 0);
							Log.i("faretext", "" + _fareEntered.getText());

						}
						// Returning false allows other listeners to react to
						// the press.
						return false;

					}

				});

	}

	private void showOKAlertWithMessage(String message, final boolean afterSwipe) {
		setButtonStatesForCardReaderInitialisation(true);
		_actitiveDialog = new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(IGConstants.OKMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (afterSwipe)
									onSwipeInit();
								// _isPayOfflineVisible = true;
								// _payOfflineButton.setVisibility(View.VISIBLE);
							}
						}).create();
		_actitiveDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (!isFinishing())
			_actitiveDialog.show();
	}

	private void showPaymentConfirmationAlertWithMessage(String message,
			final boolean afterSwipe) {
		_actitiveDialog = new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(IGConstants.ConfirmMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// navigates to the Swipe Card page.
								_isConfirmAlertShown = true;

								_payOfflineButton.setClickable(true);
								if (afterSwipe)
									onSwipePerformed();

							}
						})
				.setNegativeButton(IGConstants.CancelMessage,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (afterSwipe)
									onSwipeInit();

							}
						}).create();
		_actitiveDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (!isFinishing())
			_actitiveDialog.show();
	}

	private void callProcessPaymentApi() {

		if (IGUtility.isNetworkAvailable(this)) {
			_processPaymentInProgress = true;
			_progressDialog = IGUtility.showProgressDialog(this);

			_totalPaymentDueTVMsgTv.setText("Processing payment...");
			IGProcessPaymentApi api = new IGProcessPaymentApi(this, this);
			if (_encryptedCardDataString != null) {
				if (_encryptedCardDataString.length() > 4) {
					api.processPayment(_job, _fareEntered.getText().toString(),
							_totalPaymentDueTV.getText().toString(),
							getCreditCardInformation(),
							_encryptedCardDataString, "");
				} else {
					api.processPayment(_job, _fareEntered.getText().toString(),
							_totalPaymentDueTV.getText().toString(),
							getCreditCardInformation(), "");
				}
			} else {
				api.processPayment(_job, _fareEntered.getText().toString(),
						_totalPaymentDueTV.getText().toString(),
						getCreditCardInformation(), "");
			}

		} else {
			_totalPaymentDueTVMsgTv.setText("Swipe card ...");
			onSwipeInit();
			if (!isFinishing()) {
				IGUtility.showDialogOk(
						this.getText(R.string.network_error_title).toString(),
						this.getText(R.string.ReachabilityMessage).toString(),
						this);
			}
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
			if (_job != null) {
				// Exception newExp = new Exception("Swipe Pay offline: "
				// + "Booking Id = " + _job + " ");
				// IGUtility.logExceptionInQLogger(newExp);
			}

		}
		callCompleteOfflineJobApi();
		// bookings/completeOffline w/s is called and on success we move back to
		// job list page
	}

	private void callCompleteOfflineJobApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			// Disable the button to prevent multiple click.
			_payOfflineButton.setEnabled(false);
			_payOfflineButton.getBackground().setAlpha(DISABLED_ALPHA);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCompleteOfflineJobApi _completeOfflineApi = new IGCompleteOfflineJobApi(
					this, this, _job);
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

		Intent intent = new Intent(IGPaymentsSwipeActivity.this,
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
							IGPaymentsSwipeActivity.this,
							IGPaymentCompletedActivity.class);
					paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
							totalPaid);
					paymentCompletedIntent.putExtra(
							IGConstants.kPaymentSuccess,
							"Your payment was successful !!!");
					paymentCompletedIntent.putExtra(IGConstants.kBookingId,
							_job);
					startActivity(paymentCompletedIntent);

				} else {
					Intent paymentCompletedIntent = new Intent(
							IGPaymentsSwipeActivity.this,
							IGPaymentSucessActivity.class);
					paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
							totalPaid);
					paymentCompletedIntent.putExtra(IGConstants.kJobId, _job);

					paymentCompletedIntent.putExtra(
							IGConstants.kPaymentSuccess, sucessString);
					startActivity(paymentCompletedIntent);
				}

			} else {

				IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
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
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (_processPaymentInProgress) {
			loadFailurePageWithMessage(
					getText(R.string.payment_failure_message).toString(),
					getText(R.string.payment_unspecified_failure_error_string)
							.toString());
			_processPaymentInProgress = false;
		} else {
			calculatordataFetchFailed = true;

			super.onNetWorkUnavailableResponse(errorResponse);
		}

	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {

		IGUtility.dismissProgressDialog(_progressDialog);
		if (_processPaymentInProgress) {
			loadFailurePageWithMessage(
					getText(R.string.payment_failure_message).toString(),
					getText(R.string.payment_unspecified_failure_error_string)
							.toString());
			_processPaymentInProgress = false;
		} else {
			calculatordataFetchFailed = true;

			super.onRequestTimedoutResponse(errorResponse);
		}
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {

		IGUtility.dismissProgressDialog(_progressDialog);
		if (_processPaymentInProgress) {
			loadFailurePageWithMessage(
					getText(R.string.payment_failure_message).toString(),
					getText(R.string.payment_unspecified_failure_error_string)
							.toString());
			_processPaymentInProgress = false;
		} else {
			calculatordataFetchFailed = true;

			super.onInternalServerErrorResponse(errorResponse);
		}

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (_processPaymentInProgress) {
			loadFailurePageWithMessage(
					getText(R.string.payment_failure_message).toString(),
					getText(R.string.payment_unspecified_failure_error_string)
							.toString());
			_processPaymentInProgress = false;
		} else {
			calculatordataFetchFailed = true;

			super.onNullResponseRecieved();
		}

	}

	@Override
	public void processPaymentCompleted(IGBookingModel bookingModel,
			String receiptInformationPageText,
			IGReceiptInformationModel receiptInformation) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_screenStatus = swipeStatus.defaultStatus;
		finish();
		if (_isUnknownPassenger) {
			Intent paymentCompletedIntent = new Intent(
					IGPaymentsSwipeActivity.this,
					IGPaymentCompletedActivity.class);
			paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
					_totalPaymentDueTV.getText().toString());
			// paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
			// "Your payment was successful !!!");
			paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
					receiptInformationPageText);
			paymentCompletedIntent.putExtra(IGConstants.kBookingId, _job);
			paymentCompletedIntent.putExtra(IGConstants.kReceiptInformationKey,
					receiptInformation);
			startActivity(paymentCompletedIntent);

		} else {
			Intent paymentCompletedIntent = new Intent(
					IGPaymentsSwipeActivity.this, IGPaymentSucessActivity.class);
			paymentCompletedIntent.putExtra(IGConstants.kJobId, _job);

			paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
					_totalPaymentDueTV.getText().toString());
			paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
					"Your payment was successful !!!");
			startActivity(paymentCompletedIntent);
		}

	}

	@Override
	public void processPaymentFailed(String errorContent) {
		IGUtility.dismissProgressDialog(_progressDialog);
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
		_screenStatus = swipeStatus.defaultStatus;

		Intent failureIntent = new Intent(IGPaymentsSwipeActivity.this,
				IGPaymentFailureActivity.class);
		failureIntent.putExtra(IGApiConstants.kIsSwipeFailure, true);
		failureIntent.putExtra(IGConstants.kJobId, _job);
		failureIntent.putExtra(IGConstants.kJobDetails, _jobDetails);
		failureIntent.putExtra("isUnknownPassenger", _isUnknownPassenger);

		failureIntent.putExtra(IGConstants.kFareEntered, _fareEntered.getText()
				.toString());
		failureIntent.putExtra(IGConstants.kTotalDueAmount, _totalPaymentDueTV
				.getText().toString());
		failureIntent.putExtra("cardinfo", getCreditCardInformation());
		failureIntent.putExtra(IGConstants.kErrorMessage, errorMessage);
		failureIntent.putExtra(IGConstants.kErrorHeading, errorHeading);
		failureIntent.putExtra(IGConstants.kBookingType,
				IGConstants.kBookingTypeBooking);
		failureIntent.putExtra(IGConstants.kBookingFee, _bookingFee.getText()
				.toString());
		failureIntent.putExtra(IGConstants.kBidExtra, _bidAmount.getText()
				.toString());
		failureIntent.putExtra(IGConstants.kBalance, _creditCardSurchargeTV
				.getText().toString());
		failureIntent.putExtra(IGConstants.kPaymentDue, _totalPaymentDueTV
				.getText().toString());
		if (_encryptedCardDataString == null) {
			failureIntent.putExtra("encryptedCardDataString", "");
		} else {
			failureIntent.putExtra("encryptedCardDataString",
					_encryptedCardDataString);
		}

		HashMap<String, Object> paymentDetails = new HashMap<String, Object>();
		paymentDetails.put(IGConstants.kBookingFee, _bookingFee.getText()
				.toString());
		paymentDetails.put(IGConstants.kBidExtra, _bidAmount.getText()
				.toString());
		paymentDetails.put(IGConstants.kBalance, _creditCardSurchargeTV
				.getText().toString());
		paymentDetails.put(IGConstants.kPaymentDue, _totalPaymentDueTV
				.getText().toString());
		paymentDetails.put(IGConstants.kFareEntered, _fareEntered.getText()
				.toString());
		paymentDetails.put(IGConstants.kJobId, "" + _job);

		paymentDetails.put(IGApiConstants.kCardDetails,
				_cardDetailsList.toString());
		failureIntent.putExtra(IGConstants.kCreditCardCount,
				getCreditCardCount());
		failureIntent.putExtra(IGApiConstants.kCardDetails, paymentDetails);
		startActivity(failureIntent);
		//finish();
	}

	private int getCreditCardCount() {
		// TODO Auto-generated method stub
		int size = 0;

		JSONObject bookingDetails = null;
		try {
			bookingDetails = new JSONObject(
					(String) _jobDetails.get(IGConstants.kDetails));

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NullPointerException e) {
			// TODO: handle exception

		}
		if (bookingDetails != null) {
			if (bookingDetails.has(ACCOUNT_DETAIL_KEY)) {
				try {
					JSONArray array = bookingDetails
							.getJSONArray(ACCOUNT_DETAIL_KEY);
					size = array.length();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {

				}
			}

		}
		return size + _cardDetailsList.size();
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
				callProcessPaymentApi();

			} else if (year == _cardInfo.getExpirationYear()) {
				if (_cardInfo.getExpirationMonth() >= month) {
					callProcessPaymentApi();
				} else {
					// _payOffline.setEnabled(true);
					if (!isFinishing()) {
						_actitiveDialog = IGUtility.showDialog("",
								"Card has expired. Please try another card.",
								this);
					}
					onSwipeInit();
				}

			} else {
				// _payOffline.setEnabled(true);
				if (!isFinishing()) {
					_actitiveDialog = IGUtility.showDialog("",
							"Card has expired. Please try another card.", this);
				}
				onSwipeInit();
			}

		} else {
			// _payOffline.setEnabled(true);
			if (!isFinishing()) {
				_actitiveDialog = IGUtility
						.showDialog(
								"",
								"Only Visa and Mastercard are accepted. Please try another card.",
								this);
			}
			onSwipeInit();
		}

	}

	/**
	 * called after a swipe is success
	 */
	private void onSwipePerformed() {

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
	public boolean getUserGrant(int type, String message) {
		Log.e("UNIMAG", "UNIMAG getUserGrant  " + " type = " + type
				+ " message = " + message);
		return true;
	}

	public void onSoftKeyboardShown(boolean isShowing) {
		_isKeyboardShown = isShowing;
	}

	private void getEncryptionStatus(int commandID, byte[] cmdReturn) {
		Log.e("UNIMAG", "UNIMAG getEncryptionStatus  " + " commandID = "
				+ commandID + " cmdReturn = " + cmdReturn);

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
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgCardData"
				+ "flagOfCardData = " + flagOfCardData + "cardData = "
				+ cardData);

		byte flag = (byte) (flagOfCardData & 0x04);
		_cardDataString = null;
		_encryptedCardDataString = null;

		if (flag == 0x00) {
			Log.e("UNIMAG", "Demo Info " + "no need of decryption");

			_cardDataString = new String(cardData);
			_encryptedCardDataString = null;
			_unimagHandler.post(onSwipeSuccess);

		}
		if (flag == 0x04) {
			// You need to dencrypt the data here first.
			Log.e("UNIMAG", "Demo Info " + "need decryption");
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
				+ IGUtility.getHexStringFromBytes(cmdReturn));
		// _unimagHandler.post(startSwipe);
		getEncryptionStatus(commandID, cmdReturn);

	}

	@Override
	public void onReceiveMsgConnected() {
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgConnected");
		// unimagReader.sendCommandEnableTDES();
		_unimagHandler.post(startSwipe);

	}

	@Override
	public void onReceiveMsgDisconnected() {
		IngogoApp.setPrimaryCardReaderAttached(true);
		IngogoApp.setInitialCardCheck(true);
		Log.e("UNIMAG", "UNIMAG " + "onReceiveMsgDisconnected");
		_isUnimagReaderConnected = false;
		_isSwipeInactive = true;

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

	private Runnable doUpdateStatus = new Runnable() {
		public void run() {
			try {
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueTVMsgTv.setText(_unimagMessage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	private Runnable onSwipeSuccess = new Runnable() {
		public void run() {
			try {
				_cardInfo = new CardInfo(_cardDataString);
			} catch (CardInfoParseException e1) {

				e1.printStackTrace();
			} catch (Exception e) {
				// IGUtility.logExceptionInAcra(e);
				Log.e("UNIMAG", "UNIMAG" + " " + e.getMessage());
			}
			_isBadSwipe = false;
			_payOfflineButton.getBackground().setAlpha(ENABLED_ALPHA);
			_payOfflineButton.setEnabled(true);
			if (!_calledProcessPaymentApi) {
				_calledProcessPaymentApi = true;
				_screenStatus = swipeStatus.defaultStatus;

				// ///////////////////////////////

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
					String message = IGPaymentsSwipeActivity.this
							.getText(R.string.payment_due_max_limit_reached_message_section_1)
							+ _maximumFareString
							+ ". "
							+ IGPaymentsSwipeActivity.this
									.getText(R.string.payment_due_max_limit_reached_message_section_2);
					showOKAlertWithMessage(message, true);
					return;
				}
				if (paymentDue > _confirmationValue) {

					if (!_isConfirmAlertShown)
						showPaymentConfirmationAlertWithMessage(
								IGPaymentsSwipeActivity.this
										.getString(R.string.payment_confirmation_msg),
								true);
					else
						onSwipePerformed();
					return;

				}

				if (_ingogoCreditBalance == 0 && getFareEntered() != 0) {
					if (paymentDue < _minimumFare) {
						String _minFareString = (String.valueOf(_decimalFormat
								.format(_minimumFare)));
						String message = IGPaymentsSwipeActivity.this
								.getText(R.string.payment_due_below_min_limit)
								+ _minFareString
								+ ". "
								+ IGPaymentsSwipeActivity.this
										.getText(R.string.payment_due_max_limit_reached_message_section_2);
						showOKAlertWithMessage(message, true);
						onSwipeInit();
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

				_totalPaymentDueTVMsgTv.setText(_unimagMessage);
				if (_confirmBtn.getVisibility() != View.VISIBLE) {
					_intialiseCardReader.setVisibility(View.VISIBLE);
					_totalPaymentDueTVMsgTv.setVisibility(View.VISIBLE);

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
				// Exception newExp = new Exception("Card Reader Ready: "
				// + "Booking Id = " + _job + " ");
				// IGUtility.logExceptionInQLogger(newExp);
				_totalPaymentDueTVMsgTv.setText("Swipe card ...");
				_intialiseCardReader.setVisibility(View.GONE);
				_intialiseCardReader.setEnabled(false);
				_screenStatus = swipeStatus.pleaseSwipe;
				setButtonStatesForCardReaderInitialisation(true);
				// _payOffline.setEnabled(true);
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
				_isSwipeInactive = true;
				_totalPaymentDueTVMsgTv.setText("Bad read, swipe again...");
				_isBadSwipe = true;
				_calledProcessPaymentApi = false;
				_screenStatus = swipeStatus.badswipe;

				if (_isKeyboardShown) {
					showOKAlertWithMessage(
							getResources().getString(
									R.string.keyboard_shown_text), true);
				} else {
					onSwipeInit();
				}

			} else {
				setButtonStatesForCardReaderInitialisation(true);

				_totalPaymentDueTVMsgTv
						.setText((String) IGPaymentsSwipeActivity.this
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

	private void showReaderNotDetectedDialog() {
		Dialog dlg = new AlertDialog.Builder(IGPaymentsSwipeActivity.this)
				.setTitle("")
				.setMessage(
						"Unable to detect ingogo certified card reader. Please attach the ingogo card reader and try again.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						resetUnimagReader();

						_initUnimagInProgress = false;
						_unimagMessage = (String) IGPaymentsSwipeActivity.this
								.getText(R.string.card_reader_not_ready);
						_unimagHandler.post(onDisconnected);
					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.setCancelable(false);
		dlg.show();
	}

	private Runnable onSwipeInit = new Runnable() {
		public void run() {
			onSwipeInit();
		}
	};

	private Runnable initialiseReaderRunnable = new Runnable() {
		public void run() {
			setButtonStatesForCardReaderInitialisation(false);

			if (unimagReader == null)
				unimagReader = new UniMagReader(IGPaymentsSwipeActivity.this,
						IGPaymentsSwipeActivity.this);
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
						IGPaymentsSwipeActivity.this,
						IGPaymentCompletedActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kTripCharge,
						totalPaid);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						"Your payment was successful !!!");
				paymentCompletedIntent.putExtra(IGConstants.kBookingId, _job);
				startActivity(paymentCompletedIntent);

			} else {
				Intent paymentCompletedIntent = new Intent(
						IGPaymentsSwipeActivity.this,
						IGPaymentSucessActivity.class);
				paymentCompletedIntent.putExtra(IGConstants.kPaymentDue,
						totalPaid);
				paymentCompletedIntent.putExtra(IGConstants.kJobId, _job);

				paymentCompletedIntent.putExtra(IGConstants.kPaymentSuccess,
						sucessString);
				startActivity(paymentCompletedIntent);
			}

		} else {

			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			goToJobsActivity();

		}
	}

	@Override
	public void completeOfflineFailed(String errorMessage,
			boolean isHandleDriverStaleState) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_payOfflineButton.setEnabled(true);
		_payOfflineButton.getBackground().setAlpha(ENABLED_ALPHA);
		if (isHandleDriverStaleState) {
			super.onNullResponseRecieved();
			return;
		}
		IGUtility.showDialog("", errorMessage, this);

	}

}