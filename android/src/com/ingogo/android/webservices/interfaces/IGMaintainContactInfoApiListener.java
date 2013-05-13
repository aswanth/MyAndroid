package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.model.IGContactInfoModel;

public interface IGMaintainContactInfoApiListener {

	public void maintainContactInfoCompleted(IGContactInfoModel contactInfo);
	public void maintainContactInfoFailed(String errorMessage);
}
