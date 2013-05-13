package com.ingogo.android.webservices;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.os.Handler;
import android.os.Process;
import android.util.Log;

import com.google.gson.Gson;
import com.ingogo.android.R;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.beans.IGBaseBean;
import com.ingogo.android.webservices.beans.response.IGBaseResponseBean;
import com.ingogo.android.webservices.beans.response.IGErrorMessageResponseBean;
import com.ingogo.android.webservices.beans.response.IGResponseMessagesBean;
import com.ingogo.android.webservices.easyssl.EasySSLSocketFactory;
import com.ingogo.android.webservices.interfaces.IGApiListener;

public class IGBaseWebserviceRunnable implements Runnable {

	private String _postParams = null;
	// private static TrustedDefaultHttpClient _httpClient = new
	// TrustedDefaultHttpClient(
	// IngogoApp.getSharedApplication().getApplicationContext());
	private static TrustedDefaultHttpClient _httpClient = null;
	private HttpResponse _httpResponse = null;
	private IGApiListener _apiListener;
	private int _httpStatus = 0;
	private String _userName = null, _password = null, _urlString = null,
			_responseString = null;
	private Class<? extends IGBaseResponseBean> _responseClass;
	private String apiCalledTime;
	Handler webservieHandler = new Handler();
	private Map<String, Object> responseMap_;
	private MultipartEntity fileUploadEntity = null;

	public IGBaseWebserviceRunnable(IGApiListener apiListener, String url) {
		this._apiListener = apiListener;
		this._urlString = url;
		this.acceptUntrustedCertificates();
	}

	public IGBaseWebserviceRunnable(IGApiListener apiListener,
			String postParams, String url) {
		this._apiListener = apiListener;
		this._postParams = postParams;
		this._urlString = url;
		this.acceptUntrustedCertificates();
	}

	public IGBaseWebserviceRunnable(IGApiListener apiListener,
			String postParams,
			Class<? extends IGBaseResponseBean> responseClass, String url) {
		this._apiListener = apiListener;
		this._postParams = postParams;
		this._urlString = url;
		this._responseClass = responseClass;
		this.acceptUntrustedCertificates();
	}

	public void setFileEntity(MultipartEntity uploadEntity) {
		this.fileUploadEntity = uploadEntity;
	}

