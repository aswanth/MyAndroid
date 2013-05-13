package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.model.IGContactInfoModel;

public interface IGRetreiveContactInfoApiListener {
	
	public void retreiveContactInfoCompleted(IGContactInfoModel contactInfo);
	public void retreiveContactInfoFailed(String errorMessage);
}
