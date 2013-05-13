/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a request to the server to cancel a job.
 */

package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGCancelJobRequestBean;
import com.ingogo.android.webservices.beans.response.IGCancelJobResponseBean;
import com.ingogo.android.webservices.beans.response.IGCollectJobResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGCancelJobListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;



public class IGCancelJobApi extends IGBaseApi implements IGApiInterface, IGApiListener {

	private IGCancelJobListener _listener;
	
	public IGCancelJobApi(IGCancelJobListener _listener, IGExceptionApiListener excptnListener) {
		super();
		this._listener = _listener;
		this._excptnListener = excptnListener;
	}

	public void cancelJob(String cancellationReason, String bookingID) { // GET req.

		String mobileNumber = IngogoApp.getSharedApplication().getUserId();
		String password = IngogoApp.getSharedApplication().getPassword();
		
		IGCancelJobRequestBean requestBean = new IGCancelJobRequestBean(mobileNumber, password, bookingID, cancellationReason);

		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGCancelJobResponseBean.class,
				requestBean.toJsonString(), this);
	}
	
	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub
		Log.e("Cancel Job SUCCESSFUL RESPONSE", response.toString());
		IGCancelJobResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGCancelJobResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			_listener.jobCancelledSuccessfully(respBean);

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGCancelJobResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.jobCancellationFailed(respBean.getResponseMessages()
						.errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		Log.e("Cancel Job FAILURE RESPONSE", errorResponse.toString());
		IGCancelJobResponseBean respBean;
		respBean = (IGCancelJobResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.jobCancellationFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kCancelJobApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
