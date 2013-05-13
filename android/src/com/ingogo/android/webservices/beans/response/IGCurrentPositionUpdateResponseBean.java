package com.ingogo.android.webservices.beans.response;

public class IGCurrentPositionUpdateResponseBean extends IGBaseResponseBean {
	private String driverStatus;
	private int staleTime;
	public String getDriverStatus() {
		return driverStatus;
	}
	public void setDriverStatus(String driverStatus) {
		this.driverStatus = driverStatus;
	}
	public int getStaleTime() {
		return staleTime;
	}
	public void setStaleTime(int staleTime) {
		this.staleTime = staleTime;
	}

}
