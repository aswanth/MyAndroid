package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

public class IGPaymentDailyHistoryResponseBean extends IGBaseResponseBean {

	private ArrayList<IGPaymentDailySummaryBean> paymentSummaries;
	private String driverName;
	private String plateNo;
	private String paidToAccount;

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getPlateNo() {
		return plateNo;
	}

	public void setPlateNo(String plateNo) {
		this.plateNo = plateNo;
	}

	public ArrayList<IGPaymentDailySummaryBean> getPaymentDailySummaryList() {
		return paymentSummaries;
	}

	public void setPaymentDailySummaryList(
			ArrayList<IGPaymentDailySummaryBean> paymentDailySummaryList) {
		this.paymentSummaries = paymentDailySummaryList;
	}

	public String getPaidToAccount() {
		return paidToAccount;
	}

	public void setPaidToAccount(String paidToAccount) {
		this.paidToAccount = paidToAccount;
	}
}
