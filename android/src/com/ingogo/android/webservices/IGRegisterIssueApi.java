package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGRegisterIssueRequestBean;
import com.ingogo.android.webservices.beans.response.IGReferralInformationResponseBean;
import com.ingogo.android.webservices.beans.response.IGRegisterIssueResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGRegisterIssueApiListener;

public class IGRegisterIssueApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {
	private IGRegisterIssueApiListener _listener;

	public IGRegisterIssueApi(IGRegisterIssueApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	}

	public void registerIssue(String issueReason, String additionalInformation) {
		String mobileNumber = IngogoApp.getSharedApplication().getUserId();
		String password = IngogoApp.getSharedApplication().getPassword();

		IGRegisterIssueRequestBean requestBean = new IGRegisterIssueRequestBean(
				mobileNumber, password, issueReason, additionalInformation);

		Log.e("registerIssue REQ  = ", requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGRegisterIssueResponseBean.class,
				requestBean.toJsonString(), this);

	}

	public void registerIssue(String mobileNumber, String issueReason,
			String additionalInformation) {

		String password = "";

		IGRegisterIssueRequestBean requestBean = new IGRegisterIssueRequestBean(
				mobileNumber, password, issueReason, additionalInformation);

		Log.e("registerIssue REQ  = ", requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGRegisterIssueResponseBean.class,
				requestBean.toJsonString(), this);

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.e("registerIssue SUCCESSFUL RESPONSE", response.toString());
		IGRegisterIssueResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGRegisterIssueResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			_listener.registerIssueCompleted();

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGRegisterIssueResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.registerIssueFailed(respBean.getResponseMessages()
						.errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("registerIssue FAILURE RESPONSE", errorResponse.toString());
		IGReferralInformationResponseBean respBean;
		respBean = (IGReferralInformationResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.registerIssueFailed(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kRegisterIssueUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
