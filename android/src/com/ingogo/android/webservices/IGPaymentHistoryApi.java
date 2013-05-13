package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.webservices.beans.request.IGPaymentHistoryRequestBean;
import com.ingogo.android.webservices.beans.response.IGPaymentHistoryDetailResponseBean;
import com.ingogo.android.webservices.beans.response.IGPaymentHistoryResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentHistoryApiListener;

public class IGPaymentHistoryApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {
	IGPaymentHistoryApiListener _listener = null;

	public IGPaymentHistoryApi(IGPaymentHistoryApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	}

	public void getPaymentHistorySummaries(String username, String password) { // GET
																				// req.

		IGPaymentHistoryRequestBean requestBean = new IGPaymentHistoryRequestBean(
				username, password);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGPaymentHistoryResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.i("IG-FETCH-PAYMENT-HISTORY-RESPONSE", response.toString());
		IGPaymentHistoryResponseBean respBean;

		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGPaymentHistoryResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if(respBean != null) {
				_listener.paymentHistoryFetchingCompleted(respBean);
			}else {
				_excptnListener.onNullResponseRecieved();
			}
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGPaymentHistoryResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.paymentHistoryFetchingFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		IGPaymentHistoryResponseBean respBean;
		respBean = (IGPaymentHistoryResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.paymentHistoryFetchingFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}

	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kPaymentSummaryApiUrl;
		return apiUrl;
	}

	
	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
