/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description :Utility class that contains number of ready made functions at disposal based on application request.
 */

package com.ingogo.android.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.logger.QLog;
import com.ingogo.android.logger.analytics.QBAnalytics;

public class IGUtility {
	static ProgressDialog myProgressDialog = null;
	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	private static String allowableStringMobile = "+";

	public static String convertStreamToString(InputStream is)
			throws IOException {
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	public static Date getDateFromTimeStamp(String timestamp)
			throws ParseException {
		Date date = new Date(Long.parseLong(timestamp));
		return date;
	}

	public static String getMd5Hash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);
			while (md5.length() < 32)
				md5 = "0" + md5;
			return md5;
		} catch (NoSuchAlgorithmException e) {
			Log.e("MD5", e.getMessage());
			return null;
		}
	}

	public static String getMd5DecrytedString(String input) {
		try {
			byte[] password = input.getBytes("UTF-8");
			byte[] ciphertext = { -68, -112, 66, 78, 85, 50, 22, -63, 16, 24,
					-45, 4, -116, -14, 88, 34, -85, 116, 105, 59, 45, -126 };
			byte[] plaintext = md5Decrypt(password, ciphertext);
			return (new String(plaintext, "UTF-8"));
		} catch (Exception e) {
			return null;
		}
	}

	public static byte[] md5Decrypt(byte[] password, byte[] ciphertext) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] hash = digest.digest(password);
			Cipher rc4 = Cipher.getInstance("RC4");
			rc4.init(Cipher.DECRYPT_MODE, new SecretKeySpec(hash, "RC4"));
			return rc4.doFinal(ciphertext);
		} catch (Exception e) {
			return null;
		}
	}

	public static void showDialogOk(String title, String message,
			Context context) {
		Dialog dlg = new AlertDialog.Builder(context).setTitle(title)
				.setMessage(message)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		Activity activity = (Activity) context;
		if (!activity.isFinishing()) {
			dlg.show();
		}
	}

	public static Dialog showDialog(String title, String message,
			Context context) {
		Dialog dlg = new AlertDialog.Builder(context).setTitle(title)
				.setMessage(message)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		Activity activity = (Activity) context;
		if (!activity.isFinishing()) {
			dlg.show();
		}
		return dlg;
	}

	public static void showDialogOkWithGoBack(String title, String message,
			final Activity activity) {
		AlertDialog.Builder adb = new AlertDialog.Builder(activity);
		adb.setTitle(title);
		adb.setMessage(message);
		adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				activity.onBackPressed();
			}
		});
		AlertDialog ad = adb.create();
		ad.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		if (!activity.isFinishing()) {
			ad.show();
		}
	}

	public static void showToast(String message, Context context) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	public static String getUSDateTime(String dateString) {
		String formattedDate = "";
		formattedDate = IGUtility.formatDateWithFormats(dateString,
				"yyyy-MM-dd HH:mm:ss", "MM-dd-yyyy hh:mm:ss a");

		return formattedDate;
	}

	public static String formatDate(String dateString) {
		String formattedDate = "";
		if (dateString.contains("/")) {
			formattedDate = IGUtility.formatDateWithFormats(dateString,
					"MM/dd/yyyy hh:mm:ss a", "yyyy-MM-dd HH:mm:ss");
		} else {
			formattedDate = IGUtility.formatDateWithFormats(dateString,
					"MM-dd-yyyy hh:mm:ss a", "yyyy-MM-dd HH:mm:ss");
		}
		return formattedDate;
	}

	public static String get24HourTimeFromDate(String dateString) {
		return (IGUtility.formatDateWithFormats(dateString,
				"yyyy-MM-dd HH:mm:ss", "HH:mm"));
	}

	public static String getTimeFromDate(String dateString) {
		return (IGUtility.formatDateWithFormats(dateString,
				"yyyy-MM-dd HH:mm:ss", "hh:mm a"));
	}

	public static String formatDOB(String DOB) {
		return formatDateWithFormats(DOB, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
	}

	public static String formatMRN(String MRN) {
		return formatDateWithFormats(MRN, "yyyy-MM-dd HH:mm:ss", "MMddyymmss");
	}

	public static String formatDateWithFormats(String dateString,
			String ipFormat, String opFormat) {
		DateFormat inputFormat = new SimpleDateFormat(ipFormat);
		DateFormat outputFormat = new SimpleDateFormat(opFormat);
		String outputDate = dateString.trim();
		Date date = null;
		try {
			date = inputFormat.parse(dateString.trim());
			outputDate = outputFormat.format(date);

		} catch (ParseException e) {
			// e.printStackTrace();
			Log.e("DATE-FORMATE - " + dateString, e.getMessage(), e);
		}
		return outputDate;
	}

	public static void removeDefaults(String key, Context context) {
		if (getDefaults(key, context) != null) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(key);
			editor.commit();
		}
	}

	public static void setDefaults(String key, String value, Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void setDefaults(String key, Float value, Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	public static String getDefaults(String key, Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String savedValue = preferences.getString(key, null);
		if (savedValue == null) {
			return null;
		}
		Log.i("savedValue", "" + savedValue);
		return savedValue;
	}

	public static float getFloatDefaults(String key, Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		float savedValue = preferences.getFloat(key, (float) 0.0);
		Log.i("savedValue", "" + savedValue);
		return savedValue;
	}

	public static void setRetryTime(float value, Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(IGConstants.kUpdatedTime, value);
		editor.commit();
	}

	public static float getRetryTime(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		Log.i("time ==", "kkk");
		float time = preferences.getFloat(IGConstants.kUpdatedTime, 0.0f);
		Log.i("time ==", "" + time);
		return time;
	}

	public static String getStartDate() {
		String date = IGUtility.getDateByAddingDayInterval(-5);
		date = date.replaceAll(" ", "T");
		return date;
	}

	public static String getEndDate() {
		String date = IGUtility.getDateByAddingDayInterval(7);
		date = date.replaceAll(" ", "T");
		return date;
	}

	public static String getDateByAddingDayInterval(int days) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		String date = dateFormat.format(cal.getTime());
		return date;
	}

	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());
		return date;
	}

	public static String getDateWithSecondsInterval(int interval) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, interval);
		String date = dateFormat.format(cal.getTime());
		return date;
	}

	public static int getDateDifferenceInMinutes(Date olderDate, Date newerDate) {
		Calendar oldCalendar = Calendar.getInstance();
		Calendar newCalendar = Calendar.getInstance();
		oldCalendar.setTime(olderDate);
		newCalendar.setTime(newerDate);

		long milliseconds1 = oldCalendar.getTimeInMillis();
		long milliseconds2 = newCalendar.getTimeInMillis();
		long diff = milliseconds2 - milliseconds1;
		long diffDays = diff / (60 * 1000);
		int hours = (int) diffDays;

		return hours;
	}

	public static String getRandomUUID() {
		UUID locationID = UUID.randomUUID();
		String key = locationID.toString();
		return key;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connec = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connec.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnected() == true) {
			return true;
		}

		return false;
	}

	public static boolean isDateValid(String date, String format) {
		DateFormat formatter = new SimpleDateFormat(format);
		try {
			formatter.parse(date);
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}

		return false;
	}

	/**
	 * Downloads the file from a URL.
	 * 
	 * @param fileUrl
	 */
	public static Bitmap downloadFileFromUrl(String fileUrl) {

		URL myFileUrl = null;
		Bitmap imageBitmap = null;

		try {
			myFileUrl = new URL(fileUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			HttpURLConnection connection = (HttpURLConnection) myFileUrl
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream is = connection.getInputStream();
			imageBitmap = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return imageBitmap;
	}

	public static boolean validateEmail(String email) {

		Pattern emailPattern = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher emailMatcher = emailPattern.matcher(email);
		if (!emailMatcher.matches()) {
			return false;
		}

		return true;
	}

	public static AlphaAnimation getAlphaAnimation() {
		AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
		alpha.setDuration(0); // Make animation instant
		alpha.setFillAfter(true); // Tell it to persist after the animation ends
		return alpha;
	}

	public static ProgressDialog showProgressDialog(Context context) {
		myProgressDialog = new ProgressDialog(context);
		myProgressDialog.setMessage("Please wait...");
		myProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		myProgressDialog.show();
		myProgressDialog.setCancelable(false);
		myProgressDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		myProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true;
				}
				return false;
			}

		});
		return myProgressDialog;
	}

	public static ProgressDialog showProgressDialogWithMsg(Context context,
			String message) {
		myProgressDialog = new ProgressDialog(context);
		myProgressDialog.setMessage(message);
		myProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		myProgressDialog.show();
		myProgressDialog.setCancelable(false);
		myProgressDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		myProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true;
				}
				return false;
			}

		});
		return myProgressDialog;
	}

	public static ProgressDialog getCurrentProgressDialog() {
		return myProgressDialog;
	}

	public static void dismissProgressDialog(ProgressDialog myProgressDialog) {
		if (myProgressDialog != null) {
			try {
				myProgressDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isGPSServiceRunning() {
		ActivityManager manager = (ActivityManager) IngogoApp
				.getSharedApplication().getSystemService(
						Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (IGConstants.GPSService.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Function to calculate card surcharge
	 * 
	 * @param meterFare
	 * @param baseFee
	 * @param servcieFee
	 * @param serviceCredit
	 * @param creditPercentage
	 * @return card surcharge
	 */
	public static double findCardSurcharge(double meterFare, double baseFee,
			double serviceFee, double serviceCredit, double creditPercentage) {
		return (creditPercentage
				* (meterFare + baseFee + serviceFee - serviceCredit) / 100);
	}

	/**
	 * Function to calculate the total payment due
	 * 
	 * @param meterFare
	 * @param baseFee
	 * @param serviceFee
	 * @param serviceCredit
	 * @param surcharge
	 * @return
	 */
	public static double findTotalPaymentDue(double meterFare, double baseFee,
			double serviceFee, double serviceCredit, double surcharge) {
		return (meterFare + baseFee + serviceFee - serviceCredit + surcharge);
	}

	public static void playSoundNotification(Context context, int resID) {
		MediaPlayer mp = MediaPlayer.create(context, resID);
		mp.start();
	}

	public static String getTopActivity() {
		ActivityManager activityManager = (ActivityManager) IngogoApp
				.getSharedApplication().getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> taskInfo = activityManager
				.getRunningTasks(1);

		ComponentName componentInfo = taskInfo.get(0).topActivity;
		return componentInfo.getShortClassName();
	}

	/**
	 * Filter to allow only mobile number without special character
	 */
	public static InputFilter filterMobileNoSpecial = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			for (int i = start; i < end; i++) {

				if (source.toString().length() <= 1
						|| source.toString().matches("^[a-zA-Z0-9]*$")) {
					if (Character.isDigit(source.charAt(i))) {
						return source;
					} else {
						if (dstart == 0
								&& new Character(source.charAt(i)).toString()
										.equalsIgnoreCase("+")
								&& !dest.toString().contains("+")) {
							return source;
						}
						return "";
					}
				} else {
					return "";
				}
			}
			return null;
		}
	};

	/**
	 * Filter to allow only mobile number with plus sign
	 */
	public static InputFilter filterMobile = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {

			for (int i = start; i < end; i++) {

				if (source.toString().length() <= 1) {
					if (Character.isDigit(source.charAt(i))
							|| allowableStringMobile.contains(source)) {
						return source;
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
			return null;
		}
	};

	public static final int INVALID = -1;
	public static final int VISA = 0;
	public static final int MASTERCARD = 1;
	public static final int AMERICAN_EXPRESS = 2;
	public static final int EN_ROUTE = 3;
	public static final int DINERS_CLUB = 4;

	private static final String[] cardNames = { "Visa", "Mastercard",
			"American Express", "En Route", "Diner's CLub/Carte Blanche", };

	public static String getXMLFileFromRaw() {
		// the target filename in the application path
		String fileNameWithPath = null;
		fileNameWithPath = "idt_unimagcfg_default.xml";

		try {
			InputStream in = IngogoApp.getSharedApplication().getResources()
					.openRawResource(R.raw.idt_unimagcfg_default);
			int length = in.available();
			byte[] buffer = new byte[length];
			in.read(buffer);
			in.close();

			IngogoApp.getSharedApplication().deleteFile(fileNameWithPath);

			FileOutputStream fout = IngogoApp.getSharedApplication()
					.openFileOutput(fileNameWithPath, 0);
			fout.write(buffer);
			fout.close();

			// to refer to the application path
			File fileDir = IngogoApp.getSharedApplication().getFilesDir();
			fileNameWithPath = fileDir.getParent() + java.io.File.separator
					+ fileDir.getName();
			fileNameWithPath = fileNameWithPath + java.io.File.separator
					+ "idt_unimagcfg_default.xml";

		} catch (Exception e) {
			e.printStackTrace();
			fileNameWithPath = null;
		}

		return fileNameWithPath;
	}

	public static boolean isFileExist(String path) {
		if (path == null)
			return false;
		File file = new File(path);
		if (!file.exists()) {
			return false;
		}
		return true;
	}

	public static String getHexStringFromBytes(byte[] data) {
		if (data.length <= 0)
			return null;
		StringBuffer hexString = new StringBuffer();
		String fix = null;
		for (int i = 0; i < data.length; i++) {
			fix = Integer.toHexString(0xFF & data[i]);
			if (fix.length() == 1)
				fix = "0" + fix;
			hexString.append(fix);
		}
		fix = null;
		fix = hexString.toString();
		return fix;
	}

	/**
	 * Valid a Credit Card number
	 */
	public static boolean validCC(String number) throws Exception {
		int CardID;
		if ((CardID = getCardID(number)) != -1)
			return validCCNumber(number);
		return false;
	}

	/**
	 * Get the Card type returns the credit card type INVALID = -1; VISA = 0;
	 * MASTERCARD = 1; AMERICAN_EXPRESS = 2; EN_ROUTE = 3; DINERS_CLUB = 4;
	 */
	public static int getCardID(String number) {
		int valid = INVALID;

		String digit1 = number.substring(0, 1);
		String digit2 = number.substring(0, 2);
		String digit3 = number.substring(0, 3);
		String digit4 = number.substring(0, 4);

		if (isNumber(number)) {
			/*
			 * ----* VISA prefix=4* ---- length=13 or 16 (can be 15 too!?!
			 * maybe)
			 */
			if (digit1.equals("4")) {
				if (number.length() == 13 || number.length() == 16)
					valid = VISA;
			}
			/*
			 * ----------* MASTERCARD prefix= 51 ... 55* ---------- length= 16
			 */
			else if (digit2.compareTo("51") >= 0 && digit2.compareTo("55") <= 0) {
				if (number.length() == 16)
					valid = MASTERCARD;
			}
			/*
			 * ----* AMEX prefix=34 or 37* ---- length=15
			 */
			else if (digit2.equals("34") || digit2.equals("37")) {
				if (number.length() == 15)
					valid = AMERICAN_EXPRESS;
			}
			/*
			 * -----* ENROU prefix=2014 or 2149* ----- length=15
			 */
			else if (digit4.equals("2014") || digit4.equals("2149")) {
				if (number.length() == 15)
					valid = EN_ROUTE;
			}
			/*
			 * -----* DCLUB prefix=300 ... 305 or 36 or 38* ----- length=14
			 */
			else if (digit2.equals("36")
					|| digit2.equals("38")
					|| (digit3.compareTo("300") >= 0 && digit3.compareTo("305") <= 0)) {
				if (number.length() == 14)
					valid = DINERS_CLUB;
			}
		}
		return valid;

		/*
		 * ----* DISCOVER card prefix = 60* -------- lenght = 16* left as an
		 * exercise ...
		 */

	}

	public static boolean isNumber(String n) {
		try {
			double d = Double.valueOf(n).doubleValue();
			return true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String getCardName(int id) {
		return (id > -1 && id < cardNames.length ? cardNames[id] : "");
	}

	public static boolean validCCNumber(String n) {
		try {
			/*
			 * * known as the LUHN Formula (mod10)
			 */
			int j = n.length();

			String[] s1 = new String[j];
			for (int i = 0; i < n.length(); i++)
				s1[i] = "" + n.charAt(i);

			int checksum = 0;

			for (int i = s1.length - 1; i >= 0; i -= 2) {
				int k = 0;

				if (i > 0) {
					k = Integer.valueOf(s1[i - 1]).intValue() * 2;
					if (k > 9) {
						String s = "" + k;
						k = Integer.valueOf(s.substring(0, 1)).intValue()
								+ Integer.valueOf(s.substring(1)).intValue();
					}
					checksum += Integer.valueOf(s1[i]).intValue() + k;
				} else
					checksum += Integer.valueOf(s1[0]).intValue();
			}
			return ((checksum % 10) == 0);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void logExceptionInQLogger(Exception e) {
		QLog.e("CARD READER", "CARD READER - " + e.getMessage());

	}

	public static void logAuthExceptionInQLogger(Exception e) {
		QLog.e("QLOG", "LOGOUT - " + e.getMessage());

	}

	public static boolean isTheEncodedCardDataSuccess(String hexString) {
		hexString = hexString.replace(" ", "");
		if (hexString.length() < 20) {
			return false;
		} else {
			String firstIndx = hexString.substring(11, 13);
			String secondIndex = hexString.substring(13, 15);
			if (firstIndx.equalsIgnoreCase("00")
					&& secondIndex.equalsIgnoreCase("00")) {
				return false;

			} else {
				return true;

			}
		}
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static String asHex(byte[] buf) {
		char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
		}
		return new String(chars);
	}

	public static InputFilter alphaNumericFilter = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence arg0, int arg1, int arg2,
				Spanned arg3, int arg4, int arg5) {
			for (int k = arg1; k < arg2; k++) {
				if (!Character.isLetterOrDigit(arg0.charAt(k))) {
					return "";
				}
			}
			return null;
		}
	};

	/**
	 * when value = 156.123, value returned will be 156.12 when value = 156.5,
	 * value returned will be 156.50
	 * 
	 * @param value
	 * @return
	 */
	public static String getInPriceFormat(Double value) {
		DecimalFormat decimalFormat = new DecimalFormat(IGConstants.priceFormat);
		String formattedString = (String.valueOf(decimalFormat.format(value)));
		return formattedString;
	}

	/**
	 * when value = 156.123, value returned will be 156.12 when value = 156.5,
	 * value returned will be 156.50
	 * 
	 * @param value
	 * @return
	 */
	public static String getInPriceFormat(float value) {
		DecimalFormat decimalFormat = new DecimalFormat(IGConstants.priceFormat);
		String formattedString = (String.valueOf(decimalFormat.format(value)));
		return formattedString;
	}

	/**
	 * when value = 156.123, value returned will be 156.12 when value = 156.5,
	 * value returned will be 156.50
	 * 
	 * @param value
	 * @return
	 */
	public static String getInPriceFormat(String value) {
		float floatValue = 0;
		try {
			floatValue = Float.parseFloat(value);
		} catch (Exception e) {
		}
		DecimalFormat decimalFormat = new DecimalFormat(IGConstants.priceFormat);
		String formattedString = (String.valueOf(decimalFormat
				.format(floatValue)));
		return formattedString;
	}

	/**
	 * A date is taken as input, with no of days to be added to; and the updated
	 * date is returned
	 * 
	 * @param inputDate
	 * @param noOfDays
	 * @return updatedDate
	 */
	public static Date addDaysToADate(Date inputDate, int noOfDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(inputDate);
		cal.add(Calendar.DATE, noOfDays);
		Date updatedDate = cal.getTime();
		return updatedDate;
	}

	public static double round(double unrounded, int precision, int roundingMode) {
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	/**
	 * 
	 * @return format time in string
	 */

	public static String timeStampToTime(int timeWithin, long timeStamp) {
		int timeWithIn = timeWithin;
		SimpleDateFormat formatter = new SimpleDateFormat("h:mmaa");
		Calendar calendar = Calendar.getInstance();
		long timestamp = timeStamp;
		Date date = new Date(timestamp);
		long newTime = date.getTime();
		Time time = new Time();
		time.set(newTime);
		time.minute += timeWithIn;
		time.normalize(false);
		time.toMillis(false);
		time.format(time.toString());
		date.setTime(time.toMillis(false));
		calendar.setTime(date);
		return (formatter.format(calendar.getTime()));
	}

	/**
	 * Date string in required format
	 * 
	 * @param timeStamp
	 * @param format
	 *            eg: dd/MM hh:mma
	 * @return
	 */
	public static String getDateString(String timeStamp, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM hh:mma");
		Date dat = null;
		try {
			dat = IGUtility.getDateFromTimeStamp(timeStamp);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e("time", "" + dat.getHours() + "***" + dat.getMinutes());
		String s = sdf.format(dat);
		return s;
	}

	public static void logDetailsToAnalytics(String eventName, String url) {
		Map<String, String> details = new HashMap<String, String>();
		if (url != null) {
			List<String> items = Arrays.asList(url.split(("/")));
			details.put("requestName", items.get(items.size() - 1));
		}
		details.put("latitude", IGLocationListener.getCurrentLatitude() + "");
		details.put("longitude", IGLocationListener.getCurrentLongitude() + "");

		QBAnalytics.logEvent(eventName, details);
		QLog.d("API REQUEST", details.toString());
	}

	public static void logDetailsToAnalytics(String eventName, String url,
			String reponse) {
		Map<String, String> details = new HashMap<String, String>();
		if (url != null) {
			List<String> items = Arrays.asList(url.split(("/")));
			details.put("requestName", items.get(items.size() - 1));

		}
		details.put("reponse", reponse);

		details.put("latitude", IGLocationListener.getCurrentLatitude() + "");
		details.put("longitude", IGLocationListener.getCurrentLongitude() + "");

		QBAnalytics.logEvent(eventName, details);
		QLog.d("API RESPONSE", details.toString());
	}

	public static void logRandomDetailsToAnalytics(String eventName,
			String detailsString) {
		Map<String, String> details = new HashMap<String, String>();
		if (details != null) {
			details.put("details", detailsString);

		}
		details.put("latitude", IGLocationListener.getCurrentLatitude() + "");
		details.put("longitude", IGLocationListener.getCurrentLongitude() + "");

		QBAnalytics.logEvent(eventName, details);
		QLog.d(eventName, details.toString());
	}

	public static void logDetailsToAnalyticsWithoutLocation(String eventName,
			String detailsString) {
		Map<String, String> details = new HashMap<String, String>();
		if (details != null) {
			details.put("details", detailsString);

		}

		QBAnalytics.logEvent(eventName, details);
		QLog.d(eventName, details.toString());
	}
	public static String getTimeString() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date(Calendar.getInstance().getTimeInMillis());
		String dateString = fmt.format(date);
		return dateString;
	}
	
	
	public static String getDateStringFromFormat(String timeStamp, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dat = null;
		try {
			dat = IGUtility.getDateFromTimeStamp(timeStamp);

		} catch (ParseException e) {
			e.printStackTrace();
		}
		Log.e("time", "" + dat.getHours() + "***" + dat.getMinutes());
		String s = sdf.format(dat);
		return s;
	}
	
	public static void hideKeyboard(Activity activity)
    {
        InputMethodManager inputManager =
                (InputMethodManager) activity
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null && activity.getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), 0);
        }
    }
//	
//	public static String getDateStringFromFormat(String timeStamp, String format) {
//		SimpleDateFormat sdf = new SimpleDateFormat(format);
//		Date dat = null;
//		try {
//			dat = IGUtility.getDateFromTimeStamp(timeStamp);
//
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Log.e("time", "" + dat.getHours() + "***" + dat.getMinutes());
//		String s = sdf.format(dat);
//		return s;
//	}

}
