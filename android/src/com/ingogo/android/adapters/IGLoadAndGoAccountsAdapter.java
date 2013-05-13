package com.ingogo.android.adapters;

import java.text.DecimalFormat;
import java.util.List;
import com.ingogo.android.R;
import com.ingogo.android.model.IGLoadAndGoAccountDetailsModel;
import com.ingogo.android.utilities.IGUtility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class IGLoadAndGoAccountsAdapter extends
		ArrayAdapter<IGLoadAndGoAccountDetailsModel> {

	private Context _context;
	private DecimalFormat _decimalFormatter;

	public IGLoadAndGoAccountsAdapter(Context context,
			List<IGLoadAndGoAccountDetailsModel> models) {
		super(context, 0, 0, models);
		this._context = context;
		_decimalFormatter = new DecimalFormat("00.00");
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(_context).inflate(
					R.layout.accountinfo_recorded_list_cell, null);
		}

		TextView name = (TextView) convertView.findViewById(R.id.name);
		TextView number = (TextView) convertView.findViewById(R.id.number);
		TextView balance = (TextView) convertView.findViewById(R.id.balance);
		TextView balanceAsAt = (TextView) convertView
				.findViewById(R.id.balanceAsAt);
		name.setText(getItem(position).getAccountName());
		number.setText(getItem(position).getAccountNumber());
		String balanceString = "00.00";
		try {
			balanceString = _decimalFormatter.format(Double
					.parseDouble(getItem(position).getAccountBalance()));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		balance.setText("$ " + balanceString);
		String balanceAsAtString = getItem(position).getBalanceAsAt();
		if (balanceAsAtString != null && !balanceAsAtString.equals("")) {
			balanceAsAt.setVisibility(View.VISIBLE);
			balanceAsAt.setText(IGUtility.getDateStringFromFormat(balanceAsAtString,"dd/MM hh:mma"));
		}else {
			balanceAsAt.setVisibility(View.GONE);
		}
		return convertView;
	}
}
