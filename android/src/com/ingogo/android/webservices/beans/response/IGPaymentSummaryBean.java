package com.ingogo.android.webservices.beans.response;


public class IGPaymentSummaryBean extends IGBaseResponseBean {
	private String paymentId;
	private String status;
	private String settled;
	private String amount;
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDate() {
		return settled;
	}
	public void setDate(String settled) {
		this.settled = settled;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
}
