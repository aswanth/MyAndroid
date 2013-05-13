package com.ingogo.android.webservices;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGCurrentPositionUpdateRequestBean;
import com.ingogo.android.webservices.beans.request.IGDriverLocalityRequestBean;
import com.ingogo.android.webservices.beans.response.IGCurrentPositionUpdateResponseBean;
import com.ingogo.android.webservices.beans.response.IGDriverLocalityResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGUpdateCurrentPositionUpdateListener;

public class IGUpdateCurrentPositionApi extends IGBaseApi implements
		IGApiInterface, IGApiListener {
	// private IGResponseListener _responseListener;
	private IGUpdateCurrentPositionUpdateListener _listner;

	public IGUpdateCurrentPositionApi(
			IGUpdateCurrentPositionUpdateListener listner) {
		this._listner = listner;
	}

	public void updatePosition(double latitude, double longitude) { // GET req.

		Log.i("STATUS-----------", "POSITION UPDATE API");
		IGCurrentPositionUpdateRequestBean requestBean = new IGCurrentPositionUpdateRequestBean(
				latitude, longitude);
		IGBaseWebservice2 webservice2 = new IGBaseWebservice2(
				IGUpdateCurrentPositionApi.this, requestBean.toJsonString(),
				IGCurrentPositionUpdateResponseBean.class);
		webservice2.execute(buildURL());
		// String apiUrl = null;
		//
		// apiUrl = IGApiConstants.kIngogoBaseURL
		// + IGApiConstants.kUpdateCurrentPositionApiURL;
		// //This web service called only when user logged in.
		// if (IngogoApp.getSharedApplication().getUserId() != null
		// && IngogoApp.getSharedApplication().getPassword() != null) {
		// IGBaseWebService webservice = new IGBaseWebService(
		// buildJSONRequest(latitude, longitude), new Handler(),
		// new IGCallbackWrapper(_responseListener), apiUrl,
		// IGApiConstants.kUpdateCurrentPositionWebServiceId);
		//
		// webservice.setAuthorizationHeader(IngogoApp.getSharedApplication()
		// .getUserId()
		// + ":"
		// + IngogoApp.getSharedApplication().getAccessToken());
		//
		// webservice.start();
	}

	private String buildJSONRequest(double latitude, double longitude) {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put(IGApiConstants.kJSONLongitudeKey, longitude);
			jObject.put(IGApiConstants.kJSONLatitudeKey, latitude);
			jObject.put(IGApiConstants.kJSONUsernameKey, IngogoApp
					.getSharedApplication().getUserId());
			jObject.put(IGApiConstants.kJSONPasswordKey, IngogoApp
					.getSharedApplication().getPassword());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d(" Update position JSON JSONREQ", jObject.toString());
		return jObject.toString();

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		IGCurrentPositionUpdateResponseBean respBean = null;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGCurrentPositionUpdateResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			if (respBean != null) {
				_listner.successfullyUpdatedCurrentPosition(
						respBean.getDriverStatus(), respBean.getStaleTime());
				return;
			}
			_listner.failToUpdateCurrentPosition();
			return;
		}
		_listner.failToUpdateCurrentPosition();
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		_listner.failToUpdateCurrentPosition();
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kUpdateCurrentPositionApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
