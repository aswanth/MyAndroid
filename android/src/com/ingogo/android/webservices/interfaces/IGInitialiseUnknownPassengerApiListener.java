package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGInitialiseUnknownPassengerResponseBean;

public interface IGInitialiseUnknownPassengerApiListener {
	public void initialiseUnknownPassengerCompleted(IGInitialiseUnknownPassengerResponseBean unknownPassengerDetails);
    public void initialiseUnknownPassengerFailed(String errorMessage);
}
