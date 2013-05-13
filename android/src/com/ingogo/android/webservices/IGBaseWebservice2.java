package com.ingogo.android.webservices;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

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
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.ingogo.android.R;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.beans.IGBaseBean;
import com.ingogo.android.webservices.beans.response.IGBaseResponseBean;
import com.ingogo.android.webservices.beans.response.IGErrorMessageResponseBean;
import com.ingogo.android.webservices.beans.response.IGResponseMessagesBean;
import com.ingogo.android.webservices.easyssl.EasySSLSocketFactory;
import com.ingogo.android.webservices.interfaces.IGApiListener;


public class IGBaseWebservice2 extends AsyncTask<String, Void, String> implements RejectedExecutionHandler{

	private String _postParams = null;
//	private static TrustedDefaultHttpClient _httpClient = new TrustedDefaultHttpClient(
//			IngogoApp.getSharedApplication().getApplicationContext());
	private static TrustedDefaultHttpClient _httpClient = null;
	private HttpResponse _httpResponse = null;
	private IGApiListener _apiListener;
	private int _httpStatus = 0;
	private String _userName = null, _password = null;
	private Class<? extends IGBaseResponseBean> _responseClass;

	public IGBaseWebservice2(IGApiListener apiListener) {
		this._apiListener = apiListener;
		this.acceptUntrustedCertificates();
	}

	public IGBaseWebservice2(IGApiListener apiListener, String postParams) {
		this._apiListener = apiListener;
		this._postParams = postParams;
		this.acceptUntrustedCertificates();
	}

	public IGBaseWebservice2(IGApiListener apiListener, String postParams,
			Class<? extends IGBaseResponseBean> responseClass) {
		this._apiListener = apiListener;
		this._postParams = postParams;
		this._responseClass = responseClass;
		this.acceptUntrustedCertificates();
	}

	private void acceptUntrustedCertificates() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
		HttpParams params = new BasicHttpParams();
	    int timeoutConnection = IngogoApp.getConnectionTimeout();
	    HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
	    int timeoutSocket = IngogoApp.getConnectionTimeout();
	    HttpConnectionParams.setSoTimeout(params, timeoutSocket);

		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		
		ClientConnectionManager cm = new SingleClientConnManager(null, schemeRegistry);
		_httpClient = new TrustedDefaultHttpClient(cm, params, IngogoApp.getSharedApplication().getApplicationContext());
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
	protected String doInBackground(String... urls) {

		String serverResponse = null;

		if (IGUtility.isNetworkAvailable(IngogoApp.getSharedApplication()
				.getApplicationContext())) {
			try {
				// POST
				_httpResponse = this.doJsonPostRequest(urls[0]);

				if (_httpResponse != null) {

					serverResponse = IGUtility
							.convertStreamToString(_httpResponse.getEntity()
									.getContent());
					_httpStatus = _httpResponse.getStatusLine().getStatusCode();

				} else {

					serverResponse = IGApiConstants.kApiError;
				}
			} catch (ClientProtocolException e) {
				serverResponse = IGApiConstants.kApiError;
				e.printStackTrace();
			} catch (IOException e) {
				serverResponse = IGApiConstants.kApiError;
				e.printStackTrace();
			} catch(RejectedExecutionException e) {
				serverResponse = IGApiConstants.kApiError;
			}
		} else {

			serverResponse = IGApiConstants.kNetworkError;

		}
		return serverResponse;
	}

	@Override
	protected void onPostExecute(String result) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put(IGApiConstants.kHttpStatusKey, _httpStatus);

		if (result.equalsIgnoreCase(IGApiConstants.kNetworkError)) {

			response.put(IGApiConstants.kErrorMsgKey,
					IGApiConstants.kNetworkErrorExceptionKey);
			_apiListener.onFailedToGetResponse(response);

		} else if (result != null
				&& (_httpStatus == HttpStatus.SC_OK || _httpStatus == HttpStatus.SC_CREATED)) {

			
			IGBaseResponseBean resultBean = (IGBaseResponseBean)getBeanFromResult(result);
			if (resultBean.getResponseCode().equalsIgnoreCase("FAILED")) {
				response.put(IGApiConstants.kApiFailedMsgKey,
						getBeanFromResult(result));
				_apiListener.onFailedToGetResponse(response);

			} else {
				response.put(IGApiConstants.kSuccessMsgKey,
						getBeanFromResult(result));
				_apiListener.onResponseReceived(response);

			}

		} else if (_httpStatus == 500 || _httpStatus == 401) {

			response.put(IGApiConstants.kErrorMsgKey,
					IGApiConstants.kInternalServerErrorExceptionKey);
			_apiListener.onFailedToGetResponse(response);

		} else if (result.equalsIgnoreCase(IGApiConstants.kApiError)) {
			response.put(IGApiConstants.kErrorMsgKey,
					IGApiConstants.kTimeOutErrorExceptionKey);
			_apiListener.onFailedToGetResponse(response);

		} else {

			response.put(IGApiConstants.kApiFailedMsgKey,
					getBeanFromResult(result));
			_apiListener.onResponseReceived(response);
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
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}

			bean.setResponseCode(IGApiConstants.kApiFailedMsgKey);
			IGResponseMessagesBean resBean = new IGResponseMessagesBean();
			IGErrorMessageResponseBean errBean = new IGErrorMessageResponseBean();
			errBean.setCode(IGApiConstants.kApiFailedMsgKey);
			errBean.setContent(IngogoApp
					.getSharedApplication()
					.getApplicationContext()
					.getResources()
					.getString(R.string.generic_error_message));
			ArrayList<IGErrorMessageResponseBean> errArray = new ArrayList<IGErrorMessageResponseBean>();
			errArray.add(errBean);
			resBean.setErrorMessages(errArray);
			bean.setResponseMessages(resBean);
			return bean;
		}
	}

	private HttpResponse doJsonPostRequest(String url)
			throws ClientProtocolException, IOException {

		
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
       try {
		httpResponse = _httpClient.execute(httpPost);
       }catch(Exception e) {
    	   
       }

		return httpResponse;
	}

	@Override
	public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put(IGApiConstants.kHttpStatusKey, "0");
		response.put(IGApiConstants.kErrorMsgKey,
				"Api error");
		_apiListener.onFailedToGetResponse(response);
		
	}
}
