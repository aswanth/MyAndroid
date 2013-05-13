/**
 * 
 */
package com.ingogo.android.activities;

import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.poll.IGUpdatePositionListener;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGGPSService;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGDriverLocalityApi;
import com.ingogo.android.webservices.interfaces.IGDriverLocalityApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

/**
 * @author dipu
 * 
 */
public class IGSplashActivity extends IGBaseActivity implements
		IGExceptionApiListener, IGDriverLocalityApiListener,
		IGUpdatePositionListener {

	private Dialog _dialog;
	private Dialog _alertDialog;
	String _latitude, _longitude;
	private Handler _gpsRestartHandler = new Handler();

	private interface GPSFailedConditions {
		static int GPS_FETCHING_TIMEOUT = 1;
		static int FIND_LOCALITY_API_FAILED = 2;
	}

	private Runnable _restartGPSRunnable = new Runnable() {

		@Override
		public void run() {
			// Invalidate the timer.
			_gpsRestartHandler.removeCallbacks(_restartGPSRunnable);
			showGPSFailedAlert(GPSFailedConditions.GPS_FETCHING_TIMEOUT);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		if (isAppRestartedAfterCrash()) {
			showCrashAlert();
		}

	}

	private boolean isAppRestartedAfterCrash() {
		Intent intent = getIntent();

		Bundle extras = intent.getExtras();
		boolean isAppRestarted = false;
		try {
			isAppRestarted = extras.getBoolean("show_crash_alert", false);

		} catch (Exception e) {
			isAppRestarted = false;
		}

		return isAppRestarted;

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isAppRestartedAfterCrash()) {
			performGpsCheck();

		}
	}

	/**
	 * show crash alert
	 */
	private void showCrashAlert() {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle("");
		adb.setMessage("An unexpected error has occurred");
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				performGpsCheck();

			}
		});
		AlertDialog ad = adb.create();
		ad.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		ad.show();

	}

	private void moveToJobListScreen() {
		Intent jobIntent = new Intent(IGSplashActivity.this,
				IGJobsActivity.class);
		startActivity(jobIntent);
	}

	/**
	 * To check that GPS in the phone is enable or not. If enable, try to get
	 * the current location by starting the GPS service.Else prompt an alert.
	 * When click on the OK button on the alert navigate to the no GPS screen.
	 * **/
	private void performGpsCheck() {
		if (gpsCheck()) {
			// Start updating user location if the Listener is not started.
			// Otherwise restart the listener.
			if (!IGUtility.isGPSServiceRunning()) {
				startService(new Intent(this, IGGPSService.class));
				_gpsRestartHandler.postDelayed(_restartGPSRunnable,
						IGConstants.logoutDelay);
				showGPSFetchingAlert();
			} else
				restartGPSListening();

		} else {
			goToGPSOffScreen();
		}
	}

	/**
	 * Shows an alert with gps diabled msg. On tapping the OK button of the
	 * alert a new screen will be showed asking to restart GPS
	 */
	private void goToGPSOffScreen() {
		// Dismiss all other alerts.
		if (_alertDialog != null && _alertDialog.isShowing()) {
			_alertDialog.dismiss();
		}

		if (_dialog != null && _dialog.isShowing()) {
			_dialog.dismiss();
		}

		Intent intent = new Intent(IGSplashActivity.this, IGGPSDisabled.class);
		finish();
		startActivity(intent);
	}

	/**
	 * Shows an alert without any butons. This alert will be dismissed when GPS
	 * fix is obtained or after 5 mins when the data not fetched alert is shown.
	 * The position of this alert is configured in the layout params.
	 */
	private void showGPSFetchingAlert() {

		// Dismiss the previously showing alert.
		if (_dialog != null && _dialog.isShowing()) {
			_dialog.dismiss();
		}
		// Initializing the variable for implementing update position listener
		IGUpdatePositionPollingTask polling = new IGUpdatePositionPollingTask(
				this);
		_dialog = new AlertDialog.Builder(this).setTitle("")
				.setMessage(getString(R.string.gps_fetch_message)).create();
		_dialog.setCancelable(false);
		_dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true;
				}
				return false;
			}

		});

		// Throughout the app the media volume should be adjusted when
		// the hard ware volume control btns are tapped.
		_dialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		_dialog.getWindow().setAttributes(getLayoutParams());
		_dialog.show();

	}

	/**
	 * Returns the layout params to place the alert below the ingogo logo.
	 * 
	 * @return
	 */
	private LayoutParams getLayoutParams() {
		Display display = getWindowManager().getDefaultDisplay();
		int height = display.getHeight();
		Log.i("height", "" + height);

		// The alert should be placed below the ingogo logo
		WindowManager.LayoutParams layoutParams = _dialog.getWindow()
				.getAttributes();
		layoutParams.y = height / 2;
		return layoutParams;
	}

	/**
	 * Used to check Gps in the mobile phone is enable or not.
	 * **/
	private boolean gpsCheck() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	}

	private void goToLoginScreen() {
		_gpsRestartHandler.removeCallbacks(_restartGPSRunnable);
		Intent loginIntent = new Intent(IGSplashActivity.this,
				IGSignupActivity.class);
		startActivity(loginIntent);
		finish();
	}

	@Override
	public void driverLocalityFetchingCompleted(String driverLocality,
			String maskedLocality, long localityId, String localityName) {
		_dialog.dismiss();
		// Saves the valid prefixes in shared preference. These prefixes has to
		// be used until the user quits the app.
		IngogoApp.getSharedApplication().setValidTaxiPlatePrefixes(
				driverLocality);
		IngogoApp.getSharedApplication().setValidMaskedTaxiPlatePrefixes(
				maskedLocality);
		IngogoApp.getSharedApplication().setLocalityId(localityId);
		IngogoApp.getSharedApplication().setLocalityName(localityName);

		// if the app is restarted after the crash move to job list screen
		// instead of login screen if the user is logged in.
		if (isAppRestartedAfterCrash()) {
			if (IngogoApp.getSharedApplication().isLoggedIn()) {
				moveToJobListScreen();
				return;
			}
		}

		goToLoginScreen();

	}

	@Override
	public void driverLocalityFetchingFailed(String errorMessage) {
		showGPSFailedAlert(GPSFailedConditions.FIND_LOCALITY_API_FAILED);
	}

	@Override
	public void initialFixObtained(double latitude, double longitude) {
		if (!isTopActivityDifferentFromNewIntentActivity(IGConstants.kSplashActivityName)) {
			_gpsRestartHandler.removeCallbacks(_restartGPSRunnable);
			getValidTaxiPlatePrefixes();
		}
	}

	/**
	 * 
	 */
	private void getValidTaxiPlatePrefixes() {

		_latitude = "" + IGLocationListener.getCurrentLatitude();
		_longitude = "" + IGLocationListener.getCurrentLongitude();
		IGDriverLocalityApi driverLocalityApi = new IGDriverLocalityApi(this,
				this);
		driverLocalityApi.getDriverLocality(_latitude, _longitude);
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
			stopService(new Intent(IGSplashActivity.this, IGGPSService.class));
		}
		if (!IGUtility.isGPSServiceRunning()) {
			startService(new Intent(IGSplashActivity.this, IGGPSService.class));
		}

		// Show the location fetching
		// message
		showGPSFetchingAlert();
	}

	/**
	 * Show an alert when GPS fetching is failed or the findLocality api fails.
	 * This alert is presented with a 'Retry' option.
	 * 
	 * @param condition
	 */
	private void showGPSFailedAlert(final int condition) {

		// If the activity is on its finishing process then do not show the
		// alert.
		if (isFinishing()) {
			return;
		}

		// If the GPS failed alert is already showing then do not show it again.
		if (_alertDialog != null && _alertDialog.isShowing()) {
			return;
		}

		// Dismiss all other alerts.
		if (_dialog != null && _dialog.isShowing() && !isFinishing()) {
			try {
				_dialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String message = "";
		if (condition == GPSFailedConditions.GPS_FETCHING_TIMEOUT) {
			message = getString(R.string.gps_unavailable);
		} else {
			message = getString(R.string.locality_api_failed);
		}
		_gpsRestartHandler.removeCallbacks(_restartGPSRunnable);
		_alertDialog = new AlertDialog.Builder(this)
				.setTitle("")
				.setMessage(message)
				.setPositiveButton("Retry",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// If the showGPSFailedAlert is called from the
								// gps fetching time out runnable task then
								// restart the gps listner on tapping the
								// 'Retry' button. If it is from an api failed
								// delegate method then call the api once again.
								if (condition == GPSFailedConditions.GPS_FETCHING_TIMEOUT) {
									_gpsRestartHandler.postDelayed(
											_restartGPSRunnable,
											IGConstants.logoutDelay);
									restartGPSListening();
								} else {
									getValidTaxiPlatePrefixes();
								}
							}
						}).create();
		_alertDialog.setCancelable(false);
		_alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true;
				}
				return false;
			}

		});

		// Throughout the app the media volume should be adjusted when
		// the hard ware volume control btns are tapped.
		_alertDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		_alertDialog.show();

	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		// super function not activated as the user is not logged in.
		showGPSFailedAlert(GPSFailedConditions.FIND_LOCALITY_API_FAILED);
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		// super function not activated as the user is not logged in.
		showGPSFailedAlert(GPSFailedConditions.FIND_LOCALITY_API_FAILED);
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		// super function not activated as the user is not logged in.
		showGPSFailedAlert(GPSFailedConditions.FIND_LOCALITY_API_FAILED);
	}

	@Override
	public void onNullResponseRecieved() {
		// super function not activated as the user is not logged in.
		showGPSFailedAlert(GPSFailedConditions.FIND_LOCALITY_API_FAILED);
	}

	@Override
	public void positionUpdateSuccessfull(double latitude, double longitude) {
		// TODO Auto-generated method stub

	}

	@Override
	public void positionUpdateFailed(String errorMessage) {
		// TODO Auto-generated method stub

	}

}
