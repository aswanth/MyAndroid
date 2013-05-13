package com.ingogo.android.webservices;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import android.os.AsyncTask;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLogCache;
import com.ingogo.android.webservices.beans.response.IGBaseResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGSendDiagnosticsListener;

public class IGSendDiagnosticsApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {

	private IGSendDiagnosticsListener _listener;
	private String _mobileNumber;

	public IGSendDiagnosticsApi(IGSendDiagnosticsListener listener,
			IGExceptionApiListener exceptionListener) {
		this._listener = listener;
		this._excptnListener = exceptionListener;
	}

	public void sendDiagnostics(String mobileNumber) {
		if(mobileNumber == null) {
			_mobileNumber = IngogoApp.getSharedApplication().getUserId();
		} else {
			_mobileNumber = mobileNumber;
		}
		//To zip the file,we need to call it in background thread.
		new ExtractZippedFileAsyncTask().execute((Void[])null);
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.d("SEND DIAGNOSTIC RESPONSE SUCCESS", response.toString());
		IGBaseResponseBean respBean;
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			_listener.successfullyySendDiagnostics(_mobileNumber);
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGBaseResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.failedToSendDiagnostics(respBean
						.getResponseMessages().errorMessagesToString());
			} else {
				_listener.failedToSendDiagnostics(null);
			}
		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.i("SEND DIAGNOSTIC RESPONSE FAILED ", errorResponse.toString());
		IGBaseResponseBean respBean = (IGBaseResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.failedToSendDiagnostics(respBean.getResponseMessages()
					.errorMessagesToString());
		} else {
			this._listener.failedToSendDiagnostics(null);
		}

	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kSendDiagnostics;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		return null;
	}
	
	
	private class ExtractZippedFileAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<String> fileList = QLogCache.getSharedInstance().getAlllFilesInFolder();
			if(fileList.size() > 0) {
				try {

					QLogCache.getSharedInstance().zip(fileList, _mobileNumber + ".zip");

				} catch (IOException e) {

					e.printStackTrace();

					return null;
				}
			}
//			 QLogCache.getSharedInstance()
//					.fetchCachedResponse( _mobileNumber);
			 return null;
		}

		protected void onPostExecute(Void param) {
			super.onPostExecute(param);
			callSendDiagnosticsApi (QLogCache.getSharedInstance().getFileWithName(_mobileNumber+ ".zip"));
			
		}
		
	}
	
	private void callSendDiagnosticsApi ( File file) {
		//zipped file returns null. Show diagnostic fail message.
		if(file == null || !file.exists() ) {
			_listener.failedToSendDiagnostics(null);
			return;
		}
		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		reqEntity.addPart("dignosticsFile", new FileBody(file));
		try {
				reqEntity.addPart("driverNumber", new StringBody(_mobileNumber));
	
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Log.i("MULTIPARTENTITY", reqEntity.toString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGBaseResponseBean.class, null, reqEntity, IGSendDiagnosticsApi.this);
	}

}
