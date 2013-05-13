/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a request to the server to set the collected status.
 */

package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGCollectJobRequestBean;
import com.ingogo.android.webservices.beans.response.IGCollectJobResponseBean;
import com.ingogo.android.webservices.beans.response.IGReferralInformationResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGCollectJobListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

public class IGCollectedJobApi extends IGBaseApi implements IGApiInterface,
IGApiListener {
	private IGCollectJobListener _listener;

	public IGCollectedJobApi(IGCollectJobListener listener,
			IGExceptionApiListener excptnListener) {
		this._listener = listener;
		this._excptnListener = excptnListener;
	}

	public void collectedBooking(String bookingID) { // GET req.

		String mobileNumber = IngogoApp.getSharedApplication().getUserId();
		String password = IngogoApp.getSharedApplication().getPassword();
		String latitude;
		String longitude;
		
		if ((IngogoApp.LATTITUDE == null) || (IngogoApp.LONGITUDE == null)) {
			latitude = "0.0";
			longitude = "0.0";
			
		} else {
			latitude = IngogoApp.LATTITUDE;
			longitude = IngogoApp.LONGITUDE;
		}
	
		IGCollectJobRequestBean requestBean = new IGCollectJobRequestBean(mobileNumber, password, latitude, longitude, bookingID);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGCollectJobResponseBean.class,
				requestBean.toJsonString(), this);

	}
	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub
		Log.e("Collect Job SUCCESSFUL RESPONSE", response.toString());
		IGCollectJobResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGCollectJobResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			_listener.collectedJobSuccessfully(respBean);

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGCollectJobResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.collectJobRequestFailed(respBean.getResponseMessages()
						.errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		Log.e("Collect Job FAILURE RESPONSE", errorResponse.toString());
		IGCollectJobResponseBean respBean;
		respBean = (IGCollectJobResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.collectJobRequestFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kCollectedJobApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}
}
