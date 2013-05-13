package com.ingogo.android.activities;

import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.ingogo.android.R;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGTimeToPickUpApi;
import com.ingogo.android.webservices.beans.response.IGTimeToPickUpResponseBean;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGTimeToPickUpApiListener;

public class IGTimeToPickUpActivity extends IGBaseActivity implements IGTimeToPickUpApiListener, IGExceptionApiListener{

	private ProgressDialog _progProgressDialog;
	private EditText _timeToPickUpEditText;
	private IGJob _job;
	private Button _comingButton;
	private Button _droppingButton;

	/**
	 * @param savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.time_to_pickup);
		initViews();
		Intent intent = getIntent();
		_job = (IGJob) intent.getSerializableExtra("Job");
	}

	protected void onPause() {
		IGUtility.dismissProgressDialog(_progProgressDialog);
		_progProgressDialog = null;
		super.onPause();
	}

	protected void onResume() {
		//To show the soft keyboard automatically.
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(_timeToPickUpEditText,
						InputMethodManager.SHOW_FORCED);
			}
		}, 500);

		super.onResume();
	}

	/**
	 * Initialise the view widgets
	 */
	private void initViews() {
		_timeToPickUpEditText = (EditText) findViewById(R.id.timeToPickUpEditText);
		_comingButton = (Button)findViewById(R.id.coming_button);
		_droppingButton = (Button)findViewById(R.id.dropping_button);
		//To limit the numbers to 60
		InputFilter filter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				int iTimeToPickUp = 0;
				try {
					iTimeToPickUp = Integer.parseInt(dest + source.toString());
				} catch (NumberFormatException e) {
					iTimeToPickUp = 0;
				}

				if (iTimeToPickUp == 0 || iTimeToPickUp > 60) {
					return "";
				}
				return null;

			}
		};
		//To limit the number of digits to two.
		InputFilter[] FilterArray = new InputFilter[2];
		FilterArray[0] = filter;
		FilterArray[1] = new InputFilter.LengthFilter(2);
		_timeToPickUpEditText.setFilters(FilterArray);
		//Any text changes in timeToPickUpEdittext is listened by this listener
		_timeToPickUpEditText.addTextChangedListener(new TextWatcher() {

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
				comingAndDroppingButtonState();
			}
		});
		

	}

	//To disable the back button functionality
	@Override
	public void onBackPressed() {
		return;
	}

	/**
	 * Any button click on coming button is got in this method.
	 * @param view
	 */
	public void onComingButtonClick(View view) {
		Log.i("Coming button tapped", "Coming button tapped");
		if (IGUtility.isNetworkAvailable(this)) {
			//To avoid multiple click on coming button.
			_comingButton.setEnabled(false);
			invokeTimeToPickUpApi(IGApiConstants.kDirectText);
		} else {
			//If there is no network in the device,prompt an alert.
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}
	
	/**
	 * Any button click on dropping button is got in this method.
	 * @param view
	 */
	public void onDroppingButtonClick(View view) {
		Log.i("Dropping button tapped", "Dropping button tapped");
		if (IGUtility.isNetworkAvailable(this)) {
			//To avoid multiple click on dropping button.
			_droppingButton.setEnabled(false);
			invokeTimeToPickUpApi(IGApiConstants.kDropOffOnRouteText);
		} else {
			//If there is no network in the device,prompt an alert.
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}
	
	/**
	 * To invoke time to pickup web service
	 * @param messageType
	 */
	private void invokeTimeToPickUpApi(String messageType) {
		_progProgressDialog = IGUtility.showProgressDialog(this);
		IGTimeToPickUpApi igTimeToPickUp = new IGTimeToPickUpApi(this,this);
		igTimeToPickUp.getTimeToPickUp(_job.getId(), _timeToPickUpEditText.getText()
				.toString(), messageType);
		
	}

	/**
	 * Validation in the time to pick up editText.
	 * @return
	 */
	private boolean validateTimeToPickUpField() {
		if (_timeToPickUpEditText.length() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If the validation return true,then enable the buttons.Otherwise disable them.
	 */
	private void comingAndDroppingButtonState() {
		if (validateTimeToPickUpField()) {
			_comingButton.setEnabled(true);
			_droppingButton.setEnabled(true);
			
		} else {
			_comingButton.setEnabled(false);
			_droppingButton.setEnabled(false);
		}
	}
	
	/**
	 * To navigate to job details page ,when the response of the time to pick up web service is success.
	 */
	private void goToJobDetailsPage() {
		Intent intent = new Intent(this, IGJobDetailsActivity.class);
		intent.putExtra("Job", _job);
		intent.putExtra("isCollectButtonEnabled", false);
		startActivity(intent);
		finish();
	}
	
	/**
	 * Override onPrepareOptionsMenu.Otherwise,show the menus in the base activity.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		return false;
	}

	@Override
	public void timeToPickUpApiCompleted(IGTimeToPickUpResponseBean responseObj) {
		Log.i("Time to pick up response", "" + responseObj);
		IGUtility.dismissProgressDialog(_progProgressDialog);
		_comingButton.setEnabled(true);
		_droppingButton.setEnabled(true);
		
			goToJobDetailsPage();
		
		
		
	}

	@Override
	public void timeToPickUpApiFailed(String message) {
		IGUtility.dismissProgressDialog(_progProgressDialog);
		_comingButton.setEnabled(true);
		_droppingButton.setEnabled(true);
		Log.i("Time to pick up Error response", "" + message);
		
	}
}