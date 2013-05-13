package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.webservices.beans.request.IGGetPaidAtRequestBean;
import com.ingogo.android.webservices.beans.response.IGFindAccountResponseBean;
import com.ingogo.android.webservices.beans.response.IGGetPaidAtResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGGetPaidAtApiListener;

public class IGGetPaidAtApi extends IGBaseApi implements
			IGApiInterface, IGApiListener {
	IGGetPaidAtApiListener _listener;
	
	
	public IGGetPaidAtApi(
			IGGetPaidAtApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;

	}
	
	public void retrieveSuburb() {
		String lat = "-33.867387"; String lon="151.207629";
		IGGetPaidAtRequestBean requestBean = new IGGetPaidAtRequestBean(IngogoApp.getSharedApplication().getUserId(), IngogoApp
				.getSharedApplication().getPassword(),
				IGLocationListener.getCurrentLongitude() + "",
				IGLocationListener.getCurrentLatitude() + "");
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGGetPaidAtResponseBean.class,
				requestBean.toJsonString(), this);
		

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.e("GET PAID AT SUCCESSFUL RESPONSE", response.toString());
		IGGetPaidAtResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGGetPaidAtResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			
			_listener.retrieveSuburbCompleted(respBean);

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGGetPaidAtResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.retrieveSuburbFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("GET PAID AT FAILURE RESPONSE", errorResponse.toString());
		IGGetPaidAtResponseBean respBean;
		respBean = (IGGetPaidAtResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.retrieveSuburbFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
					
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kGetPaidAtUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}
	
}
