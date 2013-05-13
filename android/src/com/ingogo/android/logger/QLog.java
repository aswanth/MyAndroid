package com.ingogo.android.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
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

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.analytics.QBAnalytics;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.TrustedDefaultHttpClient;
import com.ingogo.android.webservices.easyssl.EasySSLSocketFactory;

public class QLog implements Thread.UncaughtExceptionHandler {

	static Application app = null;
	private static Thread.UncaughtExceptionHandler defaultUEH;
	private static QLog mQLogInstatnce;

	private static String QLOG_BASE_URL = "https://www.ingogo.mobi/qlogwebservices/summarylog/";
	private static String QLOG_DETAILED_BASE_URL = "https://www.ingogo.mobi/qlogwebservices/detailedreport/";
	private static String LOG_MEGGASE_TAG = "log_msg";
	private static String MODEL_TAG = "model";
	private static String BRAND_TAG = "brand";
	private static String VERSION_TAG = "version";
	private static String TIMESTAMP_TAG = "timestamp";
	private static String APP_ID_TAG = "app_id";
	private static String SEVERITY_TAG = "severity";
	private static String AVAILABLE_MEMORY_TAG = "avail_mem";
	private static String TOTAL_MEMORY_TAG = "total_mem";
	private static String NETWORK_STATUS_TAG = "status";

	private static String SEVERITY_DEBUG = "debug";
	private static String SEVERITY_WARNING = "warning";
	private static String SEVERITY_ERROR = "error";
	private static String SEVERITY_INFORMATION = "information";
	private static String SEVERITY_CRITICAL = "critical";

	private static boolean remoteLoggingEnabled;

	private QLog() {
		super();
		acceptUntrustedCertificates();

	}

	public static synchronized QLog getInstance() {

		if (mQLogInstatnce == null) {

			synchronized (QLog.class) {

				if (mQLogInstatnce == null) {
					mQLogInstatnce = new QLog();
				}

			}

		}
		return mQLogInstatnce;
	}

	/*
	 * For implementation, initialize QLog by calling QLog.setupLogging(this) in
	 * Application class.
	 */

	public static void setupLogging(Application application,
			boolean remoteLoggingEnabled) {
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		app = application;
		setRemoteLoggingEnabled(remoteLoggingEnabled);
		Thread.setDefaultUncaughtExceptionHandler(QLog.getInstance());
	}

	public static boolean isRemoteLoggingEnabled() {
		return remoteLoggingEnabled;
	}

	public static void setRemoteLoggingEnabled(boolean loggingEnabled) {
		remoteLoggingEnabled = loggingEnabled;
	}

	public static void v(java.lang.String tag, java.lang.String msg) {
		android.util.Log.v(tag, msg);
	}

	public static void v(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.v(tag, msg, tr);
	}

	public static void d(java.lang.String tag, java.lang.String msg) {
		android.util.Log.d(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_DEBUG, false);
	}

	public static void d(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.d(tag, msg, tr);
		postLog(tag + " - " + msg, SEVERITY_DEBUG, false);
	}

	public static void d(java.lang.String tag, java.lang.String msg,
			boolean isDetailedReport) {
		android.util.Log.d(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_DEBUG, isDetailedReport);
	}

	public static void w(java.lang.String tag, java.lang.String msg) {
		android.util.Log.w(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_WARNING, false);
	}

	public static void w(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.w(tag, msg, tr);
		postLog(tag + " - " + msg, SEVERITY_WARNING, false);
	}

	public static void w(java.lang.String tag, java.lang.String msg,
			boolean isDetailedReport) {
		android.util.Log.w(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_WARNING, isDetailedReport);
	}

	public static void e(java.lang.String tag, java.lang.String msg) {
		android.util.Log.e(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_ERROR, false);
	}

	public static void e(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.e(tag, msg, tr);
		postLog(tag + " - " + msg, SEVERITY_ERROR, false);
	}

	public static void e(java.lang.String tag, java.lang.String msg,
			boolean isDetailedReport) {
		android.util.Log.e(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_ERROR, isDetailedReport);
	}

	public static void i(java.lang.String tag, java.lang.String msg) {
		android.util.Log.i(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_INFORMATION, false);
	}

