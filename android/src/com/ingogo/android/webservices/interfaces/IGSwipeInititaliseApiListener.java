package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGSwipeInititaliseResponseBean;

public interface IGSwipeInititaliseApiListener {
	public void swipeInitialiseCompleted(IGSwipeInititaliseResponseBean initialiseDetails);

	public void swipeInitialiseFailed(String errorMessage);

}
