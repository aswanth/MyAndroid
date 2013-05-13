package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGCancelJobResponseBean;


public interface IGCancelJobListener {

	public void jobCancelledSuccessfully(IGCancelJobResponseBean response);
	public void jobCancellationFailed(String errorMessage);
}
