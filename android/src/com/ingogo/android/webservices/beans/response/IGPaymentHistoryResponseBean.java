package com.ingogo.android.webservices.beans.response;

import java.util.ArrayList;

public class IGPaymentHistoryResponseBean extends IGBaseResponseBean {

	private ArrayList<IGPaymentSummaryBean> paymentSummaries;
	private Double pendingAmount;
	private String currentCommssionLevel;
	private String currentCommission;

	public Double getPendingAmount() {
		return pendingAmount;
	}

	public void setPendingAmount(Double pendingAmount) {
		this.pendingAmount = pendingAmount;
	}

	public ArrayList<IGPaymentSummaryBean> getPaymentSummaryList() {
		return paymentSummaries;
	}

	public void setPaymentSummaryList(
			ArrayList<IGPaymentSummaryBean> paymentSummaryList) {
		this.paymentSummaries = paymentSummaryList;
	}

	public String getCurrentCommssionLevel() {
		return currentCommssionLevel;
	}

	public void setCurrentCommssionLevel(String currentCommssionLevel) {
		this.currentCommssionLevel = currentCommssionLevel;
	}

	public String getCurrentCommission() {
		return currentCommission;
	}

	public void setCurrentCommission(String currentCommission) {
		this.currentCommission = currentCommission;
	}
}
