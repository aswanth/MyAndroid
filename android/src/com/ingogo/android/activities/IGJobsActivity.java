/**
e * Package Name : com.ingogo.android.activities
 * Author 		: Ingogo
 * Copyright 	: Ingogo @ 2010-2011
 * Description 	: This activity displays available jobs for the drivers. 
 *                Drivers can set their status (busy or available) from this screen.
 */

package com.ingogo.android.activities;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGSwipeCalculatorActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.model.IGJobListModel;
import com.ingogo.android.poll.IGAvailableJobsListener;
import com.ingogo.android.poll.IGAvailableJobsPollingTask;
import com.ingogo.android.poll.IGUpdatePositionListener;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGCustomProgressDialog;
import com.ingogo.android.utilities.IGGPSService;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGAvailableApi;
import com.ingogo.android.webservices.IGBaseWebserviceThreadPool;
import com.ingogo.android.webservices.IGBusyApi;
import com.ingogo.android.webservices.IGFindCurrentDriverStateApi;
import com.ingogo.android.webservices.IGLogoutApi;
import com.ingogo.android.webservices.IGReconnectAttemptedApi;
import com.ingogo.android.webservices.IGTakePaymentApi;
import com.ingogo.android.webservices.interfaces.IGReconnectAttemptedApiListener;
import com.ingogo.android.webservices.interfaces.IGTakePaymentApiListener;

/**
 * @author midhun
 * 
 */
public class IGJobsActivity extends IGBaseActivity implements

