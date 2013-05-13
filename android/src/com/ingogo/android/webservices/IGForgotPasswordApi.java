package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;

public class IGForgotPasswordApi {
	private IGResponseListener _responseListener;
	private String _userId;

	public IGForgotPasswordApi(IGResponseListener callbackObject, String userId) {
		// userId
		// is
		// the
		// mobile
		// number.
		this._responseListener = callbackObject;
		this._userId = userId;
	}

	public void requestNewPassword() { // GET req.

		String apiUrl = null;
		apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kForgotPasswordApiURL;

		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
				new Handler(), new IGCallbackWrapper(_responseListener),
				apiUrl, IGApiConstants.kForgotPasswordWebServiceId);

		webservice.start();

	}

	private String buildJSONRequest() {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put(IGApiConstants.kJSONUsernameKey, _userId);
			jObject.put(IGApiConstants.kDevice, IGApiConstants.kAndroidDevice);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("JSONREQ", jObject.toString());
		return jObject.toString();

	}
}
