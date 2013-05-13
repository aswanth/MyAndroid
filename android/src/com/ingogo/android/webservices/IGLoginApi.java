/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a request to the server to login the user.
 */

package com.ingogo.android.webservices;

import java.util.Map;
import android.util.Log;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.webservices.beans.request.IGLoginRequestBean;
import com.ingogo.android.webservices.beans.response.IGLoginResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGLoginApiListener;

public class IGLoginApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	private String _username;
	private String _password;
	private String _licenseNumber;
	private IGLoginApiListener _listener;

	public IGLoginApi(String username, String password, String licenseNumber,
			IGLoginApiListener listener) {
		this._username = username;
		this._password = password;
		this._licenseNumber = licenseNumber;
		this._listener = listener;
	}

	public void login() {
		IGLoginRequestBean requestBean = new IGLoginRequestBean(_username,
				_password, _licenseNumber);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGLoginResponseBean.class,
				requestBean.toJsonString(), this);

	}

	

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.e("LOGIN SUCCESSFUL RESPONSE", response.toString());
		IGLoginResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGLoginResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			_listener.loginSuccessfully( respBean );

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGLoginResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.failedToLogin(respBean.getResponseMessages()
						.errorMessagesToString());
			}

		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.e("LOGIN FAILURE RESPONSE", errorResponse.toString());
		IGLoginResponseBean respBean;
		respBean = (IGLoginResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.failedToLogin(respBean.getResponseMessages()
					.errorMessagesToString());
		} else if (errorResponse.get("HttpStatus").toString().trim().equalsIgnoreCase("401")) {
			_listener.failedToLogin("Please enter a valid user name and password.");
		}

	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kLoginApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}
