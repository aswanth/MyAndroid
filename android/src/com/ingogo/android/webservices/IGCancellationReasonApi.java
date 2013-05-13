package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGCancellationReasonApi {
	
	private IGResponseListener _responseListener;

	public IGCancellationReasonApi(IGResponseListener callingActivity) {
		this._responseListener = callingActivity;
	}

	public void cancellationReason() { // GET req.

		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kCancellationReasonApiURL;

		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
				new Handler(), new IGCallbackWrapper(_responseListener),
				apiUrl, IGApiConstants.kCancellationReasonWebServiceId);

		webservice.setAuthorizationHeader(IngogoApp.getSharedApplication()
				.getUserId()
				+ ":"
				+ IngogoApp.getSharedApplication().getAccessToken());

		webservice.start();

	}

	private String buildJSONRequest() {
		JSONObject jObject = new JSONObject();
		try {
			
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
