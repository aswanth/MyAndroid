/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : A wrapper class that performs get and post requests and returns http response back to the calling class.
 */

package com.ingogo.android.webservices;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGBaseActivity;
import com.ingogo.android.activities.IGJobsActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.poll.IGAvailableJobsPollingTask;
import com.ingogo.android.poll.IGChatService;
import com.ingogo.android.poll.IGIncomingMessagePollingTask;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.easyssl.EasySSLSocketFactory;

/**

 */

public class IGBaseWebService extends Thread {

	private static TrustedDefaultHttpClient httpClient = new TrustedDefaultHttpClient(
			IngogoApp.getSharedApplication().getApplicationContext());
	String _postContent;
	HttpResponse _response;
	private Handler _handler;
	private IGCallbackWrapper _wrapper;
	private String _url;
	private int _apiId;
	private String _authHeaderValue = null;
	private Activity _currentActivity = null;

	JSONObject _jsonObject = new JSONObject();
	Map<String, Object> _responseMap = new HashMap<String, Object>();
	ArrayList<String> _list;
	private String apiCalledTime;

	/**
	 * Basic constructor for a HTTP GET / POST.
	 * 
	 * @param postParams
	 *            - Parameters for post request
	 * @param handler
	 *            - Handler used to update the response on the main thread.
	 * @param wrapper
	 *            - Callback wrapper which fires the interface method calls to
	 *            the calling activity.
	 * @param url
	 *            - URL on which this request should be made.
	 */

	protected IGBaseWebService(String postContent, Handler handler,
			IGCallbackWrapper wrapper, String url, int apiId) {

		this._postContent = postContent;
		this._handler = handler;
		this._wrapper = wrapper;
		this._url = url;
		this._apiId = apiId;
		setPriority(Thread.MIN_PRIORITY);
		apiCalledTime = IGUtility.getTimeString();
		this.acceptUntrustedCertificates();
		
	}

	private void acceptUntrustedCertificates() {
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
		httpClient = new TrustedDefaultHttpClient(cm, params, IngogoApp
				.getSharedApplication().getApplicationContext());
	}

	public void setAuthorizationHeader(String headerValue) {
		this._authHeaderValue = headerValue;
	}

