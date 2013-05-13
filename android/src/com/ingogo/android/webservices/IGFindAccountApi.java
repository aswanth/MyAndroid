package com.ingogo.android.webservices;

import java.util.Map;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGFindAccountRequestBean;
import com.ingogo.android.webservices.beans.response.IGDriverLocalityResponseBean;
import com.ingogo.android.webservices.beans.response.IGFindAccountResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGFindAccountApiListener;

public class IGFindAccountApi extends IGBaseApi implements IGApiInterface,
IGApiListener{
	
	IGFindAccountApiListener _listener;

	public IGFindAccountApi(IGFindAccountApiListener apiListener,IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;

	}
	
	public void getFindAccountStatus(String searchString){
		IGFindAccountRequestBean requestBean = new IGFindAccountRequestBean(IngogoApp.getSharedApplication()
				.getUserId(), IngogoApp.getSharedApplication()
				.getPassword(), searchString);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGFindAccountResponseBean.class,
				requestBean.toJsonString(), this);
		
	}
	
	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub
		
		IGFindAccountResponseBean respBean = new IGFindAccountResponseBean();
		
		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGFindAccountResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				_listener.findAccountCompleted(respBean.getPassengerInformation());
				return;
			}
			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGFindAccountResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.findAccountFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		IGFindAccountResponseBean respBean;
		respBean = (IGFindAccountResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.findAccountFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kFindAccountUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
