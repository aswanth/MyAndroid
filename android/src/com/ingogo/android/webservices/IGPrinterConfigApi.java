package com.ingogo.android.webservices;

import java.util.Map;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGPrinterConfigRequestBean;
import com.ingogo.android.webservices.beans.response.IGPaymentHistoryResponseBean;
import com.ingogo.android.webservices.beans.response.IGPrinterConfigResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPrinterConfigApiListener;

public class IGPrinterConfigApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {
	
	IGPrinterConfigApiListener _listener;
	
	public IGPrinterConfigApi(IGPrinterConfigApiListener apiListener, IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;

	}
	
	public void initialisePrinterConfig(String deviceID) {
		IGPrinterConfigRequestBean requestBean = new IGPrinterConfigRequestBean(IngogoApp.getSharedApplication()
				.getUserId(), IngogoApp.getSharedApplication()
				.getPassword(), deviceID);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGPrinterConfigResponseBean.class,
				requestBean.toJsonString(), this);
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub
		IGPrinterConfigResponseBean respBean = new IGPrinterConfigResponseBean();

		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGPrinterConfigResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				_listener.printerConfigCompleted(respBean.getDeviceName(),respBean.getDevicePin());
				return;
			}

			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGPrinterConfigResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.printerConfigFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		IGPrinterConfigResponseBean respBean;
		respBean = (IGPrinterConfigResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.printerConfigFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			super.detectErrorMessage(errorResponse);
		}
	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kPrinterConfigUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
