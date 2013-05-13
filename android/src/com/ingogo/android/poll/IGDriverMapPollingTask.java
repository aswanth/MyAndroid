package com.ingogo.android.poll;

import java.util.Map;

import android.os.Handler;
import android.widget.Toast;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.IGMapInfoApi;
import com.ingogo.android.webservices.beans.response.IGMapInfoResponseBean;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGMapInfoApiListener;

/**
 * Polling Task for Driver map.
 * 
 * @author vineeth
 * 
 */
public class IGDriverMapPollingTask implements IGMapInfoApiListener,
		IGExceptionApiListener {
	private IGMapInfoApiListener _mapListener;
	private IGMapInfoApi _mapInfoApi = null;
	private Runnable _mapPoller;
	private Handler _handler = new Handler();
	private int _pollInterval = 0;
	String _jobID;

	/**
	 * Constuctor
	 * 
	 * @param jobId
	 * @param listener
	 */
	public IGDriverMapPollingTask(String jobId, IGMapInfoApiListener listener) {
		this._jobID = jobId;
		this._mapListener = listener;
		this.setupPollingProcess();
	}

	/**
	 * For setting up the polling process.
	 * 
	 */
	private void setupPollingProcess() {
		_mapPoller = null;
		_mapPoller = new Runnable() {

			@Override
			public void run() {
				if (_mapInfoApi == null) {
					_mapInfoApi = new IGMapInfoApi(IGDriverMapPollingTask.this,
							IGDriverMapPollingTask.this);

				}
				_mapInfoApi.getMapInfo(_jobID);
				_handler.postDelayed(this, _pollInterval); // continue polling

			}
		};

	}

	/**
	 * Starts the polling process.
	 * Requires polling interval as parameter.
	 * @param interval
	 */
	public void startPolling(int interval) {
		_pollInterval = interval;
		_handler.postDelayed(_mapPoller, 0); // Start polling now
	}

	/**
	 * Stops the polling process.
	 */
	public void stopPolling() {
		if(_mapPoller != null && _handler != null)
			_handler.removeCallbacks(_mapPoller);
		_mapPoller = null;
		_handler = null;
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

	}

	@Override
	public void mapInfoCompleted(IGMapInfoResponseBean mapInfo) {
		_mapListener.mapInfoCompleted(mapInfo);

	}

	@Override
	public void mapInfoFailed(String errorMessage) {
		Toast.makeText(
				IngogoApp.getSharedApplication().getApplicationContext(),
				errorMessage, Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onAuthenticationErrorResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		
	}

}
