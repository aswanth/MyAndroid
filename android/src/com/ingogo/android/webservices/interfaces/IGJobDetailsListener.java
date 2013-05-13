package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.model.IGBookingModel;

public interface IGJobDetailsListener {

	public void jobDetailsFetchingCompleted(IGBookingModel bookingModel);

	public void jobDetailsFetchingFailed(String errorMessage);

}