IGAvailableJobsListener, IGUpdatePositionListener, IGTakePaymentApiListener,
		IGReconnectAttemptedApiListener {

	static int replayNotificationDelay;
	int _lastArrivedJobID;
	private static String _previousJobMsg;
	private ListView _jobListView;
	private RelativeLayout _availableStatus, _busyStatus, _historyRL;
	private TextView _historyTV;

	private ProgressDialog _progressDialog, _igCustomProgressDialog,
			_gpsReEstablishedDialog;
	private Dialog _staleAlert, _gpsDataUnavailableDialog;

	private IGUpdatePositionPollingTask _positionPollingTask = null;
	private IGAvailableJobsPollingTask _availableJobsPoller = null;

	private static ArrayList<IGJob> _jobList;// Contains All Jobs
	private ArrayList<IGJob> _activeJobList;
	private boolean _takePaymentButtonPressed;// Contains jobs which are not
												// cancelled or ignored.

	/**
	 * Initially on driver login, status is set to available.
	 */
	private static Boolean _showNotification;
	private static boolean _showGpsReEstablishedDialog;
	private static Boolean _notificationShowed = false;
	public static boolean checkDriverStatus;
	static boolean _replayAlert;
	AlertDialog _issueDialog;

	private NotificationManager mNotificationManager;
	ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();

	/**
	 * If the driver is available,this flag is set to true.Otherwise set to
	 * false
	 */

	private BaseAdapter _jobListAdapter;

	private Handler _handlerForLogout = new Handler();
	private Handler _handlerForGPSReEstablishedDialog = new Handler();

	/**
	 * @return notification show status.
	 */
	public static Boolean getnotificationShowed() {
		return _notificationShowed;
	}

	/**
	 * Enable after showing the notification.
	 * 
	 * @param notificationShowed
	 */
	public static void setnotificationShowed(Boolean notificationShowed) {
		_notificationShowed = notificationShowed;
	}

	/**
	 * @return to show notification.
	 */
	public static Boolean getshowNotification() {
		return _showNotification;
	}

	/**
	 * Set the notification flag
	 * 
	 * @param showNotification
	 */
	public static void setshowNotification(Boolean showNotification) {
		IGJobsActivity._showNotification = showNotification;
	}

	private TextView _availableStatusText, _busyStatusText;
	// private MediaPlayer _mp = null;
	// private MediaPlayer _mpNew = null;
	private static MediaPlayer _mediaPlayer = null;

	private static Handler _notificationSoundHandler = new Handler();
	private static Runnable _notificationSoundRunnable = new Runnable() {

		@Override
		public void run() {
			_replayAlert = false;
			if (IGJobListModel.isLatestJobAvailable(_jobList)) {
				playSound(R.raw.job_remainder);
			}
		}
	};

	private RelativeLayout reconnectLayout;
	private ImageButton _ingogoPayButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.jobs);
		initViews();

		// The job remainder time out is returned in seconds. So multiply it
		// with 1000 to convert to milliseconds
		replayNotificationDelay = Integer.parseInt(IngogoApp
				.getSharedApplication().getJobRemainderInterval());
		replayNotificationDelay *= 1000;

		checkDriverStatus = true;

		// when the activity is restarted
		// to apply theme always the
		// available status is shown.
		// To avoid this
		// check the status of the driver.
		// If the driver is busy then show
		// "I'm busy" text.
		if (IngogoApp.getAvailable()) {
			changeStatus(View.VISIBLE, View.GONE);
		} else {
			stopJobRemainderAlertRunnable();
			changeStatus(View.GONE, View.VISIBLE);
		}

	}

	@Override
	protected void onResume() {

		Log.e("POLLING TASK", "On resume called");
		if (!IGBaseActivity._infoDialogShown) {

			Log.d("STATUS______", "JOBS ACTIVITY ON RESUME");
			Log.e("Test", "On resume called");
			// if the job activity is coming after pressing payoffline in the
			// swipe pay.
			// Show the issue dialog.
			if (IngogoApp.getSharedApplication().isComingFromPayOffline()) {
				showPayofflineIssueDialog();
			} else {
				conditionsInOnResume();
			}

		}
		if (_positionPollingTask != null) {
			_positionPollingTask.setListner(this);
		}
		super.onResume();
		// else {
		// super.onResume();
		// }
	}

	private void conditionsInOnResume() {
		if (IGLocationListener.isInitialFixObtained() && gpsCheck()) {

			setReconnectLayoutVisibility(View.GONE);

			// For avoid multiple call for findCurrentDriverState web
			// service.
			if (checkDriverStatus == true) {

				findCurrentDriverStateApiCall();
				checkDriverStatus = false;
			}

			if (IngogoApp.getAvailable()) {
				// Call the available jobs api once, as the polling task
				// will
				// start only after the delay.
				_progressDialog = IGUtility.showProgressDialog(this);
				startUpdatingAvailableJobs();
			} else if (getshowNotification() && !getnotificationShowed()) {

				// The job remainder alert should not be heard after the
				// driver
				// entering the stale state.
				stopJobRemainderAlertRunnable();
				changeStatus(View.GONE, View.VISIBLE);
				showNotification();
				showStaleAlert();
			} else {
				loadSavedJobs();
			}

		} else {
			if (checkDriverStatus == true) {

				findCurrentDriverStateApiCall();
				checkDriverStatus = false;
			}

			performGpsCheck();
		}
		setDriverJobHistory(_previousJobMsg);
		if (IngogoApp.isPlayLoginAlert()) {
			IngogoApp.setPlayLoginAlert(false);
			_replayAlert = false;
			playSound(R.raw.beep);
		}

	}

	private void showPayofflineIssueDialog() {

		if (_issueDialog != null && _issueDialog.isShowing()) {
			return;
		}
		_issueDialog = new AlertDialog.Builder(this)
				.setTitle("")
				.setMessage(
						getResources().getText(
								R.string.payoffline_issue_dialog_msg))

				.setPositiveButton("Issue",
						new DialogInterface.OnClickListener() {
							boolean isButtonTapped = false;

							public void onClick(DialogInterface dialog,
									int whichButton) {
								IngogoApp.getSharedApplication()
										.setComingFromPayOffline(false);
								if (!isButtonTapped) {
									Intent reportAnIssueIntent = new Intent(
											IGJobsActivity.this,
											IGReportAnIssueActivity.class);
									startActivity(reportAnIssueIntent);
									isButtonTapped = true;
								}
							}
						})
				.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							boolean isButtonTapped = false;

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								IngogoApp.getSharedApplication()
										.setComingFromPayOffline(false);
								if (!isButtonTapped) {
									conditionsInOnResume();
									isButtonTapped = true;
								}

							}
						}).create();
		_issueDialog.setCancelable(false);
		_issueDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
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
		_issueDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		_issueDialog.show();
	}

	/**
	 * Load the jobs saved in shared preferences. Used to update the job list
	 * fastly while switching the themes
	 */
	private void loadSavedJobs() {

		_jobList = IngogoApp.getSharedApplication().getJobList();
		if (null != _jobList && _jobList.size() > 0) {
			_activeJobList = IGJobListModel.getActiveJobs(_jobList);

			try {
				_jobListAdapter.notifyDataSetChanged();
			} catch (NullPointerException e) {
				// TODO: handle exception
				setupViews();
				_jobListAdapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * To call findCurrentDriverState web service.
	 */
	private void findCurrentDriverStateApiCall() {

		if (IGUtility.isNetworkAvailable(this)) {
			String username = IngogoApp.getSharedApplication().getUserId();
			String password = IngogoApp.getSharedApplication().getPassword();
			String plateNumber = IGUtility.getDefaults(
					IGConstants.kPlateNumber, this);

			// _progressDialog = IGUtility.showProgressDialog(this);
			IGFindCurrentDriverStateApi igFindCurrentDriverStateApi = new IGFindCurrentDriverStateApi(
					IGJobsActivity.this, IGJobsActivity.this);
			igFindCurrentDriverStateApi.findCurrentDriverState(username,
					password, plateNumber, false);
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * Show alert when driver is in stale state.
	 */
	private void showStaleAlert() {
		if (_staleAlert != null) {
			_staleAlert.dismiss();
		}
		createDialogue();
	}

	/**
	 * Create dialogue when driver is in stale state.
	 */
	private void createDialogue() {

		_staleAlert = new AlertDialog.Builder(this)
				.setTitle("")
				.setMessage(
						getResources()
								.getText(R.string.notification_text)
								.toString()
								.replace(
										"15",
										IGUtility.getDefaults(
												IGConstants.kStaleTime, this)))
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (getnotificationShowed()) {
							mNotificationManager
									.cancel(IGConstants.NotificationID);
							setnotificationShowed(false);
						}
					}
				}).create();

		_staleAlert.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		_staleAlert.setCancelable(false);
		_staleAlert.show();
	}

	/**
	 * Show notification when driver is in stale state.
	 */

	private void showNotification() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		setshowNotification(false);
		CharSequence notificationTitle = getResources().getText(
				R.string.notification_title);

		Intent notifyIntent = new Intent(getApplicationContext(),
				IGJobsActivity.class);

		PendingIntent intent = PendingIntent.getActivity(this, 0, notifyIntent,
				android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		final Notification notifyDetails = new Notification(R.drawable.logo,
				notificationTitle, System.currentTimeMillis());

		notifyDetails.setLatestEventInfo(this,
				getResources().getText(R.string.notification_details_title),
				getResources().getText(R.string.notification_text), intent);

		notifyDetails.audioStreamType = AudioManager.STREAM_MUSIC;
		notifyDetails.tickerText = notificationTitle;
		notifyDetails.defaults = Notification.DEFAULT_ALL;
		notifyDetails.tickerText = notificationTitle;

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.custom_notification_layout);
		contentView.setImageViewResource(R.id.image, R.drawable.logo);
		contentView.setTextViewText(R.id.text,
				getResources().getText(R.string.notification_text));
		notifyDetails.contentView = contentView;

		notifyDetails.flags = Notification.FLAG_AUTO_CANCEL;

		setnotificationShowed(true);
		mNotificationManager.notify(IGConstants.NotificationID, notifyDetails);

	}

	@Override
	protected void onPause() {

		if ((_gpsDataUnavailableDialog != null)
				&& (_gpsDataUnavailableDialog.isShowing())) {
			_gpsDataUnavailableDialog.dismiss();
			_gpsDataUnavailableDialog = null;
		}
		_handlerForLogout.removeCallbacks(_logoutRunnable);
		stopUpdatingAvailableJobs();
		IGUtility.dismissProgressDialog(_progressDialog);
		_progressDialog = null;
		if (_igCustomProgressDialog != null) {
			_igCustomProgressDialog.dismiss();
			_igCustomProgressDialog = null;
		}
		if (_positionPollingTask != null)
			_positionPollingTask.setListner(null);
		_positionPollingTask = null;
		// stopJobRemainderAlertRunnable();
		if (_mediaPlayer != null) {
			_mediaPlayer.release();
			_mediaPlayer = null;
		}
		super.onPause();
	}

	/**
	 * Stops the job remainder runnable.
	 */
	public static void stopJobRemainderAlertRunnable() {
		if (_notificationSoundHandler != null) {
			_notificationSoundHandler
					.removeCallbacks(_notificationSoundRunnable);
		}
	}

	private void initViews() {
		// Initialise the listview
		_jobListView = (ListView) findViewById(R.id.jobListView);

		// Initialise the relative layouts
		_availableStatus = (RelativeLayout) findViewById(R.id.availableButton);
		_busyStatus = (RelativeLayout) findViewById(R.id.busyButton);
		_historyRL = (RelativeLayout) findViewById(R.id.historyRL);

		// Initialise the text views
		_availableStatusText = (TextView) findViewById(R.id.availableStatusTv);
		_busyStatusText = (TextView) findViewById(R.id.busyStatusTv);
		_historyTV = (TextView) findViewById(R.id.historyTV);

		_availableStatusText.setText(this.getText(R.string.available_label)
				+ " - "
				+ IGUtility.getDefaults(IGConstants.kPlateNumber,
						IGJobsActivity.this));
		_busyStatusText.setText(this.getText(R.string.busy_label)
				+ " - "
				+ IGUtility.getDefaults(IGConstants.kPlateNumber,
						IGJobsActivity.this));
		_showNotification = false;
		_jobList = null;
		reconnectLayout = (RelativeLayout) findViewById(R.id.reconnectLayout);
		_ingogoPayButton = (ImageButton) findViewById(R.id.takePaymentBtn);
	}

	/**
	 * Views are populated with data from api.
	 */
	private void setupViews() {

		setUpJobList();

	}

	/**
	 * JobListView is populated and listened for user interaction.
	 */
	private void setUpJobList() {
		_jobListAdapter = new BaseAdapter() {
			@Override
			public int getCount() {
				return _activeJobList.size();
			}

			@Override
			public Object getItem(int i) {
				return _activeJobList.get(i);
			}

			@Override
			public long getItemId(int i) {
				return 0;
			}

			@Override
			public View getView(int i, View view, ViewGroup viewGroup) {
				if (view == null) {
					view = LayoutInflater.from(IGJobsActivity.this).inflate(
							R.layout.job_list_cell, null);
				}
				// Update Job details in view
				TextView descTextView = (TextView) view
						.findViewById(R.id.job_desc_text);
				TextView extraTextView = (TextView) view
						.findViewById(R.id.job_extra_text);
				TextView dropOffText = (TextView) view
						.findViewById(R.id.drop_off_text);
				TextView pickUpTime = (TextView) view
						.findViewById(R.id.pickUpTime);
				TextView meridian = (TextView) view.findViewById(R.id.meridian);
				// Fetch job from job list.
				IGJob job = (IGJob) getItem(i);
				if ((job.getExtraOffer()) != 0) {
					String extraOfferString = new DecimalFormat("0.#").format(
							job.getExtraOffer()).toString();
					extraTextView.setVisibility(View.VISIBLE);
					extraTextView.setText(extraOfferString + " points");
					displayPickUpTime(pickUpTime, meridian, job);
				} else {
					extraTextView.setText("");
					displayPickUpTime(pickUpTime, meridian, job);
				}
				descTextView.setText(job.getJobDescription());
				dropOffText.setText(job.getDropOffTo());

				// To show passenger status based on string available from web
				// service.
				ImageView passengerstatusIcon = (ImageView) view
						.findViewById(R.id.passengerStatusImageView);
				RelativeLayout passengerStatusLayout = (RelativeLayout) view
						.findViewById(R.id.passengerStatusLayout);
				String passengerStatusString = job.getPassengerStatus();
				if (passengerStatusString
						.equalsIgnoreCase(IGConstants.kPassengerStatusBiz)) {
					passengerstatusIcon.setBackgroundResource(R.drawable.biz);
					passengerStatusLayout
							.setBackgroundResource(R.drawable.biz_trans);

				} else if (passengerStatusString
						.equalsIgnoreCase(IGConstants.kPassengerStatusVip)) {
					passengerstatusIcon.setBackgroundResource(R.drawable.vip);
					passengerStatusLayout
							.setBackgroundResource(R.drawable.vip_trans);
				} else {
					passengerstatusIcon
							.setBackgroundResource(R.drawable.new_passenger_status);
					passengerStatusLayout
							.setBackgroundResource(R.drawable.new_trans);
				}

				return view;
			}
		};

		_jobListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int pos, long arg3) {
				if (IngogoApp.getAvailable()) {
					IGJob job = _activeJobList.get(pos);
					Intent intent = null;
					if (job.getStatus() != IGConstants.kJobOpen) {

						intent = new Intent(IGJobsActivity.this,
								IGJobDetailsActivity.class);
						intent.putExtra("isCollectButtonEnabled", false);

					} else {
						intent = new Intent(IGJobsActivity.this,
								IGJobPreviewActivity.class);
					}

					intent.putExtra("Job", job);
					startActivityForResult(intent, job.getStatus());

					if (job.isNewJob()) {
						_jobList.get(_jobList.indexOf(job)).setNewJob(false);
						_activeJobList.get(pos).setNewJob(false);
						_jobListAdapter.notifyDataSetChanged();
					}
				} else {
					IGUtility.showDialogOk("",
							IGJobsActivity.this
									.getText(R.string.busy_alert_msg)
									.toString(), IGJobsActivity.this);
				}
			}
		});

		_jobListView.setAdapter(_jobListAdapter);
	}

	private void displayPickUpTime(TextView pickUpTime, TextView meridian,
			IGJob job) {
		String timeWithMeridian = timeStampToTime(job);
		String timeString = timeWithMeridian.substring(0,
				timeWithMeridian.length() - 2);
		String meridianString = timeWithMeridian.substring(timeWithMeridian
				.length() - 2);
		pickUpTime.setText(timeString);
		meridian.setVisibility(View.VISIBLE);
		meridian.setText(meridianString);
	}

	/**
	 * Available or busy api listeners
	 */
	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {

		super.onResponseReceived(response, apiID);
		IGUtility.dismissProgressDialog(_progressDialog);
		JSONObject availableResponse = (JSONObject) response
				.get(IGConstants.kDataKey);
		_availableStatus.setEnabled(true);
		_busyStatus.setEnabled(true);
		if (availableResponse != null) {

			// When response for available web service get,then start polling
			// for location update and available jobs. When response for busy
			// web service get,then stop polling for location update and
			// available jobs.

			if (apiID == IGApiConstants.kAvailableWebServiceId) {

				changeStatus(View.VISIBLE, View.GONE);
				IngogoApp.setAvailable(true);
				stopUpdatingAvailableJobs();
				setshowNotification(true);
				if (getnotificationShowed()) {
					try {
						mNotificationManager.cancel(IGConstants.NotificationID);
					} catch (NullPointerException e) {
						mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
						mNotificationManager.cancel(IGConstants.NotificationID);
					}
					setnotificationShowed(false);
				}
				Log.e("POLLING TASK", "Called in api response");
				if (IGLocationListener.isInitialFixObtained() && gpsCheck())
					startUpdatingAvailableJobs();

			} else if (apiID == IGApiConstants.kBusyWebServiceId) {
				stopJobRemainderAlertRunnable();
				changeStatus(View.GONE, View.VISIBLE);
				IngogoApp.setAvailable(false);
				setshowNotification(false);
				stopUpdatingAvailableJobs();
			} else if (apiID == IGApiConstants.kLogoutWebServiceId) {
				performLogout();
			}
		}

	}

	/**
	 * Plays the new job alert and sets the flag to play the job remainder
	 * alert.
	 */
	void playNewJobAlert() {
		_replayAlert = true;
		playSound(R.raw.newjob);
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		IGUtility.dismissProgressDialog(_progressDialog);
		_availableStatus.setEnabled(true);
		_busyStatus.setEnabled(true);

		// If the webservice failed is the logout webservice
		// then the driver is logged out.
		// TODO: Inform the driver.
		if (apiID == IGApiConstants.kLogoutWebServiceId) {
			performLogout();
			return;
		} else if (apiID == IGApiConstants.kJobsWebServiceId
				|| apiID == IGApiConstants.kFindCurrentDriverStateWebServiceId) {
			return;
		}
		super.onFailedToGetResponse(errorResponse, apiID);
	}

	/**
	 * When a job is accepted, ignored, collected, completed and cancelled, job
	 * result will be available returned as activity result. Depending upon the
	 * activity result job list is reloaded.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null || _jobList == null)
			return;
		if (requestCode == IGConstants.kJobAccepted
				|| requestCode == IGConstants.kJobCollected
				|| requestCode == IGConstants.kJobOpen) {
			IGJob job = (IGJob) data.getSerializableExtra("Job");
			if (job != null) {
				if (_jobList.contains(job)) {
					// Get the job from job list and update it status.
					IGJob oldJob = _jobList.get(_jobList.indexOf(job));
					oldJob.setStatus(resultCode);
					// Modify job list and update job list view with updated job
					// status.
					_activeJobList = IGJobListModel.getActiveJobs(_jobList);
					_jobListAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	/**
	 * Get all available jobs
	 */
	@Override
	public void availableJobsUpdated(ArrayList<IGJob> jobList) {
		IGUtility.dismissProgressDialog(_progressDialog);
		if (jobList != null) {
			if (_jobList == null) {
				if (IngogoApp.getSharedApplication().getJobList() != null) {
					if (IGJobListModel.syncJobs(jobList, IngogoApp
							.getSharedApplication().getJobList())) {
						// startSoundNotification();
						_replayAlert = true;
						playNewJobAlert();
						// playSound(R.raw.beep);
					}
					_jobList = jobList;
					_activeJobList = IGJobListModel.getActiveJobs(_jobList);
					try {
						_jobListAdapter.notifyDataSetChanged();
					} catch (NullPointerException e) {
						setupViews();
						_jobListAdapter.notifyDataSetChanged();
					}

				} else {
					_activeJobList = jobList;
					_jobList = jobList;
					setupViews();
					// startSoundNotification();
					// _replayAlert = true;
					// playSound(R.raw.beep);
				}
			} else {
				if (IGJobListModel.syncJobs(jobList, _jobList)) {

					playNewJobAlert();
				}
				_jobList = jobList;
				_activeJobList = IGJobListModel.getActiveJobs(_jobList);
				try {
					_jobListAdapter.notifyDataSetChanged();
				} catch (NullPointerException e) {
					setupViews();
					_jobListAdapter.notifyDataSetChanged();
				}
			}
		}
		IngogoApp.getSharedApplication().setJobList(_jobList);
	}

	@Override
	public void availableJobsUpdateFailed(String errorMessage) {
		Log.e("AVAILABLE_JOBS", errorMessage);
		IGUtility.dismissProgressDialog(_progressDialog);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	/**
	 * Generate sound when available jobs obtained.
	 */
	// public void startSoundNotification() {
	// if (_mp == null || !_mp.isPlaying()) {
	// _mp = MediaPlayer.create(this, R.raw.beep);
	// _mp.start();
	// }
	//
	// }

	/**
	 * Generate sound when new job found.
	 */
	// public void startNewJobSoundNotification() {
	// if (_mpNew == null || !_mpNew.isPlaying()) {
	// _mpNew = MediaPlayer.create(this, R.raw.newjob);
	// _mpNew.start();
	// }
	//
	// }

	/**
	 * Plays the sound file. Resets the notification sound replaying timer.
	 * 
	 * @param sound
	 */
	static void playSound(int sound) {
		Log.e("Test", "Play sound called");
		if (_mediaPlayer == null || !_mediaPlayer.isPlaying()) {
			_mediaPlayer = MediaPlayer.create(IngogoApp.getSharedApplication()
					.getApplicationContext(), sound);
			_mediaPlayer.start();
		}

		if (_replayAlert) {
			if (null != _notificationSoundHandler) {
				_notificationSoundHandler
						.removeCallbacks(_notificationSoundRunnable);
				_notificationSoundHandler.postDelayed(
						_notificationSoundRunnable, replayNotificationDelay);
			}
		}

	}

	/**
	 * 
	 * @param job
	 * @return format time in string
	 */
	public String timeStampToTime(IGJob job) {
		SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
		Calendar calendar = Calendar.getInstance();
		long timestamp = job.getTimeStamp();
		Date date = new Date(timestamp);
		long newTime = date.getTime();
		Time time = new Time();
		time.set(newTime);
		time.minute += job.getTimeWithin();
		time.normalize(false);
		time.toMillis(false);
		time.format(time.toString());
		date.setTime(time.toMillis(false));
		calendar.setTime(date);
		return (formatter.format(calendar.getTime()));
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

		Log.d("LOCATION_UPDATE", "Updated to : " + String.valueOf(latitude)
				+ " , " + String.valueOf(longitude));

	}

	@Override
	public void positionUpdateFailed(String errorMessage) {
		setReconnectLayoutVisibility(View.VISIBLE);

	}

	/**
	 * Get the initial location of the driver.
	 */
	@Override
	public void initialFixObtained(double latitude, double longitude) {
		Log.e("POLLING TASK", "Inital Fix obtained");
		if (!isTopActivityDifferentFromNewIntentActivity(IGConstants.kJobsActivityName)) {

			_handlerForLogout.removeCallbacks(_logoutRunnable);

			if (_showGpsReEstablishedDialog) { // show dialog after
												// re-establishing
												// GPS
				if (_gpsReEstablishedDialog != null) {
					if (!_gpsReEstablishedDialog.isShowing()) {
						_gpsReEstablishedDialog = IGUtility
								.showProgressDialogWithMsg(this,
										"GPS positioning re-established. Jobs will now be available");
					}
				} else {
					_gpsReEstablishedDialog = IGUtility
							.showProgressDialogWithMsg(this,
									"GPS positioning re-established. Jobs will now be available");
				}
				_handlerForGPSReEstablishedDialog.postDelayed(
						removeGPSReEstablishedDialog, 5 * 1000);
			}

			// if (_igCustomProgressDialog != null) {
			// if (_igCustomProgressDialog.isShowing()) {
			// _igCustomProgressDialog.dismiss();
			setReconnectLayoutVisibility(View.GONE);

			findCurrentDriverStateApiCall();

			if (IngogoApp.getAvailable()) {
				stopUpdatingAvailableJobs();
				startUpdatingAvailableJobs();
			}
		}
		//
		// }
		// }

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

	/**
	 * Invokes the logout API Starts the the progress dialog: stops when the
	 * response is recieved/failed Fetches the username and password from shared
	 * preferences
	 */
	private void invokeLogoutAPI() {
		_progressDialog = IGUtility.showProgressDialog(IGJobsActivity.this);
		IGLogoutApi igLogOutApi = new IGLogoutApi(IGJobsActivity.this);
		String username = IngogoApp.getSharedApplication().getUserId();
		String password = IngogoApp.getSharedApplication().getPassword();
		igLogOutApi.logout(username, password);
	}

	/**
	 * Perform the log out actions. The user credentials are removed first and
	 * then navigate to the login screen.
	 */
	private void performLogout() {
		// stop calling currentLocationApi
		stopPositionLocationUpdateApi();

		// Remove all locally stored Data.
		IGUtility.removeDefaults(IGConstants.kJobInProgress,
				IGJobsActivity.this);
		IngogoApp.getSharedApplication().removeLoginCredentials();
		IngogoApp.getSharedApplication().setLoggedIn(false);

		navigateToLogin();

	}

	/**
	 * Restart the GPS Service. Show the location fetching alert Start the
	 * runnable to logout the driver if the above alert is shown for a long
	 * time.
	 */
	private void restartGPSListening() {
		// If the GPS service is runnig stop it and then start the service
		// again
		if (IGUtility.isGPSServiceRunning()) {
			stopService(new Intent(IGJobsActivity.this, IGGPSService.class));
		}
		if (!IGUtility.isGPSServiceRunning()) {
			startService(new Intent(IGJobsActivity.this, IGGPSService.class));
		}

		// Show the location fetching
		// message
		// showLocationFetchingMessage();

		// Start the runnable to logout the
		// driver after 5 mins if the
		// location fetching alert
		// is still shown.
		// startLogoutRunnable();
	}

	private Runnable removeGPSReEstablishedDialog = new Runnable() {
		@Override
		public void run() {
			IGUtility.dismissProgressDialog(_gpsReEstablishedDialog);

		}
	};

	/**
	 * Runnable for logging out the user if please wait alert is shown for 5
	 * mins._pollInterval
	 */
	private Runnable _logoutRunnable = new Runnable() {

		@Override
		public void run() {

			if (_igCustomProgressDialog != null) {
				// if please wait is shown dismiss that progress dialogue
				if (_igCustomProgressDialog.isShowing()) {
					_igCustomProgressDialog.dismiss();

					// alert dialog which cannot be dismissed by
					// back button, search button and menu button.
					_gpsDataUnavailableDialog = new AlertDialog.Builder(
							IGJobsActivity.this)
							.setTitle("")
							.setMessage(
									getResources().getString(
											R.string.gps_unavailable))
							.setCancelable(false)
							.setOnKeyListener(
									new DialogInterface.OnKeyListener() {

										public boolean onKey(
												DialogInterface dialog,
												int keyCode, KeyEvent event) {
											if (keyCode == KeyEvent.KEYCODE_SEARCH
													&& event.getRepeatCount() == 0) {
												return true;
											}
											return false;
										}

									})
							.setPositiveButton(
									getResources().getString(R.string.logout),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {

											// Logs out the driver
											// Invoke the logout API
											invokeLogoutAPI();
										}
									})
							.setNegativeButton(
									getResources()
											.getString(R.string.try_again),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog, int id) {

											// Restart the GPS/
											restartGPSListening();

										}
									}).create();
					_gpsDataUnavailableDialog
							.setVolumeControlStream(AudioManager.STREAM_MUSIC);
					_gpsDataUnavailableDialog.show();

				}
			}

		}
	};

	/**
	 * Starts a runnable which logs out the driver if the please wait alert is
	 * shown for 5 mins.
	 */
	private void startLogoutRunnable() {
		// Start the thread to logout the driver if this porgress dialog
		// is shown for logoutDelay.
		if (_handlerForLogout == null) {
			_handlerForLogout = new Handler();
		}
		_handlerForLogout.postDelayed(_logoutRunnable, IGConstants.logoutDelay);
	}

	/**
	 * Displays a progress dialog with the GPS data fetching message
	 */
	private void showLocationFetchingMessage() {
		_igCustomProgressDialog = IGCustomProgressDialog.show(this, "",
				getString(R.string.progress_dialog_message));
		_igCustomProgressDialog
				.setOnKeyListener(new DialogInterface.OnKeyListener() {

					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_SEARCH
								&& event.getRepeatCount() == 0) {
							return true;
						}
						return false;
					}

				});

	}

	/**
	 * Navigates to Login activity. Set the status flag as off line to clear the
	 * locally saved data from login activity.
	 */
	private void navigateToLogin() {

		Intent intent = new Intent(IGJobsActivity.this, IGSignupActivity.class);
		intent.putExtra(IGConstants.kStatus, IGConstants.driverOffline);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void setReconnectLayoutVisibility(int visibilityStatus) {
		Log.e("setReconnectLayoutVisibility", "setReconnectLayoutVisibility - "
				+ visibilityStatus);
		reconnectLayout.setVisibility(visibilityStatus);
		int jobListViewVisibility = 0;
		if (visibilityStatus == View.VISIBLE) {
			IGUtility.logDetailsToAnalyticsWithoutLocation(
					IGConstants.kAnalyticsGPSDropout,
					IGConstants.kAnalyticsGPSDropout);
			if (IGUtility.isGPSServiceRunning()) {
				stopService(new Intent(IGJobsActivity.this, IGGPSService.class));
				Log.e("STOP GPS SERVICE", "STOP GPS SERVICE");
			}
			stopPositionLocationUpdateApi();
			stopUpdatingAvailableJobs();
			jobListViewVisibility = View.GONE;
		} else {
			jobListViewVisibility = View.VISIBLE;

		}
		_jobListView.setVisibility(jobListViewVisibility);
	}

	/**
	 * To obtained the initial location.
	 */
	private void performGpsCheck() {
		if (gpsCheck()) {

			// a runable to logout the driver, if the please wait alert is shown
			// for 5 mins
			// startLogoutRunnable();

			// show an alert which indicates location fetching is in progress
			// showLocationFetchingMessage();
			setReconnectLayoutVisibility(View.VISIBLE);

			// Start updating user location
			if (!IGUtility.isGPSServiceRunning()) {
				startService(new Intent(this, IGGPSService.class));
			}

			// Created an instance of the position polling task to
			// get the initialFixObtained delegate method.
			if (_positionPollingTask == null)
				_positionPollingTask = new IGUpdatePositionPollingTask(this);
		} else {
			// Dialog dlg = new AlertDialog.Builder(this)
			// .setTitle("")
			// .setMessage(getString(R.string.gps_enable_message))
			// .setCancelable(false)
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
			setReconnectLayoutVisibility(View.VISIBLE);

		}
	}

	/**
	 * Asking to reconnect
	 * 
	 * @param v
	 */
	public void onClickReconnectButton(View v) {

		// restartGPSListening();

		if (IGUtility.isNetworkAvailable(this)) {
			IGUtility.dismissProgressDialog(_progressDialog);
			_progressDialog = IGUtility.showProgressDialog(this);

			IGReconnectAttemptedApi api = new IGReconnectAttemptedApi(this,
					this);
			api.reconnectAttemptedForDriver();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * To check whether GPS enable or not in the phone.
	 * 
	 * @return
	 */
	private boolean gpsCheck() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	}

	/**
	 * Starts polling the available jobs polling task.
	 */
	private void startUpdatingAvailableJobs() {
		stopUpdatingAvailableJobs();

		if (_availableJobsPoller == null) {
			Log.e("POLLING TASK", "Start polling called in Jobs activity");
			_availableJobsPoller = new IGAvailableJobsPollingTask(this);

		}
		long pollingInterval = 1;
		if (IngogoApp.getSharedApplication().getPollJobsInterval() >= 1) {
			pollingInterval = IngogoApp.getSharedApplication()
					.getPollJobsInterval();
		}
		_availableJobsPoller.startPolling(pollingInterval * 1000);
	}

	/**
	 * Stops polling the available jobs polling task.
	 */
	private void stopUpdatingAvailableJobs() {
		if (_availableJobsPoller != null) {
			Log.e("POLLING TASK", "Stop polling called in Jobs activity");
			_availableJobsPoller.stopPolling();
			_availableJobsPoller = null;
		}
	}

	/**
	 * Click on the available button is caught by this method. Then call busy
	 * web service.
	 * 
	 * @param view
	 */
	public void onAvailableButtonClick(View view) {
		Log.i("available button tapped", "available button tapped");
		Log.i("call  busy api", "call  busy api");
		callBusyApi();

	}

	/**
	 * Click on the busy button is caught by this method. Then call available
	 * web service.
	 * 
	 * @param view
	 */

	public void onBusyButtonClick(View view) {
		Log.i("busy button tapped", "available button tapped");

		Log.i("call available  api", "call available api");
		callAvailableApi();

	}

	/**
	 * To call busy web service.
	 */
	private void callBusyApi() {
		_availableStatus.setEnabled(false);
		_progressDialog = IGUtility.showProgressDialog(IGJobsActivity.this);
		IGBusyApi busyApi = new IGBusyApi(IGJobsActivity.this);
		busyApi.setBusy();
	}

	/**
	 * To call available web service.
	 */
	private void callAvailableApi() {
		_busyStatus.setEnabled(false);
		_progressDialog = IGUtility.showProgressDialog(IGJobsActivity.this);
		IGAvailableApi availableApi = new IGAvailableApi(IGJobsActivity.this);
		availableApi.setAvailable();

	}

	/**
	 * To change the driver status from available to busy or vice versa.
	 * 
	 * @param available
	 * @param busy
	 */
	private void changeStatus(int available, int busy) {
		_availableStatus.setVisibility(available);
		_busyStatus.setVisibility(busy);

	}

	/**
	 * When status is true, driver state is changed to available. Otherwise
	 * changed to busy.
	 */
	@Override
	public void getDriverStatus(boolean status) {
		Log.i("Driver availability in jobs activity", "" + status);
		if (status) {
			IngogoApp.setAvailable(true);
			changeStatus(View.VISIBLE, View.GONE);
			stopUpdatingAvailableJobs();
			if (IGLocationListener.isInitialFixObtained() && gpsCheck()) {
				startUpdatingAvailableJobs();
			}
		} else {
			stopJobRemainderAlertRunnable();
			IngogoApp.setAvailable(false);
			changeStatus(View.GONE, View.VISIBLE);
			stopUpdatingAvailableJobs();
		}
	}

	@Override
	public void onBackPressed() {
		// Disable the back button as the back button forces the driver to
		// log in again.
		return;
	}

	/**
	 * Function to set the history text.
	 * 
	 * @param history
	 */
	void setDriverJobHistory(String message) {
		if (null != message && message.length() > 0) {
			_historyRL.setVisibility(View.VISIBLE);
			_historyTV.setText(message);
		} else {
			_historyRL.setVisibility(View.GONE);

		}
	}

	@Override
	public void getPreviousJobMessage(String message) {
		if (message != null) {
			_previousJobMsg = message;
			setDriverJobHistory(message);
		}
	}

	public void onIngogoPaymentButtonClick(View view) {
		isIngogoPay = true;
		callTakePaymentApi();
	}

	public void onSwipePaymentButtonClick(View view) {
		isIngogoPay = false;
		// callTakePaymentApi();
		IGBaseWebserviceThreadPool.getSharedInstance().shutDownThreadPool();
		Intent swipePay = new Intent(this, IGSwipeCalculatorActivity.class);
		IGSwipeCalculatorActivity.clearCachedValues();
		swipePay.putExtra("isUnknownPassenger", true);

		swipePay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// swipePay.putExtra("isUnknownPassenger", true);
		IngogoApp.getSharedApplication().setMeterFare("00.00");
		startActivity(swipePay);

		IngogoApp.setSwipeButtonTappedTime(getTimeString());

	}

	String getTimeString() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date(Calendar.getInstance().getTimeInMillis());
		String dateString = fmt.format(date);
		return dateString;
	}

	@Override
	public void reconnectAttemptedRequestCompleted() {
		IGUtility.dismissProgressDialog(_progressDialog);
		_showGpsReEstablishedDialog = true;
		restartGPSListening();
	}

	@Override
	public void reconnectAttemptedRequestFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		Log.e("reconnectAttemptedRequestFailed", errorMessage);
		IGUtility.showDialogOk("", errorMessage, this);
	}

	protected void callTakePaymentApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_ingogoPayButton.setEnabled(false);
			_takePaymentButtonPressed = true;
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

	public void takePaymentCompleted(boolean status) {
		_takePaymentButtonPressed = false;
		_ingogoPayButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.takePaymentCompleted(status);

	}

	public void takePaymentFailed(String errorMessage) {
		_ingogoPayButton.setEnabled(true);
		_takePaymentButtonPressed = false;
		IGUtility.dismissProgressDialog(_progressDialog);
		super.takePaymentFailed(errorMessage);

	}

	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		_ingogoPayButton.setEnabled(true);
		if (!_takePaymentButtonPressed) {
			IGUtility.dismissProgressDialog(_progressDialog);

		}
		super.onNetWorkUnavailableResponse(errorResponse);

	}

	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		_ingogoPayButton.setEnabled(true);
		if (!_takePaymentButtonPressed) {
			IGUtility.dismissProgressDialog(_progressDialog);

		}
		super.onRequestTimedoutResponse(errorResponse);

	}

	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		_ingogoPayButton.setEnabled(true);
		if (!_takePaymentButtonPressed) {
			IGUtility.dismissProgressDialog(_progressDialog);

		}
		super.onInternalServerErrorResponse(errorResponse);

	}

	public void onNullResponseRecieved() {
		_ingogoPayButton.setEnabled(true);
		if (!_takePaymentButtonPressed) {
			IGUtility.dismissProgressDialog(_progressDialog);

		}
		super.onNullResponseRecieved();

	}

}
