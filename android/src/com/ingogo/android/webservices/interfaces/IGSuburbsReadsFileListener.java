package com.ingogo.android.webservices.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import com.ingogo.android.model.IGSuburbModel;

public interface IGSuburbsReadsFileListener {
	
	public void readSuburbsSuccessfully( HashMap<String, ArrayList<IGSuburbModel>> serializedSuburbs);
	public void failedToReadSuburbs();

}
