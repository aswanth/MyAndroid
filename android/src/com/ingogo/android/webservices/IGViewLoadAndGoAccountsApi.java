package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGLoadAndGoAccountDetailsRequestBean;
import com.ingogo.android.webservices.beans.response.IGLoadAndGoAccountDetailsResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGViewLoadAndGoAccountsListener;

public class IGViewLoadAndGoAccountsApi extends IGBaseApi implements
		IGApiInterface, IGApiListener {

	private IGViewLoadAndGoAccountsListener _listener;

	public IGViewLoadAndGoAccountsApi(IGViewLoadAndGoAccountsListener listener,
			IGExceptionApiListener excptnListener) {
		this._listener = listener;
		this._excptnListener = excptnListener;

	}

	public void viewLoadAndGoAccounts() {
		IGLoadAndGoAccountDetailsRequestBean requestBean = new IGLoadAndGoAccountDetailsRequestBean(
				IngogoApp.getSharedApplication().getUserId(), IngogoApp
						.getSharedApplication().getPassword());
		IGBaseWebservice2 webserviceTask = new IGBaseWebservice2(this,
				requestBean.toJsonString(),
				IGLoadAndGoAccountDetailsResponseBean.class);
		webserviceTask.execute(buildURL());
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.i("IG-LOAD-AND-GO-ACCOUNTS-RESPONSE", response.toString());
		IGLoadAndGoAccountDetailsResponseBean respBean;

		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGLoadAndGoAccountDetailsResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			_listener.successToFetchLoadAndGoAccounts(respBean.getAccounts(), respBean.getBalancesAreAsAt());
			
			
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGLoadAndGoAccountDetailsResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.failedToFetchLoadAndGoAccounts(respBean
						.getResponseMessages().errorMessagesToString());
			}else {
				this._excptnListener.onNullResponseRecieved();
			}

		} else {
			this._excptnListener.onNullResponseRecieved();
		}


	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		IGLoadAndGoAccountDetailsResponseBean respBean;
		respBean = (IGLoadAndGoAccountDetailsResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.failedToFetchLoadAndGoAccounts(respBean
					.getResponseMessages().errorMessagesToString());
		} else {
			this._excptnListener.onNullResponseRecieved();
		}

	}

	@Override
	public String buildURL() {
		return IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kLoadAndGoAccountsUrl;

	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
