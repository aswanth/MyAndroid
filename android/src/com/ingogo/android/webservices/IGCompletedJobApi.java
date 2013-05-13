/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a request to the server to set the collected status.
 */

package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGCompletedJobApi {
	private IGResponseListener _responseListener;
	private int _jobID;

	public IGCompletedJobApi(IGResponseListener callingActivity, int jobID) {
		this._responseListener = callingActivity;
		this._jobID = jobID;
	}

	public void completed() { // GET req.

		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kCompletedJobApiURL;

		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
				new Handler(), new IGCallbackWrapper(_responseListener),
				apiUrl, IGApiConstants.kCompletedJobWebServiceId);
		webservice.setAuthorizationHeader(IngogoApp.getSharedApplication()
				.getUserId()
				+ ":"
				+ IngogoApp.getSharedApplication().getAccessToken());

		webservice.start();

	}

	private String buildJSONRequest() {
		JSONObject jObject = new JSONObject();
		try {
			if ((IngogoApp.LATTITUDE == null) || (IngogoApp.LONGITUDE == null)) {
				jObject.put(IGApiConstants.kJSONLatitudeKey, 0.0);
				jObject.put(IGApiConstants.kJSONLongitudeKey, 0.0);
			} else {
				jObject.put(IGApiConstants.kJSONLatitudeKey,
						IngogoApp.LATTITUDE);
				jObject.put(IGApiConstants.kJSONLongitudeKey,
						IngogoApp.LONGITUDE);
			}
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

}
