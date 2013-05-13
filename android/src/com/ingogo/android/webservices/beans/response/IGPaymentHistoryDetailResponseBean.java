package com.ingogo.android.webservices.beans.response;

public class IGPaymentHistoryDetailResponseBean extends IGBaseResponseBean {
	
	private String when; 
	private String pickupAddress ;
	private String pickupSuburb; 
	private String dropoffSuburb ;
	private String meterAmount;  
	private String baseFee;  
	private String serviceFee ; 
	private String serviceCredit; 
	private String settlingAmount ;
	private String shareOfCreditCardFees;
	private String pointsRevenue ;
	private String bookingStatus;
	private String passengerPaid;
	private boolean paidByCorporateAccount;
	
	private Boolean offline;
	
	public String getWhen() {
		return when;
	}
	public void setWhen(String when) {
		this.when = when;
	}
	public String getPickupAddress() {
		return pickupAddress;
	}
	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}
	public String getPickupSuburb() {
		return pickupSuburb;
	}
	public void setPickupSuburb(String pickupSuburb) {
		this.pickupSuburb = pickupSuburb;
	}
	public String getDropoffSuburb() {
		return dropoffSuburb;
	}
	public void setDropoffSuburb(String dropoffSuburb) {
		this.dropoffSuburb = dropoffSuburb;
	}
	public String getMeterAmount() {
		return meterAmount;
	}
	public void setMeterAmount(String meterAmount) {
		this.meterAmount = meterAmount;
	}
	public String getBaseFee() {
		return baseFee;
	}
	public void setBaseFee(String baseFee) {
		this.baseFee = baseFee;
	}
	public String getServiceFee() {
		return serviceFee;
	}
	public void setServiceFee(String serviceFee) {
		this.serviceFee = serviceFee;
	}
	public String getServiceCredit() {
		return serviceCredit;
	}
	public void setServiceCredit(String serviceCredit) {
		this.serviceCredit = serviceCredit;
	}
	public String getSettlingAmount() {
		return settlingAmount;
	}
	public void setSettlingAmount(String settlingAmount) {
		this.settlingAmount = settlingAmount;
	}
	public String getShareOfCreditCardFees() {
		return shareOfCreditCardFees;
	}
	public void setShareOfCreditCardFees(String shareOfCreditCardFees) {
		this.shareOfCreditCardFees = shareOfCreditCardFees;
	}
	public String getPointRevenue() {
		return pointsRevenue;
	}
	public void setPointRevenue(String pointRevenue) {
		this.pointsRevenue = pointRevenue;
	}
	public Boolean getOffline() {
		return offline;
	}
	public void setOffline(Boolean offline) {
		this.offline = offline;
	}
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
	public boolean isPaidByCorporateAccount() {
		return paidByCorporateAccount;
	}
	public void setPaidByCorporateAccount(boolean paidByCorporateAccount) {
		this.paidByCorporateAccount = paidByCorporateAccount;
	}
	
	
}
