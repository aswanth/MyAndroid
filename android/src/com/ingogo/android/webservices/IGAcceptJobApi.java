/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a request to the server for accepting a job.
 */

package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.webservices.beans.request.IGAcceptJobRequestBean;
import com.ingogo.android.webservices.beans.response.IGAcceptJobResponseBean;
import com.ingogo.android.webservices.interfaces.IGAcceptJobListener;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

public class IGAcceptJobApi extends IGBaseApi implements IGApiInterface, IGApiListener{
	private  IGAcceptJobListener _listener;
	
	public IGAcceptJobApi(IGAcceptJobListener callingActivity,IGExceptionApiListener exceptionListener ) {
		this._listener = callingActivity;
		this._excptnListener=exceptionListener;
	}

	public void acceptBooking(String bookingId, int bidExtra, int bidInterval) { // GET req.

		String mobileNumber = IngogoApp.getSharedApplication().getUserId();
		String password = IngogoApp.getSharedApplication().getPassword();
		String latitude = IGLocationListener.getCurrentLongitude()+"";
		String longitude = IGLocationListener.getCurrentLongitude()+"";
		IGAcceptJobRequestBean requestBean = new IGAcceptJobRequestBean(mobileNumber, password, longitude, latitude, bookingId, bidInterval, bidExtra);
	
		
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGAcceptJobResponseBean.class,
				requestBean.toJsonString(), this);
		
	}

	public void onResponseReceived(Map<String, Object> response) {
		Log.e("AcceptJob SUCCESSFUL RESPONSE", response.toString());
		IGAcceptJobResponseBean responseBean;
		if(response.containsKey(IGApiConstants.kSuccessMsgKey)){
			responseBean = (IGAcceptJobResponseBean) response.get(IGApiConstants.kSuccessMsgKey);
			_listener.acceptJobCompleted(responseBean);
		}else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)){
			responseBean = (IGAcceptJobResponseBean) response.get(IGApiConstants.kApiFailedMsgKey);
			if(responseBean!=null){
				_listener.acceptJobFailed(responseBean.getResponseMessages().errorMessagesToString());
			}
				
		}
	}
	
	
	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("AcceptJob FAILURE RESPONSE", errorResponse.toString());
		IGAcceptJobResponseBean respBean;
		respBean = (IGAcceptJobResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.acceptJobFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {

			super.detectErrorMessage(errorResponse);
		}
	}
	
	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kAcceptJobApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}
}
