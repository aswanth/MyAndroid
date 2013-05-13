package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGCreditDetailsApi {

	private IGResponseListener _responseListener;
	private int _jobID;

	public IGCreditDetailsApi(IGResponseListener callingActivity, int jobID) {
		this._responseListener = callingActivity;
		this._jobID = jobID;
	}

	public void getCreditDetails() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kCreditDetailsApiURL;

		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
				new Handler(), new IGCallbackWrapper(_responseListener),
				apiUrl, IGApiConstants.kCreditDetailsWebServiceId);

		webservice.setAuthorizationHeader(IngogoApp.getSharedApplication()
				.getUserId()
				+ ":"
				+ IngogoApp.getSharedApplication().getAccessToken());

		webservice.start();
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
}
