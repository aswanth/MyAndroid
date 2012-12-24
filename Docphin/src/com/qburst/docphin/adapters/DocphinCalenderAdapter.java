package com.qburst.docphin.adapters;

import java.util.ArrayList;
import java.util.TreeSet;

import com.qburst.docphin.R;
import com.qburst.docphin.datamodels.DocphinCalenderModel;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DocphinCalenderAdapter extends BaseAdapter {
	private static final int TYPE_ITEM = 0;
	private static final int TYPE_SEPARATOR = 1;
	private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
	private Activity _activity;
	private ArrayList<DocphinCalenderModel> mData = new ArrayList<DocphinCalenderModel>();
	private LayoutInflater mInflater;
	private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();

	public DocphinCalenderAdapter(Activity activity) {
		_activity = activity;
		mInflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}

	public void addItem(DocphinCalenderModel item) {
		mData.add(item);
		notifyDataSetChanged();
	}

	public void addDateItem(DocphinCalenderModel item) {
		mData.add(item);
		mSeparatorsSet.add(mData.size() - 1);
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		int type = getItemViewType(position);
		System.out.println("getView " + position + " " + convertView
				+ " type = " + type);
		if (convertView == null) {
			holder = new ViewHolder();
			DocphinCalenderModel item1 = mData.get(position);
			switch (type) {

			case TYPE_ITEM:
				convertView = mInflater.inflate(
						R.layout.docphin_calender_item1, null);

				holder.tvtime = (TextView) convertView
						.findViewById(R.id.tv_time);
				holder.tvevent = (TextView) convertView
						.findViewById(R.id.tv_event);
				holder.tvplace = (TextView) convertView
						.findViewById(R.id.tv_place);
				convertView.setTag(holder);
				holder.tvtime.setText(item1.getEventTime());
				holder.tvevent.setText(item1.getTitle());
				holder.tvplace.setText(item1.getEventDateTime());

				break;
			case TYPE_SEPARATOR:
				convertView = mInflater.inflate(
						R.layout.docphin_calender_item2, null);
				holder.tvdate = (TextView) convertView
						.findViewById(R.id.tv_date);
				convertView.setTag(holder);
				holder.tvdate.setText(item1.getEventDate());
				break;
			}

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public class ViewHolder {
		public TextView tvdate;
		public TextView tvtime;
		public TextView tvevent;
		public TextView tvplace;
	}
}
