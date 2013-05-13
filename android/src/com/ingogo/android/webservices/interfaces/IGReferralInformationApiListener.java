package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGReferralInformationResponseBean;


public interface IGReferralInformationApiListener {
	public void retrieveReferralInformationCompleted(IGReferralInformationResponseBean response);
	public void retrieveReferralInformationFailed(String errorMessage);

}
