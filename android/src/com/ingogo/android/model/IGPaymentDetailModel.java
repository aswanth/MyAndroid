package com.ingogo.android.model;

import java.io.Serializable;

public class IGPaymentDetailModel implements Serializable {
	private String _when;
	private String _pickupAddress;
	private String _pickupSuburb;
	private String _dropoffSuburb;
	private String _meterAmount;
	private String _baseFee;
	private String _serviceFee;
	private String _serviceCredit;
	private String _settlingAmount;
	private String _shareOfCreditCardFees;
	private String _pointRevenue;
	private Boolean _paidOffline;
	private String bookingStatus;
	private String passengerPaid;
	private boolean paidByCorporateAccount;

	public String getBookingStatus() {
		return bookingStatus;
	}
	public void setBookingStatus(String bookingStatus) {
		this.bookingStatus = bookingStatus;
	}
	public String getPassengerPaid() {
		return passengerPaid;
	}
	public void setPassengerPaid(String passengerPaid) {
		this.passengerPaid = passengerPaid;
	}
	public IGPaymentDetailModel() {
		_when = "";
		_pickupAddress = "";
		_pickupSuburb = "";
		_dropoffSuburb = "";
		_meterAmount = "";
		_baseFee = "";
		_serviceFee = "";
		_serviceCredit = "";
		_settlingAmount = "";
		_shareOfCreditCardFees = "";
		_pointRevenue = "";
		_paidOffline = false;

	}
	public Boolean getPaidOffline(){
		return this._paidOffline;
	}
	
	public void setPaidOffline(Boolean paidOffline){
		this._paidOffline = paidOffline;
	}
	public String getWhen() {
		return _when;
	}

	public void setWhen(String _when) {
		this._when = _when;
	}

	public String getPickupAddress() {
		return _pickupAddress;
	}

	public void setPickupAddress(String pickupAddress) {
		this._pickupAddress = pickupAddress;
	}

	public String getPickupSuburb() {
		return _pickupSuburb;
	}

	public void setPickupSuburb(String pickupSuburb) {
		this._pickupSuburb = pickupSuburb;
	}

	public String getDropoffSuburb() {
		return _dropoffSuburb;
	}

	public void setDropoffSuburb(String dropoffSuburb) {
		this._dropoffSuburb = dropoffSuburb;
	}

	public String getMeterAmount() {
		return _meterAmount;
	}

	public void setMeterAmount(String meterAmount) {
		this._meterAmount = meterAmount;
	}

	public String getBaseFee() {
		return _baseFee;
	}

	public void setBaseFee(String baseFee) {
		this._baseFee = baseFee;
	}

	public String getServiceFee() {
		return _serviceFee;
	}

	public void setServiceFee(String serviceFee) {
		this._serviceFee = serviceFee;
	}

	public String getServiceCredit() {
		return _serviceCredit;
	}

	public void setServiceCredit(String serviceCredit) {
		this._serviceCredit = serviceCredit;
	}

	public String getSettlingAmount() {
		return _settlingAmount;
	}

	public void setSettlingAmount(String settlingAmount) {
		this._settlingAmount = settlingAmount;
	}

	public String getShareOfCreditCardFees() {
		return _shareOfCreditCardFees;
	}

	public void setShareOfCreditCardFees(String shareOfCreditCardFees) {
		this._shareOfCreditCardFees = shareOfCreditCardFees;
	}

	public String getPointRevenue() {
		return _pointRevenue;
	}

	public void setPointRevenue(String pointRevenue) {
		this._pointRevenue = pointRevenue;
	}
	public boolean isPaidByCorporateAccount() {
		return paidByCorporateAccount;
	}
	public void setPaidByCorporateAccount(boolean paidByCorporateAccount) {
		this.paidByCorporateAccount = paidByCorporateAccount;
	}

}
