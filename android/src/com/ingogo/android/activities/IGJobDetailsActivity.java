/*
 * Package Name : com.ingogo.android.activities
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : This activity displays jobdetails Screen, which is accessible from the job preview activity screen.
 */

package com.ingogo.android.activities;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGPaymentsSwipeActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.app.IngogoApp.jobStatusEnum;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.poll.IGChatService;
import com.ingogo.android.poll.IGChatServiceListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCollectedJobApi;
import com.ingogo.android.webservices.IGCompleteOfflineJobApi;
import com.ingogo.android.webservices.IGCreditDetailsApi;
import com.ingogo.android.webservices.IGMapInfoApi;
import com.ingogo.android.webservices.IGNoShowApi;
import com.ingogo.android.webservices.beans.response.IGCollectJobResponseBean;
import com.ingogo.android.webservices.beans.response.IGMapInfoResponseBean;
import com.ingogo.android.webservices.interfaces.IGCollectJobListener;
import com.ingogo.android.webservices.interfaces.IGCompleteOfflineListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGMapInfoApiListener;

public class IGJobDetailsActivity extends IGBaseActivity implements
		IGChatServiceListener, IGMapInfoApiListener, IGCollectJobListener,
		IGExceptionApiListener {

	private TextView _jobDetailsStatusLabel, _passengerNameLabel, _pickupLabel,
			_dropoffLabel, _paysLabel, _numberOfUnreadMessagesLabel/*
																	 * ,_collectBy
																	 */;
	private IGJob _job;
	private ProgressDialog _progressDialog;
	private int CHAT_ACTIVITY_ID = 67;
	private Button _noShowButton, _cancelButton, 
			_processPaymentButton;
	private ImageButton _collectedButton;

	private static boolean _showProgressDialogueOnResume = false;
	private boolean _hasCreditCards;
	JSONObject _bookingDetails;

	private Runnable _resetNumberOfMessages = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			_numberOfUnreadMessagesLabel.setText("0");

		}
	};

	private Date date = null;
	public static boolean _collectedInTime;
	public static boolean _isNavigateFromMenu = false;
	private AlertDialog _alertNavigateFromMenu;
	private boolean _isMapButtonPressed = false;

	boolean _navigateToCalculator = false;
	Handler _collectionHandler = new Handler();
	boolean _enableCollectionButton = false;


	private Runnable doUpdateCollectionButtonStatus = new Runnable() {
		public void run() {
			_enableCollectionButton = true;
			if (_job!=null) {
				if (_job.getStatus() == IGConstants.kJobAccepted) {
					updateCollectionButtonStatus(true);
					
		
				} else if (_job.getStatus() == IGConstants.kJobCompleted) {
					updateCollectionButtonStatus(false);

				} else if (_job.getStatus() == IGConstants.kJobCancelled) {
					updateCollectionButtonStatus(false);

				}		
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		setContentView(R.layout.job_details);
		initViews();
		Intent intent = getIntent();
		_job = (IGJob) intent.getSerializableExtra("Job");
		setUpViews();
		updateCollectionButtonStatus(intent.getBooleanExtra("isCollectButtonEnabled", false));
		if (!(intent.getBooleanExtra("isCollectButtonEnabled", false))) {
			_collectionHandler.postDelayed(doUpdateCollectionButtonStatus, 10000);
		}


	}

	@Override
	protected void onResume() {
		super.onResume();
		IGChatService.getInstance(Integer.parseInt(_job.getId())).setListener(
				this);
		IGChatService.getInstance(Integer.parseInt(_job.getId())).start();
		this.updateUiStatus();
		showAlertForJobInProgress();

		// When the app enters back ground and a the progress dialogue is shown
		// then _showProgressDialogueOnResume is set true. So if the
		// _showProgressDialogueOnResume
		// flag is true then show the progress dialog on onResume.
		if (_showProgressDialogueOnResume) {
			_showProgressDialogueOnResume = false;
			_progressDialog = IGUtility.showProgressDialog(this);
		}
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (_collectionHandler!=null) {
			_collectionHandler.removeCallbacks(doUpdateCollectionButtonStatus);
		}
		super.onDestroy();
	}

	private void updateCollectionButtonStatus(boolean status) {
		_collectedButton.setEnabled(status);
		if (status) {
			_collectedButton.setAlpha(255);
		} else {
			_collectedButton.setAlpha(110);

		}

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

				}
			});
			_alertNavigateFromMenu = adb.create();
			_alertNavigateFromMenu
					.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			_alertNavigateFromMenu.show();
		}
	}

	@Override
	protected void onPause() {

		if (_progressDialog != null) {

			// When the app enters back ground and a the progress dialogue is
			// shown
			// then _showProgressDialogueOnResume is set true. So if the
			// _showProgressDialogueOnResume
			// flag is true then show the progress dialog on onResume.
			if (_progressDialog.isShowing())
				_showProgressDialogueOnResume = true;
			_progressDialog.dismiss();
			_progressDialog = null;

		}
		// If alert is there,when activity finishes.We have to dismiss the
		// alert.
		if (_alertNavigateFromMenu != null
				&& _alertNavigateFromMenu.isShowing()) {
			_alertNavigateFromMenu.dismiss();
			_alertNavigateFromMenu = null;
		}

		IGChatService.getInstance(Integer.parseInt(_job.getId())).stop();

		super.onPause();
	}

	/*
	 * Function to initialize different views.
	 */
	private void initViews() {
		_jobDetailsStatusLabel = (TextView) findViewById(R.id.job_details_status_view);
		_passengerNameLabel = (TextView) findViewById(R.id.job_details_passenger_prompt_name);
		_pickupLabel = (TextView) findViewById(R.id.job_details_passenger_pickup_labelname);
		_dropoffLabel = (TextView) findViewById(R.id.job_details_dropoff_prompt_name);
		_numberOfUnreadMessagesLabel = (TextView) findViewById(R.id.no_of_messages_text);
		_paysLabel = (TextView) findViewById(R.id.rewardLabel);

		_cancelButton = (Button) findViewById(R.id.cancel_button);
		_collectedButton = (ImageButton) findViewById(R.id.collect_button);
		_noShowButton = (Button) findViewById(R.id.noshow_button);
	}

	/*
	 * Function to set up values to different views.
	 */

	private void setUpViews() {
		if (_job != null) {
			if (_job.getPassengerName() != null) {
				_passengerNameLabel.setText(_job.getPassengerName());
			} else {
				_passengerNameLabel.setText("");

			}

			_pickupLabel.setText("" + _job.getPickupFrom());
			_dropoffLabel.setText("" + _job.getDropOffTo());
			_jobDetailsStatusLabel.setText(getJobStatus());

			String _time = IGUtility.timeStampToTime(_job.getTimeWithin(),
					_job.getTimeStamp());
			String _reward = "";

			if (_job.getExtraOffer() != 0) {
				// Format the extra offer to avoid decimal point.
				String extraOfferString = new DecimalFormat("#.#").format(
						_job.getExtraOffer()).toString();
				_reward = "		" + extraOfferString + " points";

			}

			_paysLabel.setText(_time + _reward);

			setResult(_job.getStatus(), getIntent());
		}

		this.updateUiStatus();

	}

	/**
	 * Button action to change the current theme If the current theme is day
	 * then set night as current theme and restart the activity. If the current
	 * theme is night then set day as current theme and restart the activity.
	 * 
	 * @param view
	 */
	public void changeThemeFromJobDetails(View view) {
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
		intent.putExtra("Job", _job);
		intent.putExtra("isCollectButtonEnabled",_collectedButton.isEnabled());
		finish();

		overridePendingTransition(0, 0);
		startActivity(intent);

	}

	private void processPassengerNoShow() {
		// Stop any incoming chats or job status messages.
		Log.i("Location obtained on job details activity", "Latitude = "
				+ IngogoApp.LATTITUDE + "...Longitude = " + IngogoApp.LONGITUDE);
		Log.d("No show button tapped", "No show button tapped");
		IGChatService.getInstance(
				Integer.parseInt(IGJobDetailsActivity.this._job.getId()))
				.stop();
		_progressDialog = IGUtility.showProgressDialog(this);
		int jobId = Integer.parseInt(_job.getId());
		IGNoShowApi igNoShowApi = new IGNoShowApi(this, jobId);
		igNoShowApi.noShow();
	}

	private void displayNoShowAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.passenger_no_show_alert))
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								IGJobDetailsActivity.this
										.processPassengerNoShow();
								// We don't need the chat listener anymore
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						_noShowButton.setEnabled(true);
						dialog.cancel();
					}
				});

		AlertDialog alert = builder.create();
		alert.show();

	}

	public void onNoshowButtonClick(View view) {
		_noShowButton.setEnabled(false);
		this.displayNoShowAlert();
	}

	/*
	 * Function to change the status on collect button click.
	 */

	public void onCollectedButtonClick(View view) {
		if (IGUtility.isNetworkAvailable(this)) {
			updateCollectionButtonStatus(false);
			Log.i("Location obtained on job details activity", "Latitude = "
					+ IngogoApp.LATTITUDE + "...Longitude = "
					+ IngogoApp.LONGITUDE);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCollectedJobApi igCollectedJobApi = new IGCollectedJobApi(this,
					this);
			igCollectedJobApi.collectedBooking(_job.getId());


		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/*
	 * Function to change the status on cancel button click.
	 */
	public void onCancelButtonClick(View view) {
		// _cancelButton.setEnabled(false);
		// this.showCancelJobAlert();
		goToCancellationReasonActivity();
	}

	private void goToCancellationReasonActivity() {
		Intent intent = new Intent(IGJobDetailsActivity.this,
				IGCancellationReasonActivity.class);
		intent.putExtra("Job", _job);
		startActivity(intent);
	}

	/**
	 * Function to display chat screen
	 */

	public void onMessagesButtonClick(View view) {

		Intent intent = new Intent(this, IGChatActivity.class);
		intent.putExtra("Job", _job);
		startActivityForResult(intent, CHAT_ACTIVITY_ID);
		Handler handler = new Handler();
		handler.postDelayed(_resetNumberOfMessages, 1000);
	}

	/*
	 * Function to display the status.
	 */
	private String getJobStatus() {
		if (_job.getStatus() == IGConstants.kJobAccepted)
			return getString(R.string.AcceptedStatusLabel);
		else if (_job.getStatus() == IGConstants.kJobCollected)
			return getString(R.string.CollectedStatusLabel);
		else if (_job.getStatus() == IGConstants.kJobCompleted)
			return getString(R.string.CompletedStatusLabel);
		else if (_job.getStatus() == IGConstants.kJobCancelled)
			return getString(R.string.CancelledStatusLabel);

		return "";
	}

	private void displayJobCompletedAlert() {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(getString(R.string.job_completed_alert));
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				goToJobActivity();
				// finish();
			}
		});

		alertbox.show();

	}

	private void displayJobCancelledAlert() {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(getString(R.string.job_cancelled_alert));
		alertbox.setCancelable(false);
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				goToJobActivity();
			}
		});
		AlertDialog dialog = alertbox.create();
		dialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dialog.show();
	}

	private void updateUiStatus() {

		if (_job.getStatus() == IGConstants.kJobAccepted) {
			if (_enableCollectionButton) {
				updateCollectionButtonStatus(true);

			}
			_cancelButton.setEnabled(true);
			_noShowButton.setEnabled(true);
		} else if (_job.getStatus() == IGConstants.kJobCollected) {
			_collectedButton.setVisibility(View.GONE);
			_cancelButton.setEnabled(false);
			_noShowButton.setEnabled(false);
		} else if (_job.getStatus() == IGConstants.kJobCompleted) {
			updateCollectionButtonStatus(false);
			_cancelButton.setEnabled(false);
			_noShowButton.setEnabled(false);
			this.displayJobCompletedAlert();
		} else if (_job.getStatus() == IGConstants.kJobCancelled) {
			updateCollectionButtonStatus(false);
			_cancelButton.setEnabled(false);
			_noShowButton.setEnabled(false);
			displayJobCancelledAlert();
		}

		_numberOfUnreadMessagesLabel.setText(""
				+ IGChatService.getInstance(Integer.parseInt(_job.getId()))
						.getNumberOfUnreadMessages());

	}

	private void processJsonResponse(int apiID) {
		if (apiID == IGApiConstants.kCollectedJobWebServiceId) {
			collectedInTime();

			setResult(IGConstants.kJobCollected, getIntent());
			_job.setStatus(IGConstants.kJobCollected);
			IngogoApp.setJobStatus(jobStatusEnum.COLLECTED);
			// Intent completeJobIntent = new Intent(IGJobDetailsActivity.this,
			// IGCompleteJobActivity.class);
			// completeJobIntent.putExtra(IGConstants.kJob,
			// Integer.parseInt(_job.getId()));
			// startActivity(completeJobIntent);
			gotoPayment();
			// finish();

		} else if (apiID == IGApiConstants.kCancelJobWebServiceId) {

			setResult(IGConstants.kJobCancelled, getIntent());
			_job.setStatus(IGConstants.kJobCancelled);

			_jobDetailsStatusLabel.setText(getJobStatus());
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.updateUiStatus();

		} else if (apiID == IGApiConstants.kNoShowWebServiceId) {
			setResult(IGConstants.kJobCancelled, getIntent());
			_job.setStatus(IGConstants.kJobCancelled);
			_jobDetailsStatusLabel.setText(getJobStatus());
			// No need to update UI. Just finish the activity.
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			goToJobActivity();

		} else if (apiID == IGApiConstants.kCompleteOfflineWebServiceId) {
			setResult(IGConstants.kJobCompleted, getIntent());
			_job.setStatus(IGConstants.kJobCompleted);

			_jobDetailsStatusLabel.setText(getJobStatus());
			this.updateUiStatus();
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
			_showProgressDialogueOnResume = false;

			IGUtility.dismissProgressDialog(_progressDialog);
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
		_showProgressDialogueOnResume = false;

		if (isCorporateAccountPresent(_bookingDetails)) {
			intent = new Intent(IGJobDetailsActivity.this,
					IGPaymentActivity.class);
		} else {
			if (_hasCreditCards) {
				intent = new Intent(IGJobDetailsActivity.this,
						IGPaymentActivity.class);
			} else {
				intent = new Intent(IGJobDetailsActivity.this,
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

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		super.onResponseReceived(response, apiID);
		if (apiID != IGApiConstants.kCollectedJobWebServiceId
				&& (apiID != IGApiConstants.kCompletedJobWebServiceId)) {
			_showProgressDialogueOnResume = false;

			IGUtility.dismissProgressDialog(_progressDialog);
		}
		// The show progress dialogue flag is set to false
		// to ensure that if the response was received when the app was in
		// background then the progress dialogue should not be shown on
		// onResume.

		if (response == null) {
			return;
		}

		JSONObject responseObject = (JSONObject) response
				.get(IGConstants.kDataKey);

		if (responseObject != null) {
			if (apiID == IGApiConstants.kCreditDetailsWebServiceId) {
				_bookingDetails = responseObject;
				Log.i("CREDIT RESP ON COMPLETE JOB", "" + _bookingDetails);
				processBookingDetailsResponse(responseObject);
				if (_navigateToCalculator) {
					goToCalculator();
					return;
				}
				setUpViews();

			} else {
				this.processJsonResponse(apiID);

			}
		} else {
			_showProgressDialogueOnResume = false;

			IGUtility.dismissProgressDialog(_progressDialog);

		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		IGUtility.dismissProgressDialog(_progressDialog);
		Log.e("JobDetails ErrorResponse", "" + errorResponse);

		// The show progress dialogue flag is set to false
		// to ensure that if the response was received when the app was in
		// background then the progress dialogue should not be shown on
		// onResume.
		_showProgressDialogueOnResume = false;
		if (apiID == IGApiConstants.kCancelJobWebServiceId) { // Restart the
																// chat service
																// which was
																// stopped prior
																// to cancelling
																// job.
			IGChatService.getInstance(
					Integer.parseInt(IGJobDetailsActivity.this._job.getId()))
					.start();
			_cancelButton.setEnabled(true);
		} else if (apiID == IGApiConstants.kCollectedJobWebServiceId) {
			if (_enableCollectionButton) {

			updateCollectionButtonStatus(true);
			}
		} else if (apiID == IGApiConstants.kCompletedJobWebServiceId) {
			_processPaymentButton.setEnabled(true);
		}

		else if (apiID == IGApiConstants.kNoShowWebServiceId) {
			IGChatService.getInstance(
					Integer.parseInt(IGJobDetailsActivity.this._job.getId()))
					.start();
			_noShowButton.setEnabled(true);
		}

		super.onFailedToGetResponse(errorResponse, apiID);
	}

	@Override
	public void incomingMessageReceived(String incomingMessage,
			boolean playMusic) {

		int unreadMessages = IGChatService.getInstance(
				Integer.parseInt(_job.getId())).getNumberOfUnreadMessages();

		if (unreadMessages > 25) {
			unreadMessages = 25;
		}

		_numberOfUnreadMessagesLabel.setText("" + unreadMessages);

	}

	@Override
	public void incomingMessageError(String errorMessage) {

	}

	@Override
	public void bookingStatusReceived(String bookingStatus) {
		Log.d("Passenger_BOOKING_STATUS", bookingStatus);
		if (bookingStatus
				.equalsIgnoreCase(IGApiConstants.kPassengerCancelledJob)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_cancelled_alert));
		} else if (bookingStatus
				.equalsIgnoreCase(IGApiConstants.kPassengerNotConfirmedJob)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_notconfirmed_alert));
		} else if (bookingStatus
				.equalsIgnoreCase(IGApiConstants.kPassengerDispatched)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_dispatched_alert));
		}

	}

	@Override
	public void chatsRecieved(ArrayList<String> chats) {

		// We won't clear all messages here.
	}

	private void processPassengerCancelledAlert(String message) {

		IGChatService.getInstance(
				Integer.parseInt(IGJobDetailsActivity.this._job.getId()))
				.stop();

		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(message);
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
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				_job.setStatus(IGConstants.kJobCancelled); // Set status to
															// cancelled.
				goToJobActivity();
			}
		});
		AlertDialog dialog = alertbox.create();
		dialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dialog.show();
	}

	@Override
	public void onBackPressed() {
		return;
	}

	/**
	 * While the user was in the chat activity and the passenger cancelled the
	 * job.
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHAT_ACTIVITY_ID && resultCode == RESULT_OK
				&& data != null) {
			if (data.getExtras() == null)
				return;
			String bookingStatus = data.getExtras().getString(
					IGApiConstants.kBookingStatusKey);
			if (bookingStatus == null)
				return;
			if (bookingStatus
					.equalsIgnoreCase(IGApiConstants.kPassengerCancelledJob)) {
				IGUtility.removeDefaults(IGConstants.kJobInProgress, this);

				this.processPassengerCancelledAlert(getString(R.string.passenger_cancelled_alert));
			} else if (bookingStatus
					.equalsIgnoreCase(IGApiConstants.kPassengerNotConfirmedJob)) {
				IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
				this.processPassengerCancelledAlert(getString(R.string.passenger_notconfirmed_alert));
			} else if (bookingStatus
					.equalsIgnoreCase(IGApiConstants.kPassengerDispatched)) {
				IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
				this.processPassengerCancelledAlert(getString(R.string.passenger_dispatched_alert));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// function to convert timestamp into a meaningful time format (hh:mm am/pm)

	public String timeStampToTime() {
		int timeWithIn = _job.getTimeWithin();
		if (timeWithIn == 0)
			return null;
		SimpleDateFormat formatter = new SimpleDateFormat("h:mmaa");
		Calendar calendar = Calendar.getInstance();
		long timestamp = _job.getTimeStamp();
		date = new Date(timestamp);
		long newTime = date.getTime();
		Time time = new Time();
		time.set(newTime);
		time.minute += timeWithIn;
		time.normalize(false);
		time.toMillis(false);
		time.format(time.toString());
		date.setTime(time.toMillis(false));
		calendar.setTime(date);
		return (formatter.format(calendar.getTime()));
	}

	private void collectedInTime() {
		Calendar calendar = Calendar.getInstance();
		Date dateCompleted = new Date(calendar.getTimeInMillis());
		if (date == null)
			return;
		if (!dateCompleted.after(date)) {
			if (dateCompleted.getTime() < date.getTime()
					|| dateCompleted.getTime() == date.getTime()) {
				_collectedInTime = true;
				Log.d("collectedInTime", "collected");
			} else {
				_collectedInTime = false;
			}
		}

	}

	// public void onCompleteOfflineButtonClick(View view) {
	// Log.d("CompleteOffline button tapped", "CompleteOffline button tapped");
	// if (IGUtility.isNetworkAvailable(this)) {
	// _progressDialog = IGUtility.showProgressDialog(this);
	// IGCompleteOfflineJobApi _completeOfflineApi = new
	// IGCompleteOfflineJobApi(
	// this,this, _job.getId());
	// _completeOfflineApi.completeOffline();
	// } else {
	// IGUtility.showDialogOk(this.getText(R.string.network_error_title)
	// .toString(), this.getText(R.string.ReachabilityMessage)
	// .toString(), this);
	// }
	// }

	public void onSeeMapBtnClick(View view) {

		if (IGUtility.isNetworkAvailable(this)) {

			if (!_isMapButtonPressed) {

				_isMapButtonPressed = true;

				_progressDialog = IGUtility.showProgressDialog(this);
				IGMapInfoApi _mapInfoApi = new IGMapInfoApi(this, this);
				_mapInfoApi.getMapInfo(_job.getId());
			}
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	private void goToJobActivity() {
		Intent intent = new Intent(IGJobDetailsActivity.this,
				IGJobsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void mapInfoCompleted(IGMapInfoResponseBean mapInfo) {

		_isMapButtonPressed = false;

		if (mapInfo.getBookingStatus().equalsIgnoreCase(
				IGApiConstants.kPassengerCancelledJob)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_cancelled_alert));
		} else if (mapInfo.getBookingStatus().equalsIgnoreCase(
				IGApiConstants.kPassengerNotConfirmedJob)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_notconfirmed_alert));
		} else if (mapInfo.getBookingStatus().equalsIgnoreCase(
				IGApiConstants.kPassengerDispatched)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_dispatched_alert));
		} else {

			if (mapInfo.getPickupLatitude() != null) {
				Intent intent = new Intent(IGJobDetailsActivity.this,
						IGDriversMapActivity.class);
				intent.putExtra(IGConstants.kMapInfo, mapInfo);
				intent.putExtra(IGConstants.kJob, _job);
				startActivity(intent);
			}
		}
		_showProgressDialogueOnResume = false;
		IGUtility.dismissProgressDialog(_progressDialog);
	}

	@Override
	public void mapInfoFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialog("", errorMessage, this);
		_isMapButtonPressed = false;
		_showProgressDialogueOnResume = false;
	}

	@Override
	public void collectedJobSuccessfully(IGCollectJobResponseBean response) {
		// TODO Auto-generated method stub
		collectedInTime();
		setResult(IGConstants.kJobCollected, getIntent());
		_job.setStatus(IGConstants.kJobCollected);
		IngogoApp.setJobStatus(jobStatusEnum.COLLECTED);
		gotoPayment();
	}

	@Override
	public void collectJobRequestFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		Log.e("JobDetails ErrorResponse", "" + errorMessage);

		// The show progress dialogue flag is set to false
		// to ensure that if the response was received when the app was in
		// background then the progress dialogue should not be shown on
		// onResume.
		_showProgressDialogueOnResume = false;
		_collectedButton.setEnabled(true);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		_showProgressDialogueOnResume = false;
		_collectedButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNetWorkUnavailableResponse(errorResponse);
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		_showProgressDialogueOnResume = false;
		_collectedButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onRequestTimedoutResponse(errorResponse);
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		_showProgressDialogueOnResume = false;
		_collectedButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onInternalServerErrorResponse(errorResponse);

	}

	@Override
	public void onNullResponseRecieved() {
		_showProgressDialogueOnResume = false;
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNullResponseRecieved();

	}

}