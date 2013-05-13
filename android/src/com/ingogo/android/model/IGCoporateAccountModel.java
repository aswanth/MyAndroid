package com.ingogo.android.model;

import java.io.Serializable;

public class IGCoporateAccountModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7404214079091185472L;
	private String accountIdentifier;
	private String accountNickname;
	private double balance;
	private boolean isEnabled = true;
	private double accountTypePercentage;
	
	public String getAccountIdentifier() {
		return accountIdentifier;
	}
	public void setAccountIdentifier(String accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}
	public String getAccountNickname() {
		return accountNickname;
	}
	public void setAccountNickname(String accountNickname) {
		this.accountNickname = accountNickname;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public double getAccountTypePercentage() {
		return accountTypePercentage;
	}
	public void setAccountTypePercentage(double accountTypePercentage) {
		this.accountTypePercentage = accountTypePercentage;
	}
	

}
