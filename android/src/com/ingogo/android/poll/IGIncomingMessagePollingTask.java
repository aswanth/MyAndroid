/*
 * Package Name : com.ingogo.android.poll
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : A polling mechanism to periodically poll incoming chat messages from the server.
 */

package com.ingogo.android.poll;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.model.IGIncomingMessageModel;
import com.ingogo.android.webservices.IGIncomingMessageApi;
import com.ingogo.android.webservices.IGResponseListener;
import com.ingogo.android.webservices.interfaces.IGIncomingMessageApiListener;

public class IGIncomingMessagePollingTask implements
		IGIncomingMessageApiListener {
	private int _jobID;
	private IGIncomingMessageListener _msgListener;
	private IGIncomingMessageApi _incomingMessageApi = null;
	private Runnable _messagePoller;
	private Handler _handler = new Handler();
	private int _pollInterval = 0;

	public IGIncomingMessagePollingTask(IGIncomingMessageListener msgListener,
			int jobID) {
		this._jobID = jobID;
		this._msgListener = msgListener;
		this.setupPollingProcess();

	}

	public void startPolling(int interval) {
		_pollInterval = interval;
		_handler.postDelayed(_messagePoller, 0); // Start polling now
	}

	public void stopPolling() {
		_handler.removeCallbacks(_messagePoller);
		_messagePoller = null;
		_handler = null;
	}

	public IGIncomingMessageListener getListener() {
		return _msgListener;
	}

	private void setupPollingProcess() {
		_messagePoller = null;
		_messagePoller = new Runnable() {

			@Override
			public void run() {
				if (_incomingMessageApi == null) {
					_incomingMessageApi = new IGIncomingMessageApi(
							IGIncomingMessagePollingTask.this, _jobID);

				}
				_incomingMessageApi.receive();
				_handler.postDelayed(this, _pollInterval); // continue polling

			}
		};

	}

	private void processIncomingResponse(
			ArrayList<IGIncomingMessageModel> messages, String bookingStatus) {
		_msgListener.bookingStatusReceived(bookingStatus);
		if (messages != null && messages.size() > 0) {
			boolean playNewChatAlert = true;
			for (int i = 0; i < messages.size(); i++) {
				IGIncomingMessageModel msgObj = messages.get(i);
				_msgListener.incomingMessageReceived(msgObj.getContent(),
						playNewChatAlert);
				playNewChatAlert = false;
			}
		}

	}

	// @Override
	// public void onResponseReceived(Map<String, Object> response, int apiID) {
	// if (response != null && response.containsKey(IGConstants.kDataKey)) {
	// JSONObject msgDetailsObj = (JSONObject) response
	// .get(IGConstants.kDataKey);
	// this.processIncomingResponse(msgDetailsObj);
	// }
	// }
	//
	// @Override
	// public void onFailedToGetResponse(Map<String, Object> errorResponse,
	// int apiID) {
	//
	// }

	@Override
	public void successfullyGetIncomingMessage(
			ArrayList<IGIncomingMessageModel> messages, String bookingStatus) {
		this.processIncomingResponse(messages, bookingStatus);
	}

	@Override
	public void failedToGetIncomingMessage() {

	}

}
