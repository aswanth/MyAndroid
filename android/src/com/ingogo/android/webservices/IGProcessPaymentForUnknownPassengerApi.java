package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.model.IGCreditCardInformation;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.webservices.beans.request.IGProcessPaymentForUnknownPassengerRequestBean;
import com.ingogo.android.webservices.beans.response.IGProcessPaymentForUnknownPassengerResponseBean;
import com.ingogo.android.webservices.beans.response.IGProcessPaymentResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGProcessPaymentForUnknownPassengerApiListener;

public class IGProcessPaymentForUnknownPassengerApi extends IGBaseApi implements
IGApiInterface, IGApiListener {

	IGProcessPaymentForUnknownPassengerApiListener _listener;
	private long _localityId;

	public IGProcessPaymentForUnknownPassengerApi(
			IGProcessPaymentForUnknownPassengerApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
		_localityId = IngogoApp.getSharedApplication().getLocalityId();

	}
	public void processPaymentForUnknownPassenger(String bookingId, String fare, String totalFare, IGCreditCardInformation cardInfo, String suburbName) {
		IGProcessPaymentForUnknownPassengerRequestBean requestBean = new IGProcessPaymentForUnknownPassengerRequestBean(bookingId,
				IGLocationListener.getCurrentLongitude() + "",
				IGLocationListener.getCurrentLatitude() + "",
				IngogoApp.getSharedApplication().getUserId(), IngogoApp
				.getSharedApplication().getPassword(),fare, totalFare,"", _localityId, suburbName);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGProcessPaymentForUnknownPassengerResponseBean.class,
				requestBean.toJsonString(), this);
	
	}

	public void processPaymentForUnknownPassenger(String bookingId, String fare, String totalFare, IGCreditCardInformation cardInfo,  String cardDetails, String suburbName) {
		IGProcessPaymentForUnknownPassengerRequestBean requestBean = new IGProcessPaymentForUnknownPassengerRequestBean(bookingId,
				IGLocationListener.getCurrentLongitude() + "",
				IGLocationListener.getCurrentLatitude() + "",
				IngogoApp.getSharedApplication().getUserId(), IngogoApp
				.getSharedApplication().getPassword(),fare, totalFare,cardDetails, _localityId, suburbName);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGProcessPaymentForUnknownPassengerResponseBean.class,
				requestBean.toJsonString(), this);
		
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.e("PROCESS PAYMENT FOR UNKNOWN PASSENGER SUCCESSFUL RESPONSE", "PROCESS PAYMENT FOR UNKNOWN PASSENGER SUCCESSFUL RESPONSE = " + response.toString());

		IGProcessPaymentForUnknownPassengerResponseBean respBean = new IGProcessPaymentForUnknownPassengerResponseBean();

		
		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGProcessPaymentForUnknownPassengerResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				if (respBean.getResponseCode().equalsIgnoreCase("FAILED")) {
					if (respBean!=null) {
						QLog.d("WEBSERVICE", "processPaymentForUnknownPassenger api failed response received for mobile number" + IngogoApp.getSharedApplication().getUserId());

					}
					_listener.processPaymentForUnknownPassengerFailed(respBean
							.getResponseMessages().errorMessagesToString(), respBean
							);
				} else {
					if (respBean!=null) {
						QLog.d("WEBSERVICE", "processPaymentForUnknownPassenger api success reponse received for mobile number" + IngogoApp.getSharedApplication().getUserId());

					}
					_listener.processPaymentForUnknownPassengerCompleted(respBean
							);
				}
				
				return;
			}
			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGProcessPaymentForUnknownPassengerResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				if (respBean!=null) {
					QLog.d("WEBSERVICE", "processPaymentForUnknownPassenger api failed response received for mobile number" + IngogoApp.getSharedApplication().getUserId());

				}
				_listener.processPaymentForUnknownPassengerFailed(respBean
						.getResponseMessages().errorMessagesToString(), respBean);
			}

		}
		
	}
	
	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("PROCESS PAYMENT FOR UNKNOWN PASSENGER FAILURE RESPONSE", "PROCESS PAYMENT FOR UNKNOWN PASSENGER FAILURE RESPONSE = " + errorResponse.toString());

		IGProcessPaymentForUnknownPassengerResponseBean respBean;
		respBean = (IGProcessPaymentForUnknownPassengerResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			if (respBean!=null) {
				QLog.d("WEBSERVICE", "processPaymentForUnknownPassenger api failed response received for mobile number" + IngogoApp.getSharedApplication().getUserId());

			}
			_listener.processPaymentForUnknownPassengerFailed(respBean
					.getResponseMessages().errorMessagesToString(), respBean);
		} else {
			try {
				Object errorMsgObject;
				errorMsgObject = errorResponse.get(IGApiConstants.kErrorMsgKey);
				if (errorMsgObject
						.equals(IGApiConstants.kNetworkErrorExceptionKey)) {

					QLog.d("WEBSERVICE", "processPaymentForUnknownPassenger api failed due to network exception for mobile number" + IngogoApp.getSharedApplication().getUserId());

				} else if (errorMsgObject
						.equals(IGApiConstants.kTimeOutErrorExceptionKey)) {

					QLog.d("WEBSERVICE", "processPaymentForUnknownPassenger api failed due to network timeout exception for mobile number" + IngogoApp.getSharedApplication().getUserId());

				}else if(errorMsgObject
						.equals(IGApiConstants.kInternalServerErrorExceptionKey)){
					QLog.d("WEBSERVICE", "processPaymentForUnknownPassenger api failed due to internal server error for mobile number" + IngogoApp.getSharedApplication().getUserId());

				}
			} catch(Exception e) {
				
			}
	

			super.detectErrorMessage(errorResponse);
			return;
		}
		
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kProcessPaymentForUnknownPassengerUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}
}
