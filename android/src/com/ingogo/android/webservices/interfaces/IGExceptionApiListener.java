package com.ingogo.android.webservices.interfaces;

import java.util.Map;

public interface IGExceptionApiListener {

	public void onNetWorkUnavailableResponse(Map<String,Object> errorResponse);
	public void onRequestTimedoutResponse(Map<String,Object> errorResponse);
	public void onInternalServerErrorResponse(Map<String,Object> errorResponse);
	public void onAuthenticationErrorResponse(Map<String,Object> errorResponse);
	public void onNullResponseRecieved();
	
}
