package com.ingogo.android.webservices.interfaces;

import java.util.ArrayList;

import com.ingogo.android.model.IGLoadAndGoAccountDetailsModel;

public interface IGViewLoadAndGoAccountsListener {
	
	public void successToFetchLoadAndGoAccounts( ArrayList<IGLoadAndGoAccountDetailsModel> accounts , String  balancesAreAsAt );
	public void failedToFetchLoadAndGoAccounts ( String errorMessage );

}
