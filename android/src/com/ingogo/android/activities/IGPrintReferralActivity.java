package com.ingogo.android.activities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGBluetoothBaseActivity;
import com.ingogo.android.adapters.IGReferralInformationAdapter;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGCommissionDetails;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGBluetoothReceiveListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGReferralInformationApi;
import com.ingogo.android.webservices.beans.response.IGReferralInformationResponseBean;
import com.ingogo.android.webservices.interfaces.IGReferralInformationApiListener;

/**
 * IGPrintReferralActivity class - Refer a friend, print referral, shows earning
 * details
 * 
 * @author suslov
 * 
 */
public class IGPrintReferralActivity extends IGBluetoothBaseActivity implements
		IGReferralInformationApiListener, IGBluetoothReceiveListener {

	private TextView referralPrimaryInfoTextView;
	private TextView referralInfoTextView;
	private ListView referralInfoListView;
	private IGReferralInformationAdapter adapter = null;
	private ProgressDialog _progressDialog;
	private boolean calledPrintFunction;
	private IGReferralInformationResponseBean referralInformation = null;
	private ScrollView contentScrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.print_referral);
		initViews();
		callRetrieveReferralInformationApi();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IGUpdatePositionPollingTask.setIgnoreStaleState(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (_bluetoothHelper!= null) {
			_bluetoothHelper.stop();
		}
		return super.onOptionsItemSelected(item);		
	}

	private void initViews() {
		referralPrimaryInfoTextView = (TextView) findViewById(R.id.refer_info_primary);
		referralInfoTextView = (TextView) findViewById(R.id.refer_info_secondary);
		referralInfoListView = (ListView) findViewById(R.id.referralListView);
		contentScrollView = (ScrollView) findViewById(R.id.details_scrollview);
	}

	/**
	 * Calling retrieve referral information api to return commission,
	 * calculation and earnings details
	 */
	private void callRetrieveReferralInformationApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGReferralInformationApi api = new IGReferralInformationApi(this,
					this);
			api.retrieveReferralInformation();
		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * Print Referral Button Click, here proper bluetooth settings and done and
	 * data is provided to the printer device
	 * 
	 * @param v
	 */
	public void onClickPrintReferral(View v) {
		if (referralInformation == null) {
			return;
		}
		if (isPrinterConfigured()) {
			if (isPrinterConnected()) {
				printReferralData();

			} else {
				calledPrintFunction = true;
				isPrintReceipt(true);
				turnOnBlutooth(false);
			}

		} else {
			IGUtility.showDialogOk("",
					getResources().getString(R.string.printer_not_paired),
					IGPrintReferralActivity.this);
		}
	}

	/**
	 * Call for print Referral Data
	 */
	private void printReferralData() {
		// _bluetoothHelper.stop();
		if (_printerDevice != null) {
			calledPrintFunction = false;
			printFile(getReferralPrintData(), null, null, false);
		} else {
			calledPrintFunction = true;
			isPrintReceipt(true);
			turnOnBlutooth(true);
		}

	}

	/**
	 * Referral data is saved to sd card and and the file name is returned
	 * 
	 * @return
	 */
	private String getReferralPrintData() {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		String mobileNumber = IngogoApp.getSharedApplication().getUserId();

		String week = referralInformation.getSignupPeriod() + "";
		if (week.equalsIgnoreCase("1")) {
			week = "week";
		} else {
			week = week + " weeks";

		}

		String commissionpercentage = referralInformation
				.getReferralPercentage() + "";

		Date referralDate = new Date(referralInformation.getReferralDate());
		String referralDateString = dateFormat.format(referralDate);

		Date validReferralDate = new Date(
				referralInformation.getReferralValidUntilDate());
		String validDateString = dateFormat.format(validReferralDate);

		String referralPercentageAppliesFor = referralInformation
				.getReferralPercentageAppliesFor() + "";
		if (referralPercentageAppliesFor.equalsIgnoreCase("1")) {
			referralPercentageAppliesFor = " month.";
		} else {
			referralPercentageAppliesFor = " " + referralPercentageAppliesFor
					+ " months.";

		}

		String content = "\n~INGOGO DRIVER REFERRAL~" +

		"\n\nDRIVER ID: "
				+ mobileNumber
				+

				"\nDATE: "
				+ referralDateString
				+ "\n\n"
				+ "This referral notice is for the NEW DRIVER who wishes to \njoin ingogo."
				+ "\n\n"
				+ "Bring this receipt, your TAXI\nAUTHORITY & DRIVERS LICENSE to\nthe ingogo office between 10am \nand 5pm any business day to \njoin ingogo."
				+ "\n\n"
				+ "If you sign up within the next\n"
				+ week
				+ " you will receive\n"
				+ commissionpercentage
				+ "% commission for the\nfirst"
				+ referralPercentageAppliesFor
				+ "\n\n"
				+ "Your commission is then based\non volume:\n\n"

				+ "------------------------------\n"
				+

				"    Reach               Earn\n"
				+ "  this month         next month\n"
				+ "------------------------------\n"
				+
				// "Reach $0 - $2999\n" +
				// "or complete < 5\n" +
				// "ingogo jobs       -      5%\n\n" +
				// "Reach $3000+ \n" +
				// "or complete 5+\n" +
				// "ingogo jobs       -      5.5%\n\n"+
				// "Reach $4000+ \n" +
				// "or complete < 5\n" +
				// "ingogo jobs       -      5.5%\n" + // sample data

				createStringFromCommissionDetails(referralInformation
						.getCommissionDetails())
				+ // table like details string

				"------------------------------\n\n"
				+

				"This referral is valid until\n"
				+ validDateString
				+ "\n~-------------------------------~\n\n"
				+ "ingogo\n"
				+ "200 O'Riordan St, Mascot\n\n"
				/*
				 * "Suite 204 (Top Floor)\n" + "National Innovation Center\n" +
				 * "Australian Technology Park\n" + "Eveleigh, NSW, 2015\n\n"
				 */
				+ "Call us on 02 9011 5441 and we\ncan arrange to meet you and \ntrain you somewhere convenient\nfor you!\n\n\n";

		// File sdcard = Environment.getExternalStorageDirectory();
		// File file = new File(sdcard, "ReferralReceipt.txt");
		File file = getFileWithName("ReferralReceipt.txt");

		if (file.exists()) {
			file.delete();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(file, false));
			buf.append(content);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "ReferralReceipt.txt";

	}

	/**
	 * From commission details list, details are formatted in a box like form
	 * and the value is returned as commissionString
	 * 
	 * @param commissionDetails
	 * @return commissionString
	 */
	private String createStringFromCommissionDetails(
			List<IGCommissionDetails> commissionDetails) {
		String commissionString = "";
		if (commissionDetails != null) {
			for (int i = 0; i < commissionDetails.size(); i++) {
				String boxOneString = commissionDetails.get(i).getTarget() + "";

				if (boxOneString.length() < 16) { // if length of string is less
													// than 16 proper spaces are
													// added to make the data
													// look uniform
					int noOfSpacesNeeded = (16 - boxOneString.length()) - 1;
					for (int k = 0; k <= noOfSpacesNeeded; k++) {
						boxOneString = boxOneString + " ";
					}
				}
				Log.e("LEFT BOX ", "LEFT BOX LENGTH" + boxOneString.length());

				boxOneString = addNewLineCharacter(boxOneString, 16); // add new
																		// line
																		// character
																		// every
																		// 16
																		// index

				// adjust data by adding spaces to avoid table structure design
				// break
				String lastLine = boxOneString.split("\n")[boxOneString
						.split("\n").length - 1];
				String extraSpaces = "";
				if (lastLine.length() < 16) {
					int noOfSpacesNeeded = (16 - lastLine.length()) - 1;
					for (int k = 0; k <= noOfSpacesNeeded; k++) {
						extraSpaces = extraSpaces + " ";
					}
				}
				boxOneString = boxOneString + extraSpaces;

				commissionString = commissionString + boxOneString
						+ "  -      "
						+ commissionDetails.get(i).getCommission() + "%\n\n"; // final
																				// string

			}
		}

		return commissionString;
	}

	/**
	 * A string input is provided. New line characters are added on every
	 * 'onEveryIndex' index and the formatted string is returned
	 * 
	 * @param inputString
	 * @param onEveryIndex
	 * @return String
	 */
	private String addNewLineCharacter(String inputString, int onEveryIndex) {
		if (inputString.length() > onEveryIndex) {
			String result = "";
			while (inputString.length() > 0) {
				try {
					result += inputString.substring(0, onEveryIndex) + '\n'; // adding
																				// new
																				// line
																				// character
					inputString = inputString.substring(onEveryIndex); // pending
																		// string
																		// to be
																		// formatted
				} catch (Exception e) {
					result = result + inputString;
					break;
				}

			}
			return result;
		}
		return inputString;
	}

	@Override
	public void retrieveReferralInformationCompleted(
			IGReferralInformationResponseBean response) {
		referralInformation = response;
		IGUtility.dismissProgressDialog(_progressDialog);
		referralPrimaryInfoTextView
				.setText(getString(R.string.refer_friend_primary_info));
		String months = response.getReferralCommissionAppliesFor() + "";
		if (months.equalsIgnoreCase("1")) {
			months = "1 month!";
		} else {
			months = months + " months!";

		}
		referralInfoTextView
				.setText("Print a referral code from our App and hand it to the driver. When they sign up and mention you, you earn "
						+ response.getReferralCommissionPercentage()
						+ "% of their turnover for " + months);
		adapter = new IGReferralInformationAdapter(this,
				response.getSampleCalculations(),
				response.getReferralCommissionPercentage(),
				response.getReferralCommissionAppliesFor());
		referralInfoListView.setAdapter(adapter);
		updateListViewHeight();
	}

	public void updateListViewHeight() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		LayoutParams listParamsChargesList = referralInfoListView
				.getLayoutParams();
		if (referralInformation.getSampleCalculations() != null) {
			int height = (int) (63 * dm.density * (referralInformation
					.getSampleCalculations().size() + 2));
			listParamsChargesList.height = height;
		} else {
			listParamsChargesList.height = 0;
		}
		referralInfoListView.setLayoutParams(listParamsChargesList);
		contentScrollView.fullScroll(ScrollView.FOCUS_UP);
		contentScrollView.smoothScrollTo(0, 0);

	}

	@Override
	public void retrieveReferralInformationFailed(String errorMessage) {
		referralInformation = null;
		IGUtility.dismissProgressDialog(_progressDialog);
		Log.e("SendReceiptFailed", errorMessage);
		IGUtility.showDialogOk("", errorMessage, this);

	}

	@Override
	public void connectedResult(String message) {
		IGPrintReferralActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				IGUtility.dismissProgressDialog(_blutoothProgressDialog);

			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (calledPrintFunction) {
					printFile(getReferralPrintData(), null, null, false);
				}
			}
		}).start();

	}

}
