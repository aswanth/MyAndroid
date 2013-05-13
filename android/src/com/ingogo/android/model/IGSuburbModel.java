package com.ingogo.android.model;

import java.io.Serializable;

public class IGSuburbModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4947432465325803139L;
	private String suburbName;
	private String postcode;
	private String latitude;
	private String longitude;
	
	public String getSuburbName() {
		return suburbName;
	}
	public void setSuburbName(String suburbName) {
		this.suburbName = suburbName;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
}

