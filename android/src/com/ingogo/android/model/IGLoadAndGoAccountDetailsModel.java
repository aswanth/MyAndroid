package com.ingogo.android.model;

import java.io.Serializable;

public class IGLoadAndGoAccountDetailsModel implements Serializable {

	
	private static final long serialVersionUID = 4791695175422286839L;
	
	private String accountName;
	private String accountNumber;
	private String accountBalance;
	private String balanceAsAt;
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getAccountBalance() {
		return accountBalance;
	}
	public void setAccountBalance(String accountBalance) {
		this.accountBalance = accountBalance;
	}
	public String getBalanceAsAt() {
		return balanceAsAt;
	}
	public void setBalanceAsAt(String balanceAsAt) {
		this.balanceAsAt = balanceAsAt;
	}

}
