package com.qburst.docphin.apilisteners;

import java.util.ArrayList;

import com.qburst.docphin.datamodels.DocphinMessageModel;

public interface DocphinMyMessageAPIListener {
	public void myMessagesFetchComplete(ArrayList<DocphinMessageModel> messageList);
	public void myMessagesFetchFailed();
}