	public void run() {

		try {
			synchronized (httpClient) {
				try {

					if (IGUtility.isNetworkAvailable(IngogoApp
							.getSharedApplication())) {
//						IGUtility.logDetailsToAnalytics(
//								IGConstants.kAnalyticsWebserviceReqCall, _url);
						// POST
						if (_url != null) {
							List<String> items = Arrays.asList(_url.split(("/")));
							Log.e("API CALL","For APi call " + items.get(items.size() - 1)+"MADE AT: "+apiCalledTime+" CALLED AT: "+IGUtility.getTimeString());
						}
						if (_postContent != null) {
							_response = doJsonPostRequest();
						} else {
							_response = doJsonGetRequest();
						}
					} else {
						_responseMap
								.put(IGApiConstants.kErrorMsgKey,
										buildLocalJSONErrorResponse(IngogoApp
												.getSharedApplication()
												.getApplicationContext()
												.getString(
														R.string.ReachabilityMessage)));
					}

					if (_response != null) {

						// Put the response status code in the response map.
						int statusCode = _response.getStatusLine()
								.getStatusCode();

						if (statusCode == IGApiConstants.kHttpStatusOK
								|| statusCode == IGApiConstants.kHttpStatusBadRequest) {
							IngogoApp.getSharedApplication()
									.setAuthFailureCount(0);
							_jsonObject = validateJSONResponse(_response
									.getEntity());

						} else if (statusCode == IGApiConstants.kHttpStatusForbidden) {
							Log.i("Count B4", ""
									+ IngogoApp.getSharedApplication()
											.getAuthFaileureCount());
							final JSONObject jObject = new JSONObject(
									IGUtility.convertStreamToString(_response
											.getEntity().getContent()));
							IGUtility
									.logDetailsToAnalytics(
											IGConstants.kAnalyticsWebserviceFailureEventName,
											_url,
											IGConstants.kAnalyticsFailedWithDetails
													+ jObject.toString());

							// Donot increment the counter for polling tasks.
							if (_apiId != IGApiConstants.kJobsWebServiceId
									&& _apiId != IGApiConstants.kIncomingMessageWebServiceId
									&& _apiId != IGApiConstants.kUpdateCurrentPositionWebServiceId) {
								Log.i("COUNT", "INCREMENTED");
								IngogoApp
										.getSharedApplication()
										.setAuthFailureCount(
												IngogoApp
														.getSharedApplication()
														.getAuthFaileureCount() + 1);
							}
							Log.i("Count AFTR", ""
									+ IngogoApp.getSharedApplication()
											.getAuthFaileureCount());
							// Invalid or expired token. Restart our application
							_handler.post(new Runnable() {
								@Override
								public void run() {
									IGBaseWebService.this
											.processForbiddenResponse(jObject);
								}

							});

						} else {
							JSONObject jObject = new JSONObject(
									IGUtility.convertStreamToString(_response
											.getEntity().getContent()));
							IGUtility
									.logDetailsToAnalytics(
											IGConstants.kAnalyticsWebserviceFailureEventName,
											_url,
											IGConstants.kAnalyticsFailedWithDetails
													+ jObject.toString());

							IngogoApp.getSharedApplication()
									.setAuthFailureCount(0);

							this.handleExceptions();
						}

					} else {
						IGUtility
								.logDetailsToAnalytics(
										IGConstants.kAnalyticsWebserviceFailureEventName,
										_url, "Failed");

						_wrapper.setErrorResponse(_responseMap, _apiId);
					}

				} catch (ConnectTimeoutException ctEx) {
					IGUtility.logDetailsToAnalytics(
							IGConstants.kAnalyticsWebserviceFailureEventName,
							_url, IGConstants.kAnalyticsWebserviceTimeout);

					if (_apiId == IGApiConstants.kCompleteOfflineWebServiceId) {

						QLog.d("WEBSERVICE",
								"Complete offline w/s time out reponse reached for mobile number "
										+ IngogoApp.getSharedApplication()
												.getUserId());
					}
					this.handleExceptions();
				} catch (SocketTimeoutException e) {
					IGUtility.logDetailsToAnalytics(
							IGConstants.kAnalyticsWebserviceFailureEventName,
							_url, IGConstants.kAnalyticsWebserviceTimeout);

					if (_apiId == IGApiConstants.kCompleteOfflineWebServiceId) {
						QLog.d("WEBSERVICE",
								"Complete offline w/s time out reponse reached for mobile number "
										+ IngogoApp.getSharedApplication()
												.getUserId());
					}
					this.handleExceptions();
				} catch (NoRouteToHostException ctEx) {
					IGUtility.logDetailsToAnalytics(
							IGConstants.kAnalyticsWebserviceFailureEventName,
							_url, "NoRouteToHostException : " + ctEx);

					this.handleExceptions();
				} catch (UnknownServiceException ctEx) {
					IGUtility.logDetailsToAnalytics(
							IGConstants.kAnalyticsWebserviceFailureEventName,
							_url, "UnknownServiceException : " + ctEx);

					this.handleExceptions();
				} catch (UnknownHostException uhEx) {
					IGUtility.logDetailsToAnalytics(
							IGConstants.kAnalyticsWebserviceFailureEventName,
							_url, "UnknownHostException : " + uhEx);

					this.handleExceptions();
				} catch (ConnectException cnEx) {
					IGUtility.logDetailsToAnalytics(
							IGConstants.kAnalyticsWebserviceFailureEventName,
							_url, "ConnectException : " + cnEx);

					this.handleExceptions();
				} catch (ClientProtocolException e) {
					IGUtility.logDetailsToAnalytics(
							IGConstants.kAnalyticsWebserviceFailureEventName,
							_url, "ClientProtocolException : " + e);

					this.handleExceptions();

					e.printStackTrace();

				} catch (IOException e) {
					IGUtility.logDetailsToAnalytics(
							IGConstants.kAnalyticsWebserviceFailureEventName,
							_url, "IOException : " + e);

					this.handleExceptions();

					e.printStackTrace();
				}
			}

		} catch (JSONException e) {
			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName, _url,
					"JSONException : " + e);
			this.handleExceptions();

			e.printStackTrace();

		}

