package com.ingogo.android.webservices.interfaces;

public interface IGPrinterConfigApiListener {

	public void printerConfigCompleted(String deviceName, String devicePin);
	public void printerConfigFailed(String errorMessage);
}
