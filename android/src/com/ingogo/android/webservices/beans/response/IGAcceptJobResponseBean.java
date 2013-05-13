package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGBookingModel;

public class IGAcceptJobResponseBean extends IGBaseResponseBean {

	private boolean hasChanged;
	private IGBookingModel booking;
	



	public boolean isHasChanged() {
		return hasChanged;
	}

	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

	public IGBookingModel getBooking() {
		return booking;
	}

	public void setBooking(IGBookingModel booking) {
		this.booking = booking;
	}

}
