/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Web service which sends a request to the server to fetch the list of available jobs.
 */

package com.ingogo.android.webservices;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.webservices.beans.request.IGGetJobsRequestBean;
import com.ingogo.android.webservices.beans.response.IGGetJobsResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGJobAvailableApiListener;

public class IGJobsApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	private IGJobAvailableApiListener _listener;

	public IGJobsApi(IGJobAvailableApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._excptnListener = excptnListener;
		this._listener = apiListener;
	}

	public void getJobs() {
		if (IngogoApp.getSharedApplication().getUserId() != null
				&& IngogoApp.getSharedApplication().getPassword() != null) {

			String mobileNumber = IngogoApp.getSharedApplication().getUserId();
			String password = IngogoApp.getSharedApplication().getPassword();
			String latitude = String.valueOf(IGLocationListener
					.getCurrentLatitude());
			String longitude = String.valueOf(IGLocationListener
					.getCurrentLongitude());

			IGGetJobsRequestBean requestBean = new IGGetJobsRequestBean(
					mobileNumber, password, latitude, longitude);

			IGBaseWebservice2 webserviceTask = new IGBaseWebservice2(this,
					requestBean.toJsonString(), IGGetJobsResponseBean.class);
			webserviceTask.setAuthenticationParams(mobileNumber, password);
			try {
				webserviceTask.execute(buildURL());
			} catch (RejectedExecutionException e) {

			}
		}
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		IGGetJobsResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGGetJobsResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			_listener.onJobAvailableResponseRecieved(respBean.getJobs(),
					respBean.getPreviousJobMessage(),
					respBean.getDriverStatus());
		} else {
			_listener.onJobAvailableResponseFailed();
		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		_listener.onJobAvailableResponseFailed();
	}

	@Override
	public String buildURL() {
		return IGApiConstants.kIngogoBaseURL + IGApiConstants.kJobsApiURL;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}
}
