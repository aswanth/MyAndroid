/**
 * 
 */
package com.ingogo.android.activities.payments;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGCoporateAccountModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGPaymentJobApi;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentJobApiListener;

/**
 * @author vineeth
 * 
 */
public class IGPaymentConfirmationActivity extends IGBaseActivity implements
		IGPaymentJobApiListener, IGExceptionApiListener {

	// private JSONArray _cardDeatils;
	private RadioGroup _cardSelectionRG, _accountSelectionRG;
	private TextView _totalFareTV, _baseFeeTV, _serviceFeeTV, _serviceCreditTV,
			_totalDueTV, _creditSurchargeTV, _pinEnterTV, _surchargeHeading;
	private ImageButton _confirmButton;
	private EditText _pinEditText;
	private ProgressDialog _progressDialog;
	private String _jobID;
	private ScrollView _selectionScrollView;
	private String _currentPaymentMethod;
	private int _currentAccountSelectedIndex;
	private double _defaultCreditCardPercentage;

	private int _currentCardIndex = 0, _invalidPinRequestNo = 0;

	private String _totalFare, _baseFee, _serviceFee, _serviceCredit,
			_totalDue;

	private Double _currentCreditCardCharge;
	private HashMap<String, Object> _dataMap;

	private static final String kCreditSurcharge = "creditPercentage";
	private static final String kCardNickName = "cardNickname";
	private static final String kDefaultKey = "isDefault";
	private static final String LEFT_BRACKET = "(";
	private static final String RIGHT_BRACKET = ")";
	private static int MAX_PIN_LENGHT = 4;
	private static final String kCardTag = "Card";
	private static final String kAccountTag = "Account";
	private static final String kPaymentMethodCard = "CARD";
	private static final String kPaymentMethodAccount = "CORP_ACCOUNT";

	private RelativeLayout _selectionLayout, _pinLayout;
	List<JSONObject> _cardDetailsList = new Vector<JSONObject>();
	static private ArrayList<IGCoporateAccountModel> _accountDetailsList;
	private String _bookingType;
	HashMap<String, Object> _jobDetails;
	JSONObject _bookingDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove the meter fare stored in the app so that
		// while navigating back to the calculator screen the
		// meter fare field will be cleared.
		IGUpdatePositionPollingTask.ignoreStaleState = true;
		IngogoApp.getSharedApplication().removeStoredMeterFare();
		setContentView(R.layout.payment_confirmation_layout);
		getBundleExtras();
		initWithViews();
		setUpViews();

		_pinEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (_pinEditText.getText().toString().length() == MAX_PIN_LENGHT
						&& _currentPaymentMethod != null) {
					if (_currentPaymentMethod.equals(kPaymentMethodAccount)) {
						RadioButton button = (RadioButton) _accountSelectionRG
								.findViewById(_currentAccountSelectedIndex);
						if (button.isEnabled())
							_confirmButton.setEnabled(true);
					} else {
						_confirmButton.setEnabled(true);
					}

				} else {
					_confirmButton.setEnabled(false);
				}
			}
		});

		_cardSelectionRG.setOnCheckedChangeListener(cardSelectionListener);
		_accountSelectionRG
				.setOnCheckedChangeListener(accountSelectionListener);

		/*
		 * Setting the pin text field maximum length to 4
		 */
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(MAX_PIN_LENGHT);
		_pinEnterTV.setFilters(FilterArray);

	}

	@Override
	protected void onResume() {
		super.onResume();
		_confirmButton.setClickable(true);
		IGUpdatePositionPollingTask.setIgnoreStaleState(true);
		if (_cardDetailsList == null || _cardDetailsList.size() == 0) {
			_currentPaymentMethod = null;
			_cardSelectionRG.setOnCheckedChangeListener(null);
			_cardSelectionRG.clearCheck();
			_cardSelectionRG.setOnCheckedChangeListener(cardSelectionListener);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		// Disable the logout, jobs and account info activities on login page.
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
					menuEnumerator.ACCOUNT_SETTINGS, IGConstants.ORDER_NONE,
					getString(R.string.account_settings_title));

		}
		if (_bookingType.equals(IGConstants.kBookingTypeHail)) {
			menu.findItem(menuEnumerator.PAYMENT_OPTION).setEnabled(false);
		}

		return true;
	}

	/**
	 * Set total Due
	 */
	private void setTotalDue() {
		Double totalDue = IGUtility.findTotalPaymentDue(
				Double.parseDouble(_totalFare), Double.parseDouble(_baseFee),
				Double.parseDouble(_serviceFee),
				Double.parseDouble(_serviceCredit), _currentCreditCardCharge);
		if (totalDue < 0) {
			totalDue = 0.0;
		}
		_totalDueTV.setText(getFormattedStringForDouble(totalDue));
		_totalDue = getFormattedStringForDouble(totalDue);
	}

	/**
	 * Set the credit card surcharge value for the card index
	 */
	private void setCreditCardChargeForIndex(int cardIndex) {
		Double currentCreditCardPercentage = null;
		if (cardIndex == -1) {
			currentCreditCardPercentage = _defaultCreditCardPercentage;
		} else if (_currentPaymentMethod.equals(kPaymentMethodAccount)) {
			currentCreditCardPercentage = _accountDetailsList.get(cardIndex)
					.getAccountTypePercentage();
		} else {
			try {
				if (_cardDetailsList.size() > 1) {
					currentCreditCardPercentage = _cardDetailsList.get(
							cardIndex).getDouble(kCreditSurcharge);
				} else {

					currentCreditCardPercentage = _cardDetailsList.get(
							cardIndex).getDouble(
							IGApiConstants.kCreditPercentage);
					;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		_currentCreditCardCharge = IGUtility
				.findCardSurcharge(Double.parseDouble(_totalFare),
						Double.parseDouble(_baseFee),
						Double.parseDouble(_serviceFee),
						Double.parseDouble(_serviceCredit),
						currentCreditCardPercentage);

		if (_currentCreditCardCharge < 0.0)
			_currentCreditCardCharge = 0.0;

	}

	/**
	 * To get the bundle extras from intent.
	 */

	private void getBundleExtras() {
		Intent intent = getIntent();
		_bookingType = intent.getExtras().getString(IGConstants.kBookingType);

		_dataMap = (HashMap) intent.getExtras()
				.get(IGConstants.kPaymentDetails);
		_jobDetails = (HashMap) intent.getExtras().get(IGConstants.kJobDetails);

		try {
			_bookingDetails = new JSONObject(
					(String) _jobDetails.get(IGConstants.kDetails));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NullPointerException e) {
			// TODO: handle exception

		}
		_defaultCreditCardPercentage = IngogoApp.getSharedApplication()
				.getCreditPercentage();
		Log.i("EXTRAS", "" + _dataMap.get(IGConstants.kPaymentDue));

		_totalFare = (String) _dataMap.get(IGConstants.kFareEntered);
		_baseFee = (String) _dataMap.get(IGConstants.kBookingFee);
		_serviceFee = (String) _dataMap.get(IGConstants.kBidExtra);

		String jsonArray = (String) _dataMap.get(IGApiConstants.kCardDetails);

		try {
			JSONArray jarray = new JSONArray(jsonArray);
			Log.i("jarray", "" + jarray);
			for (int i = 0; i < jarray.length(); i++) {

				_cardDetailsList.add(jarray.getJSONObject(i));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (_jobDetails != null && _accountDetailsList == null) {
			try {
				JSONArray accountDetailArray = _bookingDetails
						.getJSONArray(ACCOUNT_DETAIL_KEY);
				Gson gson = new Gson();
				_accountDetailsList = new ArrayList<IGCoporateAccountModel>();

				for (int i = 0; i < accountDetailArray.length(); i++) {
					JSONObject object = accountDetailArray.getJSONObject(i);
					IGCoporateAccountModel account = gson.fromJson(
							object.toString(), IGCoporateAccountModel.class);
					_accountDetailsList.add(account);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		setServiceCreditForRegisteredCard();
		_totalDue = (String) _dataMap.get(IGConstants.kPaymentDue);
		_jobID = (String) _dataMap.get(IGConstants.kJobId);

		if (_serviceFee == null) {
			_serviceFee = "0.00";
		}

	}

	/**
	 * To initialize views from the layout
	 */
	private void initWithViews() {
		_cardSelectionRG = (RadioGroup) findViewById(R.id.cardSelectionRG);
		_totalFareTV = (TextView) findViewById(R.id.totalFareTV);
		_baseFeeTV = (TextView) findViewById(R.id.baseFeeTV);
		_serviceFeeTV = (TextView) findViewById(R.id.serviceFeeTV);
		_serviceCreditTV = (TextView) findViewById(R.id.serviceCreditTV);
		_creditSurchargeTV = (TextView) findViewById(R.id.creditSurchargeTV);
		_totalDueTV = (TextView) findViewById(R.id.totalDueTv);
		_confirmButton = (ImageButton) findViewById(R.id.confirmBtn);
		_pinEditText = (EditText) findViewById(R.id.pinET);
		_selectionLayout = (RelativeLayout) findViewById(R.id.selectionLayout);
		_pinEnterTV = (TextView) findViewById(R.id.pinEnterLabel);
		_pinLayout = (RelativeLayout) findViewById(R.id.pinLayout);
		_selectionScrollView = (ScrollView) findViewById(R.id.radioGroupScrollView);
		_accountSelectionRG = (RadioGroup) findViewById(R.id.accountSelectionRG);
		_surchargeHeading = (TextView) findViewById(R.id.creditSurchargeLabel);
	}

	/**
	 * Setup the views with initial values
	 */
	private void setUpViews() {
		_confirmButton.setEnabled(false);
		_totalFareTV.setText(_totalFare);
		_baseFeeTV.setText(_baseFee);
		_serviceFeeTV.setText(_serviceFee);
		if (_serviceCredit != null) {
			_serviceCreditTV.setText("(" + _serviceCredit + ")");
		} else {
			_serviceCreditTV.setText("(" + IGConstants.zeroBalance + ")");
		}
		_totalDueTV.setText(_totalDue);

		if (isCorporateAccountPresent(_bookingDetails)) {
			configureRadioButton();
		} else {
			_currentPaymentMethod = kPaymentMethodCard;
			_accountSelectionRG.setVisibility(View.GONE);
			if (_cardDetailsList != null) {
				configureRadioButton();
				if (Double.parseDouble(_totalDue) == 0
						|| _cardDetailsList.size() == 0) {
					_selectionLayout.setVisibility(View.GONE);
					_selectionScrollView.setVisibility(View.GONE);
					_pinEnterTV.setVisibility(View.GONE);
					_pinEditText.setVisibility(View.GONE);
					_pinLayout.setPadding(0, 70, 0, 0);
					_confirmButton.setEnabled(true);
				} else if (_cardDetailsList.size() == 1) {
					_selectionLayout.setVisibility(View.GONE);
					_selectionScrollView.setVisibility(View.GONE);
					_pinLayout.setPadding(0, 70, 0, 0);
				}

			} else {
				_currentCreditCardCharge = 0.0;
				_selectionLayout.setVisibility(View.GONE);
				_selectionScrollView.setVisibility(View.GONE);
				_pinEnterTV.setVisibility(View.GONE);
				_pinEditText.setVisibility(View.GONE);
				_pinLayout.setPadding(0, 70, 0, 0);

			}

			_creditSurchargeTV
					.setText(getFormattedStringForDouble(_currentCreditCardCharge));
		}

		/*
		 * Set visiblity and padding of the card selection layout and pin edit
		 * text according to card details.
		 */

	}

	/**
	 * convert the string to formatted string
	 */
	private String getFormattedStringForDouble(Double currentValue) {
		DecimalFormat decimalFormat = new DecimalFormat(IGConstants.zeroBalance);
		String formattedString = (String.valueOf(decimalFormat
				.format(currentValue)));
		return formattedString;
	}

	/**
	 * Set up the radio buttons according to the credit card list
	 */
	private void configureRadioButton() {

		addCreditCardsList();
		addCooperateAccountList();

	}

	private void addCooperateAccountList() {
		try {
			if (_accountDetailsList.size() > 0) {

				for (int i = 0; i < _accountDetailsList.size(); i++) {
					String accountName = _accountDetailsList.get(i)
							.getAccountNickname();
					RadioButton radioButton = new RadioButton(this);
					radioButton.setText(accountName);
					radioButton.setId(i);
					radioButton.setTag(kAccountTag);
					boolean isEnabled = _accountDetailsList.get(i).isEnabled();
					int alpha = 0;
					if (isEnabled) {
						alpha = 255;
					} else {
						alpha = 110;
						radioButton.setEnabled(false);
					}
					if (IngogoApp.getThemeID() == 1) {
						radioButton.setTextColor(Color.argb(alpha, 114, 114,
								114));

					} else {
						radioButton.setTextColor(Color.argb(alpha, 255, 255,
								255));

					}
					radioButton.setEnabled(_accountDetailsList.get(i)
							.isEnabled());
					radioButton
							.setButtonDrawable(R.drawable.radio_btn_selector);
					_accountSelectionRG.addView(radioButton);

				}
			}

		} catch (NullPointerException e) {

		}
	}

	private void addCreditCardsList() {
		if (_cardDetailsList.size() > 0) {
			try {

				for (int j = 0; j < _cardDetailsList.size(); j++) {
					String cardNickName = _cardDetailsList.get(j).getString(
							kCardNickName);

					RadioButton radioButton = new RadioButton(this);
					radioButton.setText(cardNickName);
					radioButton.setTag(kCardTag);
					radioButton.setId(j);
					boolean selected = _cardDetailsList.get(j).getBoolean(
							kDefaultKey);
					if (selected)
						_currentCardIndex = j;

					if (IngogoApp.getThemeID() == 1) {
						radioButton.setTextColor(getResources().getColor(R.color.grey_color));

					} else {
						radioButton.setTextColor(Color.rgb(255, 255, 255));

					}
					radioButton
							.setButtonDrawable(R.drawable.radio_btn_selector);
					_cardSelectionRG.addView(radioButton);
				}
				_currentPaymentMethod = kPaymentMethodCard;

				_cardSelectionRG.check(_currentCardIndex);
				setCreditCardChargeForIndex(_currentCardIndex);
				setSurchargeHeading();
				_creditSurchargeTV
						.setText(getFormattedStringForDouble(_currentCreditCardCharge));
				setTotalDue();
				_confirmButton.setEnabled(false);
				setIngogoCredit();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			_currentPaymentMethod = kPaymentMethodCard;
			setCreditCardChargeForIndex(-1);
			setSurchargeHeading();
			_creditSurchargeTV
					.setText(getFormattedStringForDouble(_currentCreditCardCharge));
			setTotalDue();
			RadioButton radioButton = new RadioButton(this);
			radioButton.setText("Swipe Credit/Debit card");
			radioButton.setTag(kCardTag);
			radioButton.setId(10);

			if (IngogoApp.getThemeID() == 1) {
				radioButton.setTextColor(getResources().getColor(R.color.grey_color));

			} else {
				radioButton.setTextColor(Color.rgb(255, 255, 255));

			}
			setIngogoCredit();
			radioButton.setButtonDrawable(R.drawable.radio_btn_selector);
			_cardSelectionRG.addView(radioButton);
		}
	}

	/**
	 * Action for confirm button Call the payment api.
	 */
	public void onConfirmButtonClick(View v) {
		_confirmButton.setClickable(false);
		callPaymentApi();
	}

	/**
	 * Call record payment api
	 */
	private void callPaymentApi() {

		/*
		 * Check if the network is available or not.
		 */
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			String cardToken = "";
			if (_currentPaymentMethod.equals(kPaymentMethodCard)) {
				try {
					cardToken = _cardDetailsList.get(_currentCardIndex)
							.getString(IGApiConstants.kTokenKey);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else {
				try {
					cardToken = _accountDetailsList.get(
							_currentAccountSelectedIndex)
							.getAccountIdentifier();
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			IGPaymentJobApi paymentApi = new IGPaymentJobApi(this, this);
			paymentApi.payment(_jobID, _totalDueTV.getText().toString(),
					_totalFareTV.getText().toString(), cardToken, _pinEditText
							.getText().toString(), _currentPaymentMethod);
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			_confirmButton.setClickable(true);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);

		}
	}

	private OnCheckedChangeListener cardSelectionListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			/*
			 * Change the credit card surcharge value and total, if the selected
			 * card id is different from the current card id
			 */

			if (_cardDetailsList == null || _cardDetailsList.size() == 0) {
				// go to swipe page.
				_accountSelectionRG.setOnCheckedChangeListener(null);
				_accountSelectionRG.clearCheck();
				_accountSelectionRG
						.setOnCheckedChangeListener(accountSelectionListener);
				AlertDialog.Builder adb = new AlertDialog.Builder(
						IGPaymentConfirmationActivity.this);
				adb.setTitle(null);
				adb.setMessage(getResources().getString(
						R.string.SWIPE_CONFIRMATION_MSG));
				_currentPaymentMethod = kPaymentMethodCard;

				setIngogoCredit();

				adb.setNeutralButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								Intent intent = new Intent(
										IGPaymentConfirmationActivity.this,
										IGPaymentsSwipeActivity.class);
								intent.putExtra(IGConstants.kJobDetails,
										_jobDetails);
								String totalFare = _totalFareTV.getText()
										.toString();
								IngogoApp.getSharedApplication().setMeterFare(
										totalFare);
								intent.putExtra(IGConstants.kFareEntered,
										_totalFareTV.getText().toString());
								intent.putExtra(IGConstants.kJobId, _jobID);
								intent.putExtra(
										IGConstants.isBackButtonEnabled, true);
								startActivity(intent);
							}
						});
				adb.setNegativeButton("No",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								_cardSelectionRG
										.setOnCheckedChangeListener(null);
								_cardSelectionRG.clearCheck();
								_currentPaymentMethod = null;
								_pinEditText.setText("");
								_cardSelectionRG
										.setOnCheckedChangeListener(cardSelectionListener);

							}
						});
				AlertDialog ad = adb.create();
				ad.setVolumeControlStream(AudioManager.STREAM_MUSIC);
				ad.show();

			} else {
				_currentPaymentMethod = kPaymentMethodCard;
				_accountSelectionRG.setOnCheckedChangeListener(null);
				_accountSelectionRG.clearCheck();
				_accountSelectionRG
						.setOnCheckedChangeListener(accountSelectionListener);
				setIngogoCredit();
				if (_currentCardIndex != checkedId) {
					_currentCardIndex = checkedId;
					_pinEditText.setText("");
					setCreditCardChargeForIndex(checkedId);
					setSurchargeHeading();
					_creditSurchargeTV
							.setText(getFormattedStringForDouble(_currentCreditCardCharge));
					setTotalDue();

				}
			}

		}
	};

	private OnCheckedChangeListener accountSelectionListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(final RadioGroup group, final int checkedId) {
			_currentCardIndex = -1;
			_currentPaymentMethod = kPaymentMethodAccount;
			_currentAccountSelectedIndex = checkedId;
			_cardSelectionRG.setOnCheckedChangeListener(null);
			_cardSelectionRG.clearCheck();
			_cardSelectionRG.setOnCheckedChangeListener(cardSelectionListener);
			
			try {
				final IGCoporateAccountModel account = _accountDetailsList
						.get(checkedId);
				Double currentBalance = account.getBalance();
				setIngogoCredit();
				setSurchargeHeading();
				_pinEditText.setText("");
				setCreditCardChargeForIndex(_currentAccountSelectedIndex);
				_creditSurchargeTV
						.setText(getFormattedStringForDouble(_currentCreditCardCharge));
				setTotalDue();
				if (currentBalance < Double.parseDouble(_totalDue)) {
					AlertDialog.Builder adb = new AlertDialog.Builder(
							IGPaymentConfirmationActivity.this);
					adb.setTitle(null);
					adb.setMessage(getResources().getString(
							R.string.balance_insufficient_msg));
					adb.setNeutralButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
									RadioButton button = (RadioButton) group
											.findViewById(checkedId);
									button.setEnabled(false);
									if (IngogoApp.getThemeID() == 1) {
										button.setTextColor(Color.argb(110,
												114, 114, 114));

									} else {
										button.setTextColor(Color.argb(110,
												255, 255, 255));

									}
									account.setEnabled(false);
									_accountDetailsList.set(checkedId, account);
									_accountSelectionRG
											.setOnCheckedChangeListener(null);
									button.setChecked(false);
									_accountSelectionRG
											.setOnCheckedChangeListener(accountSelectionListener);
								}
							});
					AlertDialog ad = adb.create();
					ad.setVolumeControlStream(AudioManager.STREAM_MUSIC);
					ad.show();

				} else {
				
				}
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	private void setSurchargeHeading() {
		if (_currentPaymentMethod.equals(kPaymentMethodCard)) {
			_surchargeHeading.setText(getResources().getString(
					R.string.creditsurcharge_label));
		} else {
			_surchargeHeading.setText(getResources().getString(
					R.string.payment_surcharge_heading));

		}

	}

	protected void setIngogoCredit() {
		if (_currentPaymentMethod.equals(kPaymentMethodCard)) {
			setServiceCreditForRegisteredCard();
			if (_serviceCredit != null) {
				_serviceCreditTV.setText("(" + _serviceCredit + ")");
			} else {
				_serviceCreditTV.setText("(" + IGConstants.zeroBalance + ")");
			}
		} else {
			_serviceCredit = "0";
			_serviceCreditTV.setText("(" + IGConstants.zeroBalance + ")");
		}
		setTotalDue();

	}

	public static void clearAccountList() {
		_accountDetailsList = null;
	}

	public void setServiceCreditForRegisteredCard() {
		_serviceCredit = (String) _dataMap.get(IGConstants.kBalance);

		if (_serviceCredit.contains(LEFT_BRACKET)) {
			_serviceCredit = _serviceCredit.replace(LEFT_BRACKET, "");
		}

		if (_serviceCredit.contains(RIGHT_BRACKET)) {
			_serviceCredit = _serviceCredit.replace(RIGHT_BRACKET, "");
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (getIntent().getExtras() != null) {
			boolean isbackButtonDisabled = getIntent().getBooleanExtra(
					IGConstants.disableBackButton, false);
			if (isbackButtonDisabled) {
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	public void paymentJobCompleted(String sucessString) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_confirmButton.setClickable(true);
		// load the success page.
		Intent sucessIntent = new Intent(IGPaymentConfirmationActivity.this,
				IGPaymentSucessActivity.class);
		sucessIntent.putExtra(IGConstants.kPaymentSuccess, sucessString);
		sucessIntent.putExtra(IGConstants.kPaymentDue, _totalDue);
		sucessIntent.putExtra(IGConstants.kJobId, _jobID);
		startActivity(sucessIntent);
	}

	@Override
	public void paymentJobFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_confirmButton.setClickable(true);
		if (errorMessage != null) {

			if (errorMessage.trim().equalsIgnoreCase(
					IGConstants.kPinErrorMessage.trim())) {

				_invalidPinRequestNo++;
				IGUtility.showDialogOk(IGApiConstants.kErrorMsgKey,
						getResources()
								.getString(R.string.invalid_pin_error_msg),
						IGPaymentConfirmationActivity.this);
				_pinEditText.setText("");

			} else if (errorMessage.trim().equalsIgnoreCase(
					IGConstants.kPinMaxRetryMessage.trim())) {
				/*
				 * To check no of invalid pin number retries. if it is more than
				 * the maximum limit, show the payment failure page with account
				 * disabled message. else shows the alert
				 */
				_invalidPinRequestNo++;

				// load the failure page.
				Dialog dlg = new AlertDialog.Builder(
						IGPaymentConfirmationActivity.this)
						.setTitle(IGApiConstants.kErrorMsgKey)
						.setMessage(
								getResources().getString(
										R.string.limit_exceeded_error_msg))
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										loadFailurePageWithError(
												getText(
														R.string.payment_pin_error_string)
														.toString()
														.replaceAll(
																"2",
																""
																		+ _invalidPinRequestNo),
												IGConstants.paymentErrorTypes.kPinError);
									}
								}).create();
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
				dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
				dlg.show();

			} else if (errorMessage.trim().equalsIgnoreCase(
					IGConstants.kUnspecifiedFailureMessage.trim())) {
				loadFailurePageWithError(
						getText(
								R.string.payment_unspecified_failure_error_string)
								.toString(),
						IGConstants.paymentErrorTypes.kUnspecifiedFailure);

			} else if (errorMessage
					.contains(IGConstants.kPassengerNotCreatedConfirmationCode)) {
				IGUtility
						.showDialog(
								"",
								getString(R.string.passenger_not_created_confirmation_code_alert),
								this);

			} else if (errorMessage
					.contains(IGConstants.kPickupRefutedErrorMessage)) {

				loadFailurePageWithError(
						getText(R.string.pickup_refuted_error_message)
								.toString(),
						IGConstants.paymentErrorTypes.kPickupRefuted);
			} else {
				// On all other failures the Payment Failed screen is
				// loaded without showing any alert messages. So the
				// super.onFailedToGetResponse is also not called.
				loadFailurePageWithError(
						getText(
								R.string.payment_unspecified_failure_error_string)
								.toString(),
						IGConstants.paymentErrorTypes.kUnspecifiedFailure);
			}

		}
	}

	/**
	 * Load error page with error
	 */
	private void loadFailurePageWithError(String errorMessage, int errorCode) {
		Intent failureIntent;
		_invalidPinRequestNo = 0;
		if (errorCode == IGConstants.paymentErrorTypes.kPinError) {
			failureIntent = new Intent(IGPaymentConfirmationActivity.this,
					IGPaymentFailureNewActivity.class);
		} else {
			failureIntent = new Intent(IGPaymentConfirmationActivity.this,
					IGPaymentFailureActivity.class);
			failureIntent.putExtra(IGApiConstants.kIsSwipeFailure, false);
			failureIntent.putExtra("isUnknownPassenger", false);

			HashMap<String, Object> paymentDetails = new HashMap<String, Object>();
			paymentDetails.put(IGApiConstants.kCardDetails,
					_cardDetailsList.toString());
			failureIntent.putExtra(IGApiConstants.kCardDetails, paymentDetails);
			failureIntent.putExtra(IGConstants.kBalance, _serviceCredit);
		}
		String cardToken = "";

		if (_currentPaymentMethod.equals(kPaymentMethodCard)) {

			try {

				cardToken = _cardDetailsList.get(_currentCardIndex).getString(

				IGApiConstants.kTokenKey);

			} catch (JSONException e) {

				e.printStackTrace();

			}

		} else {

			try {

				cardToken = _accountDetailsList.get(

				_currentAccountSelectedIndex).getAccountIdentifier();

			} catch (NullPointerException e) {

				// TODO Auto-generated catch block

				e.printStackTrace();

			}

		}
		Double creditSurcharge = Double.parseDouble(_creditSurchargeTV
				.getText().toString());
		Double totalFare = Double
				.parseDouble(_totalFareTV.getText().toString());
		Double amtWithoutSurcharge = totalFare - creditSurcharge;

		DecimalFormat decimalFormat = new DecimalFormat(IGConstants.zeroBalance);

		HashMap<String, String> dataMap = new HashMap<String, String>();
		dataMap.put(IGConstants.kJobId, _jobID);
		dataMap.put(IGConstants.kPaymentDue,
				(String.valueOf(decimalFormat.format(amtWithoutSurcharge))));
		dataMap.put(IGConstants.kFareEntered, _totalFareTV.getText().toString());
		dataMap.put(IGConstants.kCardNumber, cardToken);
		dataMap.put(IGConstants.kPinNumber, _pinEditText.getText().toString());
		_dataMap.put(IGConstants.kCardNumber, cardToken);
		_dataMap.put(IGConstants.kPinNumber, _pinEditText.getText().toString());
		failureIntent.putExtra(IGConstants.kPaymentDetails, dataMap);
		failureIntent.putExtra(IGConstants.kErrorCode, errorCode);
		failureIntent.putExtra(IGConstants.kErrorMessage, errorMessage);
		failureIntent.putExtra(IGConstants.kCreditCardCount, getCardCount());
		failureIntent.putExtra(IGConstants.kPaymentMethod,
				_currentPaymentMethod);
		Double baseFee = Double.parseDouble(_baseFeeTV.getText().toString());
		Double serviceFee = Double.parseDouble(_serviceFeeTV.getText()
				.toString());

		failureIntent.putExtra(IGConstants.kbaseFee, _baseFeeTV.getText()
				.toString());
		failureIntent.putExtra(IGConstants.kServiceFee, _serviceFeeTV.getText()
				.toString());

		failureIntent.putExtra(
				IGConstants.kTripCharge,
				String.valueOf(decimalFormat.format(totalFare + baseFee
						+ serviceFee)));

		failureIntent.putExtra(IGConstants.kTotalDueAmount, _totalDueTV
				.getText().toString());
		failureIntent.putExtra(IGConstants.kBookingType, _bookingType);
		if (_bookingType.equals(IGConstants.kBookingTypeHail)) {
			String passengerId = (String) getIntent().getExtras().get(
					IGConstants.kPassengerID);
			failureIntent.putExtra(IGConstants.kPassengerID, passengerId);
			failureIntent.putExtra(IGConstants.kPaymentDetails, _dataMap);

		} else {
			HashMap<String, Object> _jobDetails = (HashMap) getIntent()
					.getExtras().get(IGConstants.kJobDetails);
			failureIntent.putExtra(IGConstants.kJobDetails, _jobDetails);
			failureIntent.putExtra(IGConstants.kDetails, _dataMap);

		}
		startActivity(failureIntent);
		_pinEditText.setText("");
	}

	private int getCardCount() {
		int count = _cardDetailsList.size();
		try {
			count = _cardDetailsList.size() + _accountDetailsList.size();
		} catch (NullPointerException e) {

		}
		return count;
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		loadFailurePageWithError(
				getText(R.string.payment_unspecified_failure_error_string)
						.toString(),
				IGConstants.paymentErrorTypes.kUnspecifiedFailure);
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		loadFailurePageWithError(
				getText(R.string.payment_unspecified_failure_error_string)
						.toString(),
				IGConstants.paymentErrorTypes.kUnspecifiedFailure);
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		loadFailurePageWithError(
				getText(R.string.payment_unspecified_failure_error_string)
						.toString(),
				IGConstants.paymentErrorTypes.kUnspecifiedFailure);
	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		loadFailurePageWithError(
				getText(R.string.payment_unspecified_failure_error_string)
						.toString(),
				IGConstants.paymentErrorTypes.kUnspecifiedFailure);
	}
}
