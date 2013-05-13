package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGBookingModel;

public class IGCollectJobResponseBean extends IGBaseResponseBean {

	private IGBookingModel booking;

	public IGBookingModel getBookingSumary() {
		return booking;
	}

	public void setBookingSumary(IGBookingModel booking) {
		this.booking = booking;
	}
	
}
