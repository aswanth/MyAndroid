package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.model.IGPaymentDetailModel;

public interface IGPaymentDetailApiListener {
	
	public void paymentDetailFetchingCompleted(IGPaymentDetailModel paymentDetail);
    public void paymentDetailFetchingFailed(String errorMessage);
    

}
