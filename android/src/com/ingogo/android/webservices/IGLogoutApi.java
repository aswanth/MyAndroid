package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGLogoutApi {
	private IGResponseListener _responseListener;

	public IGLogoutApi(IGResponseListener callingActivity) {
		this._responseListener = callingActivity;
	}

	public void logout(String username, String password) { // GET req.

		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kLogoutApiURL;

		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(
				username, password), new Handler(), new IGCallbackWrapper(
				_responseListener), apiUrl, IGApiConstants.kLogoutWebServiceId);

		webservice.setAuthorizationHeader(IngogoApp.getSharedApplication()
				.getUserId()
				+ ":"
				+ IngogoApp.getSharedApplication().getAccessToken());

		webservice.start();

	}

	private String buildJSONRequest(String username, String password) {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put(IGApiConstants.kJSONUsernameKey, username);
			jObject.put(IGApiConstants.kJSONPasswordKey, password);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("JSONREQ", jObject.toString());
		return jObject.toString();

	}
}
