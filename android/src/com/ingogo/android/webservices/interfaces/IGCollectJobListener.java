package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGCollectJobResponseBean;

public interface IGCollectJobListener {

	public void collectedJobSuccessfully(IGCollectJobResponseBean response);
	public void collectJobRequestFailed(String errorMessage);
}
