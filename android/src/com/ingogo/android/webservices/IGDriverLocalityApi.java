package com.ingogo.android.webservices;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGDriverLocalityRequestBean;
import com.ingogo.android.webservices.beans.response.IGDriverLocalityResponseBean;
import com.ingogo.android.webservices.beans.response.IGTakePaymentResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGDriverLocalityApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

public class IGDriverLocalityApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	IGDriverLocalityApiListener _listener;

	public IGDriverLocalityApi(IGDriverLocalityApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	}

	public void getDriverLocality(String latitude, String longitude) { // GET
																		// req.

		IGDriverLocalityRequestBean requestBean = new IGDriverLocalityRequestBean(
				latitude, longitude);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGDriverLocalityResponseBean.class,
				requestBean.toJsonString(), this);
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		IGDriverLocalityResponseBean respBean = new IGDriverLocalityResponseBean();
		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGDriverLocalityResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			if (respBean != null) {

				long localityId = respBean.getLocalityId();
				String localityName = respBean.getLocalityName();

				String savingsPercentage = respBean.getSavingsPercentage();

				if (savingsPercentage != null && !savingsPercentage.equals("")) {

					IngogoApp.getSharedApplication().setSavingsPercentage(
							savingsPercentage);

				} else {

					IngogoApp.getSharedApplication()
							.setSavingsPercentage("0.0");

				}

				ArrayList<String> supportedPrefixes = respBean
						.getDriverLocality();

				String supportedPrefixesString = "";
				Log.i("Supported location", "" + supportedPrefixes);
				if (supportedPrefixes != null && supportedPrefixes.size() > 0) {

					for (String prefix : supportedPrefixes) {
						supportedPrefixesString = supportedPrefixesString
								+ prefix + ",";
					}
					if (supportedPrefixesString.length() > 0) {
						supportedPrefixesString = supportedPrefixesString
								.substring(0, supportedPrefixesString
										.lastIndexOf(","));
					}
				} else {
					supportedPrefixesString = "";
				}

				ArrayList<String> maskedPrefixes = respBean.getSupportedMasks();
				String maskedPrefixesString = "";
				Log.i("Supported location", "" + maskedPrefixes);
				if (maskedPrefixes != null && maskedPrefixes.size() > 0) {

					for (String prefix : maskedPrefixes) {
						maskedPrefixesString = maskedPrefixesString + prefix
								+ ",";
					}
					if (maskedPrefixesString.length() > 0) {
						maskedPrefixesString = maskedPrefixesString.substring(
								0, maskedPrefixesString.lastIndexOf(","));
					}
				} else {
					maskedPrefixesString = "";
				}

				_listener.driverLocalityFetchingCompleted(
						supportedPrefixesString, maskedPrefixesString,
						localityId, localityName);
				return;

			}

			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGDriverLocalityResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.driverLocalityFetchingFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		IGDriverLocalityResponseBean respBean;
		respBean = (IGDriverLocalityResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.driverLocalityFetchingFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}

	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kDriverLocalityApiUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {

		return null;
	}

}
