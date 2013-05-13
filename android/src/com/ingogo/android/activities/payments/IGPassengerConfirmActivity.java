package com.ingogo.android.activities.payments;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGPaymentBaseActivity;
import com.ingogo.android.model.IGPassengerInformationModel;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCreateBookingForPaymentApi;
import com.ingogo.android.webservices.IGFindAccountApi;
import com.ingogo.android.webservices.beans.response.IGCreateBookingForPaymentResponseBean;
import com.ingogo.android.webservices.interfaces.IGCreateBookingForPaymentApiListener;
import com.ingogo.android.webservices.interfaces.IGFindAccountApiListener;

public class IGPassengerConfirmActivity extends IGPaymentBaseActivity implements
		IGCreateBookingForPaymentApiListener, IGFindAccountApiListener {

	private TextView _nameTextView;
	private EditText _mobileNumber;
	private static String allowableStringMobile = "+";
	private ImageButton _findButton;
	private String _mobileNumberString;
	private IGPassengerInformationModel _passengerInfo;

	private static final int DISABLED_ALPHA = 110;
	private static final int ENABLED_ALPHA = 255;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passenger_confirm_layout);
		initViews();
		getBundleExtras();
		setUpViews();
		setMobileTextFilter();

	}

	/*
	 * Get values passed from intent.
	 */
	private void getBundleExtras() {
		// TODO Auto-generated method stub
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			_passengerInfo = (IGPassengerInformationModel) extras
					.getSerializable("passengerInfo");
			_mobileNumberString = extras.getString("mobileNumber");
			modifyPassengerInfo();
		}
	}

	private void initViews() {
		// TODO Auto-generated method stub
		_nameTextView = (TextView) findViewById(R.id.nameTV);
		_mobileNumber = (EditText) findViewById(R.id.numberET);
		_findButton = (ImageButton) findViewById(R.id.findButton);
		_findButton.setEnabled(false);
		_findButton.setAlpha(DISABLED_ALPHA);

	}

	/*
	 * Set up the views with default values.
	 */
	private void setUpViews() {
		// TODO Auto-generated method stub
		/**
		 * Any text change in MobileNumberField is caught by this listener
		 */
		_mobileNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// continueButtonState();

				String currentString = s.toString();
				try {
					if (currentString.substring(0, 1).equalsIgnoreCase("+")
							|| Character.isDigit(s.charAt(0))) {
						if (currentString.matches("^[0-9]*$")) {

							Pattern p = Pattern.compile("^\\+(?:[0-9] ?){12}$");
							Matcher m = p.matcher(currentString);
							boolean numberFound = m.matches();

							if (numberFound) {
							} else {

							}

						} else {
							InputFilter[] mobileFilter = new InputFilter[2];
							mobileFilter[0] = filterMobileNoSpecial;
							mobileFilter[1] = new InputFilter.LengthFilter(12);
							_mobileNumber.setFilters(mobileFilter);
						}
						InputFilter[] mobileFilter = new InputFilter[2];
						mobileFilter[0] = filterMobileNoSpecial;
						mobileFilter[1] = new InputFilter.LengthFilter(12);
						_mobileNumber.setFilters(mobileFilter);
					}
				} catch (StringIndexOutOfBoundsException e) {
					setMobileTextFilter();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				setMobileTextFilter();
			}

			@Override
			public void afterTextChanged(Editable s) {
				setButtonState();
			}
		});
	}

	private void setButtonState() {
		String _mobileNoString = _mobileNumber.getText().toString().trim();

		if (_mobileNoString.equals("") || _mobileNoString.length() < 10) {

			_findButton.setEnabled(false);
			_findButton.setAlpha(DISABLED_ALPHA);
		} else if (_mobileNoString.contains("+")
				&& (_mobileNoString.length() != 12)) {

			_findButton.setEnabled(false);
			_findButton.setAlpha(DISABLED_ALPHA);

		} else if (!_mobileNoString.contains("+")
				&& _mobileNoString.length() > 10) {

			_findButton.setEnabled(false);
			_findButton.setAlpha(DISABLED_ALPHA);

		} else {
			if (_mobileNoString.length() == 10
					|| _mobileNoString.length() == 12) {
				Pattern p = null;
				if (_mobileNoString.length() == 10) {
					p = Pattern.compile("[0-9]*");
				} else {
					p = Pattern.compile("[+][0-9]*");
				}
				Matcher m = p.matcher(_mobileNoString);
				if (m.matches() == false) {
					_findButton.setEnabled(false);
					_findButton.setAlpha(DISABLED_ALPHA);

				} else {
					_findButton.setEnabled(true);
					_findButton.setAlpha(ENABLED_ALPHA);

				}
			}
		}
	}

	private void setMobileTextFilter() {
		InputFilter[] mobileFilter = new InputFilter[2];
		mobileFilter[0] = filterMobile;
		mobileFilter[1] = new InputFilter.LengthFilter(12);
		_mobileNumber.setFilters(mobileFilter);
	}

	/**
	 * Filter to allow only mobile number without special character
	 */
	private static InputFilter filterMobileNoSpecial = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			for (int i = start; i < end;) {

				if (source.toString().length() <= 1
						|| source.toString().matches("^[a-zA-Z0-9]*$")) {
					if (Character.isDigit(source.charAt(i))) {
						return source;
					} else {
						if (dstart == 0
								&& new Character(source.charAt(i)).toString()
										.equalsIgnoreCase("+")
								&& !dest.toString().contains("+")) {
							return source;
						}
						return "";
					}
				} else {
					return "";
				}
			}
			return null;
		}
	};

	/**
	 * Filter to allow only mobile number with plus sign
	 */
	private InputFilter filterMobile = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {

			for (int i = start; i < end;) {

				if (source.toString().length() <= 1) {
					if (Character.isDigit(source.charAt(i))
							|| allowableStringMobile.contains(source)) {
						return source;
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
			return null;
		}
	};
	private ProgressDialog _progressDialog;

	/*
	 * Action for confirm button
	 */
	public void onConfirmButtonClick(View v) {
		// call bookings/createBookingForTakingPayment WS
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCreateBookingForPaymentApi api = new IGCreateBookingForPaymentApi(
					this, this);
			api.createBookingForPassenger(_passengerInfo.getPassengerId());

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/*
	 * 
	 */
	public void onFindClick(View v) {
		// call passenger/find webservice.
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGFindAccountApi igFindAccountApi = new IGFindAccountApi(
					IGPassengerConfirmActivity.this,
					IGPassengerConfirmActivity.this);
			igFindAccountApi.getFindAccountStatus(_mobileNumber.getText()
					.toString());

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	@Override
	public void createBookingForPaymentCompleted(IGCreateBookingForPaymentResponseBean bookingSummary) {
		// TODO Auto-generated method stub
//		IGUtility.dismissProgressDialog(_progressDialog);
//
//		if (bookingSummary.isHasRegisteredCard()) {
//			Intent paymentActivity = new Intent(
//					IGPassengerConfirmActivity.this, IGPaymentActivity.class);
//			paymentActivity.putExtra(IGConstants.kJob, bookingSummary.getBookingId());
//			paymentActivity.putExtra("disableTakePaymentOption", true);
//
//			startActivity(paymentActivity);
//		} else {
//			Intent swipeCalculator = new Intent(
//					IGPassengerConfirmActivity.this,
//					IGSwipeCalculatorActivity.class);
//			swipeCalculator.putExtra("jobid", bookingSummary.getBookingId());
//			swipeCalculator.putExtra("passengerid", _passengerInfo.getPassengerId());
//			startActivity(swipeCalculator);
//		}

	}

	@Override
	public void createBookingForPaymentFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialogOk("", errorMessage, this);
	}

	@Override
	public void findAccountCompleted(IGPassengerInformationModel passengerInfo) {
		// TODO Auto-generated method stub
		_passengerInfo = passengerInfo;
		_mobileNumberString = _mobileNumber.getText().toString();
		modifyPassengerInfo();
		IGUtility.dismissProgressDialog(_progressDialog);
	}

	private void modifyPassengerInfo() {

		// TODO Auto-generated method stub
		// _mobileNumberString.
		String passengerInfo = _passengerInfo.getInitial() + " "
				+ _passengerInfo.getSurname() + ", "
				+ formatString(_mobileNumberString);
		_nameTextView.setText(passengerInfo);
	}

	private String formatString(String string) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (char c : string.toCharArray()) {
			i++;
			sb.append(c);
			if (i % 3 == 1 && i > 1) {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	@Override
	public void findAccountFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		_mobileNumber.setText("");
		Dialog dlg = new AlertDialog.Builder(this).setTitle("")
				.setMessage(errorMessage)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();

					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dlg.show();

	}

}
