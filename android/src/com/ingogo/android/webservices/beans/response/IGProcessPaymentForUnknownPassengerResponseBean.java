package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGReceiptInformationModel;

public class IGProcessPaymentForUnknownPassengerResponseBean extends IGBaseResponseBean {
	private boolean canTakePayment;                         /* if on the initial swipe a payment 
																	can not be made due to a booking in.
	 														progress (collected), or an existing payment 
															in progress this will be set to false*/
    private IGBookingModel bookingSummary;                  // if canTakePayment is false, this is populated with the in-progress booking details. If there is a failure, this contains the details of the booking structure created as part of this request. The bookingId can be retrieved from this and passed in subsequent retries.
    private String receiptInformationPageText;
    private IGReceiptInformationModel receiptInformation;
	public boolean isCanTakePayment() {
		return canTakePayment;
	}
	public void setCanTakePayment(boolean canTakePayment) {
		this.canTakePayment = canTakePayment;
	}
	public IGBookingModel getBookingSummary() {
		return bookingSummary;
	}
	public void setBookingSummary(IGBookingModel bookingSummary) {
		this.bookingSummary = bookingSummary;
	}
	public String getReceiptInformationPageText() {
		return receiptInformationPageText;
	}
	public void setReceiptInformationPageText(String receiptInformationPageText) {
		this.receiptInformationPageText = receiptInformationPageText;
	}
	public IGReceiptInformationModel getReceiptInformation() {
		return receiptInformation;
	}
	public void setReceiptInformation(IGReceiptInformationModel receiptInformation) {
		this.receiptInformation = receiptInformation;
	}
}
