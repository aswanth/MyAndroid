package com.ingogo.android.webservices;

import java.util.Map;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.webservices.beans.request.IGCreateBookingForPaymentRequestBean;
import com.ingogo.android.webservices.beans.response.IGCreateBookingForPaymentResponseBean;
import com.ingogo.android.webservices.beans.response.IGTakePaymentResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGCreateBookingForPaymentApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

public class IGCreateBookingForPaymentApi extends IGBaseApi implements
		IGApiInterface, IGApiListener {

	IGCreateBookingForPaymentApiListener _listener;

	public IGCreateBookingForPaymentApi(
			IGCreateBookingForPaymentApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;

	}

	public void createBookingForPassenger(String passengerId) {
		IGCreateBookingForPaymentRequestBean requestBean = new IGCreateBookingForPaymentRequestBean(
				IngogoApp.getSharedApplication().getUserId(), IngogoApp
						.getSharedApplication().getPassword(), passengerId,
				String.valueOf(IGLocationListener.getCurrentLatitude()),
				String.valueOf(IGLocationListener.getCurrentLongitude()));
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGCreateBookingForPaymentResponseBean.class,
				requestBean.toJsonString(), this);
		
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		IGCreateBookingForPaymentResponseBean respBean = new IGCreateBookingForPaymentResponseBean();

		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGCreateBookingForPaymentResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				_listener.createBookingForPaymentCompleted(respBean);
				return;
			}

			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGCreateBookingForPaymentResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.createBookingForPaymentFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		IGCreateBookingForPaymentResponseBean respBean;
		respBean = (IGCreateBookingForPaymentResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.createBookingForPaymentFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kCreateBookingForPaymentUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
