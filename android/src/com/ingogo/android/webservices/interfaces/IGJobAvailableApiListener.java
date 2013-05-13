package com.ingogo.android.webservices.interfaces;

import java.util.ArrayList;

import com.ingogo.android.model.IGJobAvailableModel;

public interface IGJobAvailableApiListener {

	public void onJobAvailableResponseRecieved(
			ArrayList<IGJobAvailableModel> jobList, String message,
			String driverStatus);

	public void onJobAvailableResponseFailed();

}
