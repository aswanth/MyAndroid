package com.qburst.docphin.Webservice;

public interface DocphinWebserviceListener {
	public void WebserviceFinishedWithResponse(String response, int statucode);
    public void WebserviceFailedWithError(String error, int statucode);
    public String getSoapURL();
}
