package com.ingogo.android.webservices;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.webservices.beans.request.IGCompleteOfflineRequestBean;
import com.ingogo.android.webservices.beans.response.IGBaseResponseBean;
import com.ingogo.android.webservices.beans.response.IGCompleteOfflineResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGCompleteOfflineListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

public class IGCompleteOfflineJobApi extends IGBaseApi implements IGApiInterface,
IGApiListener {

	//private IGResponseListener _responseListener;
	private String _jobID;
	private IGCompleteOfflineListener _listener;

	public IGCompleteOfflineJobApi(IGCompleteOfflineListener listener,IGExceptionApiListener exceptionListener,  String jobID) {
		this._listener = listener;
		this._excptnListener = exceptionListener;
		this._jobID = jobID;

	}

	public void completeOffline() {

//		String apiUrl = IGApiConstants.kIngogoBaseURL
//				+ IGApiConstants.kCompleteOfflineApiURL;
//
//		QLog.d("WEBSERVICE", "Complete offline w/s called for mobile number "+ IngogoApp.getSharedApplication().getUserId());
//		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
//				new Handler(), new IGCallbackWrapper(_responseListener),
//				apiUrl, IGApiConstants.kCompleteOfflineWebServiceId);
//		webservice.setAuthorizationHeader(IngogoApp.getSharedApplication()
//				.getUserId()
//				+ ":"
//				+ IngogoApp.getSharedApplication().getAccessToken());
//
//		webservice.start();
		QLog.d("WEBSERVICE", "Complete offline w/s called for mobile number "+ IngogoApp.getSharedApplication().getUserId());
		String latitude,longitude;
		if ((IngogoApp.LATTITUDE == null) || (IngogoApp.LONGITUDE == null)) {
			latitude = "0.0";
			longitude = "0.0";
		} else {
			latitude = IngogoApp.LATTITUDE;
			longitude = IngogoApp.LONGITUDE;
		}
		IGCompleteOfflineRequestBean requestBean = new IGCompleteOfflineRequestBean(_jobID, latitude, longitude);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGCompleteOfflineResponseBean.class,
				requestBean.toJsonString(), this);
		

	}

	

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.d("COMPLETE OFFLINE RESPONSE", response.toString());
		IGCompleteOfflineResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGCompleteOfflineResponseBean)response.get(IGApiConstants.kSuccessMsgKey);
			if(respBean != null) {
				IngogoApp.getSharedApplication().setComingFromPayOffline(true);
				_listener.completeOfflineSuccess(respBean.getReceiptInformation(), respBean.getTotalPaid());
				return;
			} 
			_listener.completeOfflineFailed(null,true);
			
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGCompleteOfflineResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.completeOfflineFailed(respBean.getResponseMessages()
						.errorMessagesToString(),false);
				return;
			}
			_listener.completeOfflineFailed(null,true);
		}
		
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.i("COMPLETE OFFLINE RESPONSE",errorResponse.toString());
		IGCompleteOfflineResponseBean respBean = (IGCompleteOfflineResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.completeOfflineFailed(respBean.getResponseMessages()
					.errorMessagesToString(),false);
		} else {
			_listener.completeOfflineFailed(null,true);
		}
		
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kCompleteOfflineApiURL;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}

}