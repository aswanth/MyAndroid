package com.ingogo.android.model;

import java.io.Serializable;

public class IGNetworkProfileModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2551423049823883233L;
	private String network;
	private String phoneNumber;

	/**
	 * @return the network
	 */
	public String getNetwork() {
		return network;
	}

	/**
	 * @param network
	 *            the network to set
	 */
	public void setNetwork(String network) {
		this.network = network;
	}

	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @param phoneNumber
	 *            the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
