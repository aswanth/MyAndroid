package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGTimeToPickUpResponseBean;

public interface IGTimeToPickUpApiListener {

	public void timeToPickUpApiCompleted(IGTimeToPickUpResponseBean responseObj);
	public void timeToPickUpApiFailed(String message);
}
