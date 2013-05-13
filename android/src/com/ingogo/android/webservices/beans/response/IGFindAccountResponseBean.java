package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.model.IGPassengerInformationModel;

public class IGFindAccountResponseBean extends IGBaseResponseBean{
	
	private boolean matchFound;
	IGPassengerInformationModel passengerInformation;
	
	public boolean isMatchFound() {
		return matchFound;
	}
	public void setMatchFound(boolean matchFound) {
		this.matchFound = matchFound;
	}
	public IGPassengerInformationModel getPassengerInformation() {
		return passengerInformation;
	}
	public void setPassengerInformation(
			IGPassengerInformationModel passengerInformation) {
		this.passengerInformation = passengerInformation;
	}
	
	

}
