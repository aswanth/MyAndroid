package com.ingogo.android.webservices;

import java.util.Map;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGTargetProgressRequestBean;
import com.ingogo.android.webservices.beans.response.IGSwipeInititaliseResponseBean;
import com.ingogo.android.webservices.beans.response.IGTargetProgressResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGTargetProgressApiListener;

public class IGTargetProgressApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	private IGTargetProgressApiListener _listener;

	public IGTargetProgressApi(IGTargetProgressApiListener listener,
			IGExceptionApiListener excptnListener) {
		this._listener = listener;
		this._excptnListener = excptnListener;
	}

	public void getTargetProgress() {
		IGTargetProgressRequestBean requestBean = new IGTargetProgressRequestBean(
				IngogoApp.getSharedApplication().getUserId(), IngogoApp
						.getSharedApplication().getPassword());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGTargetProgressResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		IGTargetProgressResponseBean respBean = new IGTargetProgressResponseBean();

		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGTargetProgressResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				_listener.targetProgressFetchingCompleted(respBean);
				return;
			}

			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGTargetProgressResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.targetProgressFetchingFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		IGTargetProgressResponseBean respBean;
		respBean = (IGTargetProgressResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.targetProgressFetchingFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			super.detectErrorMessage(errorResponse);
		}
	}
	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kTargetProgressUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
