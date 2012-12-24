package com.qburst.docphin.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.qburst.docphin.R;
import com.qburst.docphin.activities.base.DocphinBaseActivity;
import com.qburst.docphin.adapters.DocphinCalenderAdapter;
import com.qburst.docphin.adapters.DocphinMyMessageListAdapter;
import com.qburst.docphin.api.DocphinEventsAPI;
import com.qburst.docphin.api.DocphinMyMessagesAPI;
import com.qburst.docphin.apilisteners.DocphinEventsAPIListener;
import com.qburst.docphin.apilisteners.DocphinMyMessageAPIListener;
import com.qburst.docphin.datamodels.DocphinCalenderModel;
import com.qburst.docphin.datamodels.DocphinMessageModel;

public class DocphinCalenderActivity extends DocphinBaseActivity implements
		DocphinEventsAPIListener {
	private View _calenderLayout;
	private ViewGroup tabBar;
	private Button paneButton;
	private DocphinCalenderAdapter _adapter;
	ArrayList<DocphinCalenderModel> eventsList;
	String currentdate = null;
	ListView eventListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initViews();
		initClickListners();
		setLayout(_calenderLayout, paneButton);
		DocphinEventsAPI eventsAPI = new DocphinEventsAPI(this);
		eventsAPI.getEvents();
	}

	private void initViews() {
		_calenderLayout = inflater.inflate(R.layout.docphin_calender, null);

		tabBar = (ViewGroup) _calenderLayout.findViewById(R.id.paneLayout);
		paneButton = (Button) tabBar.findViewById(R.id.paneButton);
		paneButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
				_menuLayout));

		eventListView = (ListView) _calenderLayout.findViewById(R.id.lvevents);
		_adapter = new DocphinCalenderAdapter(this);
		eventsList = new ArrayList<DocphinCalenderModel>();
		
		Log.d("size of array", "" + eventsList.size());
	}

	private void initClickListners() {
		paneButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
				_menuLayout));
	}


	@Override
	public void eventsFetchComplete(ArrayList<DocphinCalenderModel> eventsList) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "sucess", Toast.LENGTH_SHORT).show();
		DocphinCalenderAdapter eventAdapter = new DocphinCalenderAdapter(this);
		Log.d("list size before", ""+eventsList.size());
		for(int j=0;j < eventsList.size(); j++){
			DocphinCalenderModel datatest = eventsList.get(j);
			Log.d("       ", "       ");
			Log.d("Title- "+j, ""+datatest.getTitle());
			Log.d("EventDateTime- "+j, ""+datatest.getEventDateTime());
			Log.d("msgID- "+j, ""+datatest.getMessageID());
			Log.d("Date- "+j, ""+datatest.getEventDate());
			Log.d("Time- "+j, ""+datatest.getEventTime());
		}
		for (int i = 0; i < eventsList.size(); i++) {
			DocphinCalenderModel data = eventsList.get(i);
			if (currentdate != data.getEventDate()) {
				currentdate = data.getEventDate();
				eventAdapter.addDateItem(data);

			}
			eventAdapter.addItem(data);
		}
		eventListView.setAdapter(eventAdapter);

	}

	@Override
	public void eventsFetchFailed() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
	}

}