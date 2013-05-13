/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a request to the server to fetch any incoming messages from the passenger.
 */

package com.ingogo.android.webservices;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGIncomingChatRequestBean;
import com.ingogo.android.webservices.beans.response.IGCurrentPositionUpdateResponseBean;
import com.ingogo.android.webservices.beans.response.IGIncomingChatResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGIncomingMessageApiListener;

public class IGIncomingMessageApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {
	//private IGResponseListener _responseListener;
	private int _jobID;
	private IGIncomingMessageApiListener _listener;

	public IGIncomingMessageApi(IGIncomingMessageApiListener listener, int jobId) {
		this._listener = listener;
		this._jobID = jobId;
	}

	public void receive() { // GET req.
		
		IGIncomingChatRequestBean requestBean = new IGIncomingChatRequestBean(_jobID);
		IGBaseWebservice2 webService2 = new IGBaseWebservice2(this, requestBean.toJsonString(), IGIncomingChatResponseBean.class);
		webService2.execute(buildURL());

//		String apiUrl = null;
//
//		apiUrl = IGApiConstants.kIngogoBaseURL
//				+ IGApiConstants.kIncomingMessageApiURL;
//
//		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
//				new Handler(), new IGCallbackWrapper(_responseListener),
//				apiUrl, IGApiConstants.kIncomingMessageWebServiceId);
//
//		webservice.setAuthorizationHeader(IngogoApp.getSharedApplication()
//				.getUserId()
//				+ ":"
//				+ IngogoApp.getSharedApplication().getAccessToken());
//
//		webservice.start();

	}

	private String buildJSONRequest() {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put(IGApiConstants.kJSONBookingIdKey, _jobID);
			jObject.put(IGApiConstants.kJSONUsernameKey, IngogoApp
					.getSharedApplication().getUserId());
			jObject.put(IGApiConstants.kJSONPasswordKey, IngogoApp
					.getSharedApplication().getPassword());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("JSONREQ", jObject.toString());
		return jObject.toString();

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		IGIncomingChatResponseBean respBean = null;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGIncomingChatResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			if (respBean != null) {
				_listener.successfullyGetIncomingMessage(respBean.getMessages(), respBean.getBookingStatus());
				return;
			}
			_listener.failedToGetIncomingMessage();
			return;
		}
		_listener.failedToGetIncomingMessage();

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		_listener.failedToGetIncomingMessage();

	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kIncomingMessageApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
