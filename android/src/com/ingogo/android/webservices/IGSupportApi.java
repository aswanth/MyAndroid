/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a request to the server to fetch help related information.
 */

package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGSupportApi {
	private IGResponseListener _responseListener;

	public IGSupportApi(IGResponseListener callingActivity) {
		this._responseListener = callingActivity;
	}

	public void getSupport() {
		// GET req.

		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kSupportApiURL;
		Thread thread = new IGBaseWebService(buildJSONRequest(), new Handler(),
				new IGCallbackWrapper(_responseListener), apiUrl,
				IGApiConstants.kSupportWebServiceId);
		thread.start();

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
