package com.ingogo.android.logger.analytics;

import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;

public class QBFlurry {

	private static String flurryKey = null;
	public static String FLURRY_APP_KEY = "H75CPVR5D8WDZJ87QDFT";

	public static void startSessionWithLocationServices(Context context) {

		/*
		 * Flurry will start the session using flurry app flurryKey at once and
		 * set the flurryKey value on successfull initialization. Once flurryKey
		 * is set other session start calls cannot initialize flurry again.
		 */
		if (flurryKey == null) {
			try {
				FlurryAgent.setReportLocation(true);
				FlurryAgent.onStartSession(context, FLURRY_APP_KEY);
				FlurryAgent.setLogEnabled(true);
				flurryKey = FLURRY_APP_KEY;
				Log.i("FlurryAgent.onStartSession",
						"started with location services");
			} catch (Exception e) {
				Log.e("FlurryAgent.onStartSession",
						"Error in initializing Flurry : " + e.getMessage());

			}
		} else {
			Log.w("FlurryAgent.onStartSession",
					"Attempt to initialize multiple flurry sessions. Another session is already running.");

		}

	}

	public static void startSession(Context context) {

//		/*
//		 * Flurry will start the session using flurry app flurryKey at once and
//		 * set the flurryKey value on successful initialization. Once flurryKey
//		 * is set other session start calls cannot initialize flurry again.
//		 */
//		if (context == null) {
//			Log.w("FlurryAgent.onStartSession",
//					"Attempt to initialize without active context");
//		} else {
//			if (flurryKey == null) {
//				try {
//					FlurryAgent.setReportLocation(false);
//					FlurryAgent.onStartSession(context, FLURRY_APP_KEY);
//					FlurryAgent.setLogEnabled(true);
//					flurryKey = FLURRY_APP_KEY;
//
//					Log.i("FlurryAgent.onStartSession", "started");
//				} catch (Exception e) {
//
//					Log.e("FlurryAgent.onStartSession",
//							"Error in initializing Flurry : " + e.getMessage());
//
//				}
//			} else {
//				Log.w("FlurryAgent.onStartSession",
//						"Attempt to initialize multiple flurry sessions. Another session is already running.");
//
//			}
//		}

	}

	public static void setUserId(String userId) {
//		if (flurryKey != null) {
//
//			try {
//				FlurryAgent.setUserId(userId);
//			} catch (Exception e) {
//				Log.e("FlurryAgent.setUserId",
//						"Error adding user id: " + e.getMessage());
//			}
//		}
	}

	public static void endSession(Context context) {

//		if (flurryKey != null) {
//			try {
//				FlurryAgent.onEndSession(context);
//				flurryKey = null;
//			} catch (Exception e) {
//				Log.e("FlurryAgent.onEndSession",
//						"Error ending session : " + e.getMessage());
//			}
//		}

	}

	public static String getFlurryKey() {
		/*
		 * If a flurry session is running, the flurry session flurryKey is
		 * returned, otherwise null value is returned to indicate flurry session
		 * is not started
		 */

		return flurryKey;
	}

	public static void logError(String errorCode, String message,
			Exception exception) {

//		if (errorCode == null || message == null) {
//			Log.w("FlurryAgent.onError",
//					"Flurry is called with null arguements");
//		} else {
//
//			if (flurryKey != null) {
//				try {
//					if (exception.getClass().getName() != null) {
//						FlurryAgent.onError(errorCode, message, exception
//								.getClass().getName());
//					} else {
//						FlurryAgent.onError(errorCode, message, "");
//					}
//				} catch (Exception e) {
//					Log.e("FlurryAgent.onLogError",
//							"Error in logging errors : " + e.getMessage());
//				}
//			} else {
//				Log.w("FlurryAgent.onError",
//						"Flurry is called without initilization");
//
//			}
//		}

	}

	public static void logEvent(String eventName, Map<String, String> parameters) {

//		if (eventName == null) {
//			Log.w("FlurryAgent.logEvent", "Flurry is called with null values");
//
//		} else {
//
//			if (flurryKey != null) {
//				try {
//					FlurryAgent.logEvent(eventName, parameters);
//					// Log.i("FlurryAgent.logEvent", "Call Success");
//				} catch (Exception e) {
//					Log.e("FlurryAgent.logEvent", "Error in logging events : "
//							+ e.getMessage());
//				}
//			} else {
//				Log.w("FlurryAgent.logEvent",
//						"Flurry is called without initilization");
//
//			}
//		}

	}

	public static void logPageViews() {

//		if (flurryKey != null) {
//			try {
//				FlurryAgent.onPageView();
//				// IGUtility.logRandomDetailsToAnalytics(IngogoApp
//				// .getSharedApplication().getApplicationContext()
//				// .getClass().getName(), null);
//				// FlurryAgent.logEvent(IngogoApp
//				// .getSharedApplication().getApplicationContext()
//				// .getClass().getName()); // comment if not needed
//				// Log.i("FlurryAgent.onPageView", "Call Success");
//			} catch (Exception e) {
//				Log.e("FlurryAgent.onLogEvent", "Error in logging events : "
//						+ e.getMessage());
//			}
//		} else {
//			Log.w("FlurryAgent.onPageView",
//					"Flurry is called without initilization");
//
//		}

	}

	public static void logEvent(String eventName) {

//		if (eventName == null) {
//			Log.w("FlurryAgent.logEvent", "Flurry is called with null values");
//
//		} else {
//
//			if (flurryKey != null) {
//				try {
//					FlurryAgent.logEvent(eventName);
//					// Log.e("FlurryAgent.logEvent", "Call Success");
//				} catch (Exception e) {
//					Log.e("FlurryAgent.logEvent", "Error in logging events : "
//							+ e.getMessage());
//				}
//			} else {
//				Log.w("FlurryAgent.logEvent",
//						"Flurry is called without initilization");
//
//			}
//		}

	}

	public static void logTimedEvent(String eventName) {

//		if (eventName == null) {
//			Log.w("FlurryAgent.logEvent", "Flurry is called with null values");
//
//		} else {
//
//			if (flurryKey != null) {
//				try {
//					FlurryAgent.logEvent(eventName, true);
//					// Log.e("FlurryAgent.logEvent", "Call Success");
//				} catch (Exception e) {
//					Log.e("FlurryAgent.logEvent", "Error in logging events : "
//							+ e.getMessage());
//				}
//			} else {
//				Log.w("FlurryAgent.logEvent",
//						"Flurry is called without initilization");
//
//			}
//		}

	}

	public static void endTimedEvent(String eventName) {

//		if (eventName == null) {
//			Log.w("FlurryAgent.endTimedEvent",
//					"Flurry is called with null values");
//
//		} else {
//
//			if (flurryKey != null) {
//				try {
//					FlurryAgent.endTimedEvent(eventName);
//					// Log.e("FlurryAgent.endTimedEvent", "Call Success");
//				} catch (Exception e) {
//					Log.e("FlurryAgent.endTimedEvent",
//							"Error in logging events : " + e.getMessage());
//				}
//			} else {
//				Log.w("FlurryAgent.endTimedEvent",
//						"Flurry is called without initilization");
//
//			}
//		}
}

}
