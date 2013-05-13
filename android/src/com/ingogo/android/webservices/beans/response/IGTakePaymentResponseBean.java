package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGBookingModel;

public class IGTakePaymentResponseBean extends IGBaseResponseBean{
	private boolean canTakePayment;
	private IGBookingModel bookingSummary;

	public void setCanTakePayment(boolean canTakePayment) {
		this.canTakePayment = canTakePayment;
	}

	public boolean getCanTakePayment() {
		return canTakePayment;
	}

	public IGBookingModel getBookingSummary() {
		return bookingSummary;
	}

	public void setBookingSummary(IGBookingModel bookingSummary) {
		this.bookingSummary = bookingSummary;
	}

}
