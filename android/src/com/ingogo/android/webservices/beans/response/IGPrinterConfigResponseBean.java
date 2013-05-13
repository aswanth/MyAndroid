package com.ingogo.android.webservices.beans.response;

public class IGPrinterConfigResponseBean extends IGBaseResponseBean {

	private String deviceName;
	private String devicePin;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDevicePin() {
		return devicePin;
	}

	public void setDevicePin(String devicePin) {
		this.devicePin = devicePin;
	}

}
