package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGGetPaidAtResponseBean;
import com.ingogo.android.webservices.beans.response.IGProcessPaymentForUnknownPassengerResponseBean;

public interface IGGetPaidAtApiListener {
	public void retrieveSuburbCompleted(IGGetPaidAtResponseBean suburbDetails);
    public void retrieveSuburbFailed(String errorMessage);

}
