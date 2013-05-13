package com.ingogo.android.adapters;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGPaymentDailySummaryActivity;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGPaymentDailySummaryModel;
import com.ingogo.android.utilities.IGUtility;

public class IGPaymentDailySummaryAdapter extends BaseAdapter {

	IGPaymentDailySummaryActivity _context;
	ArrayList<IGPaymentDailySummaryModel> _paymentDailySummary;
	TextView whenTv, meterFareTv, settlingAmountTv;
	String _totalAmount;
	float _addAmount = 0;

	public IGPaymentDailySummaryAdapter(
			IGPaymentDailySummaryActivity igPaymentHistorySummary,
			ArrayList<IGPaymentDailySummaryModel> paymentDailySummary) {
		this._context = igPaymentHistorySummary;
		this._paymentDailySummary = paymentDailySummary;

	}

	@Override
	public int getCount() {
		if (_paymentDailySummary == null || _paymentDailySummary.size() == 0) {
			return 0;
		} else {
			return _paymentDailySummary.size();
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return _paymentDailySummary.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = LayoutInflater.from(_context).inflate(
				R.layout.payment_daily_list_cell, null);

		whenTv = (TextView) convertView.findViewById(R.id.textDate);
		meterFareTv = (TextView) convertView.findViewById(R.id.textMeterFare);
		settlingAmountTv = (TextView) convertView
				.findViewById(R.id.textSettling);

		String date = _paymentDailySummary.get(position).getWhen();
		Log.e("date", "" + date);
		whenTv.setText(getDateString(date));
		Log.e("when", "" + getDateString(date));

		setMeterAmount(meterFareTv, _paymentDailySummary.get(position)
				.getMeterAmount());
		setSettlingAmount(settlingAmountTv, _paymentDailySummary.get(position)
				.getSettlingAmount());
		return convertView;
	}

	private void setOfflineText(TextView textView) {
		textView.setText("offline");
		textView.setTextColor(IngogoApp.getSharedApplication()
				.getApplicationContext().getResources()
				.getColor(R.color.red_color));
	}

	private void setMeterAmount(TextView textView, String amount) {

		if (amount == null || amount.contains("null")) {
			setOfflineText(textView);
		} else {
			Float amountDouble = Float.parseFloat(amount);
			Log.i("AMT DOUBLE", "" + amountDouble);
			if (amount.contains("-")) {
				String amt = amount.replaceAll("-", "");
				textView.setText("$ "
						+ "("
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amt)) + ")");
			} else if (amountDouble == 00.00) {
				setOfflineText(textView);
			} else {
				textView.setText("$ "
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amount)));
			}
		}
	}

	private void setSettlingAmount(TextView textView, String amount) {

		if (amount.contains("null") || amount == null) {
			textView.setText(IGConstants.zeroBalance);
		} else {
			if (amount.contains("-")) {
				String amt = amount.replaceAll("-", "");
				textView.setText("$ "
						+ "("
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amt)) + ")");
			} else {
				textView.setText("$ "
						+ new DecimalFormat("0.00").format(Double
								.parseDouble(amount)));
			}
		}
	}

	private String getDateString(String dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM hh:mma");
		Date dat = null;
		try {
			dat = IGUtility.getDateFromTimeStamp(dateTime);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e("time", "" + dat.getHours() + "***" + dat.getMinutes());
		String s = sdf.format(dat);
		return s;
	}

}
