package com.ingogo.android.webservices.beans.response;

public class IGCompleteOfflineResponseBean extends IGBaseResponseBean {
	private String receiptInformation;
	private String totalPaid;
	public String getReceiptInformation() {
		return receiptInformation;
	}
	public void setReceiptInformation(String receiptInformation) {
		this.receiptInformation = receiptInformation;
	}
	public String getTotalPaid() {
		return totalPaid;
	}
	public void setTotalPaid(String totalPaid) {
		this.totalPaid = totalPaid;
	}

}
