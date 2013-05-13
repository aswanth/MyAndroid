package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGTakePaymentRequestBean;
import com.ingogo.android.webservices.beans.response.IGTakePaymentResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGTakePaymentApiListener;

public class IGTakePaymentApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {
	IGTakePaymentApiListener _listener;

	public IGTakePaymentApi(IGTakePaymentApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;

	}

	public void getTakePaymentStatus() {
		IGTakePaymentRequestBean requestBean = new IGTakePaymentRequestBean(
				IngogoApp.getSharedApplication().getUserId(), IngogoApp
						.getSharedApplication().getPassword());

		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGTakePaymentResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {

		Log.e("API CALL", "can take payment response sucess");
		// TODO Auto-generated method stub
		IGTakePaymentResponseBean respBean = new IGTakePaymentResponseBean();

		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGTakePaymentResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				_listener.takePaymentCompleted(respBean.getCanTakePayment());
				return;
			}

			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGTakePaymentResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.takePaymentFailed(respBean.getResponseMessages()
						.errorMessagesToString());
			}

		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub

		IGTakePaymentResponseBean respBean;
		respBean = (IGTakePaymentResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.takePaymentFailed(respBean.getResponseMessages()
					.errorMessagesToString());
			Log.e("API CALL", "can take payment response Failure "
					+ respBean.getResponseMessages().errorMessagesToString());
		} else {
			Log.e("API CALL", "can take payment response Failure "
					+ errorResponse.get(IGApiConstants.kErrorMsgKey));

			super.detectErrorMessage(errorResponse);
		}

	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kTakePaymentUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;

	}

}