	public static void i(java.lang.String tag, java.lang.String msg,
			java.lang.Throwable tr) {
		android.util.Log.i(tag, msg, tr);
		postLog(tag + " - " + msg, SEVERITY_INFORMATION, false);
	}

	public static void i(java.lang.String tag, java.lang.String msg,
			boolean isDetailedReport) {
		android.util.Log.i(tag, msg);
		postLog(tag + " - " + msg, SEVERITY_INFORMATION, isDetailedReport);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Auto-generated method stub
		StackTraceElement[] arr = e.getStackTrace();
		String report = e.toString() + "\n\n";
		report += "--------- Stack trace ---------\n\n";
		for (int i = 0; i < arr.length; i++) {
			report += "    " + arr[i].toString() + "\n";
		}
		report += "-------------------------------\n\n";

		// If the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		report += "--------- Cause ---------\n\n";
		Throwable cause = e.getCause();
		if (cause != null) {
			report += cause.toString() + "\n\n";
			arr = cause.getStackTrace();
			for (int i = 0; i < arr.length; i++) {
				report += "    " + arr[i].toString() + "\n";
				String causeString = arr[i].toString();
				if (causeString.contains("com.ingogo.android")) {
					// QBAnalytics.logError(cause.toString(), "",causeString);
					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put("errorCode", cause.toString());
					parameters.put("errorClass", causeString);
					parameters.put("TIME", "" + System.currentTimeMillis());
					QBAnalytics.logErrorEvent("Exceptions", parameters);
				}
			}
		}
		report += "-------------------------------\n\n";

		postLog(report, SEVERITY_CRITICAL, true);
		try {
			// IGUtility.logRandomDetailsToAnalytics("Exceptions", report);

		} catch (Exception ex) {

		}

		//IngogoApp.getSharedApplication().resetApp();
		 defaultUEH.uncaughtException(t, e);

	}

	private static void postLog(final String report, final String severity,
			final boolean isDetailedReport) {

		HashMap<String, String> logDetailsMap = new HashMap<String, String>();
		logDetailsMap.put("TIME", "" + System.currentTimeMillis());
		logDetailsMap.put("IMEI", getIMEINumber());
		if (IngogoApp.getSharedApplication().getUserId() != null) {
			logDetailsMap.put("MOBILE NUMBER", IngogoApp.getSharedApplication()
					.getUserId());
		}
		logDetailsMap.put("DETAILS", report);
		String detailsString = logDetailsMap.toString();
		Log.i("QLOG DETAILS MAP", "QLOG = " + detailsString);
		QLogCache.getSharedInstance().cacheLog(detailsString);

	}

	static String getIMEINumber() {
		TelephonyManager telephonyManager = (TelephonyManager) IngogoApp
				.getSharedApplication().getApplicationContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceID = telephonyManager.getDeviceId();
		return deviceID;
	}

	private static class PostAsync extends AsyncTask<String, Void, String> {

		String severity, report;
		boolean isDetailedReport;

		public PostAsync(final String report, final String severity,
				final boolean isDetailedReport) {
			this.severity = severity;
			this.report = report;
			this.isDetailedReport = isDetailedReport;
		}

