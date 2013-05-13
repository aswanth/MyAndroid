package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGBookingModel;

public class IGJobDetailsResponseBean extends IGBaseResponseBean {

	private IGBookingModel bookingSumary;

	public IGBookingModel getBookingSumary() {
		return bookingSumary;
	}

	public void setBookingSumary(IGBookingModel bookingSumary) {
		this.bookingSumary = bookingSumary;
	}

}
