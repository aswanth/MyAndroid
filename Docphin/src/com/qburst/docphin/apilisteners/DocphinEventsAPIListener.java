package com.qburst.docphin.apilisteners;

import java.util.ArrayList;

import com.qburst.docphin.datamodels.DocphinCalenderModel;


public interface DocphinEventsAPIListener {
	public void eventsFetchComplete(ArrayList<DocphinCalenderModel> eventsList);
	public void eventsFetchFailed();
}
