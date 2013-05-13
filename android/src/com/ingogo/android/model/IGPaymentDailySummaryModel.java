package com.ingogo.android.model;

import java.io.Serializable;

public class IGPaymentDailySummaryModel implements Serializable {
	
	private String _bookingId;
	private String _when;
	private String _meterAmount;
	private String _settlingAmount;
	
	public IGPaymentDailySummaryModel(){
		_bookingId = "";
		_when = "";
		_meterAmount= "";
		_settlingAmount = "";
	}

	public String getBookingId() {
		return _bookingId;
	}

	public void setBookingId(String bookingId) {
		this._bookingId = bookingId;
	}

	public String getWhen() {
		return _when;
	}

	public void setWhen(String when) {
		this._when = when;
	}

	public String getMeterAmount() {
		return _meterAmount;
	}

	public void setMeterAmount(String meterAmount) {
		this._meterAmount = meterAmount;
	}

	public String getSettlingAmount() {
		return _settlingAmount;
	}

	public void setSettlingAmount(String settlingAmount) {
		this._settlingAmount = settlingAmount;
	}
}