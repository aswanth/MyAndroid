package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGSwipeInititaliseRequestBean;
import com.ingogo.android.webservices.beans.response.IGBaseResponseBean;
import com.ingogo.android.webservices.beans.response.IGSwipeInititaliseResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGSwipeInititaliseApiListener;

public class IGSwipeInititaliseApi extends IGBaseApi implements IGApiInterface,
IGApiListener {
	private IGSwipeInititaliseApiListener _listener;
	
	public IGSwipeInititaliseApi(IGSwipeInititaliseApiListener listener,
			IGExceptionApiListener excptnListener) {
		this._listener = listener;
		this._excptnListener = excptnListener;
	}
	public void initialiseSwipe(String passengerid) {
		IGSwipeInititaliseRequestBean requestBean =  new IGSwipeInititaliseRequestBean(IngogoApp.getSharedApplication().getUserId(), IngogoApp.getSharedApplication().getPassword(), passengerid);
		Log.e("Test",requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGSwipeInititaliseResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.i("SWIPE INITIIALISE SUCCESSFUL RESPONSE", response.toString());
		IGSwipeInititaliseResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGSwipeInititaliseResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			_listener.swipeInitialiseCompleted(respBean);

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGSwipeInititaliseResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.swipeInitialiseFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}

		
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		Log.i("SWIPE INITIIALISE FAILURE RESPONSE", errorResponse.toString());
		IGSwipeInititaliseResponseBean respBean;
		respBean = (IGSwipeInititaliseResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.swipeInitialiseFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {
		//	this._excptnListener.onNullResponseRecieved();
			super.detectErrorMessage(errorResponse);

		}
		
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
		+ IGApiConstants.kSwipeInitialiseApiUrl;
		Log.i("SWIPE INITIIALISE API URL", apiUrl);

		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
