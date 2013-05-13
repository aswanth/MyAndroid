package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;

public class IGResetPasswordApi {
	private IGResponseListener _responseListener;

	public IGResetPasswordApi(IGResponseListener callbackObject) {
		// userId is
		// the
		// mobile
		// number.
		this._responseListener = callbackObject;
	}

	public void requestNewPassword(String passphrase, String newPassword,
			String userId) {
		// GET req.

		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kResetPasswordApiURL;

		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(
				passphrase, newPassword, userId), new Handler(),
				new IGCallbackWrapper(_responseListener), apiUrl,
				IGApiConstants.kResetPasswordWebServiceId);

		webservice.start();

	}

	private String buildJSONRequest(String passphrase, String newPassword,
			String userId) {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put(IGApiConstants.kJSONPassphraseKey, passphrase);
			jObject.put(IGApiConstants.kJSONUsernameKey, userId);
			jObject.put(IGApiConstants.kJSONPasswordKey, newPassword);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("JSONREQ", jObject.toString());
		return jObject.toString();

	}

}
