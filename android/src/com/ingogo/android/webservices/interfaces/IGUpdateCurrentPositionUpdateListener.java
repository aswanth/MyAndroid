package com.ingogo.android.webservices.interfaces;

public interface IGUpdateCurrentPositionUpdateListener {
	public void successfullyUpdatedCurrentPosition( String driverStatus, int staleTime );
	public void failToUpdateCurrentPosition();

}
