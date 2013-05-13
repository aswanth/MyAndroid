package com.ingogo.android.model;

import java.io.Serializable;

public class IGPaymentSummaryModel implements Serializable {
	
	private String _amount;
	private String _status;
	private String _settled;
	private String _paymentId;
	
	public IGPaymentSummaryModel(){
		_amount = "";
		_status = "";
		_settled= "";
		_paymentId = "";
	}

	public String getAmount() {
		return _amount;
	}

	public void setAmount(String amount) {
		this._amount = amount;
	}

	public String getStatus() {
		return _status;
	}

	public void setStatus(String status) {
		this._status = status;
	}

	public String getSettled() {
		return _settled;
	}

	public void setSettled(String settled) {
		this._settled = settled;
	}

	public String getPaymentId() {
		return _paymentId;
	}

	public void setPaymentId(String paymentId) {
		this._paymentId = paymentId;
	}
	
	

}
