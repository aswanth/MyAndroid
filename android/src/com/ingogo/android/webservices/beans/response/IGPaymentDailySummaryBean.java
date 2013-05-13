package com.ingogo.android.webservices.beans.response;

public class IGPaymentDailySummaryBean extends IGBaseResponseBean {
	private String bookingId;
	private String when;
	private String meterAmount;
	private String settlingAmount;
	
	public String getBookingId() {
		return bookingId;
	}
	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}
	public String getWhen() {
		return when;
	}
	public void setWhen(String when) {
		this.when = when;
	}
	public String getMeterAmount() {
		return meterAmount;
	}
	public void setMeterAmount(String meterAmount) {
		this.meterAmount = meterAmount;
	}
	public String getSettlingAmount() {
		return settlingAmount;
	}
	public void setSettlingAmount(String settlingAmount) {
		this.settlingAmount = settlingAmount;
	}

}
