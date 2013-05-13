/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a message to the server from the driver.
 */

package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGSendMessageRequestBean;
import com.ingogo.android.webservices.beans.response.IGBaseResponseBean;
import com.ingogo.android.webservices.beans.response.IGContactInfoResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGSendMessageApiListener;

public class IGSendMessageApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	private IGSendMessageApiListener _listener;
	private String _content;
	private int _chatIndex;

	public IGSendMessageApi(IGSendMessageApiListener listener,
			IGExceptionApiListener exceptionListener) {
		this._listener = listener;
		this._excptnListener = exceptionListener;
	}

	public void sendMessage(String bookingId, String content, int chatIndex) {
		this._content = content;
		this._chatIndex = chatIndex;
		IGSendMessageRequestBean requestBean = new IGSendMessageRequestBean();
		requestBean.setBookingId(bookingId);
		requestBean.setContent(content);
		requestBean.setMobileNumber(IngogoApp.getSharedApplication().getUserId());
		requestBean.setPassword(IngogoApp.getSharedApplication().getPassword());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGBaseResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.d("sendMessage RESPONSE", response.toString());
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			_listener.messageSent();
		} else {
			_listener.messageSentingFailed(_content, _chatIndex);
		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.i("Chat Failed","Chat Failed");
		_listener.messageSentingFailed(_content, _chatIndex);
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kSendMessageApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
