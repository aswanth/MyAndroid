/*
 * Package Name : com.ingogo.android.app
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : A singleton class which holds general application related data which is used through out the app ( eg : ApplicationContext ).
 */

package com.ingogo.android.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ingogo.android.activities.IGLoginActivity;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.utilities.IGUtility;

public class IngogoApp extends Application {

	private static IngogoApp sharedApplication;
	private static boolean DEBUG_MODE;
	private static boolean isAppUpdated;
	private static boolean playLoginAlert;
	private static String _userAgent = null;
	private static boolean _isPrimaryCardReaderAttached;
	private static boolean _isInitialCardCheck;
	private static int _connectionTimeout = 30;
	// Used in IGPaymentSwipeActivity, IGBaseActivity, IGBaseMapActivity
	public static boolean sPaymentOfflineButtonEnableFlag = false;
	private long _paymentSwipeActivityCreatedTime;

	private Float minTotalDueValue;
	private Float maxTotalDueValue;
	private Float confirmationValue;
	private Float creditPercentage;

	public static boolean sPaymentOfflineButtonEnableFlagInSwipeCalculator = false;
	private long _swipeCalculatorActivityCreatedTime;

	private static String swipeButtonTappedTime = null;
	private static String swipeScreenCreatedTime = null;
	private static String swipeInitialisationStartedTime = null;
	private static String swipeInitialisationCompleteTime = null;
	private static String swipeRecordedTime = null;
	private static String swipeCompletedScreenLoadedTime = null;
	private static String swipeFailedScreenLoadedTime = null;

	private boolean isComingFromPayOffline = false;

	public static void clearTimeTrackerHistory() {
		swipeButtonTappedTime = null;
		swipeScreenCreatedTime = null;
		swipeInitialisationStartedTime = null;
		swipeInitialisationCompleteTime = null;
		swipeRecordedTime = null;
		swipeCompletedScreenLoadedTime = null;
		swipeFailedScreenLoadedTime = null;
	}

	/**
	 * @return the swipeRecordedTime
	 */
	public static String getSwipeRecordedTime() {
		return swipeRecordedTime;
	}

	/**
	 * @param swipeRecordedTime
	 *            the swipeRecordedTime to set
	 */
	public static void setSwipeRecordedTime(String swipeRecordedTime) {
		IngogoApp.swipeRecordedTime = swipeRecordedTime;
	}

	/**
	 * @return the swipeCompletedScreenLoadedTime
	 */
	public static String getSwipeCompletedScreenLoadedTime() {
		return swipeCompletedScreenLoadedTime;
	}

	/**
	 * @param swipeCompletedScreenLoadedTime
	 *            the swipeCompletedScreenLoadedTime to set
	 */
	public static void setSwipeCompletedScreenLoadedTime(
			String swipeCompletedScreenLoadedTime) {
		IngogoApp.swipeCompletedScreenLoadedTime = swipeCompletedScreenLoadedTime;
	}

	/**
	 * @return the swipeFailedScreenLoadedTime
	 */
	public static String getSwipeFailedScreenLoadedTime() {
		return swipeFailedScreenLoadedTime;
	}

	/**
	 * @param swipeFailedScreenLoadedTime
	 *            the swipeFailedScreenLoadedTime to set
	 */
	public static void setSwipeFailedScreenLoadedTime(
			String swipeFailedScreenLoadedTime) {
		IngogoApp.swipeFailedScreenLoadedTime = swipeFailedScreenLoadedTime;
	}

	/**
	 * @return the swipeButtonTappedTime
	 */
	public static String getSwipeButtonTappedTime() {
		return swipeButtonTappedTime;
	}

	/**
	 * @param swipeButtonTappedTime
	 *            the swipeButtonTappedTime to set
	 */
	public static void setSwipeButtonTappedTime(String swipeButtonTappedTime) {
		IngogoApp.swipeButtonTappedTime = swipeButtonTappedTime;
	}

	/**
	 * @return the swipeScreenCreatedTime
	 */
	public static String getSwipeScreenCreatedTime() {
		return swipeScreenCreatedTime;
	}

