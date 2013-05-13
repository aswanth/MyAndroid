/*
 * Package Name : com.ingogo.android.poll
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : A polling mechanism to periodically poll driver location and send it to the server.
 */

package com.ingogo.android.poll;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGAvailableApi;
import com.ingogo.android.webservices.IGResponseListener;
import com.ingogo.android.webservices.IGUpdateCurrentPositionApi;
import com.ingogo.android.webservices.interfaces.IGUpdateCurrentPositionUpdateListener;

public class IGUpdatePositionPollingTask implements
		IGUpdateCurrentPositionUpdateListener, IGResponseListener {

	private static IGUpdatePositionListener _positionListener = null;
	private IGUpdateCurrentPositionApi _positionApi = null;
	public static Runnable _positionPoller;
	public static Handler _handler = new Handler();

	private static double _curLatitude = 0.0;
	private static double _curLongitude = 0.0;

	public static boolean ignoreStaleState = false;

	private long _pollInterval = 0;
	private boolean _addNotification = true;

	public static boolean isIgnoreStaleState() {
		return ignoreStaleState;
	}

	public static void setIgnoreStaleState(boolean ignoreStaleState) {
		IGUpdatePositionPollingTask.ignoreStaleState = ignoreStaleState;
	}

	public IGUpdatePositionPollingTask(IGUpdatePositionListener positionListener) {
		IGUpdatePositionPollingTask._positionListener = positionListener;
	}
	
	public void setListner(IGUpdatePositionListener positionListener) {
		IGUpdatePositionPollingTask._positionListener = positionListener;
	}

	static final String currentPositionPollingEvent = "Current Position Polling Task";

	/**
	 * Clears the lat and long values stored
	 */
	private static void clearPreviousLocationDetails() {
		_curLatitude = 0.0;
		_curLongitude = 0.0;
	}

	public void startPolling() {
		IGUtility.logRandomDetailsToAnalytics(currentPositionPollingEvent,
				"Start Polling");

		clearPreviousLocationDetails();
		if (_handler == null)
			_handler = new Handler();
		_handler.postDelayed(_positionPoller, 0);
	}

	public void stopPolling() {
		IGUtility.logRandomDetailsToAnalytics(currentPositionPollingEvent,
				"Stop Polling");
		if (_handler != null) {
			_handler.removeCallbacks(_positionPoller);

			_handler = null;

		}
		if (_positionPoller != null) {
			_positionPoller = null;
		}
	}

	public static void initialFixObtained() {
		_curLatitude = IGLocationListener.getCurrentLatitude();
		_curLongitude = IGLocationListener.getCurrentLongitude();
		if (_positionListener != null) {
			_positionListener.initialFixObtained(_curLatitude, _curLongitude);
		}
	}

	public void setupPollingProcess() {

		_pollInterval = IngogoApp.getSharedApplication()
				.getBroadcastPositionInterval() * 1000;
		_positionPoller = null;
		Log.w("STATUS", "setupPollingProcess");
		_positionPoller = new Runnable() {

			@Override
			public void run() {
				if (_positionApi == null && _handler != null) {
					_positionApi = new IGUpdateCurrentPositionApi(
							IGUpdatePositionPollingTask.this);

				}
				_curLatitude = IGLocationListener.getCurrentLatitude();
				_curLongitude = IGLocationListener.getCurrentLongitude();
				Log.i("LATITUDE", "" + _curLatitude);
				Log.i("LONGITUDE", "" + _curLongitude);

				if ((_curLatitude != 0.0) && (_curLongitude != 0.0)) {

					_positionApi.updatePosition(_curLatitude, _curLongitude);

				}
				if (_handler != null) {
					IGUtility.logRandomDetailsToAnalytics(
							currentPositionPollingEvent,
							"Continue Polling after delay : " + _pollInterval);
					_handler.postDelayed(this, _pollInterval); // continue
																// polling
				}

			}
		};

	}

	public Activity getCallingActivity() {
		return (Activity) _positionListener;
	}

	private void processIncomingResponse() {
		_positionListener
				.positionUpdateSuccessfull(_curLatitude, _curLongitude);
	}

	// @Override
	// public void onResponseReceived(Map<String, Object> response, int apiID) {
	//
	// /**
	// * If the user is not logged in when the response is received then do
	// * not process the response
	// */
	// if (!(IngogoApp.getSharedApplication().isLoggedIn())
	// || (IngogoApp.getSharedApplication().getUserId() == null)) {
	// return;
	// }
	// /**
	// * If the initial location is not got then do not process the response.
	// */
	// if (!IGLocationListener.isInitialFixObtained()) {
	// return;
	// }
	// if (response != null && response.containsKey(IGConstants.kDataKey)) {
	// Log.e("RESP FOR UPDATE POSITION", "" + response);
	// JSONObject msgDetailsObj = (JSONObject) response
	// .get(IGConstants.kDataKey);
	// if (msgDetailsObj != null) {
	// Log.i("STATUS-------", "-----UPDATE RESP RECIEVED--" + response);
	// try {
	// String driverStatus = msgDetailsObj
	// .getString(IGConstants.kDriverAvailability);
	//
	// String activeJob = IGUtility.getDefaults(
	// IGConstants.kJobInProgress, IngogoApp
	// .getSharedApplication()
	// .getApplicationContext());
	// if (msgDetailsObj.has(IGConstants.kStaleTime)) {
	// int iStatleTime = 0;
	// if (!(msgDetailsObj.equals(IGConstants.kNull))
	// && !msgDetailsObj
	// .isNull(IGConstants.kStaleTime)) {
	// Log.i("staleTime",
	// ""
	// + msgDetailsObj
	// .getInt(IGConstants.kStaleTime));
	//
	// iStatleTime = Math.round(((float) msgDetailsObj
	// .getInt(IGConstants.kStaleTime) / 60));
	// }
	// String staleTimeString = String.valueOf(iStatleTime);
	// Log.i("staleTime in minutes", staleTimeString);
	// IGUtility.setDefaults(IGConstants.kStaleTime,
	// staleTimeString, IngogoApp
	// .getSharedApplication()
	// .getApplicationContext());
	//
	// }
	// Log.e("IgnoreStaleState Flag", "" + ignoreStaleState);
	// if ((activeJob == null)
	// && (driverStatus.equals(IGApiConstants.kStaleState))) {
	//
	// // If ignoreStaleState flag == true then stale
	// // conditon
	// // is not activated.
	//
	// if (ignoreStaleState == false) {
	// /**
	// * The notification needs to be shown only once. So
	// * after adding a notification the _addNotification
	// * flag is set to false. If the status is already
	// * busy then the notification is not needed, So the
	// * status of the available flag is also checked.
	// */
	//
	// if (_addNotification && IngogoApp.getAvailable()) {
	// _addNotification = false;
	//
	// Log.i("STATUS",
	// "DRIVER UNAVAILABLE IN POSITION POLLING RESP");
	// Intent intent = new Intent(IngogoApp
	// .getSharedApplication()
	// .getApplicationContext(),
	// IGJobsActivity.class);
	// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// IngogoApp.setAvailable(false);
	// IGJobsActivity.setshowNotification(true);
	// IngogoApp.getSharedApplication()
	// .getApplicationContext()
	// .startActivity(intent);
	//
	// } else {
	// _addNotification = true;
	// }
	//
	// } else {
	// IGAvailableApi availableApi = new IGAvailableApi(
	// IGUpdatePositionPollingTask.this);
	// availableApi.setAvailable();
	// }
	// } else {
	// _addNotification = true;
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	//
	// this.processIncomingResponse();
	// }
	// }
	// }

	// @Override
	// public void onFailedToGetResponse(Map<String, Object> errorResponse,
	// int apiID) {
	//
	// }

	@Override
	public void successfullyUpdatedCurrentPosition(String driverStatus,
			int staleTime) {
		/**
		 * If the user is not logged in when the response is received then do
		 * not process the response
		 */
		if (!(IngogoApp.getSharedApplication().isLoggedIn())
				|| (IngogoApp.getSharedApplication().getUserId() == null)) {
			return;
		}
		/**
		 * If the initial location is not got then do not process the response.
		 */
		if (!IGLocationListener.isInitialFixObtained()) {
			return;
		}

		String activeJob = IGUtility.getDefaults(IGConstants.kJobInProgress,
				IngogoApp.getSharedApplication().getApplicationContext());
		int iStatleTime = 0;

		iStatleTime = Math.round(((float) staleTime / 60));

		String staleTimeString = String.valueOf(iStatleTime);
		Log.i("staleTime in minutes", staleTimeString);
		IGUtility.setDefaults(IGConstants.kStaleTime, staleTimeString,
				IngogoApp.getSharedApplication().getApplicationContext());

		Log.e("IgnoreStaleState Flag", "" + ignoreStaleState);
		if ((activeJob == null)
				&& (driverStatus.equals(IGApiConstants.kStaleState))) {

			// If ignoreStaleState flag == true then stale
			// conditon
			// is not activated.

			if (ignoreStaleState == false) {
				/**
				 * The notification needs to be shown only once. So after adding
				 * a notification the _addNotification flag is set to false. If
				 * the status is already busy then the notification is not
				 * needed, So the status of the available flag is also checked.
				 */

				if (_addNotification && IngogoApp.getAvailable()) {
					_addNotification = false;

					Log.i("STATUS",
							"DRIVER UNAVAILABLE IN POSITION POLLING RESP");
					Intent intent = new Intent(IngogoApp.getSharedApplication()
							.getApplicationContext(), IGJobsActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					IngogoApp.setAvailable(false);
					IGJobsActivity.setshowNotification(true);
					IngogoApp.getSharedApplication().getApplicationContext()
							.startActivity(intent);

				} else {
					_addNotification = true;
				}

			} else {
				IGAvailableApi availableApi = new IGAvailableApi(
						IGUpdatePositionPollingTask.this);
				availableApi.setAvailable();
			}
		} else {
			_addNotification = true;
		}

		this.processIncomingResponse();

	}

	@Override
	public void failToUpdateCurrentPosition() {

	}

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		
	}
}
