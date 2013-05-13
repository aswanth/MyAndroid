package com.ingogo.android.activities.payments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.adapters.IGPaymentDailySummaryAdapter;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGPaymentDailySummaryModel;
import com.ingogo.android.model.IGPaymentSummaryModel;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGBluetoothReceiveListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGPaymentDailyHistoryApi;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentDailyHistoryApiListener;

public class IGPaymentDailySummaryActivity extends IGBluetoothBaseActivity
		implements IGExceptionApiListener, IGPaymentDailyHistoryApiListener,
		IGBluetoothReceiveListener {

	private ProgressDialog _progressDialog;

	private TextView _totalTV, _carriedForwardTV, _meterFareTotalTv,
			_paidToAccount;
	private ListView _paymentDailySummaryListView;
	private String _username, _password, _paymentStatus;
	private IGPaymentSummaryModel _paymentModel;
	private long _paymentId;
	private ArrayList<IGPaymentDailySummaryModel> _paymentDailySummary;
	private IGPaymentDailySummaryAdapter adapter = null;
	private boolean calledPrintFunction;
	private float _meterTotal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_daily_summary);
		initViews();
		getDailyTripHistory();

	}

	private void initViews() {
		_username = IngogoApp.getSharedApplication().getUserId();
		_password = IngogoApp.getSharedApplication().getPassword();
		_paymentModel = (IGPaymentSummaryModel) getIntent()
				.getSerializableExtra(IGConstants.kPaymentHistoryItem);

		Log.e("payment id", "***" + _paymentId);
		if (_paymentModel != null) {
			_paymentId = Long.parseLong(_paymentModel.getPaymentId());
			_paymentStatus = _paymentModel.getStatus();
		} else {
			_paymentStatus = getIntent().getStringExtra(
					IGConstants.kPaymentStatus);
		}
		_paidToAccount = (TextView) findViewById(R.id.paidToAccountTV);
		_totalTV = (TextView) findViewById(R.id.textPaymentTotal);
		_meterFareTotalTv = (TextView) findViewById(R.id.meterFareTotalTv);
		_carriedForwardTV = (TextView) findViewById(R.id.textCF);
		_paymentDailySummaryListView = (ListView) findViewById(R.id.listDailySummary);
		_paymentDailySummaryListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Intent intent = new Intent(
								IGPaymentDailySummaryActivity.this,
								IGPaymentHistoryDetailActivity.class);
						intent.putExtra(IGConstants.kBookingId,
								_paymentDailySummary.get(arg2).getBookingId());
						startActivity(intent);
					}
				});
	}

	private void getDailyTripHistory() {
		if (IGUtility.isNetworkAvailable(this)) {
			if (!isFinishing()) {
				_progressDialog = IGUtility.showProgressDialog(this);
			}
			IGPaymentDailyHistoryApi paymentDailyHistoryApi = new IGPaymentDailyHistoryApi(
					this, this);
			paymentDailyHistoryApi.getPaymentDailyHistorySummaries(_username,
					_password, _paymentId, _paymentStatus);
		} else {
			// IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	@Override
	public void paymentDailyHistoryFetchingCompleted(
			ArrayList<IGPaymentDailySummaryModel> paymentDailySummaries,
			String paidToAccount) {
		IGUtility.dismissProgressDialog(_progressDialog);
		sortPaymentDailySummaries(paymentDailySummaries);
		// filterPaymentDailySummaries();
		adapter = new IGPaymentDailySummaryAdapter(this, paymentDailySummaries);
		_paymentDailySummaryListView.setAdapter(adapter);

		float addAmount = 0;
		_meterTotal = 0;
		for (int i = 0; i < paymentDailySummaries.toArray().length; i++) {

			try {
				addAmount = addAmount
						+ Float.parseFloat(_paymentDailySummary.get(i)
								.getSettlingAmount());
				String meterFareString = _paymentDailySummary.get(i)
						.getMeterAmount();
				if (meterFareString != null
						&& !meterFareString.trim().equals("null"))
					_meterTotal = _meterTotal
							+ Float.parseFloat(meterFareString);
			} catch (NumberFormatException e) {
			}
		}
		DecimalFormat formatter = new DecimalFormat("00.00");

		setTotalAmount(_totalTV, formatter.format(addAmount));


		if (paidToAccount != null
				&& _paymentStatus.trim().equalsIgnoreCase(
						IGApiConstants.kPaymentStatus.trim())) {
			_paidToAccount.setText(getResources().getString(
					R.string.paid_to_account)
					+ " " + paidToAccount);
		}

		if (_meterTotal != 0)
			_meterFareTotalTv.setText("$ " + formatter.format(_meterTotal));
	}

	private void setTotalAmount(TextView textView, String totalAmount) {

		if (totalAmount == null || totalAmount.contains("null")) {
			textView.setText(IGConstants.zeroBalance);
		} else {
			if (totalAmount.contains("-")) {
				String amt = totalAmount.replaceAll("-", "");
				textView.setText("$ " + "(" + amt + ")" + "*");
				_carriedForwardTV.setText(IGConstants.kCarriedForward);

			} else {
				textView.setText("$ " + totalAmount);
			}
		}
	}

	private void sortPaymentDailySummaries(
			ArrayList<IGPaymentDailySummaryModel> paymentDailySummaries) {
		_paymentDailySummary = paymentDailySummaries;
		Collections.sort(_paymentDailySummary,
				new Comparator<IGPaymentDailySummaryModel>() {

					@Override
					public int compare(IGPaymentDailySummaryModel lhs,
							IGPaymentDailySummaryModel rhs) {
						return (new Long(rhs.getWhen()).compareTo(new Long(lhs
								.getWhen())));

					}

				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		IGUpdatePositionPollingTask.setIgnoreStaleState(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		IGUpdatePositionPollingTask.setIgnoreStaleState(false);
	}

	/**
	 * Referral data is saved to sd card and and the file name is returned
	 * 
	 * @return
	 */
	private String savePrintData() {

//		File sdcard = Environment.getExternalStorageDirectory();
//		File file = new File(sdcard, "IGPaymentDailySummary.txt");
		 File file = getFileWithName("IGPaymentDailySummary.txt");

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
			buf.append(getPaymentDailyDataToBePrinted());
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "IGPaymentDailySummary.txt";

	}

	/**
	 * Get the whole data to be passsed to the printer
	 * 
	 * @return
	 */
	private String getPaymentDailyDataToBePrinted() {
		// _paymentDailySummary
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String currentDate = dateFormat.format(new Date());
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma");
		String currentTime = timeFormat.format(new Date());

		String paymentStatus = _paymentStatus.toUpperCase();
		String owedText = "Owed";
		if (_paymentStatus.toUpperCase().equals(
				IGConstants.kPaymentStatusSettled)) {
			paymentStatus = IGConstants.kPaymentStatusPaid;
			owedText = "Paid";
		}
		DecimalFormat formatter = new DecimalFormat("0.00");

		String content = "\n~ingogo report~\n\n" + "Name:     "
				+ IngogoApp.getSharedApplication().getDriverName() + "\n"
				+ "Mobile:   " + IngogoApp.getSharedApplication().getUserId()
				+ "\n" + "Plate:    "
				+ IngogoApp.getSharedApplication().getPlateNumber() + "\n"
				+ "Date printed:     " + currentDate + "\n"
				+ "Time printed:     " + currentTime + "\n"
				+ "Status of funds:  " + paymentStatus + "\n\n\n" + "\n~"
				+ alignString("Trip", 9, 0) + alignString("Meter", 11, 1)
				+ alignString(owedText, 11, 2) + "~"
				+ "\n--------------------------------" +

				"\n\n" + getPaymentDailyDataInTableFormat()
				+ "--------------------------------" + "\n" + "~"
				+ alignString("TOTAL", 9, 0)
				+ alignString("$ " + formatter.format(_meterTotal), 11, 1)
				+ alignString(getTotalAmount(), 12, 2) + "~" + "\n\n"
				+ "*Commission:  $ " + formatter.format(getCommission())
				+ "\n\nPending amounts are settled\n"
				+ "next business day.\n\n" + "Settled amounts have been\n"
				+ "paid to your nominated " + "account\n\n";
		return content;
	}

	private float getCommission() {

		float commission = getTotalAmountInFloat() - _meterTotal;

		return commission;
	}

	/**
	 * get the daily payment details array to the
	 * 
	 * @return
	 */
	private String getPaymentDailyDataInTableFormat() {
		String tableData = "";
		_meterTotal = 0;
		if (_paymentDailySummary != null) {
			for (int i = 0; i < _paymentDailySummary.size(); i++) {

				String timeStamp = _paymentDailySummary.get(i).getWhen();
				String dateWithTime = IGUtility.getDateString(timeStamp,
						"dd/MM hh:mma");
				String dateString = dateWithTime.split(" ")[0] + "\n";

				String timeString = dateWithTime.split(" ")[1];
				timeString = alignString(timeString, 9, 0);

				String meterFareString = getMeterAmount(_paymentDailySummary
						.get(i).getMeterAmount());
				if (_paymentDailySummary.get(i).getMeterAmount() != null
						&& !_paymentDailySummary.get(i).getMeterAmount().trim()
								.equals("null"))
					_meterTotal = _meterTotal
							+ Float.parseFloat(_paymentDailySummary.get(i)
									.getMeterAmount());

				meterFareString = alignString(meterFareString, 11, 1);

				String settlingAmountString = getSettlingAmount(_paymentDailySummary
						.get(i).getSettlingAmount());
				settlingAmountString = alignString(settlingAmountString, 11, 2);

				String rowData = dateString + timeString + meterFareString
						+ settlingAmountString;
				tableData = tableData + rowData + "\n\n";
			}
		}

		return tableData;
	}

	/**
	 * 
	 * @param inputString
	 * @param maxLength
	 * @param alignmentStyle
	 *            -- possibleValues are < 0 for left align > < 1 for center
	 *            align > < 2 for right align >
	 * @return
	 */
	private String alignString(String inputString, int maxLength,
			int alignmentStyle) {
		String outputString = inputString;
		if (alignmentStyle == 0) {
			if (outputString.length() < maxLength) {
				int noOfSpacesNeeded = (maxLength - outputString.length()) - 1;
				for (int k = 0; k <= noOfSpacesNeeded; k++) {
					outputString = outputString + " ";
				}
			}
		} else if (alignmentStyle == 1) {
			if (outputString.length() < maxLength) {
				int noOfSpacesNeeded = (maxLength - outputString.length()) - 1;
				for (int k = 0; k <= noOfSpacesNeeded; k++) {
					if (k % 2 == 0) {
						outputString = " " + outputString;

					} else {
						outputString = outputString + " ";

					}
				}
			}
		} else if (alignmentStyle == 2) {
			if (outputString.length() < maxLength) {
				int noOfSpacesNeeded = (maxLength - outputString.length()) - 1;
				for (int k = 0; k <= noOfSpacesNeeded; k++) {
					outputString = " " + outputString;
				}
			}
		}
		return outputString;

	}

	/**
	 * Get total amount in printing format
	 * 
	 * @return
	 */
	private String getTotalAmount() {
		float addAmount = 0;
		for (int i = 0; i < _paymentDailySummary.toArray().length; i++) {

			try {
				addAmount = addAmount
						+ Float.parseFloat(_paymentDailySummary.get(i)
								.getSettlingAmount());
			} catch (NumberFormatException e) {
			}
		}
		DecimalFormat formatter = new DecimalFormat("00.00");
		String totalAmount = formatter.format(addAmount);

		String amount = "";
		if (totalAmount == null || totalAmount.contains("null")) {
			amount = IGConstants.zeroBalance;
		} else {
			if (totalAmount.contains("-")) {
				String amt = totalAmount.replaceAll("-", "");
				amount = "$ " + "(" + amt + ")" + "*" + "\n"
						+ IGConstants.kCarriedForward;

			} else {
				amount = "$ " + totalAmount;

			}
		}
		return amount;
	}

	/**
	 * Get total amount in printing format
	 * 
	 * @return
	 */
	private float getTotalAmountInFloat() {
		float addAmount = 0;
		for (int i = 0; i < _paymentDailySummary.toArray().length; i++) {

			try {
				addAmount = addAmount
						+ Float.parseFloat(_paymentDailySummary.get(i)
								.getSettlingAmount());
			} catch (NumberFormatException e) {
			}
		}
		return addAmount;
	}

	/**
	 * Get settling amount in printing format
	 * 
	 * @param amount
	 * @return
	 */
	private String getSettlingAmount(String amount) {
		String updatedAmount = "";

		if (amount.contains("null") || amount == null) {
			updatedAmount = IGConstants.zeroBalance;
		} else {
			if (amount.contains("-")) {
				String amt = amount.replaceAll("-", "");
				updatedAmount = "$ "
						+ "("
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amt)) + ")";
			} else {
				updatedAmount = "$ "
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amount));
			}
		}
		return updatedAmount;
	}

	/**
	 * Get meter amount in printing format
	 * 
	 * @param amount
	 * @return
	 */
	private String getMeterAmount(String amount) {
		String updatedAmount = "";
		if (amount == null || amount.contains("null")) {
			updatedAmount = "offline";
		} else {
			Float amountDouble = Float.parseFloat(amount);
			Log.i("AMT DOUBLE", "" + amountDouble);
			if (amount.contains("-")) {
				String amt = amount.replaceAll("-", "");
				updatedAmount = "$ "
						+ "("
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amt)) + ")";
			} else if (amountDouble == 00.00) {
				updatedAmount = "offline";
			} else {
				updatedAmount = "$ "
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amount));
			}
		}
		return updatedAmount;
	}

	@Override
	public void paymentDailyHistoryFetchingFailed(String errorMessage) {
		IGUtility.dismissProgressDialog(_progressDialog);
		IGUtility.showDialogOk("", errorMessage, this);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNetWorkUnavailableResponse(errorResponse);
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onRequestTimedoutResponse(errorResponse);
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onInternalServerErrorResponse(errorResponse);

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNullResponseRecieved();

	}

	/**
	 * Calling the print data based on the status of the printer
	 * 
	 * @param v
	 */
	public void onPrintButtonClick(View v) {

		if (isPrinterConfigured()) {
			if (isPrinterConnected()) {
				printReferralData();
				calledPrintFunction = false;

			} else {
				calledPrintFunction = true;
				isPrintReceipt(true);
				turnOnBlutooth(false);
			}

		} else {
			IGUtility.showDialogOk("",
					getResources().getString(R.string.printer_not_paired),
					IGPaymentDailySummaryActivity.this);
		}
	}

	/**
	 * Call for print Referral Data
	 */
	private void printReferralData() {
		// _bluetoothHelper.stop();
		if (_printerDevice != null) {
			calledPrintFunction = true;
			printFile(savePrintData(), null, null, false);
		} else {
			calledPrintFunction = true;
			isPrintReceipt(true);
			turnOnBlutooth(true);
		}

	}

	@Override
	public void connectedResult(String message) {
		IGPaymentDailySummaryActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				IGUtility.dismissProgressDialog(_blutoothProgressDialog);

			}
		});
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (calledPrintFunction) {
					printFile(savePrintData(), null, null, false);
				}
			}
		}).start();

	}

}