	/**
	 * @param swipeScreenCreatedTime
	 *            the swipeScreenCreatedTime to set
	 */
	public static void setSwipeScreenCreatedTime(String swipeScreenCreatedTime) {
		IngogoApp.swipeScreenCreatedTime = swipeScreenCreatedTime;
	}

	/**
	 * @return the swipeInitialisationStartedTime
	 */
	public static String getSwipeInitialisationStartedTime() {
		return swipeInitialisationStartedTime;
	}

	/**
	 * @param swipeInitialisationStartedTime
	 *            the swipeInitialisationStartedTime to set
	 */
	public static void setSwipeInitialisationStartedTime(
			String swipeInitialisationStartedTime) {
		IngogoApp.swipeInitialisationStartedTime = swipeInitialisationStartedTime;
	}

	/**
	 * @return the swipeInitialisationCompleteTime
	 */
	public static String getSwipeInitialisationCompleteTime() {
		return swipeInitialisationCompleteTime;
	}

	/**
	 * @param swipeInitialisationCompleteTime
	 *            the swipeInitialisationCompleteTime to set
	 */
	public static void setSwipeInitialisationCompleteTime(
			String swipeInitialisationCompleteTime) {
		IngogoApp.swipeInitialisationCompleteTime = swipeInitialisationCompleteTime;
	}

	public static boolean isPlayLoginAlert() {
		return playLoginAlert;
	}

	public static void setPlayLoginAlert(boolean playLoginAlert) {
		IngogoApp.playLoginAlert = playLoginAlert;
	}

	// Used to store latitude and longitude that got from locationListener.
	public static String LATTITUDE;
	public static String LONGITUDE;
	public static boolean responsePendingInJobPreview;
	public static ArrayList<IGJob> jobList;

	private static Activity _currentActivityOnTop;
	private String _selectedSuburbName;

	public static interface jobStatusEnum {
		int COLLECTED = 1;
		int PAYMENT_DUE = 2;
		int COMPLETED = 3;
	}

	public static int jobStatus = 0;
	private long _localityId;
	private String _localityName;

	public String getSavingsPercentage() {
		
		String savings = getAppPreferences().getString(IGConstants.kSavingsPercentage,
				"0.0");
		if(savings != null && savings.contains("%"))
		{
			savings = savings.replace("%", "");
		}
		return savings;
	}

