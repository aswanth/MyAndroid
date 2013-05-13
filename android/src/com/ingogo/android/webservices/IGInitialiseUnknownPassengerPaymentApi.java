package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.webservices.beans.request.IGInitialiseUnknownPassengerRequestBean;
import com.ingogo.android.webservices.beans.response.IGGetPaidAtResponseBean;
import com.ingogo.android.webservices.beans.response.IGInitialiseUnknownPassengerResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGInitialiseUnknownPassengerApiListener;

public class IGInitialiseUnknownPassengerPaymentApi extends IGBaseApi implements
		IGApiInterface, IGApiListener {

	IGInitialiseUnknownPassengerApiListener _listener;

	public IGInitialiseUnknownPassengerPaymentApi(
			IGInitialiseUnknownPassengerApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;

	}

	public void getUnknownPassengerPaymentStatus() {
		IGInitialiseUnknownPassengerRequestBean requestBean = new IGInitialiseUnknownPassengerRequestBean(
				IGLocationListener.getCurrentLongitude() + "",
				IGLocationListener.getCurrentLatitude() + "", IngogoApp
						.getSharedApplication().getUserId(), IngogoApp
						.getSharedApplication().getPassword());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGInitialiseUnknownPassengerResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub

		IGInitialiseUnknownPassengerResponseBean respBean = new IGInitialiseUnknownPassengerResponseBean();

		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGInitialiseUnknownPassengerResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				_listener.initialiseUnknownPassengerCompleted(respBean);

				Log.i("getMaxTotalDueValue",
						"" + respBean.getMaxTotalDueValue());

				return;
			}
			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGInitialiseUnknownPassengerResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.initialiseUnknownPassengerFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		IGInitialiseUnknownPassengerResponseBean respBean;
		respBean = (IGInitialiseUnknownPassengerResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.initialiseUnknownPassengerFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			super.detectErrorMessage(errorResponse);
		}
	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kInitialiseUnknownPassengerPaymentUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
