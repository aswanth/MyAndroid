package com.ingogo.android.webservices;

import java.util.Map;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGContactInfoModel;
import com.ingogo.android.webservices.beans.request.IGContactInfoRequestBean;
import com.ingogo.android.webservices.beans.response.IGContactInfoResponseBean;
import com.ingogo.android.webservices.beans.response.IGPrintReceiptResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGRetreiveContactInfoApiListener;

	public class IGRetrieveContactInfoApi extends IGBaseApi implements IGApiInterface,
	IGApiListener {
		IGRetreiveContactInfoApiListener _listener;

		public IGRetrieveContactInfoApi(IGRetreiveContactInfoApiListener apiListener, IGExceptionApiListener excptnListener) {
			this._listener = apiListener;
			this._excptnListener = excptnListener;

		}
		
		public void retreiveContactInfo(IGContactInfoModel contactInformation) {
			IGContactInfoRequestBean requestBean = new IGContactInfoRequestBean(IngogoApp.getSharedApplication()
					.getUserId(), IngogoApp.getSharedApplication()
					.getPassword(), contactInformation);
			IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
					buildURL(), IGContactInfoResponseBean.class,
					requestBean.toJsonString(), this);
			
		}

		@Override
		public void onResponseReceived(Map<String, Object> response) {
			// TODO Auto-generated method stub
			IGContactInfoResponseBean respBean = new IGContactInfoResponseBean();

			if (response != null
					&& response.containsKey(IGApiConstants.kSuccessMsgKey)) {
				respBean = (IGContactInfoResponseBean) response
						.get(IGApiConstants.kSuccessMsgKey);

				if (respBean != null) {
					_listener.retreiveContactInfoCompleted(respBean.getContactInformation());
					return;
				}

				this._excptnListener.onNullResponseRecieved();
			} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

				respBean = (IGContactInfoResponseBean) response
						.get(IGApiConstants.kApiFailedMsgKey);
				if (respBean != null) {
					_listener.retreiveContactInfoFailed(respBean
							.getResponseMessages().errorMessagesToString());
				}

			}
			
		}

		@Override
		public void onFailedToGetResponse(Map<String, Object> errorResponse) {
			// TODO Auto-generated method stub
			IGContactInfoResponseBean respBean;
			respBean = (IGContactInfoResponseBean) errorResponse
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.retreiveContactInfoFailed(respBean
						.getResponseMessages().errorMessagesToString());
			} else {

				super.detectErrorMessage(errorResponse);
			}
			
		}

		@Override
		public String buildURL() {
			// TODO Auto-generated method stub
			String apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kRetrieveContactInfoUrl;
			return apiUrl;
		}

		@Override
		public String buildURL(String string) {
			// TODO Auto-generated method stub
			return null;

		}

	}

