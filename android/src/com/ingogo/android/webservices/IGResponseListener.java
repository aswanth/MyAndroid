/*
 * Package Name : com.ingogo.android.webservices
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Basic web service callback used by activities / polling agents to get the response of the service.
 */

package com.ingogo.android.webservices;

import java.util.Map;

public interface IGResponseListener {

	public void onResponseReceived(Map<String, Object> response, int apiID);
	public void onFailedToGetResponse(Map<String, Object> errorResponse, int apiID);
}
