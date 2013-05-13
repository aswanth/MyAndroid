package com.ingogo.android.webservices;

import java.util.Map;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGMapInfoRequestBean;
import com.ingogo.android.webservices.beans.response.IGContactInfoResponseBean;
import com.ingogo.android.webservices.beans.response.IGMapInfoResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGMapInfoApiListener;

public class IGMapInfoApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	IGMapInfoApiListener _listener;

	public IGMapInfoApi(IGMapInfoApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	}

	public void getMapInfo(String bookingID) {
		IGMapInfoRequestBean requestBean = new IGMapInfoRequestBean(bookingID,
				IngogoApp.getSharedApplication().getUserId(), IngogoApp
						.getSharedApplication().getPassword());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGMapInfoResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub

		IGMapInfoResponseBean respBean = new IGMapInfoResponseBean();

		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGMapInfoResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				_listener.mapInfoCompleted(respBean);
				return;
			}

			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGMapInfoResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.mapInfoFailed(respBean.getResponseMessages()
						.errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub

		IGMapInfoResponseBean respBean;
		respBean = (IGMapInfoResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.mapInfoFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {

			super.detectErrorMessage(errorResponse);
		}
	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kMapInfoUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
