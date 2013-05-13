package com.ingogo.android.model;

import java.io.Serializable;

public class IGReceiptInformationModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1341612869607912862L;
	
	private String date;
    private String time;
    private String pickUp;
    private String paidAt;
    private String longitude;
    private String latitude;
    private String taxiPlate;
    private String authorityReference;
    private String driverHeading;
    private boolean displayDriverCompanyDetails;
    private String driverCompanyName;
	private String driverABN;
    private String meterAmount;
    private String baseFee;
    private String serviceFee;
    private String ccSurcharge;
    private String totalPaid;
    private String gstOnTotalPaid;
    private String creditApportionedToFare;
    private String creditApportionedToCredit;
    private String fareNetPaid;
    private String gstOnFareNetPaid;
    private String feesNetPaid;
    private String gstOnFeesNetPaid;
    private String receiptUrl;
    private String paidWith; 

    
	public String getDate() {
		if (date != null)
			return date;

		return "";
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getTime() {
		if (time != null)
			return time;

		return "";
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getPickUp() {
		if (pickUp != null)
			return pickUp;

		return "";
	}
	
	public void setPickUp(String pickUp) {
		this.pickUp = pickUp;
	}
	
	public String getPaidAt() {
		if (paidAt != null)
			return paidAt;

		return "";
	}
	
	public void setPaidAt(String paidAt) {
		this.paidAt = paidAt;
	}
	
	public String getLongitude() {
		if (longitude != null)
			return longitude;

		return "";
	}
	
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public String getLatitude() {
		if (latitude != null)
			return latitude;

		return "";
	}
	
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	public String getTaxiPlate() {
		if (taxiPlate != null)
			return taxiPlate;

		return "";
	}
	
	public void setTaxiPlate(String taxiPlate) {
		this.taxiPlate = taxiPlate;
	}
	
	public String getAuthorityReference() {
		if (authorityReference != null)
			return authorityReference;

		return "";
	}
	
	public void setAuthorityReference(String authorityReference) {
		this.authorityReference = authorityReference;
	}
	
	public String getDriverHeading() {
		if (driverHeading != null)
			return driverHeading;

		return "";
	}
	
	public void setDriverHeading(String driverHeading) {
		this.driverHeading = driverHeading;
	}
    
    public boolean isDisplayDriverCompanyDetails() {
		return displayDriverCompanyDetails;
	}

	public void setDisplayDriverCompanyDetails(boolean displayDriverCompanyDetails) {
		this.displayDriverCompanyDetails = displayDriverCompanyDetails;
	}

	public String getDriverCompanyName() {
		if (driverCompanyName != null)
			return driverCompanyName+"\n";
		
		return "";
	}

	public void setDriverCompanyName(String driverCompanyName) {
		this.driverCompanyName = driverCompanyName;
	}

	public String getDriverABN() {
		if (driverABN != null)
			return driverABN+"\n";
		
		return "";
	}

	public void setDriverABN(String driverABN) {
		this.driverABN = driverABN;
	}
	
	public String getMeterAmount() {
		if (meterAmount != null)
			return meterAmount;

		return "";
	}
	
	public void setMeterAmount(String meterAmount) {
		this.meterAmount = meterAmount;
	}
	
	public String getBaseFee() {
		if (baseFee != null)
			return baseFee;

		return "";
	}
	
	public void setBaseFee(String baseFee) {
		this.baseFee = baseFee;
	}
	
	public String getServiceFee() {
		if (serviceFee != null)
			return serviceFee;

		return "";
	}
	
	public void setServiceFee(String serviceFee) {
		this.serviceFee = serviceFee;
	}
	
	public String getCcSurcharge() {
		if (ccSurcharge != null)
			return ccSurcharge;

		return "";
	}
	
	public void setCcSurcharge(String ccSurcharge) {
		this.ccSurcharge = ccSurcharge;
	}
	
	public String getTotalPaid() {
		if (totalPaid != null)
			return totalPaid;

		return "";
	}
	
	public void setTotalPaid(String totalPaid) {
		this.totalPaid = totalPaid;
	}
	
	public String getGstOnTotalPaid() {
		if (gstOnTotalPaid != null)
			return gstOnTotalPaid;

		return "";
	}
	
	public void setGstOnTotalPaid(String gstOnTotalPaid) {
		this.gstOnTotalPaid = gstOnTotalPaid;
	}
	
	public String getCreditApportionedToFare() {
		if (creditApportionedToFare != null)
			return creditApportionedToFare;

		return "";
	}
	
	public void setCreditApportionedToFare(String creditApportionedToFare) {
		this.creditApportionedToFare = creditApportionedToFare;
	}
	
	public String getCreditApportionedToCredit() {
		if (creditApportionedToCredit != null)
			return creditApportionedToCredit;

		return "";
	}
	
	public void setCreditApportionedToCredit(String creditApportionedToCredit) {
		this.creditApportionedToCredit = creditApportionedToCredit;
	}
	
	public String getFareNetPaid() {
		if (fareNetPaid != null)
			return fareNetPaid;

		return "";
	}
	
	public void setFareNetPaid(String fareNetPaid) {
		this.fareNetPaid = fareNetPaid;
	}
	
	public String getGstOnFareNetPaid() {
		if (gstOnFareNetPaid != null)
			return gstOnFareNetPaid;

		return "";
	}
	
	public void setGstOnFareNetPaid(String gstOnFareNetPaid) {
		this.gstOnFareNetPaid = gstOnFareNetPaid;
	}
	
	public String getFeesNetPaid() {
		if (feesNetPaid != null)
			return feesNetPaid;

		return "";
	}
	
	public void setFeesNetPaid(String feesNetPaid) {
		this.feesNetPaid = feesNetPaid;
	}
	
	public String getGstOnFeesNetPaid() {
		if (gstOnFeesNetPaid != null)
			return gstOnFeesNetPaid;

		return "";
	}
	
	public void setGstOnFeesNetPaid(String gstOnFeesNetPaid) {
		this.gstOnFeesNetPaid = gstOnFeesNetPaid;
	}

	public String getReceiptUrl() {
		return receiptUrl;
	}

	public void setReceiptUrl(String receiptUrl) {
		this.receiptUrl = receiptUrl;
	}

	public String getPaidWith() {
		return paidWith;
	}

	public void setPaidWith(String paidWith) {
		this.paidWith = paidWith;
	}


	
}
