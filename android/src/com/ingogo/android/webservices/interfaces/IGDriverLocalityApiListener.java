package com.ingogo.android.webservices.interfaces;


public interface IGDriverLocalityApiListener {

	public void driverLocalityFetchingCompleted(String driverLocality, String maskedLocality, long localityId, String localityName );
    public void driverLocalityFetchingFailed(String errorMessage);
}