		// process response on Main Thread

		_handler.post(_wrapper);

	}

	private void handleExceptions() {

		_responseMap.put(IGApiConstants.kErrorMsgKey,
				buildLocalJSONErrorResponse(IGApiConstants.kExceptionKey));

		_wrapper.setErrorResponse(_responseMap, _apiId);
	}

	private void processForbiddenResponse(JSONObject jObject) {
		try {
			
			String status = jObject.getString(IGApiConstants.kStatusKey);
			Log.d("API STATUS", "API ID : " + _apiId
					+ ", Forbidden - STATUS = " + status);

			if (status.equals(IGApiConstants.kStatusError)) {
				_responseMap.put(IGApiConstants.kErrorMsgKey, jObject);
			}

		} catch (Exception e) {
			IGUtility
			.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_url,
					"processForbiddenResponse : " + e);
			e.printStackTrace();
		}

		if (_apiId == IGApiConstants.kUpdateCurrentPositionWebServiceId) {
			IGUpdatePositionPollingTask pollingTask = (IGUpdatePositionPollingTask) _wrapper
					.getResponseListener();
			if (pollingTask != null) {

				_currentActivity = pollingTask.getCallingActivity();
			}

		} else if (_apiId == IGApiConstants.kJobsWebServiceId) {
			IGAvailableJobsPollingTask pollingTask = (IGAvailableJobsPollingTask) _wrapper
					.getResponseListener();
			if (pollingTask != null) {
				_currentActivity = pollingTask.getCallingActivity();
			}

		} else if (_apiId == IGApiConstants.kIncomingMessageWebServiceId) {
			IGIncomingMessagePollingTask pollingTask = (IGIncomingMessagePollingTask) _wrapper
					.getResponseListener();

			if (pollingTask != null) {
				IGChatService pollingService = (IGChatService) pollingTask
						.getListener();
				if (pollingService != null) {
					_currentActivity = pollingService.getCallingActivity();
				}
			}

		} else {
			_currentActivity = (Activity) _wrapper.getResponseListener();
		}

		if (_currentActivity != null) {

			IGBaseActivity activity = (IGBaseActivity) _currentActivity;
			activity.onAuthenticationFailure(_responseMap, _apiId);
			this.interrupt();
		}
	}

	private JSONObject buildLocalJSONErrorResponse(String errorMsg) {
		JSONObject jObject = new JSONObject();
		try {
			jObject.put(IGApiConstants.kStatusKey, IGApiConstants.kStatusError);
			JSONArray errorArray = new JSONArray();
			JSONObject errorObj = new JSONObject();
			JSONObject errorMsgObj = new JSONObject();

			errorObj.put("code", "E099");
			errorObj.put("content", errorMsg);
			errorArray.put(errorObj);
			errorMsgObj.put("errorMessages", errorArray);
			jObject.put("responseMessages", errorMsgObj);

		} catch (JSONException e) {
			IGUtility
			.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_url,
					"buildLocalJSONErrorResponse : " + e);
			e.printStackTrace();
		}
		return jObject;
	}

	private JSONObject validateJSONResponse(HttpEntity entity)
			throws JSONException, UnsupportedEncodingException,
			IllegalStateException, IOException {

		JSONObject jObject = new JSONObject(
				IGUtility.convertStreamToString(entity.getContent()));
		String status = jObject.getString(IGApiConstants.kStatusKey);
		Log.d("API STATUS", "API ID : " + _apiId + ", STATUS = " + status);

		if (status.equals(IGApiConstants.kStatusOK)) {
			_responseMap.put(IGConstants.kDataKey, jObject);
			_wrapper.setResponse(_responseMap, _apiId);
			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceSuccessEventName, _url,
					"Success");
			// If passenger pays through pay offline ,then an alert must be
			// shown
			// in jobs activity. For that we use this condition.
			if (_apiId == IGApiConstants.kCompleteOfflineWebServiceId) {
				IngogoApp.getSharedApplication().setComingFromPayOffline(true);
				QLog.d("WEBSERVICE",
						"Complete offline w/s success reponse reached for mobile number "
								+ IngogoApp.getSharedApplication().getUserId());

			}

		} else if (status.equals(IGApiConstants.kStatusError)) {

			_responseMap.put(IGApiConstants.kErrorMsgKey, jObject);
			_wrapper.setErrorResponse(_responseMap, _apiId);
			if (_apiId == IGApiConstants.kCompleteOfflineWebServiceId) {
				QLog.d("WEBSERVICE",
						"Complete offline w/s failed reponse reached for mobile number "
								+ IngogoApp.getSharedApplication().getUserId());
			}
			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_url,
					IGConstants.kAnalyticsFailedWithDetails
							+ jObject.toString());

		} else if (status.equals(IGApiConstants.kStatusSystemError)) {
			if (_apiId == IGApiConstants.kCompleteOfflineWebServiceId) {
				QLog.d("WEBSERVICE",
						"Complete offline w/s failed reponse reached for mobile number "
								+ IngogoApp.getSharedApplication().getUserId());
			}
			_responseMap.put(IGApiConstants.kErrorMsgKey, jObject);
			_wrapper.setErrorResponse(_responseMap, _apiId);
			IGUtility.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_url,
					IGConstants.kAnalyticsFailedWithDetails
							+ jObject.toString());

		}

		return jObject;
	}

	/**
	 * Performs a HTTP Get Request using JSON and returns an HTTP response.
	 * 
	 * @return http get response
	 * @throws ClientProtocolException
	 * @throws IOException
	 */

	public HttpResponse doJsonGetRequest() throws ClientProtocolException,
			IOException {

		HttpGet request = new HttpGet(_url);
		if (_authHeaderValue != null) {
			request.addHeader(IGApiConstants.kAuthHeaderKey, _authHeaderValue);
		}

		request.setHeader("User-Agent", IngogoApp.getUserAgent());
		return httpClient.execute(request);

	}

	/**
	 * Performs an HTTP Post request using JSON and returns an HTTP Response.
	 * 
	 * @return http post response
	 * @throws ClientProtocolException
	 * @throws IOException
	 */

	public HttpResponse doJsonPostRequest() throws ClientProtocolException,
			IOException {

		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				IngogoApp.getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(httpClient.getParams(),
				IngogoApp.getConnectionTimeout());
		HttpResponse httpResponse = null;

		HttpPost httppost = new HttpPost(_url);

		// Add post json body.
		StringEntity se = new StringEntity(_postContent);
		httppost.setHeader("accept", "application/json");
		httppost.setHeader("Content-Type", "application/json");
		httppost.setHeader("User-Agent", IngogoApp.getUserAgent());
		httppost.setEntity(se);

		// Execute the request
		try {
			httpResponse = httpClient.execute(httppost);
		} catch (IllegalStateException e) {
			Log.i("Illegal State Exception", "Illegal State Exception");
			IGUtility
			.logDetailsToAnalytics(
					IGConstants.kAnalyticsWebserviceFailureEventName,
					_url,
					"doJsonPostRequest : " + e);
		}catch(OutOfMemoryError e) {
			
		}

		return httpResponse;
	}

}
