package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.model.IGPassengerInformationModel;

public interface IGFindAccountApiListener {
	public void findAccountCompleted(IGPassengerInformationModel passengerInfo);
    public void findAccountFailed(String errorMessage);
}
