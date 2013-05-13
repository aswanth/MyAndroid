package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.webservices.beans.request.IGJobDetailsRequestBean;
import com.ingogo.android.webservices.beans.response.IGJobDetailsResponseBean;
import com.ingogo.android.webservices.beans.response.IGTakePaymentResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGJobDetailsListener;

public class IGJobDetailsApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	private IGJobDetailsListener _listener;

	public IGJobDetailsApi(IGJobDetailsListener listener,
			IGExceptionApiListener excptnListener) {
		this._listener = listener;
		this._excptnListener = excptnListener;
	}

	public void getJobDetails(String mobileNumber, String password,
			String bookingID) {
		IGJobDetailsRequestBean requestBean = new IGJobDetailsRequestBean(
				mobileNumber, password, bookingID);

		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGJobDetailsResponseBean.class,
				requestBean.toJsonString(), this);


	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.i("JOB DETAILS SUCCESSFUL RESPONSE", response.toString());
		IGJobDetailsResponseBean respBean;
		IGBookingModel bookingModel = null;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGJobDetailsResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			try {
				bookingModel = respBean.getBookingSumary();
			} catch (NullPointerException e) {
				bookingModel = null;
			}
			_listener.jobDetailsFetchingCompleted(bookingModel);

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGJobDetailsResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.jobDetailsFetchingFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.i("JOB DETAILS FAILURE RESPONSE", errorResponse.toString());
		IGJobDetailsResponseBean respBean;
		respBean = (IGJobDetailsResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.jobDetailsFetchingFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {
			this._excptnListener.onNullResponseRecieved();
		}

	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kJobDetailsApiUrl;
		Log.i("JOB DETAILS API URL", apiUrl);
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
