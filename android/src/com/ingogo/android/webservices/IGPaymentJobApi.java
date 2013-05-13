package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.webservices.beans.request.IGPaymentJobRequestBean;
import com.ingogo.android.webservices.beans.response.IGPaymentJobResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentJobApiListener;

public class IGPaymentJobApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	IGPaymentJobApiListener _listener;

	public IGPaymentJobApi(IGPaymentJobApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;

	}

	public void payment(String bookingId, String totalFare, String fare,
			String token, String confCode, String paymentMethod) {
		IGPaymentJobRequestBean requestBean = new IGPaymentJobRequestBean(
				bookingId, totalFare, fare, token, confCode, paymentMethod);
		Log.e("PAYMENT API REQUEST", requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGPaymentJobResponseBean.class,
				requestBean.toJsonString(), this);
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
	

		IGPaymentJobResponseBean respBean = new IGPaymentJobResponseBean();
		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGPaymentJobResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				if (respBean.getResponseCode().equalsIgnoreCase("FAILED")) {
					QLog.d("WEBSERVICE",
							"IGPaymentJobApi failed response received for mobile number"
									+ IngogoApp.getSharedApplication()
											.getUserId());
					_listener.paymentJobFailed(respBean.getResponseMessages()
							.errorMessagesToString());
				} else {
					QLog.d("WEBSERVICE",
							"IGPaymentJobApi success reponse received for mobile number"
									+ IngogoApp.getSharedApplication()
											.getUserId());
					_listener.paymentJobCompleted(respBean.getReceipt());
				}

				return;
			}
			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGPaymentJobResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				QLog.d("WEBSERVICE",
						"IGPaymentJobApi failed response received for mobile number"
								+ IngogoApp.getSharedApplication().getUserId());
				_listener.paymentJobFailed(respBean.getResponseMessages()
						.errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("PROCESS PAYMENT FOR UNKNOWN PASSENGER FAILURE RESPONSE",
				"PROCESS PAYMENT FOR UNKNOWN PASSENGER FAILURE RESPONSE = "
						+ errorResponse.toString());

		IGPaymentJobResponseBean respBean;
		respBean = (IGPaymentJobResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			QLog.d("WEBSERVICE",
					"IGPaymentJobApi failed response received for mobile number"
							+ IngogoApp.getSharedApplication().getUserId());
			_listener.paymentJobFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {
			try {
				Object errorMsgObject;
				errorMsgObject = errorResponse.get(IGApiConstants.kErrorMsgKey);
				if (errorMsgObject
						.equals(IGApiConstants.kNetworkErrorExceptionKey)) {
					QLog.d("WEBSERVICE",
							"IGPaymentJobApi failed due to network exception for mobile number"
									+ IngogoApp.getSharedApplication()
											.getUserId());
				} else if (errorMsgObject
						.equals(IGApiConstants.kTimeOutErrorExceptionKey)) {
					QLog.d("WEBSERVICE",
							"IGPaymentJobApi failed due to network timeout exception for mobile number"
									+ IngogoApp.getSharedApplication()
											.getUserId());
				} else if (errorMsgObject
						.equals(IGApiConstants.kInternalServerErrorExceptionKey)) {
					QLog.d("WEBSERVICE",
							"IGPaymentJobApi failed due to internal server error for mobile number"
									+ IngogoApp.getSharedApplication()
											.getUserId());
				}
			} catch (Exception e) {

			}
			super.detectErrorMessage(errorResponse);
			return;
		}

	}

	@Override
	public String buildURL() {
		return IGApiConstants.kIngogoBaseURL + IGApiConstants.KPaymentApiURL;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
