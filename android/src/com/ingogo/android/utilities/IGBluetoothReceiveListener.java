package com.ingogo.android.utilities;

public interface IGBluetoothReceiveListener {
	public void changeOfStatusResult(int state);

	public void connectionFailedResult(String message);

	public void connectionLostResult(String message);

	public void connectedResult(String message);

	public void writeDataResult(byte[] dataByte);

	public void readDataResult(byte[] buffer);
	
	public void pairingCompletedResult();
	
	public void pairingFailedResult();
}
