/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Basic callback wrapper which fires the delegated response from the base web service to the calling activity..
 */

package com.ingogo.android.webservices;

import java.util.HashMap;
import java.util.Map;

public class IGCallbackWrapper implements Runnable {

	private IGResponseListener _callbackActivity;
	private int _apiID;

	Map<String, Object> _response = new HashMap<String, Object>();
	Map<String, Object> _errorResponse = new HashMap<String, Object>();

	public IGCallbackWrapper(IGResponseListener callbackActivity) {
		this._callbackActivity = callbackActivity;
	}

	public void run() {
		if (_response != null && _callbackActivity != null) {
			_callbackActivity.onResponseReceived(_response, _apiID);
		} else if (_callbackActivity != null) {
			_callbackActivity.onFailedToGetResponse(_errorResponse, _apiID);
		}
	}

	public void setResponse(Map<String, Object> responseMap, int apiID) {
		this._response = responseMap;
		this._apiID = apiID;
	}

	public void setErrorResponse(Map<String, Object> responseMap, int apiID) {
		this._response = null;
		this._errorResponse = responseMap;
		this._apiID = apiID;
	}

	public IGResponseListener getResponseListener() {
		return _callbackActivity;
	}

}