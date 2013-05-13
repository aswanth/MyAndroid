package com.ingogo.android.adapters;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGPaymentHistorySummaryActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGPaymentSummaryModel;
import com.ingogo.android.utilities.IGUtility;

public class IGPaymentSummariesAdapter extends BaseAdapter {

	IGPaymentHistorySummaryActivity _context;
	ArrayList<IGPaymentSummaryModel> _paymentSummaries;
	Double _pendingAmount;

	public IGPaymentSummariesAdapter(
			IGPaymentHistorySummaryActivity igPaymentHistorySummary,
			ArrayList<IGPaymentSummaryModel> paymentSummaries,
			Double pendingAmount) {
		this._context = igPaymentHistorySummary;
		this._paymentSummaries = paymentSummaries;
		this._pendingAmount = pendingAmount;
	}

	@Override
	public int getCount() {
		if (_paymentSummaries == null || _paymentSummaries.size() == 0) {
			return 1;
		} else {
			return _paymentSummaries.size() + 1;
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return _paymentSummaries.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = LayoutInflater.from(_context).inflate(
				R.layout.payment_history_list_cell, null);

		final TextView dateTv = (TextView) convertView
				.findViewById(R.id.date_text);
		final TextView amountTv = (TextView) convertView
				.findViewById(R.id.amount_text);
		final TextView statusTv = (TextView) convertView
				.findViewById(R.id.status_text);
		if (position == 0) {
			if (null != _pendingAmount || !_pendingAmount.equals("null")) {
				setAmountText(amountTv, String.valueOf(new DecimalFormat(
						"00.00").format(_pendingAmount)));
			} else {
				setAmountText(amountTv, IGConstants.zeroBalance);
			}
			setStatusText(statusTv, "PENDING");
		} else {
			if (_paymentSummaries != null) {
				String date = _paymentSummaries.get(position - 1).getSettled();
				dateTv.setText(getDateString(date));
				setAmountText(amountTv, _paymentSummaries.get(position - 1)
						.getAmount());
				setStatusText(statusTv, _paymentSummaries.get(position - 1)
						.getStatus());
				convertView.setTag(_paymentSummaries.get(position - 1)
						.getStatus());
			}
		}
		return convertView;
	}

	private void setStatusText(TextView statusTv, String status) {
//		if (IngogoApp.getThemeID() == 1) { // day
			if (status.equalsIgnoreCase("Settled")) {
				statusTv.setTextColor(IngogoApp.getSharedApplication()
						.getApplicationContext().getResources()
						.getColor(R.color.green_color));
			} else if (status.equalsIgnoreCase("Pending")) {
				statusTv.setTextColor(IngogoApp.getSharedApplication()
						.getApplicationContext().getResources()
						.getColor(R.color.red_color));
			} else {
				statusTv.setTextColor(IngogoApp.getSharedApplication()
						.getApplicationContext().getResources()
						.getColor(R.color.red_color));
			}
//		} else { // night
//			if (status.equalsIgnoreCase("Settled")) {
//				statusTv.setTextColor(IngogoApp.getSharedApplication()
//						.getApplicationContext().getResources()
//						.getColor(R.color.green_color));
//			} else if (status.equalsIgnoreCase("Pending")) {
//				statusTv.setTextColor(IngogoApp.getSharedApplication()
//						.getApplicationContext().getResources()
//						.getColor(R.color.red_color));
//			}
//		}
		statusTv.setText(status.toUpperCase());
	}

	private void setAmountText(TextView amountTv, String amount) {

		if (amount.contains("null") || amount == null) {
			amountTv.setText("");
		} else {
			if (amount.contains("-")) {
				String amt = amount.replaceAll("-", "");
				amountTv.setText("$"
						+ "   "
						+ "("
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amt)) + ")");

			} else {
				amountTv.setText("$"
						+ "   "
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amount)));
			}
		}

	}

	private String getDateString(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		Date dat = null;
		try {
			dat = IGUtility.getDateFromTimeStamp(date);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String s = sdf.format(dat);
		return s;

	}

}
