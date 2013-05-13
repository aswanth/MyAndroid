package com.ingogo.android.webservices.beans.request;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.webservices.beans.IGBaseBean;

public class IGDriverLocalityRequestBean extends IGBaseBean {

	private String latitude;
	private String longitude;
	private String device;
	
	public IGDriverLocalityRequestBean(String latitude,String longitude){
		this.latitude = latitude;
		this.longitude = longitude;
		this.device = IGApiConstants.kAndroidDevice;
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