	private void acceptUntrustedCertificates() {

		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND
				+ Process.THREAD_PRIORITY_MORE_FAVORABLE);
		apiCalledTime = IGUtility.getTimeString();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(),
				443));
		HttpParams params = new BasicHttpParams();
		int timeoutConnection = IngogoApp.getConnectionTimeout();
		HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
		int timeoutSocket = IngogoApp.getConnectionTimeout();
		HttpConnectionParams.setSoTimeout(params, timeoutSocket);

		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
				new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		ClientConnectionManager cm = new SingleClientConnManager(null,
				schemeRegistry);
		_httpClient = new TrustedDefaultHttpClient(cm, params, IngogoApp
				.getSharedApplication().getApplicationContext());
	}

	public void setAuthenticationParams(String username, String password) {
		this._userName = username;
		this._password = password;
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(_userName, _password
						.toCharArray());
			}
		});
	}

	@Override
	public void run() {
		String serverResponse = null;

		if (IGUtility.isNetworkAvailable(IngogoApp.getSharedApplication()
				.getApplicationContext())) {
			try {
				// POST
				if (_urlString != null) {
					List<String> items = Arrays.asList(_urlString.split(("/")));
					Log.e("API CALL",
							"For APi call " + items.get(items.size() - 1)
									+ "MADE AT: " + apiCalledTime
									+ " CALLED AT: "
									+ IGUtility.getTimeString());
				}

				if (this.fileUploadEntity == null) {
					_httpResponse = this.doJsonPostRequest(_urlString);
				} else {
					_httpResponse = this.doMultipartJsonPostRequest(_urlString);
				}

				if (_httpResponse != null) {

					serverResponse = IGUtility
							.convertStreamToString(_httpResponse.getEntity()
									.getContent());
					_httpStatus = _httpResponse.getStatusLine().getStatusCode();

				} else {

					serverResponse = IGApiConstants.kApiError;
				}
			} catch (ClientProtocolException e) {
				IGUtility.logDetailsToAnalytics(
						IGConstants.kAnalyticsWebserviceFailureEventName,
						_urlString, "ClientProtocolException" + e);

				serverResponse = IGApiConstants.kApiError;
				e.printStackTrace();
			} catch (IOException e) {
				IGUtility.logDetailsToAnalytics(
						IGConstants.kAnalyticsWebserviceFailureEventName,
						_urlString, "IOException" + e);

				serverResponse = IGApiConstants.kApiError;

				e.printStackTrace();
			} catch (IllegalStateException e) {
				IGUtility.logDetailsToAnalytics(
						IGConstants.kAnalyticsWebserviceFailureEventName,
						_urlString, "IOException" + e);

				serverResponse = IGApiConstants.kApiError;

				e.printStackTrace();
			}
		} else {
			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_urlString, "network not available");
			serverResponse = IGApiConstants.kNetworkError;

		}
		Log.e("API RESPONSE", serverResponse);
		_responseString = serverResponse;
		onComplete(serverResponse);
	}

	private Runnable onFailedToGetResponse = new Runnable() {
		public void run() {
			_apiListener.onFailedToGetResponse(responseMap_);
		}
	};
	private Runnable onResponseReceived = new Runnable() {
		public void run() {
			_apiListener.onResponseReceived(responseMap_);
		}
	};

	private void onComplete(String result) {

		responseMap_ = new HashMap<String, Object>();
		responseMap_.put(IGApiConstants.kHttpStatusKey, _httpStatus);

		if (result.equalsIgnoreCase(IGApiConstants.kNetworkError)) {

			responseMap_.put(IGApiConstants.kErrorMsgKey,
					IGApiConstants.kNetworkErrorExceptionKey);
			webservieHandler.post(onFailedToGetResponse);

			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_urlString, "Network Error has occured");

		} else if (result != null
				&& (_httpStatus == HttpStatus.SC_OK || _httpStatus == HttpStatus.SC_CREATED)) {

			IGBaseResponseBean resultBean = (IGBaseResponseBean) getBeanFromResult(result);
			if (resultBean.getResponseCode().equalsIgnoreCase("FAILED")) {
				responseMap_.put(IGApiConstants.kApiFailedMsgKey,
						getBeanFromResult(result));
				IGUtility.logDetailsToAnalytics(
						IGConstants.kAnalyticsWebserviceFailureEventName,
						_urlString, IGConstants.kAnalyticsFailedWithDetails
								+ result);
				webservieHandler.post(onFailedToGetResponse);

			} else {
				responseMap_.put(IGApiConstants.kSuccessMsgKey,
						getBeanFromResult(result));
				IGUtility.logDetailsToAnalytics(
						IGConstants.kAnalyticsWebserviceSuccessEventName,
						_urlString, "Success");
				webservieHandler.post(onResponseReceived);

			}

		} else if (_httpStatus == 500) {

			// IGBaseResponseBean resultBean = (IGBaseResponseBean)
			// getBeanFromResult(result);
			// if (!IngogoApp.getSharedApplication().isLoggedIn()) {
			// responseMap_.put(IGApiConstants.kApiFailedMsgKey, resultBean);
			// }

			responseMap_.put(IGApiConstants.kErrorMsgKey,
					IGApiConstants.kInternalServerErrorExceptionKey);

			webservieHandler.post(onFailedToGetResponse);

			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_urlString, IGConstants.kAnalyticsFailedWithDetails
							+ result);

		} else if (_httpStatus == 401) {
			IGBaseResponseBean resultBean;
			Gson gson = new Gson();
			try {
				resultBean = gson.fromJson(result, _responseClass);
			} catch (Exception ex) {
				resultBean = null;
			}
			if (!IngogoApp.getSharedApplication().isLoggedIn()) {
				responseMap_.put(IGApiConstants.kApiFailedMsgKey, resultBean);
			}
			responseMap_.put(IGApiConstants.kErrorMsgKey,
					IGApiConstants.kAuthenticationErrorExceptioney);

			webservieHandler.post(onFailedToGetResponse);

			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_urlString, IGConstants.kAnalyticsFailedWithDetails
							+ result);

		} else if (result.equalsIgnoreCase(IGApiConstants.kApiError)) {
			responseMap_.put(IGApiConstants.kErrorMsgKey,
					IGApiConstants.kTimeOutErrorExceptionKey);
			webservieHandler.post(onFailedToGetResponse);

			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_urlString, IGConstants.kAnalyticsWebserviceTimeout);

		} else {

			responseMap_.put(IGApiConstants.kApiFailedMsgKey,
					getBeanFromResult(result));
			webservieHandler.post(onResponseReceived);

			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_urlString, IGConstants.kAnalyticsFailedWithDetails
							+ result);

		}

	}

	private IGBaseBean getBeanFromResult(String result) {
		Gson gson = new Gson();
		try {
			return gson.fromJson(result, _responseClass);
		} catch (Exception ex) {
			IGBaseResponseBean bean = null;
			try {
				bean = (IGBaseResponseBean) _responseClass.newInstance();
			} catch (IllegalAccessException e) {
				IGUtility.logDetailsToAnalytics(
						IGConstants.kAnalyticsWebserviceFailureEventName,
						_urlString,
						"getBeanFromResult - IllegalAccessException : " + e);
				e.printStackTrace();
			} catch (InstantiationException e) {
				IGUtility.logDetailsToAnalytics(
						IGConstants.kAnalyticsWebserviceFailureEventName,
						_urlString,
						"getBeanFromResult - InstantiationException : " + e);
				e.printStackTrace();
			}

			bean.setResponseCode(IGApiConstants.kApiFailedMsgKey);
			IGResponseMessagesBean resBean = new IGResponseMessagesBean();
			IGErrorMessageResponseBean errBean = new IGErrorMessageResponseBean();
			errBean.setCode(IGApiConstants.kApiFailedMsgKey);
			errBean.setContent(IngogoApp.getSharedApplication()
					.getApplicationContext().getResources()
					.getString(R.string.generic_error_message));
			ArrayList<IGErrorMessageResponseBean> errArray = new ArrayList<IGErrorMessageResponseBean>();
			errArray.add(errBean);
			resBean.setErrorMessages(errArray);
			bean.setResponseMessages(resBean);
			return bean;
		}
	}

	private HttpResponse doJsonPostRequest(String url)
			throws ClientProtocolException, IOException, IllegalStateException {

		HttpResponse httpResponse = null;

		HttpConnectionParams.setConnectionTimeout(_httpClient.getParams(),
				IngogoApp.getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(_httpClient.getParams(),
				IngogoApp.getConnectionTimeout());

		HttpPost httpPost = new HttpPost(url);

		// Add post body.

		StringEntity se = new StringEntity(_postParams);
		httpPost.setHeader("Content-type", "application/json");

		httpPost.setHeader("User-Agent", IngogoApp.getUserAgent());
		if (_userName != null) {
			httpPost.setHeader("Authorization", _userName + ":" + _password);
		}

		httpPost.setEntity(se);

		// Execute the request

		httpResponse = _httpClient.execute(httpPost);

		return httpResponse;
	}

	public HttpResponse doMultipartJsonPostRequest(String url)
			throws ClientProtocolException, IOException {
		HttpResponse httpResponse = null;
		HttpConnectionParams.setConnectionTimeout(_httpClient.getParams(),
				IngogoApp.getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(_httpClient.getParams(),
				120*1000);
		HttpPost httppost = new HttpPost(url);

		// Add post body.
		
		//httppost.setHeader("Content-type", "multipart/form-data");
		//httppost.setHeader("Content-Type", "multipart/form-data; boundary=randomBoundaryNotInAnyOfParts");
		
		httppost.setHeader("User-Agent", IngogoApp.getUserAgent());
		if (_userName != null) {
			httppost.setHeader("Authorization", _userName + ":" + _password);
		}
		httppost.setEntity(fileUploadEntity);

		// Execute the request
		httpResponse = _httpClient.execute(httppost);

		return httpResponse;
	}

}
