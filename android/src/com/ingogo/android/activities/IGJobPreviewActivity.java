/*
 * Package Name : com.ingogo.android.activities
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : This activity displays details of job. The job can be accepted or rejected by the driver
 */

package com.ingogo.android.activities;

import java.text.DecimalFormat;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGAddress;
import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGAcceptJobApi;
import com.ingogo.android.webservices.IGJobDetailsApi;
import com.ingogo.android.webservices.beans.response.IGAcceptJobResponseBean;
import com.ingogo.android.webservices.interfaces.IGAcceptJobListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGJobDetailsListener;

public class IGJobPreviewActivity extends IGBaseActivity implements
		IGJobDetailsListener, IGExceptionApiListener, IGAcceptJobListener {

	private TextView _passengerName;
	private TextView _pickUp;
	private TextView _dropOff;
	private TextView _pays;
	private IGJob _job;
	private ProgressDialog _progressDialog;
	private RelativeLayout _payLayout, _pickUpByLayout;
	private TextView _pickUpBy;
	private Button _acceptButton, _ignoreButton;
	private boolean responsePendingInJobPreview;

	private static interface erroMsgType {
		int JOB_IN_PROGRESS = 1;
		int JOB_UNAVAILABLE = 2;
		int JOB_CANCELLED = 3;
	}

	private boolean isResponsePendingInJobPreview() {
		return responsePendingInJobPreview;
	}

	private void setResponsePendingInJobPreview(
			boolean responsePendingInJobPreview) {
		this.responsePendingInJobPreview = responsePendingInJobPreview;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.job_preview);
		initViews();
		// Get the job's details from jobs activity.
		Intent intent = getIntent();
		_job = (IGJob) intent.getSerializableExtra("Job");
		Log.d("JOB ID", _job.getId());
		setUpViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// When the app enters back ground and a the progress dialogue is shown
		// then a flag is set true in IngogoApp. So if that
		// flag is true then show the progress dialog on onResume.
		if (isResponsePendingInJobPreview() == true) {
			setResponsePendingInJobPreview(false);
			_progressDialog = IGUtility.showProgressDialog(this);
		} else {
			invokeJobDetailsApi();
		}
	}

	private void invokeJobDetailsApi() {

		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGJobDetailsApi jobDetailsApi = new IGJobDetailsApi(this, this);
			jobDetailsApi.getJobDetails(IngogoApp.getSharedApplication()
					.getUserId(), IngogoApp.getSharedApplication()
					.getPassword(), _job.getId());

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * Initialise and set up the views
	 */
	private void setUpViews() {
		if (_job != null) {
			_passengerName.setText(_job.getPassengerName());
			_pickUp.setText(_job.getshortAddress());
			_dropOff.setText(_job.getDropOffTo());
			_pickUpBy.setText(IGUtility.timeStampToTime(_job.getTimeWithin(),
					_job.getTimeStamp()));
			if (_job.getExtraOffer() != 0) {
				// Format the extra offer to avoid decimal point.
				String extraOfferString = new DecimalFormat("#.#").format(
						_job.getExtraOffer()).toString();
				_pays.setText(extraOfferString + " points");
				_payLayout.setVisibility(View.VISIBLE);
				// _pickUpByLayout.setVisibility(View.VISIBLE);
			} else {
				_payLayout.setVisibility(View.GONE);
				// _pickUpByLayout.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Initialise the views
	 */

	private void initViews() {
		_passengerName = (TextView) findViewById(R.id.passengerNameTv);
		_pickUp = (TextView) findViewById(R.id.addressTv);
		_dropOff = (TextView) findViewById(R.id.cityNameTv);
		_pays = (TextView) findViewById(R.id.paysTv);
		_payLayout = (RelativeLayout) findViewById(R.id.payLayout);
		_pickUpByLayout = (RelativeLayout) findViewById(R.id.pickupByLayout);
		_pickUpBy = (TextView) findViewById(R.id.pickupBy);
		_acceptButton = (Button) findViewById(R.id.acceptJobButton);
		_ignoreButton = (Button) findViewById(R.id.ignoreJobButton);
	}

	/**
	 * When the user tap on the accept button, an API call for accept job is
	 * invoked
	 * 
	 * @param view
	 */

	public void onAcceptJobButtonClick(View view) {
		
		if (IGUtility.isNetworkAvailable(this)) {
			_acceptButton.setEnabled(false);
			if (_job != null) {
				_progressDialog = IGUtility.showProgressDialog(this);
				Log.i("time within, extra offer", "" + _job.getTimeWithin()
						+ "" + (int) _job.getExtraOffer());
				IGAcceptJobApi api = new IGAcceptJobApi(this, this);
				api.acceptBooking(_job.getId(),(int)_job.getExtraOffer(),_job.getTimeWithin());
			//	IGAcceptJobApi api = new IGAcceptJobApi(this, _job.getId(),
			//			_job.getTimeWithin(), (int) _job.getExtraOffer());
			//	api.accept();
			}

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * When the user tap on the ignore button, the selected job is ignored
	 * 
	 * @param view
	 */

	public void onIgnoreJobButtonClick(View view) {
		_ignoreButton.setEnabled(false);

		// When ignore button tapped,an alert is created.On pressing the 'OK'
		// button finishes the activity.
		// On pressing the 'CANCEL' button cancels the dialogue.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.job_ignore_confirm_prompt))
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (_job != null) {
									_job.setStatus(IGConstants.kJobIgnored);
									setResult(IGConstants.kJobIgnored,
											getIntent());
									finish();
								}
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						_ignoreButton.setEnabled(true);
						dialog.cancel();
					}
				});

		AlertDialog alert = builder.create();
		alert.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		alert.show();

		Log.e("Ignore Job", "Ignore Job button tapped");

	}

	/**
	 * When a job is accepted, collected or open, set the result and finish the
	 * activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == IGConstants.kJobAccepted
				|| requestCode == IGConstants.kJobCollected
				|| requestCode == IGConstants.kJobOpen) {
			setResult(resultCode, getIntent());
			finish();
		}
	}

	/**
	 * Process the response that got by calling the API for accept job
	 * 
	 * @param loginObj
	 */

	private void processAcceptJobResponse() {
		if (_job != null) {
			_job.setStatus(IGConstants.kJobAccepted);
			setResult(IGConstants.kJobAccepted, getIntent());
			// If accept web service response is success, then navigate to job
			// details activity.
			// Intent intent = new Intent(this, IGJobDetailsActivity.class);
			// intent.putExtra("Job", _job);
			// startActivityForResult(intent, IGConstants.kJobAccepted);

			// Go to Time to pick up page
			Intent intent = new Intent(this, IGTimeToPickUpActivity.class);
			intent.putExtra("Job", _job);
			startActivity(intent);
			finish();
		}
	}




	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		if (_progressDialog != null) {

			// When the app enters back ground and a the progress dialogue is
			// shown then a flag is set true in IngogoApp. So if that
			// flag is true then show the progress dialog on onResume.
			if (_progressDialog.isShowing())
				setResponsePendingInJobPreview(true);
			_progressDialog.dismiss();
			_progressDialog = null;

		}
		super.onPause();
	}

	private void showAlert(String errorMessageString, final int errorType) {

		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(errorMessageString);
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
		alertbox.setNeutralButton(IGApiConstants.kStatusOK,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (errorType == erroMsgType.JOB_UNAVAILABLE) {
							IGJobPreviewActivity.this.finish();
						}
						if (errorType == erroMsgType.JOB_IN_PROGRESS) {
							String userId = IngogoApp.getSharedApplication()
									.getUserId();
							String password = IngogoApp.getSharedApplication()
									.getPassword();
							String plateNumber = IGUtility.getDefaults(
									IGConstants.kPlateNumber,
									IGJobPreviewActivity.this);
							invokeCurrentDriverStateAPI(userId, password,
									plateNumber);
						}
						if(errorType == erroMsgType.JOB_CANCELLED) {
							setResult(IGConstants.kJobCancelled,getIntent());
							IGJobPreviewActivity.this.finish();
						}
					}
				});
		AlertDialog alert = alertbox.create();
		alert.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		alert.show();
	}

	@Override
	public void jobDetailsFetchingCompleted(IGBookingModel bookingModel) {
		setResponsePendingInJobPreview(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		if (bookingModel != null) {
			processJobDetails(bookingModel);
			setUpViews();
		}
	}

	@Override
	public void jobDetailsFetchingFailed(String errorMessage) {
		setResponsePendingInJobPreview(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialogOk("", errorMessage, this);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		setResponsePendingInJobPreview(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNetWorkUnavailableResponse(errorResponse);
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		setResponsePendingInJobPreview(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onRequestTimedoutResponse(errorResponse);
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		setResponsePendingInJobPreview(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onInternalServerErrorResponse(errorResponse);

	}

	@Override
	public void onNullResponseRecieved() {
		setResponsePendingInJobPreview(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNullResponseRecieved();

	}

	private String getFullAddress(IGAddress igAddress) {
		String address = "";
		String buildingName = igAddress.getBuildingName();
		if (buildingName == null || buildingName.equalsIgnoreCase("null")) {
			buildingName = "";
		} else {
			address += "" + buildingName + ", ";
		}

		String unitNumber = igAddress.getUnitNumber();
		if (unitNumber == null || unitNumber.equalsIgnoreCase("null")) {
			unitNumber = "";
		} else {
			if (buildingName.equals("")) {
				address += "" + unitNumber;

			} else {
				address += "" + unitNumber;
			}
		}
		String streetNumber = igAddress.getStreetNumber();
		if (streetNumber == null || streetNumber.equalsIgnoreCase("null")) {
			streetNumber = "";
		} else {
			if (buildingName.equals("") && unitNumber.equals("")) {
				address += "" + streetNumber;
			} else if ((!buildingName.equals("")) && unitNumber.equals("")) {
				address += "" + streetNumber;
			} else {
				address += "/" + streetNumber;
			}
		}
		String addressLine1 = igAddress.getAddressLine1();
		if (addressLine1 == null || addressLine1.equalsIgnoreCase("null")) {
			addressLine1 = "";
		} else if (buildingName.equals("") && unitNumber.equals("")
				&& streetNumber.equals("")) {
			address += "" + addressLine1;
		} else {
			address += " " + addressLine1;
		}

		String addressLine2 = igAddress.getAddressLine2();
		if (addressLine2 == null || addressLine2.equalsIgnoreCase("null")) {
			addressLine2 = "";
		} else {
			if (addressLine1.equals("")) {
				address += " " + addressLine2;
			} else if (buildingName.equals("") && unitNumber.equals("")
					&& streetNumber.equals("") && addressLine1.equals("")) {
				address += "" + addressLine2;
			} else {
				address += ", " + addressLine2;
			}
		}
		String addressLine3 = igAddress.getAddressLine3();
		if (addressLine3 == null || addressLine3.equalsIgnoreCase("null")) {
			addressLine3 = "";
		}
		if (addressLine2.equals("")) {
			address += " " + addressLine3;
		} else if (buildingName.equals("") && unitNumber.equals("")
				&& streetNumber.equals("") && addressLine1.equals("")
				&& addressLine2.equals("")) {
			address += "" + addressLine3;
		} else {
			address += ", " + addressLine3;
		}

		String suburb = igAddress.getSuburb();
		if (suburb == null || suburb.equalsIgnoreCase("null")) {
			suburb = "";
		} else {
			if (addressLine1.equals("") && addressLine2.equals("")
					&& addressLine3.equalsIgnoreCase("")
					&& unitNumber.equals("") && streetNumber.equals("")) {
				address += "" + suburb;
			} else if (addressLine1.equals("") || addressLine2.equals("")
					|| addressLine3.equals("") || unitNumber.equals("")
					|| streetNumber.equals("")) {
				address += ", " + suburb;
			} else {
				address += ", " + suburb;
			}
		}

		return address;
	}

	private String getShortAddress(IGAddress igAddress) {
		String address = "";
		int streetNumberLength = 0;
		String reverseMaskedString = "";
		String maskedStreetNumber = "";
		String streetNumber = igAddress.getStreetNumber();
		int i = 0;
		if (streetNumber != null && !streetNumber.equalsIgnoreCase("null")) {
			streetNumberLength = streetNumber.length();
			i = streetNumberLength - 1;
		} else {
			streetNumberLength = 0;
		}
		if (streetNumberLength != 0) {
			if (streetNumberLength == 1) {
				if (Character.isDigit(streetNumber.charAt(0))) {
					maskedStreetNumber = "*";
				} else {
					maskedStreetNumber += streetNumber.charAt(0);
				}
			} else {
				while (i >= 1) {

					if (Character.isDigit(streetNumber.charAt(i))) {
						try {
							if (Character.isDigit(streetNumber.charAt(i - 1))) {
								reverseMaskedString += "*";
							} else if (Character.isDigit(streetNumber
									.charAt(i + 1))) {
								reverseMaskedString += streetNumber.charAt(i);
							} else {
								reverseMaskedString += "*";
							}
						} catch (StringIndexOutOfBoundsException e) {
							reverseMaskedString += "*";

						}

					} else {
						reverseMaskedString += streetNumber.charAt(i);
					}
					i--;
				}
				reverseMaskedString += streetNumber.charAt(0);
				Log.i("reverse masked number", "" + reverseMaskedString);
				maskedStreetNumber = new StringBuffer(reverseMaskedString)
						.reverse().toString();
			}
		}
		if (streetNumber == null || streetNumber.equalsIgnoreCase("null")) {
			maskedStreetNumber = "";
		} else {
			address += maskedStreetNumber;
		}
		String addressLine1 = igAddress.getAddressLine1();
		if (addressLine1 == null || addressLine1.equalsIgnoreCase("null")) {
			addressLine1 = "";
		} else {
			if (null != streetNumber && streetNumber.equalsIgnoreCase("")) {
				address += addressLine1;
			} else {
				address += " " + addressLine1;
			}
		}
		String suburb = igAddress.getSuburb();
		if (suburb == null || suburb.equalsIgnoreCase("null")) {
			suburb = "";
		} else {
			if (addressLine1.equals("")) {
				address += "" + suburb;
			} else {
				address += ", " + suburb;
			}
		}
		return address;
	}

	private void processJobDetails(IGBookingModel model) {
		_job.setId(model.getBookingId());
		_job.setPassengerName(model.getPassengerName());
		_job.setTimeStamp(Long.parseLong(model.getBooked()));
		_job.setPickupFrom(getFullAddress(model.getPickupFrom()));
		_job.setshortAddress(getShortAddress(model.getPickupFrom()));
		_job.setDropOffTo(model.getDropOffAt().getSuburb().toUpperCase());
		try {
			_job.setExtraOffer(Float.parseFloat(model.getBidExtra()));
		} catch (NumberFormatException e) {
			_job.setExtraOffer(0.0f);
		}
		try {
			_job.setTimeWithin(Integer.parseInt(model.getBidInterval()));
		} catch (NumberFormatException e) {
			_job.setTimeWithin(0);
		}
		_job.setStatus(IGConstants.kJobOpen);
	}

	

	
	@Override
	public void acceptJobCompleted(IGAcceptJobResponseBean responseObj) {
		// TODO Auto-generated method stub
	
			_acceptButton.setEnabled(true);
	
		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInJobPreview(false);
		Log.e("IGAcceptJobApi", "" + responseObj);
		if (responseObj != null) {
		//	JSONObject acceptDetailsObj = (JSONObject) response
			//		.get(IGConstants.kDataKey);
//			if (acceptDetailsObj != null) {
				boolean hasChanged = false;
				IGBookingModel booking = null;
				String bookingStatus = null;
				hasChanged = responseObj.isHasChanged();
				booking = responseObj.getBooking();
				Log.i("booking", "" + booking.toString());
				bookingStatus = responseObj.getBooking().getBookingStatus();
				if (hasChanged) {
					processJobDetails(booking);
					setUpViews();
					IGUtility.showDialogOk("",
							this.getString(R.string.job_details_updated_alert),
							this);
				} else {
					Log.i("status", "" + bookingStatus);
					if (bookingStatus != null
							&& bookingStatus
									.equalsIgnoreCase(IGApiConstants.kAcceptKey)) {
						// set jobInProgress to true.
						IGUtility.setDefaults(IGConstants.kJobInProgress,
								IGConstants.kTrue, this);
						this.processAcceptJobResponse();
					}
				}
			
		}
		
		
	}

	@Override
	public void acceptJobFailed(String message) {
		
			_acceptButton.setEnabled(true);
		
		IGUtility.dismissProgressDialog(_progressDialog);
		// Used to avoid multiple alert when job is unavailable.
		setResponsePendingInJobPreview(false);
//		String[] errorMessages = parseErrorMessages(errorResponse);
		if (message != null || message !="") {
			//for (String errorMessageString : errorMessages) {
				if (message.trim().equalsIgnoreCase(
						getResources()
								.getString(R.string.job_unavailable_alert)
								.trim())) {
					showAlert(message, erroMsgType.JOB_UNAVAILABLE);
					return;
				}
				if (message.trim().equalsIgnoreCase(
						getResources().getString(R.string.job_in_progress))) {
					showAlert(message, erroMsgType.JOB_IN_PROGRESS);
					return;
				}
				if (message.trim().equalsIgnoreCase(
						getResources().getString(R.string.job_cancelled_alert_job_preview))) {
					showAlert(message, erroMsgType.JOB_CANCELLED);
					return;
				}
			//}
		}
		Log.e("JobDetails ErrorResponse", "" + message);
		

		
	}

}
