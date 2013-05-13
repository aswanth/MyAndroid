package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

public class IGGetPaidAtResponseBean extends IGBaseResponseBean {
	private ArrayList< String > suburbs = new ArrayList< String >();
    private float showCompanyDetailsIfValueExceeds;
    private ArrayList< String > localitiesRequiringCompanyDetails = new ArrayList< String >();
    private String driverCompanyName;
    private String driverABN;
    private String taxiNumber;
    
	public ArrayList< String > getSuburbs() {
		return suburbs;
	}
	public void setSuburbs(ArrayList< String > suburbs) {
		this.suburbs = suburbs;
	}
	public float getShowCompanyDetailsIfValueExceeds() {
		return showCompanyDetailsIfValueExceeds;
	}
	public void setShowCompanyDetailsIfValueExceeds(
			float showCompanyDetailsIfValueExceeds) {
		this.showCompanyDetailsIfValueExceeds = showCompanyDetailsIfValueExceeds;
	}
	public ArrayList< String > getLocalitiesRequiringCompanyDetails() {
		return localitiesRequiringCompanyDetails;
	}
	public void setLocalitiesRequiringCompanyDetails(
			ArrayList< String > localitiesRequiringCompanyDetails) {
		this.localitiesRequiringCompanyDetails = localitiesRequiringCompanyDetails;
	}
	public String getDriverCompanyName() {
		return driverCompanyName;
	}
	public void setDriverCompanyName(String driverCompanyName) {
		this.driverCompanyName = driverCompanyName;
	}
	public String getDriverABN() {
		return driverABN;
	}
	public void setDriverABN(String driverABN) {
		this.driverABN = driverABN;
	}
	public String getTaxiNumber() {
		return taxiNumber;
	}
	public void setTaxiNumber(String taxiNumber) {
		this.taxiNumber = taxiNumber;
	}


}
