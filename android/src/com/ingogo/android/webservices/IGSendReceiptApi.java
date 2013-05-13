package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGSendReceiptRequestBean;
import com.ingogo.android.webservices.beans.response.IGBaseResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGSendReceiptApiListener;

public class IGSendReceiptApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	IGSendReceiptApiListener _listener;
	
	public IGSendReceiptApi(IGSendReceiptApiListener listener,
			IGExceptionApiListener exceptionListener) {
		this._listener = listener;
		this._excptnListener = exceptionListener;
	}
	
	public void sendReceiptMobileNumber(String bookingId, String passengerMobileNumber) {

		IGSendReceiptRequestBean requestBean = new IGSendReceiptRequestBean();
		requestBean.setBookingId(bookingId);
		requestBean.setPassengerMobileNumber(passengerMobileNumber);
		requestBean.setMobileNumber(IngogoApp.getSharedApplication().getUserId());
		requestBean.setPassword(IngogoApp.getSharedApplication().getPassword());
		Log.e("Req: Bean", requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGBaseResponseBean.class,
				requestBean.toJsonString(), this);

	}
	
	public void sendReceiptEmailAddress(String bookingId, String passengerEmailAddress) {

		IGSendReceiptRequestBean requestBean = new IGSendReceiptRequestBean();
		requestBean.setBookingId(bookingId);
		requestBean.setPassengerEmailAddress(passengerEmailAddress);
		requestBean.setMobileNumber(IngogoApp.getSharedApplication().getUserId());
		requestBean.setPassword(IngogoApp.getSharedApplication().getPassword());
		Log.e("Req: Bean", requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGBaseResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.d("sendReceipt RESPONSE", response.toString());
		IGBaseResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			_listener.sendReceiptCompleted(response.toString());
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGBaseResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.sendReceiptFailed(respBean.getResponseMessages()
						.errorMessagesToString());
			}
		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.i("sendReceipt Failed RESPONSE",errorResponse.toString());
		IGBaseResponseBean respBean = (IGBaseResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.sendReceiptFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {
			this._excptnListener.onNullResponseRecieved();
		}
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kSendReceiptUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
