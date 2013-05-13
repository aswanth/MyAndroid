package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGTargetProgressResponseBean;

public interface IGTargetProgressApiListener {
	
	void targetProgressFetchingCompleted(IGTargetProgressResponseBean response);
	void targetProgressFetchingFailed(String errorMessge);

}
