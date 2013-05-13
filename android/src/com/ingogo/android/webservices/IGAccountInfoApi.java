package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGAccountInfoApi {

	private IGResponseListener _responseListener;

	public IGAccountInfoApi(IGResponseListener callingActivity) {
		this._responseListener = callingActivity;
	}

	public void getAccountDetails() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kAccountInfoApiURL;
		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
				new Handler(), new IGCallbackWrapper(_responseListener),
				apiUrl, IGApiConstants.kAccountInfoWebServiceId);
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

		Log.d("ACCOUNT INFO JOBJ", jObject.toString());
		return jObject.toString();

	}
}
