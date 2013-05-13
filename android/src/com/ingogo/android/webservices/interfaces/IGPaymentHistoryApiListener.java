package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGPaymentHistoryResponseBean;

/**
 * 
 * @author sharika
 *
 */
public interface IGPaymentHistoryApiListener {

	public void paymentHistoryFetchingCompleted(IGPaymentHistoryResponseBean response);
    public void paymentHistoryFetchingFailed(String errorMessage);
}
