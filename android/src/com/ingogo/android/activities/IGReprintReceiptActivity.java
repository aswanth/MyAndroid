package com.ingogo.android.activities;

import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGBluetoothBasePaymentActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.model.IGReceiptInformationModel;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGReprintLastReceiptApi;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGReprintLastReceiptApiListener;

public class IGReprintReceiptActivity extends IGBluetoothBasePaymentActivity
		implements IGReprintLastReceiptApiListener, IGExceptionApiListener {

	private ProgressDialog _progressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reprint_receipt);

		if (getIntent().hasExtra(IGConstants.kDataKey)) {

			new Handler().postDelayed(new Runnable() {
				// @Override
				public void run() {
					printReceiptFromData();
				}
			}, 100);

		} else {
			reprintLastReceiptDialog();
		}

	}

	private void printReceiptFromData() {

		if (isPrinterConfigured()) {
			processExtras();
		} else {
			Dialog dlg = new AlertDialog.Builder(IGReprintReceiptActivity.this)
					.setTitle("")
					.setMessage(
							getResources().getString(
									R.string.printer_not_paired))
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									IGReprintReceiptActivity.this
											.finishReprintReceipt();
								}
							}).create();
			if (!IGReprintReceiptActivity.this.isFinishing())
				dlg.show();

		}
	}

	private void processExtras() {
		IGReceiptInformationModel receiptObj = (IGReceiptInformationModel) getIntent()
				.getSerializableExtra(IGConstants.kReceiptInformationKey);

		writeReceipt(receiptObj);
		turnOnBlutooth(true);
	}

	private void reprintLastReceiptDialog() {

		Dialog dlg = new AlertDialog.Builder(this).setTitle("")
				.setMessage("Re-printing Last Receipt")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						if (isPrinterConfigured()) {
							callReprintLastReceiptApi();
						} else {
							Dialog dlg = new AlertDialog.Builder(
									IGReprintReceiptActivity.this)
									.setTitle("")
									.setMessage(
											getResources()
													.getString(
															R.string.printer_not_paired))
									.setPositiveButton(
											"OK",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {

													IGReprintReceiptActivity.this
															.finishReprintReceipt();
												}
											}).create();
							if (!IGReprintReceiptActivity.this.isFinishing())
								dlg.show();

						}
					}
				}).create();
		dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		Activity activity = (Activity) this;
		if (!activity.isFinishing()) {
			dlg.show();
		}
	}

	private void callReprintLastReceiptApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGReprintLastReceiptApi receiptApi = new IGReprintLastReceiptApi(
					this, this);
			receiptApi.reprintLastReceipt();

		} else {
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	@Override
	public void reprintLastReceiptCompleted(
			IGReceiptInformationModel contactInfo) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		if (contactInfo != null) {
			writeReceipt(contactInfo);
			turnOnBlutooth(true);
		}
	}

	@Override
	public void reprintLastReceiptFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		Dialog dlg = new AlertDialog.Builder(this).setTitle("")
				.setMessage(errorMessage)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						IGReprintReceiptActivity.this.finishReprintReceipt();
					}
				}).create();
		if (!IGReprintReceiptActivity.this.isFinishing())
			dlg.show();
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		/*
		 * IGUtility.dismissProgressDialog(_progressDialog); finish();
		 */
		handleDriverStaleState();
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		/*
		 * IGUtility.dismissProgressDialog(_progressDialog); finish();
		 */
		handleDriverStaleState();
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		/*
		 * IGUtility.dismissProgressDialog(_progressDialog); finish();
		 */
		handleDriverStaleState();
	}

	@Override
	public void onNullResponseRecieved() {
		/*
		 * IGUtility.dismissProgressDialog(_progressDialog); finish();
		 */
		handleDriverStaleState();
	}

	@Override
	protected void finishReprintReceipt() {
		finish();
	}

}
