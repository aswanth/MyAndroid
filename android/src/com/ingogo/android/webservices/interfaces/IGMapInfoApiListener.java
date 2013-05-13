package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGMapInfoResponseBean;

public interface IGMapInfoApiListener {
	
	public void mapInfoCompleted(IGMapInfoResponseBean mapInfo);
	public void mapInfoFailed(String errorMessage);
}
