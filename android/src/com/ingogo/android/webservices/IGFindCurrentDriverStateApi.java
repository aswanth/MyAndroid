package com.ingogo.android.webservices;

import java.util.Map;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.webservices.beans.request.IGFindCurrentDriverStateRequestBean;
import com.ingogo.android.webservices.beans.response.IGFindCurrentDriverStateResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGFindCurrentDriverStateApiListener;

public class IGFindCurrentDriverStateApi extends IGBaseApi implements
		IGApiInterface, IGApiListener {

	IGFindCurrentDriverStateApiListener _listener;

	public IGFindCurrentDriverStateApi(
			IGFindCurrentDriverStateApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	}

	public void findCurrentDriverState(String mobileNumber, String password,
			String licenseNumber, boolean errorRecovery) {
		IGFindCurrentDriverStateRequestBean requestBean = new IGFindCurrentDriverStateRequestBean(
				mobileNumber, password, licenseNumber, errorRecovery);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGFindCurrentDriverStateResponseBean.class,
				requestBean.toJsonString(), this);
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		IGFindCurrentDriverStateResponseBean respBean = new IGFindCurrentDriverStateResponseBean();

		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGFindCurrentDriverStateResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				if (respBean.getResponseCode().equalsIgnoreCase("FAILED")) {
					QLog.d("WEBSERVICE",
							"FindCurrentDriverStateApi api failed response received for mobile number"
									+ IngogoApp.getSharedApplication()
											.getUserId());
					_listener.findCurrentDriverStateFailed(respBean
							.getResponseMessages().errorMessagesToString());
				} else {
					QLog.d("WEBSERVICE",
							"FindCurrentDriverStateApi api success reponse received for mobile number"
									+ IngogoApp.getSharedApplication()
											.getUserId());

					_listener.findCurrentDriverStateCompleted(
							respBean.getBookingSummaries(),
							respBean.getReceiptInformation());
				}

				return;
			}
			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGFindCurrentDriverStateResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				QLog.d("WEBSERVICE",
						"FindCurrentDriverStateApi api failed response received for mobile number"
								+ IngogoApp.getSharedApplication().getUserId());
				_listener.findCurrentDriverStateFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}
		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		IGFindCurrentDriverStateResponseBean respBean = (IGFindCurrentDriverStateResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			if (respBean != null) {
				QLog.d("WEBSERVICE",
						"processPaymentForUnknownPassenger api failed response received for mobile number"
								+ IngogoApp.getSharedApplication().getUserId());

			}
			_listener.findCurrentDriverStateFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {
//			String errorMessage = null;
//			try {
//				Object errorMsgObject;
//				
//				errorMsgObject = errorResponse.get(IGApiConstants.kErrorMsgKey);
//				if (errorMsgObject
//						.equals(IGApiConstants.kNetworkErrorExceptionKey)) {
//					errorMessage = IGApiConstants.kNetworkErrorMessage;
//				} else if (errorMsgObject
//						.equals(IGApiConstants.kTimeOutErrorExceptionKey)) {
//					errorMessage = IGApiConstants.kNetworkTimeoutErrorMessage;
//				} else if (errorMsgObject
//						.equals(IGApiConstants.kInternalServerErrorExceptionKey)) {
//					errorMessage = IGApiConstants.kInternalServerErrorMessage;
//					QLog.d("WEBSERVICE",
//							"processPaymentForUnknownPassenger api failed due to internal server error for mobile number"
//									+ IngogoApp.getSharedApplication()
//											.getUserId());
//				}
//			} catch (Exception e) {
//
//			}
//			_listener.findCurrentDriverStateFailed(errorMessage);
			super.detectErrorMessage(errorResponse);
			return;
		}
	}

	@Override
	public String buildURL() {
		return IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kFindCurrentDriverStateApiURL;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