		@Override
		protected String doInBackground(String... message) {

			String responseString = "";
			try {
				if (isRemoteLoggingEnabled() == false) {
					return responseString;
				}
				TrustedDefaultHttpClient httpClient = new TrustedDefaultHttpClient(
						IngogoApp.getSharedApplication()
								.getApplicationContext());
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				schemeRegistry.register(new Scheme("https",
						new EasySSLSocketFactory(), 443));

				HttpParams params = new BasicHttpParams();
				int timeoutConnection = 5000;
				HttpConnectionParams.setConnectionTimeout(params,
						timeoutConnection);
				int timeoutSocket = 10000;
				HttpConnectionParams.setSoTimeout(params, timeoutSocket);

				params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
				params.setParameter(
						ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
						new ConnPerRouteBean(30));
				params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE,
						false);
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

				ClientConnectionManager cm = new SingleClientConnManager(null,
						schemeRegistry);
				httpClient = new TrustedDefaultHttpClient(cm, params, IngogoApp
						.getSharedApplication().getApplicationContext());
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return (new PasswordAuthentication("ingogomanager",
								"p$g%hk2ksSa".toCharArray()));
					}
				});

				// HttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost();
				if (isDetailedReport) {
					request.setURI(new URI(QLOG_DETAILED_BASE_URL));
				} else {

					request.setURI(new URI(QLOG_BASE_URL));
				}
				Log.e("QLOG REQUEST", report);
				request.setHeader("Content-Type", "text/xml; charset=utf-8");

				StringEntity se = new StringEntity(getPostParamsForReport(
						report, severity, false));

				request.setEntity(se);
				HttpResponse response = httpClient.execute(request);
				response.getEntity().consumeContent();
				InputStream inputStream = response.getEntity().getContent();
				responseString = collectDataString(new InputStreamReader(
						inputStream, "UTF-8"));
				Log.e("QLOG", "QLOG RESPONSE" + responseString);
				return responseString;

			} catch (Exception e) {
				// /////
				StackTraceElement[] arr = e.getStackTrace();
				String report = e.toString() + "\n\n";
				report += "--------- Stack trace ---------\n\n";
				for (int i = 0; i < arr.length; i++) {
					report += "    " + arr[i].toString() + "\n";
				}
				report += "-------------------------------\n\n";

				// If the exception was thrown in a background thread inside
				// AsyncTask, then the actual exception can be found with
				// getCause
				report += "--------- Cause ---------\n\n";
				Throwable cause = e.getCause();
				if (cause != null) {
					report += cause.toString() + "\n\n";
					arr = cause.getStackTrace();
					for (int i = 0; i < arr.length; i++) {
						report += "    " + arr[i].toString() + "\n";

					}
				}
				report += "-------------------------------\n\n";
				// //////////////
				cacheReponse(report, report);
				e.printStackTrace();

			}
			return responseString;
		}

	}

	private static void cacheReponse(String report, String exception) {
		if (report.trim().length() > 1) {
			long timestamp = System.currentTimeMillis() / 1000L;
			report = "\n\nFAILED QLOG - " + String.valueOf(timestamp) + "\n"
					+ report + "\n QLOG FAILURE REASON : " + exception;
			QLogCache.getSharedInstance().cacheLog(report);
		}

	}

	private void acceptUntrustedCertificates() {

	}

	protected static String getPostParamsForReport(String report,
			String severity, boolean isDetailedReport) {

		// phone model
		String PhoneModel = android.os.Build.MODEL;

		// Android version
		String AndroidVersion = android.os.Build.VERSION.RELEASE;

		// Brand
		String Brand = android.os.Build.BRAND;

		// Unix timestamp
		long timestamp = System.currentTimeMillis() / 1000L;

		// Unique app identifier
		String appId = app.getPackageName();
		String driverId = IngogoApp.getSharedApplication().getUserId();
		if (driverId != null) {
			report = "Driver id: " + driverId + " Message: " + report;
		} else {
			report = " Message: " + report;

		}

		String postBody = new String();

		postBody += "&" + LOG_MEGGASE_TAG + "=" + report;

		postBody += "&" + MODEL_TAG + "=" + PhoneModel;

		postBody += "&" + BRAND_TAG + "=" + Brand;

		postBody += "&" + VERSION_TAG + "=" + AndroidVersion;

		postBody += "&" + TIMESTAMP_TAG + "=" + String.valueOf(timestamp);

		postBody += "&" + APP_ID_TAG + "=" + appId;

		postBody += "&" + SEVERITY_TAG + "=" + severity;

		if (isDetailedReport) {
			postBody += "&" + AVAILABLE_MEMORY_TAG + "="
					+ String.valueOf(getAvailableInternalMemorySize());
			postBody += "&" + TOTAL_MEMORY_TAG + "="
					+ String.valueOf(getTotalInternalMemorySize());

			NetworkInfo info = ((ConnectivityManager) app
					.getApplicationContext().getSystemService(
							Context.CONNECTIVITY_SERVICE))
					.getActiveNetworkInfo();
			postBody += "&" + NETWORK_STATUS_TAG + "=" + info.getTypeName();
		}
		return postBody;

	}

	protected static String collectDataString(InputStreamReader isr) {
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String aLine = null;
		do {
			try {
				aLine = br.readLine();
				if (aLine != null)
					sb.append(aLine.trim());
			} catch (IOException e) {
			}
		} while (aLine != null);
		return sb.toString();
	}

	// Memory Info
	protected static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	protected static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

}
