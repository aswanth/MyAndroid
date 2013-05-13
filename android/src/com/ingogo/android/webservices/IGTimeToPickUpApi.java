package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGTimeToPickUpRequestBean;
import com.ingogo.android.webservices.beans.response.IGTimeToPickUpResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGTimeToPickUpApiListener;

public class IGTimeToPickUpApi extends IGBaseApi implements IGApiInterface, IGApiListener{
	private IGTimeToPickUpApiListener _listener;
	
	
	public IGTimeToPickUpApi(IGTimeToPickUpApiListener callingActivity, IGExceptionApiListener exceptionListener) {
		this._listener = callingActivity;
		this._excptnListener = exceptionListener;
	}
	public void getTimeToPickUp(String bookingId, String timeToPickUp, String messageType ) { // GET req.


		String mobileNumber = IngogoApp.getSharedApplication().getUserId();
		String password = IngogoApp.getSharedApplication().getPassword();
		
		IGTimeToPickUpRequestBean requestBean = new IGTimeToPickUpRequestBean(mobileNumber, password, bookingId, timeToPickUp, messageType);
		
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGTimeToPickUpResponseBean.class,
				requestBean.toJsonString(), this);
		
	
	}

	public void onResponseReceived(Map<String, Object> response) {
		Log.e("TimeToPickUp SUCCESSFUL RESPONSE", response.toString());
		IGTimeToPickUpResponseBean responseBean;
		if(response.containsKey(IGApiConstants.kSuccessMsgKey)){
			responseBean =(IGTimeToPickUpResponseBean)response.get(IGApiConstants.kSuccessMsgKey);
			_listener.timeToPickUpApiCompleted(responseBean);
		}else if(response.containsKey(IGApiConstants.kApiFailedMsgKey)){
			responseBean = (IGTimeToPickUpResponseBean)response.get(IGApiConstants.kApiFailedMsgKey);
			if(responseBean!= null){
			_listener.timeToPickUpApiFailed(responseBean.getResponseMessages().errorMessagesToString());
			}
			
		}
		
	}
	
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("TimeToPickUp FAILURE RESPONSE", errorResponse.toString());
		IGTimeToPickUpResponseBean responseBean;
		responseBean = (IGTimeToPickUpResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (responseBean != null) {
			_listener.timeToPickUpApiFailed(responseBean.getResponseMessages()
					.errorMessagesToString());
		} else {

			super.detectErrorMessage(errorResponse);
		}
	}
	
	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kTimeToPickUpApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}
}
