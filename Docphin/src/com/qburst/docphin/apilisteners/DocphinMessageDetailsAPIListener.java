package com.qburst.docphin.apilisteners;

import java.util.ArrayList;

import com.qburst.docphin.datamodels.DocphinMessageDetails;

public interface DocphinMessageDetailsAPIListener {
	public void messageDetailsFetchComplete(
			ArrayList<DocphinMessageDetails> messageDetails);
	public void messagesDetailsFetchFailed();
}
