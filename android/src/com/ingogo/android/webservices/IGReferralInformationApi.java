package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGReferralInformationRequestBean;
import com.ingogo.android.webservices.beans.response.IGReconnectAttemptedResponseBean;
import com.ingogo.android.webservices.beans.response.IGReferralInformationResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGReferralInformationApiListener;

/**
 * IGReferralInformationApi 
 * @author suslov
 *
 */
public class IGReferralInformationApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {
	
	private IGReferralInformationApiListener _listener;
	
	public IGReferralInformationApi(IGReferralInformationApiListener apiListener, IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	} 
	
	public void retrieveReferralInformation() {
		String mobileNumber =IngogoApp.getSharedApplication()
				.getUserId();
		String password = IngogoApp.getSharedApplication()
				.getPassword();
			
		IGReferralInformationRequestBean requestBean = new IGReferralInformationRequestBean(mobileNumber, password);
		
		Log.e("REFERRAL INFORMATION REQ  = ",requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGReferralInformationResponseBean.class,
				requestBean.toJsonString(), this);
		
		
	}
	

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.e("REFERRAL INFORMATION SUCCESSFUL RESPONSE", response.toString());
		IGReferralInformationResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGReferralInformationResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			
			_listener.retrieveReferralInformationCompleted(respBean);

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGReferralInformationResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.retrieveReferralInformationFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("REFERRAL INFORMATION FAILURE RESPONSE", errorResponse.toString());
		IGReferralInformationResponseBean respBean;
		respBean = (IGReferralInformationResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.retrieveReferralInformationFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
				
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kReferralInformationUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
