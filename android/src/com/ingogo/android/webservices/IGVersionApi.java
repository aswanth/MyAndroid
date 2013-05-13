package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGVersionApi {
	private IGResponseListener _responseListener;

	public IGVersionApi(IGResponseListener callingActivity) {
		this._responseListener = callingActivity;
	}

	public void getLatestAppVersion() {
		// GET req.

		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kVersionApiURL;
		Thread thread = new IGBaseWebService(buildJSONRequest(), new Handler(),
				new IGCallbackWrapper(_responseListener), apiUrl,
				IGApiConstants.kVersionWebServiceId);
		thread.start();

	}

	private String buildJSONRequest() {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put(IGApiConstants.kVersionNumber,
					(float) IngogoApp.getVersionCode());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("JSONREQ", jObject.toString());
		return jObject.toString();

	}
}
