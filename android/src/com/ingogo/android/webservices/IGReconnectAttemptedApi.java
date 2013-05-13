package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGReconnectAttemptedRequestBean;
import com.ingogo.android.webservices.beans.response.IGProcessPaymentForUnknownPassengerResponseBean;
import com.ingogo.android.webservices.beans.response.IGReconnectAttemptedResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGReconnectAttemptedApiListener;

public class IGReconnectAttemptedApi extends IGBaseApi implements IGApiInterface,
IGApiListener {

	private IGReconnectAttemptedApiListener _listener;

	public IGReconnectAttemptedApi(IGReconnectAttemptedApiListener apiListener, IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	} 

	public void reconnectAttemptedForDriver() {
		String mobileNumber =IngogoApp.getSharedApplication()
				.getUserId();
		String password = IngogoApp.getSharedApplication()
				.getPassword();

		IGReconnectAttemptedRequestBean requestBean = new IGReconnectAttemptedRequestBean(mobileNumber, password);

		Log.e("ReconnectAttempted REQ  = ",requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGReconnectAttemptedResponseBean.class,
				requestBean.toJsonString(), this);
			

	}


	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.e("ReconnectAttempted SUCCESSFUL RESPONSE", response.toString());
		IGReconnectAttemptedResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGReconnectAttemptedResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			_listener.reconnectAttemptedRequestCompleted();

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGReconnectAttemptedResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.reconnectAttemptedRequestFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("ReconnectAttempted FAILURE RESPONSE", errorResponse.toString());
		IGReconnectAttemptedResponseBean respBean;
		respBean = (IGReconnectAttemptedResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.reconnectAttemptedRequestFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}

	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kReconnectAttemptedUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}
}
