package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGNoShowApi {
	private IGResponseListener _responseListener;
	private int _jobID;

	public IGNoShowApi(IGResponseListener callingActivity, int jobId) {
		this._responseListener = callingActivity;
		this._jobID = jobId;
	}

	public void noShow() { // GET req.

		String apiUrl = null;
		apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kNoShowApiURL;

		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
				new Handler(), new IGCallbackWrapper(_responseListener),
				apiUrl, IGApiConstants.kNoShowWebServiceId);

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
			if ((IngogoApp.LATTITUDE == null) || (IngogoApp.LONGITUDE == null)) {
				jObject.put(IGApiConstants.kJSONLatitudeKey, 0.0);
				jObject.put(IGApiConstants.kJSONLongitudeKey, 0.0);
			} else {
				jObject.put(IGApiConstants.kJSONLatitudeKey,
						IngogoApp.LATTITUDE);
				jObject.put(IGApiConstants.kJSONLongitudeKey,
						IngogoApp.LONGITUDE);
			}
			jObject.put(IGApiConstants.kJSONUsernameKey, IngogoApp
					.getSharedApplication().getUserId());
			jObject.put(IGApiConstants.kJSONPasswordKey, IngogoApp
					.getSharedApplication().getPassword());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("JSONREQ - NOSHOW", jObject.toString());
		return jObject.toString();

	}

}
