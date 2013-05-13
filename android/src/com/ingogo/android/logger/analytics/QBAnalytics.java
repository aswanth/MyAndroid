package com.ingogo.android.logger.analytics;

import java.util.Map;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;

import com.ingogo.android.app.IngogoApp;

/**
 * Analytics wrapper class --> included flurry - future plans to add other
 * analytics methods like google analytics, embedded agent and Qlog
 * 
 * @author suslov
 * 
 */
public class QBAnalytics {

	private static String flurryKey = null;
	private static boolean isAnalyticsEnabled = false;

	private static boolean isFlurryEnabled;

	/**
	 * Starts the Analytics session with location service
	 * 
	 * @param context
	 */
	public static void startSessionWithLocationServices(Context context) {

		if (isFlurryEnabled) {

			QBFlurry.startSessionWithLocationServices(context);

		}

	}

	/**
	 * Starts the Analytics session without location service
	 * 
	 * @param context
	 */
	public static void startSession(Context context) {

		if (isFlurryEnabled) {

			QBFlurry.startSession(context);

		}
	}

	/**
	 * Ends the Analytics session
	 * 
	 * @param context
	 */
	public static void endSession(Context context) {

		if (context == null) {
			/*
			 * Flurry will stop the session
			 */
			Log.w("FlurryAgent.onStartSession",
					"Attempt to initialize without active context");
		} else {
			if (isFlurryEnabled) {

				QBFlurry.endSession(context);
			}
		}
	}

	/**
	 * Retrieves the Analytics application flurryKey for the started session
	 * 
	 * @return
	 */
	public static String getFlurryKey() {
		/*
		 * If a flurry session is running, the flurry session flurryKey is
		 * returned, otherwise null value is returned to indicate flurry session
		 * is not started
		 */

		return flurryKey;
	}

	/**
	 * logs the error
	 * 
	 * @param errorCode
	 * @param message
	 * @param exception
	 */
	public static void logError(String errorCode, String message,
			Exception exception) {
		if (isFlurryEnabled) {

			QBFlurry.logError(errorCode, message, exception);

		}

	}

	/**
	 * not to use logs the events
	 * 
	 * @param eventName
	 * @param parameters
	 */
	public static void logEvent(String eventName, Map<String, String> parameters) {

		if (isFlurryEnabled) {
			
			if (IngogoApp.getSharedApplication().getUserId() != null) {
				parameters.put("mobileNumber", IngogoApp.getSharedApplication()
						.getUserId());
			}

			QBFlurry.logEvent(eventName, parameters);
		}

	}
	

	public static void logErrorEvent(String eventName, Map<String, String> parameters) {

		if (isFlurryEnabled) {
			
//			QBFlurry.setLocation();
			QBFlurry.logEvent(eventName, parameters);
		}

	}


	/**
	 * Page view count calculation
	 */
	public static void logPageViews() {
		if (isFlurryEnabled) {


		QBFlurry.logPageViews();

		}
	}

	public static void trackScreenEntry(String screenName) {
		if (isFlurryEnabled) {

			QBFlurry.logPageViews();

		}

	}

	/**
	 * logs the events
	 * 
	 * @param eventName
	 */
	public static void logEvent(String eventName) {
		if (isFlurryEnabled) {

			QBFlurry.logEvent(eventName);
		}
	}

	/**
	 * log timed event
	 * 
	 * @param eventName
	 * @param parameters
	 */
	public static void logTimedEvent(String eventName) {
		if (isFlurryEnabled) {

			QBFlurry.logTimedEvent(eventName);
		}

	}

	/**
	 * end a timed event
	 * 
	 * @param eventName
	 */
	public static void endTimedEvent(String eventName) {
		if (isFlurryEnabled) {

			QBFlurry.endTimedEvent(eventName);

		}

	}

	public static void setFlurryEnabled(boolean isFlurryEnabled) {
		QBAnalytics.isFlurryEnabled = isFlurryEnabled;
	}

	public static boolean isFlurryEnabled() {
		return isFlurryEnabled;
	}

	public static void setAnalyticsEnabled(boolean isAnalyticsEnabled) {
		Log.i("setAnalyticsEnabled", "" + isAnalyticsEnabled);
		QBAnalytics.isAnalyticsEnabled = isAnalyticsEnabled;
		isFlurryEnabled = isAnalyticsEnabled;

	}

	public static boolean isAnalyticsEnabled() {
		return isAnalyticsEnabled;
	}

}