	public void setSavingsPercentage(String savingsPercentage) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor
				.putString(IGConstants.kSavingsPercentage, savingsPercentage);
		prefsEditor.commit();
	}

	@Override
	public void onTerminate() {
		//
		// QBAnalytics.logEvent("Application closed");
		// QBAnalytics.endSession(this);
		super.onTerminate();
	}

	public static int getJobStatus() {
		return jobStatus;
	}

	public static void setJobStatus(int jobStatus) {
		IngogoApp.jobStatus = jobStatus;
	}

	public ArrayList<IGJob> getJobList() {
		return jobList;
	}

	public void setJobList(ArrayList<IGJob> jobList) {
		this.jobList = jobList;
	}

	// Theme constants
	public static int themeID = 2;

	public static Boolean _available = true;

	/**
	 * @param available
	 *            : Set current user status
	 */
	public static void setAvailable(Boolean availableFlag) {
		_available = availableFlag;
	}

	/**
	 * @return current user status.
	 */
	public static Boolean getAvailable() {
		return _available;
	}

	public static int getThemeID() {
		return themeID;
	}

	public static void setThemeID(int themeID) {
		IngogoApp.themeID = themeID;
	}

	public static boolean isResponsePendingInJobPreview() {
		return responsePendingInJobPreview;
	}

	public static void setResponsePendingInJobPreview(
			boolean responsePendingInJobPreview) {
		IngogoApp.responsePendingInJobPreview = responsePendingInJobPreview;
	}

	/**
	 * 
	 * @return login state
	 */
	public boolean isLoggedIn() {
		return getAppPreferences().getBoolean(IGConstants.kLoggedIn, false);
	}

	/**
	 * Set to true,when login.Otherwise false
	 * 
	 * @param loggedIn
	 */
	public void setLoggedIn(boolean loggedIn) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putBoolean(IGConstants.kLoggedIn, loggedIn);
		prefsEditor.commit();
	}

	/**
	 * 
	 * @return access token.
	 */
	public String getAccessToken() {

		return getAppPreferences().getString(IGConstants.kAccessToken, null);

	}

	/**
	 * To remove saved details which are got from login response.The details
	 * include access token,broadcast position
	 * interval,mobileNumber,password,jobs poll interval.
	 */
	public void removeLoginCredentials() {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.remove(IGConstants.kAccessToken);
		prefsEditor.remove(IGConstants.kbroadcastPositionInterval);
		prefsEditor.remove(IGConstants.kUsernameKey);
		prefsEditor.remove(IGConstants.kPasswordKey);
		prefsEditor.remove(IGConstants.kPollJobsInterval);
		prefsEditor.remove(IGConstants.kPinRetriesMaxLimit);
		prefsEditor.commit();

		sharedApplication.setUserId(null);
		sharedApplication.setPassword(null);
		sharedApplication.setDriverName(null);
		sharedApplication.setPlateNumber(null);
	}

	public void setDriverName(String driverName) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kDriverNameKey, driverName);
		prefsEditor.commit();
	}

	public String getDriverName() {
		return getAppPreferences().getString(IGConstants.kDriverNameKey, null);
	}

	public void setPlateNumber(String plateNumber) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kPlateNumber, plateNumber);
		prefsEditor.commit();
	}

	public String getPlateNumber() {
		return getAppPreferences().getString(IGConstants.kPlateNumber, null);
	}

	/**
	 * Remove the meter fare saved in shared preference.
	 */
	public void removeStoredMeterFare() {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.remove(IGConstants.kFareEntered);
		prefsEditor.commit();
	}

	/**
	 * To set the access token.
	 * 
	 * @param accessToken
	 */
	public void setAccessToken(String accessToken) {

		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kAccessToken, accessToken);
		prefsEditor.commit();
	}

	/**
	 * To set the broadcast position interval.
	 * 
	 * @param interval
	 */
	public void setBroadcastPositionInterval(long interval) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putLong(IGConstants.kbroadcastPositionInterval, interval);
		prefsEditor.commit();
	}

	/**
	 * To set the jobs poll interval.
	 * 
	 * @param interval
	 */
	public void setPollJobsInterval(long interval) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putLong(IGConstants.kPollJobsInterval, interval);
		prefsEditor.commit();
	}

	/**
	 * 
	 * @return broadcast position interval.
	 */
	public long getBroadcastPositionInterval() {
		return getAppPreferences().getLong(
				IGConstants.kbroadcastPositionInterval, 0);
	}

	/**
	 * 
	 * @return jobs poll interval.
	 */
	public long getPollJobsInterval() {
		Log.e("POLLING TASK",
				"polling get interval"
						+ getAppPreferences().getLong(
								IGConstants.kPollJobsInterval, 1));
		return getAppPreferences().getLong(IGConstants.kPollJobsInterval, 1);
	}

	/**
	 * To set the mobileNumber.
	 * 
	 * @param userId
	 */
	public void setUserId(String userId) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kUsernameKey, userId);
		prefsEditor.commit();
	}

	/**
	 * 
	 * @return mobileNumber
	 */
	public String getUserId() {
		if (getAppPreferences().getString(IGConstants.kUsernameKey, null) != null) {
			// Log.e("User Id",
			// getAppPreferences().getString(IGConstants.kUsernameKey,
			// null));

		}
		return getAppPreferences().getString(IGConstants.kUsernameKey, null);
	}

	/**
	 * To set the mobileNumber.
	 * 
	 * @param userId
	 */
	public void setAuthFailureCount(int count) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putInt(IGConstants.kAuthErrorCount, count);
		prefsEditor.commit();
	}

	/**
	 * 
	 * @return mobileNumber
	 */
	public int getAuthFaileureCount() {
		return getAppPreferences().getInt(IGConstants.kAuthErrorCount, 0);
	}

	/*
	 * Saves the valid taxi plate prefixes
	 */
	public void setValidTaxiPlatePrefixes(String prefixes) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kSupportedPrefixes, prefixes);
		prefsEditor.commit();
	}

	/**
	 * Returns the saved taxi plate prefixes
	 * 
	 * @return
	 */
	public String getValidTaxiPlatePrefixes() {
		return getAppPreferences().getString(IGConstants.kSupportedPrefixes,
				null);
	}

	public void setValidMaskedTaxiPlatePrefixes(String prefixes) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kMaskedPrefixes, prefixes);
		prefsEditor.commit();
	}

	/**
	 * Returns the saved taxi plate prefixes
	 * 
	 * @return
	 */
	public String getValidMaskedTaxiPlatePrefixes() {
		return getAppPreferences().getString(IGConstants.kMaskedPrefixes, null);
	}

	/**
	 * 
	 * @param meterFare
	 */
	public void setMeterFare(String meterFare) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kFareEntered, meterFare);
		prefsEditor.commit();
	}

	/**
	 * 
	 * @return saved meter fare
	 */
	public String getMeterFare() {
		return getAppPreferences().getString(IGConstants.kFareEntered,
				IGConstants.zeroBalance);
	}

	/**
	 * To set the password.
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kPasswordKey, password);
		prefsEditor.commit();
	}

	/**
	 * 
	 * @return password.
	 */

	public String getPassword() {
		return getAppPreferences().getString(IGConstants.kPasswordKey, null);
	}

	/**
	 * Saves the joblist refresh time out
	 */
	public void setJobRemainderInterval(String timeInSeconds) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kRemainderKey, timeInSeconds);
		prefsEditor.commit();
	}

	/**
	 * 
	 */
	public String getJobRemainderInterval() {
		return getAppPreferences().getString(IGConstants.kRemainderKey, null);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// QBAnalytics.setAnalyticsEnabled(true);
		// QBAnalytics.startSession(this);
		// QBAnalytics.logEvent("Application launched");
		isAppUpdated = false;
		sharedApplication = this;
		setDebugMode(false);
		QLog.setupLogging(this, true);
		_isPrimaryCardReaderAttached = true;
	}

	/**
	 * 
	 * @return IngogoApp object
	 */
	public static synchronized IngogoApp getSharedApplication() {
		return sharedApplication;
	}

	/**
	 * 
	 * @return SharedPreferences object
	 */

	public static SharedPreferences getAppPreferences() {

		SharedPreferences preferences = IngogoApp.getSharedApplication()
				.getSharedPreferences(IGConstants.kSharedPreference,
						MODE_WORLD_READABLE);
		return preferences;
	}

	/**
	 * Get screen density based on which further dynamic pixel calculations can
	 * be made.
	 * 
	 * @return density ratio of device.
	 */

	public static float getCurrentDensity() {
		return IngogoApp.getSharedApplication().getResources()
				.getDisplayMetrics().density;

	}

	/**
	 * 
	 * @return debug mode state.
	 */
	public static boolean isDebugMode() {
		return DEBUG_MODE;
	}

	/**
	 * @param mode
	 */
	public static void setDebugMode(boolean mode) {
		DEBUG_MODE = mode;
	}

	/**
	 * 
	 * @return version name.
	 */
	public static String getVersionName() {
		String version = "?";
		try {
			PackageInfo pi = IngogoApp
					.getSharedApplication()
					.getPackageManager()
					.getPackageInfo(
							IngogoApp.getSharedApplication().getPackageName(),
							0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("Version Number", "Package name not found", e);
		}
		;
		return version;
	}

	public static String getUserAgent() {

		if (_userAgent == null) {
			String version = android.os.Build.VERSION.RELEASE;
			_userAgent = "Android-Driver" + " "
					+ String.valueOf(IngogoApp.getVersionCode())
					+ " (Android OS " + version + ")";
		}
		Log.e("User Agent", " " + _userAgent);
		return _userAgent;
	}

	/**
	 * 
	 * @return version code
	 */
	public static int getVersionCode() {
		int code = 1;
		try {
			PackageInfo pi = IngogoApp
					.getSharedApplication()
					.getPackageManager()
					.getPackageInfo(
							IngogoApp.getSharedApplication().getPackageName(),
							0);
			code = pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("Version Number", "Package name not found", e);
		}
		;
		return code;
	}

	/**
	 * 
	 * @return update status
	 */
	public static boolean getAppUpdateStatus() {
		return isAppUpdated;
	}

	public static void setAppUpdateStatus(Boolean status) {
		isAppUpdated = status;
	}

	public void setPreviousUserId(String userId) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putString(IGConstants.kPreviousUserId, userId);
		prefsEditor.commit();
	}

	public String getPreviousUserId() {
		return getAppPreferences().getString(IGConstants.kPreviousUserId, null);
	}

	public void resetApp() {

		// if
		// (IGSearchAvailableActivity.class.isInstance(getCurrentTopActivity()))
		// {

		Intent notifyIntent = new Intent(getCurrentActivityOnTop()
				.getApplicationContext(), IGLoginActivity.class);
		notifyIntent.putExtra("show_crash_alert", true);

		PendingIntent intent = PendingIntent.getActivity(
				getCurrentActivityOnTop(), 0, notifyIntent,
				android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
						| android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager alarmManager = (AlarmManager) getCurrentActivityOnTop()
				.getSystemService(Context.ALARM_SERVICE);

		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				intent);

		android.os.Process.killProcess(android.os.Process.myPid());

		// } else {

		// }
		// TODO Auto-generated method stub

	}

	public static void setCurrentActivityOnTop(Activity _currentActivityOnTop) {
		if (_currentActivityOnTop instanceof IGLoginActivity) {
			return;
		}
		IngogoApp._currentActivityOnTop = _currentActivityOnTop;
	}

	public static Activity getCurrentActivityOnTop() {
		return _currentActivityOnTop;
	}

	public static void setPrimaryCardReaderAttached(
			boolean isPrimaryCardReaderAttached) {
		if (isPrimaryCardReaderAttached) {
			Log.e("UNIMAG OR SQUARE", "UNIMAG is Default");
		} else {
			Log.e("UNIMAG OR SQUARE", "SQUARE is Default");
		}
		_isPrimaryCardReaderAttached = isPrimaryCardReaderAttached;
	}

	public static boolean isPrimaryCardReaderAttached() {
		return _isPrimaryCardReaderAttached;
	}

	public static void setInitialCardCheck(boolean isInitialCardCheck) {

		_isInitialCardCheck = isInitialCardCheck;
	}

	public static boolean isInitialCardCheck() {
		return _isInitialCardCheck;
	}

	public static int getConnectionTimeout() {
		_connectionTimeout = getAppPreferences().getInt("connectionTimeout", 0);
		if (_connectionTimeout == 0) {
			_connectionTimeout = 30;
		}
		return _connectionTimeout * 1000;
	}

	public static void setConnectionTimeout(int _connectionTimeout) {
		SharedPreferences.Editor prefsEditor = getAppPreferences().edit();
		prefsEditor.putInt("connectionTimeout", _connectionTimeout);
		prefsEditor.commit();
		IngogoApp._connectionTimeout = _connectionTimeout;
	}

	public double getMinTotalDueValue() {
		float minTotal = IGUtility.getFloatDefaults("minTotalDueValue",
				getCurrentActivityOnTop().getApplicationContext());
		return (double) minTotal;
	}

	public void setMinTotalDueValue(String minTotalDueValue) {
		try {
			this.minTotalDueValue = Float.parseFloat(minTotalDueValue);

		} catch (NumberFormatException e) {
			this.minTotalDueValue = (float) 0.0;
		}
		IGUtility.setDefaults("minTotalDueValue", this.minTotalDueValue,
				getCurrentActivityOnTop().getApplicationContext());
	}

	public long getPaymentSwipeActivityCreatedTime() {
		return _paymentSwipeActivityCreatedTime;
	}

	public void setPaymentSwipeActivityCreatedTime(
			long _paymentSwipeActivityCreatedTime) {
		this._paymentSwipeActivityCreatedTime = _paymentSwipeActivityCreatedTime;

	}

	public double getMaxTotalDueValue() {
		return (double) IGUtility.getFloatDefaults("maxTotalDueValue",
				getCurrentActivityOnTop().getApplicationContext());
	}

	public void setMaxTotalDueValue(String maxTotalDueValue) {
		try {
			this.maxTotalDueValue = Float.parseFloat(maxTotalDueValue);

		} catch (NumberFormatException e) {
			this.maxTotalDueValue = (float) 0.0;
		}
		IGUtility.setDefaults("maxTotalDueValue", this.maxTotalDueValue,
				getCurrentActivityOnTop().getApplicationContext());
	}

	public double getConfirmationValue() {
		return (double) IGUtility.getFloatDefaults("confirmationValue",
				getCurrentActivityOnTop().getApplicationContext());
	}

	public void setConfirmationValue(String confirmationValue) {
		try {
			this.confirmationValue = Float.parseFloat(confirmationValue);

		} catch (NumberFormatException e) {
			this.confirmationValue = (float) 0.0;
		}
		IGUtility.setDefaults("confirmationValue", this.confirmationValue,
				getCurrentActivityOnTop().getApplicationContext());
	}

	public double getCreditPercentage() {
		return (double) IGUtility.getFloatDefaults("creditPercentage",
				getCurrentActivityOnTop().getApplicationContext());
	}

	public void setCreditPercentage(String creditPercentage) {
		try {
			this.creditPercentage = Float.parseFloat(creditPercentage);

		} catch (NumberFormatException e) {
			this.creditPercentage = (float) 0.0;
		}
		IGUtility.setDefaults("creditPercentage", this.creditPercentage,
				getCurrentActivityOnTop().getApplicationContext());

	}

	public void resetPaymentBaseValues() {
		this.minTotalDueValue = (float) 0.0;
		this.maxTotalDueValue = (float) 0.0;
		this.confirmationValue = (float) 0.0;
		this.creditPercentage = (float) 0.0;
		IGUtility.setDefaults("minTotalDueValue", this.minTotalDueValue,
				getCurrentActivityOnTop().getApplicationContext());
		IGUtility.setDefaults("maxTotalDueValue", this.maxTotalDueValue,
				getCurrentActivityOnTop().getApplicationContext());
		IGUtility.setDefaults("confirmationValue", this.confirmationValue,
				getCurrentActivityOnTop().getApplicationContext());
		IGUtility.setDefaults("creditPercentage", this.creditPercentage,
				getCurrentActivityOnTop().getApplicationContext());

	}

	public long getSwipeCalculatorActivityCreatedTime() {
		return _swipeCalculatorActivityCreatedTime;
	}

	public void setSwipeCalculatorActivityCreatedTime(
			long _swipeCalculatorActivityCreatedTime) {
		this._swipeCalculatorActivityCreatedTime = _swipeCalculatorActivityCreatedTime;
	}

	public long getLocalityId() {
		return _localityId;
	}

	public void setLocalityId(long _localityId) {
		this._localityId = _localityId;
	}

	public String getLocalityName() {
		return _localityName;
	}

	public void setLocalityName(String _localityName) {
		this._localityName = _localityName;
	}

	public String getSelectedSuburbName() {
		return _selectedSuburbName;
	}

	public void setSelectedSuburbName(String _selectedSuburbName) {
		this._selectedSuburbName = _selectedSuburbName;
	}

	public boolean isComingFromPayOffline() {
		return isComingFromPayOffline;
	}

	public void setComingFromPayOffline(boolean isComingFromPayOffline) {
		this.isComingFromPayOffline = isComingFromPayOffline;
	}

}
