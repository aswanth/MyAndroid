/*
 * Package Name : com.ingogo.android.poll
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : A polling mechanism to periodically poll available jobs from the server.
 */

package com.ingogo.android.poll;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.model.IGJobAvailableModel;
import com.ingogo.android.model.IGJobListModel;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGJobsApi;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGJobAvailableApiListener;

public class IGAvailableJobsPollingTask implements IGJobAvailableApiListener,
		IGExceptionApiListener {
	private IGAvailableJobsListener _jobsListener;
	private IGJobsApi _jobsApi = null;
	private Runnable _jobsPoller;
	private Handler _handler = new Handler();
	private long _pollInterval = 0;
	static final String availableJobsPollingEvent = "Available Jobs Polling Task";

	public IGAvailableJobsPollingTask(IGAvailableJobsListener jobsListener) {
		this._jobsListener = jobsListener;

		this.setupPollingProcess();

	}

	public void startPolling(long interval) {
		IGUtility.logRandomDetailsToAnalytics(availableJobsPollingEvent,
				"Start Polling");
		Log.e("POLLING TASK","Start polling called in Polling Task");
		_pollInterval = interval;
		_handler.removeCallbacks(_jobsPoller);
		_handler.postDelayed(_jobsPoller, 0); // Start polling now
	}

	public void stopPolling() {
		IGUtility.logRandomDetailsToAnalytics(availableJobsPollingEvent,
				"Stop Polling");
		Log.e("POLLING TASK","Stop polling called in Polling Task");

		_handler.removeCallbacks(_jobsPoller);
		_jobsPoller = null;
		_handler = null;
	}

	public Activity getCallingActivity() {
		return (Activity) _jobsListener;
	}

	private void setupPollingProcess() {
		if(_handler!=null) {
			stopPolling();
		}
		 _handler = new Handler();
		_jobsPoller = new Runnable() {

			@Override
			public void run() {
				if (_jobsApi == null) {
					_jobsApi = new IGJobsApi(IGAvailableJobsPollingTask.this,
							IGAvailableJobsPollingTask.this);

				}
				Log.e("POLLING TASK", "Job polling Task Called");
				_jobsApi.getJobs();
				IGUtility.logRandomDetailsToAnalytics(
						availableJobsPollingEvent, "Continue Polling");
				_handler.postDelayed(this, _pollInterval); // continue polling

			}
		};

	}

	/*
	 * @Override public void onResponseReceived(Map<String, Object> response,
	 * int apiID) { Log.i("JOB LIST RESP", "" + response); if (response != null
	 * && response.containsKey(IGConstants.kDataKey)) { JSONObject jobsObj =
	 * (JSONObject) response .get(IGConstants.kDataKey);
	 * this.processIncomingResponse(jobsObj); // This is not required now as
	 * stale state is handled in update // current position api. // TODO: Remove
	 * this. // this.processDriverStatus(jobsObj);
	 * 
	 * } }
	 * 
	 * @Override public void onFailedToGetResponse(Map<String, Object>
	 * errorResponse, int apiID) { Log.i("JOB LIST ERROR RESP", "" +
	 * errorResponse); _jobsListener.availableJobsUpdateFailed("Error"); }
	 */

	@Override
	public void onJobAvailableResponseRecieved(
			ArrayList<IGJobAvailableModel> jobListModel, String message,
			String driverStatus) {
		ArrayList<IGJob> jobList = new IGJobListModel(jobListModel)
				.getAvailablejobs();
		_jobsListener.availableJobsUpdated(jobList);
		_jobsListener.getPreviousJobMessage(message);
		// This is not required now as stale state is handled in update
		// current position api.
		// TODO: Remove this.
		// _jobsListener.getDriverStatus(driverStatus);
	}

	@Override
	public void onJobAvailableResponseFailed() {
		_jobsListener.availableJobsUpdateFailed("Error");
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		Toast.makeText(
				IngogoApp.getSharedApplication().getApplicationContext(),
				(CharSequence) errorResponse
						.get(IGApiConstants.kApiFailedMsgKey),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		Toast.makeText(
				IngogoApp.getSharedApplication().getApplicationContext(),
				(CharSequence) errorResponse
						.get(IGApiConstants.kApiFailedMsgKey),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		Toast.makeText(
				IngogoApp.getSharedApplication().getApplicationContext(),
				(CharSequence) errorResponse
						.get(IGApiConstants.kApiFailedMsgKey),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNullResponseRecieved() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAuthenticationErrorResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		
	}
}
