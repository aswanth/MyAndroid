package com.ingogo.android.webservices.interfaces;

import java.util.ArrayList;

import com.ingogo.android.model.IGJobAvailableModel;
import com.ingogo.android.model.IGReceiptInformationModel;

public interface IGFindCurrentDriverStateApiListener {
	public void findCurrentDriverStateCompleted(
			ArrayList<IGJobAvailableModel> bookingSummaries,
			IGReceiptInformationModel receiptInformation);

	public void findCurrentDriverStateFailed(String errorMessage);
}
