package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGIssueReasonsRequestBean;
import com.ingogo.android.webservices.beans.response.IGInitialiseUnknownPassengerResponseBean;
import com.ingogo.android.webservices.beans.response.IGIssueReasonsResponseBean;
import com.ingogo.android.webservices.beans.response.IGReferralInformationResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGIssueReasonsApiListener;

public class IGIssueReasonsApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {
	private IGIssueReasonsApiListener _listener;

	public IGIssueReasonsApi(IGIssueReasonsApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	}

	public void retrieveReasonsList() {
		String mobileNumber = IngogoApp.getSharedApplication().getUserId();
		String password = IngogoApp.getSharedApplication().getPassword();

		IGIssueReasonsRequestBean requestBean = new IGIssueReasonsRequestBean(
				mobileNumber, password);

		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGIssueReasonsResponseBean.class,
				requestBean.toJsonString(), this);

	}

	public void retrieveReasonsList(String mobileNumber) {

		String password = "";

		IGIssueReasonsRequestBean requestBean = new IGIssueReasonsRequestBean(
				mobileNumber, password);

		Log.e("IssueReasons REQ  = ", requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGIssueReasonsResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.e("IssueReasons SUCCESSFUL RESPONSE", response.toString());
		IGIssueReasonsResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGIssueReasonsResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			_listener.retrieveReasonsCompleted(respBean);

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGIssueReasonsResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.retrieveReasonsFailed(respBean.getResponseMessages()
						.errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("IssueReasons FAILURE RESPONSE", errorResponse.toString());
		IGReferralInformationResponseBean respBean;
		respBean = (IGReferralInformationResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.retrieveReasonsFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kIssueReasonsUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
