package com.ingogo.android.webservices;

import java.util.Map;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.webservices.beans.request.IGPrintReceiptRequestBean;
import com.ingogo.android.webservices.beans.response.IGPrintReceiptResponseBean;
import com.ingogo.android.webservices.beans.response.IGRegisterIssueResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGReprintLastReceiptApiListener;

public class IGReprintLastReceiptApi extends IGBaseApi implements IGApiInterface,
IGApiListener {

	IGReprintLastReceiptApiListener _listener;
	
	public IGReprintLastReceiptApi(IGReprintLastReceiptApiListener apiListener, IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	}
	
	public void reprintLastReceipt() {
		IGPrintReceiptRequestBean requestBean = new IGPrintReceiptRequestBean(IngogoApp.getSharedApplication()
				.getUserId(), IngogoApp.getSharedApplication()
				.getPassword());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGPrintReceiptResponseBean.class,
				requestBean.toJsonString(), this);
	
	}
	
	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub
		IGPrintReceiptResponseBean respBean = new IGPrintReceiptResponseBean();

		if (response != null
				&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGPrintReceiptResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);

			if (respBean != null) {
				_listener.reprintLastReceiptCompleted(respBean.getReceiptInformation());
				return;
			}

			this._excptnListener.onNullResponseRecieved();
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGPrintReceiptResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.reprintLastReceiptFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		// TODO Auto-generated method stub
		IGPrintReceiptResponseBean respBean;
		respBean = (IGPrintReceiptResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.reprintLastReceiptFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			super.detectErrorMessage(errorResponse);
		}
	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kReprintLastReceiptUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
