package com.ingogo.android.adapters;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ingogo.android.R;
import com.ingogo.android.activities.IGPrintReferralActivity;
import com.ingogo.android.model.IGSampleCalculation;
import com.ingogo.android.utilities.IGUtility;

/**
 * IGReferralInformationAdapter to show commission details 
 * @author suslov
 *
 */
public class IGReferralInformationAdapter extends BaseAdapter{
	IGPrintReferralActivity _context;
	List<IGSampleCalculation> _sampleCalculationList;
	float  _referralCommissionPercentage;
	int  _referralCommissionAppliesFor;
	
	public IGReferralInformationAdapter(
			IGPrintReferralActivity igPaymentHistorySummary,
			List<IGSampleCalculation> sampleCalculationList,float  referralCommissionPercentage,
			int  referralCommissionAppliesFor) {
		this._context = igPaymentHistorySummary;
		this._sampleCalculationList = sampleCalculationList;
		this._referralCommissionPercentage = referralCommissionPercentage;
		this._referralCommissionAppliesFor = referralCommissionAppliesFor;

	}
	@Override
	public int getCount() {
		if (_sampleCalculationList == null || _sampleCalculationList.size() == 0) {
			return 0;
		} else {
			return _sampleCalculationList.size() + 2;
		}
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewCache viewCache;
		LayoutInflater inflater;

		inflater = _context.getLayoutInflater();
		rowView = inflater.inflate(R.layout.referral_information_list_cell, null);
		viewCache = new ViewCache(rowView);
		rowView.setTag(viewCache);
		
		ImageView seperatorBottom = viewCache.getSeperatorBottom();
		ImageView seperatorCenter = viewCache.getSeperatorCenter();
		TextView fullTextView = viewCache.getFullTextView();
		TextView leftTextView = viewCache.getLeftTextView();
		TextView rightTextView = viewCache.getRightTextView();


		if (_sampleCalculationList.size() + 1 == position) { // bottom seperator should be displayed only for bottom row
			seperatorBottom.setVisibility(View.VISIBLE);
		} else {
			seperatorBottom.setVisibility(View.GONE);
		}
		
		if (position<1) { // Table main Header
			fullTextView.setVisibility(View.VISIBLE);
			leftTextView.setVisibility(View.GONE);
			rightTextView.setVisibility(View.GONE);
			seperatorCenter.setVisibility(View.GONE);
			fullTextView.setText("Refer a driver - Earn " + _referralCommissionPercentage+ "% commisssion");

			
		} else {
			fullTextView.setVisibility(View.GONE);
			leftTextView.setVisibility(View.VISIBLE);
			rightTextView.setVisibility(View.VISIBLE);
			seperatorCenter.setVisibility(View.VISIBLE);
			if (position==1) { // table section headers
				String referralCommissionAppliesFor = _referralCommissionAppliesFor + "";
				String box1MonthValue = "";
				String box2MonthValue = "";

				if (referralCommissionAppliesFor.equalsIgnoreCase("1")) {
					box1MonthValue = "1 month";
					box2MonthValue = "month";
				} else {
					box1MonthValue = referralCommissionAppliesFor + " months";
					box2MonthValue = referralCommissionAppliesFor + " months";

				}
				
				leftTextView.setText("If they do these payments/month for "+ box1MonthValue);
				rightTextView.setText("YOU EARN this commission on their 1st "+ box2MonthValue);
			} else { // data
				String amount = IGUtility.getInPriceFormat((double)_sampleCalculationList.get(position-2).getAmount());
				String earnings = IGUtility.getInPriceFormat((double)_sampleCalculationList.get(position-2).getEarnings());
				leftTextView.setText("$"+ amount);
				rightTextView.setText("$"+ earnings);
			}

		}
		
		
		return rowView;
	}
	public class ViewCache {
		private View baseView;
		private ImageView seperatorBottom;
		private ImageView seperatorCenter;
		private TextView fullTextView;
		private TextView leftTextView;
		private TextView rightTextView;

		public ViewCache(View baseView) {
			this.baseView = baseView;
		}


		public ImageView getSeperatorBottom() {
			if (seperatorBottom == null) {
				seperatorBottom = (ImageView) baseView
						.findViewById(R.id.sepBottom);
			}
			return seperatorBottom;
		}
		
		public ImageView getSeperatorCenter() {
			if (seperatorCenter == null) {
				seperatorCenter = (ImageView) baseView
						.findViewById(R.id.sepCenter);
			}
			return seperatorCenter;
		}


		public TextView getFullTextView() {
			if (fullTextView == null) {
				fullTextView = (TextView) baseView
						.findViewById(R.id.fullText);
			}
			return fullTextView;
		}


		public TextView getLeftTextView() {
			if (leftTextView == null) {
				leftTextView = (TextView) baseView
						.findViewById(R.id.LeftText);
			}
			return leftTextView;
		}


		public TextView getRightTextView() {
			if (rightTextView == null) {
				rightTextView = (TextView) baseView
						.findViewById(R.id.RightText);
			}
			return rightTextView;
		}

	}
}
