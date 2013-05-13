/*
 * Package Name : com.ingogo.android.poll
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Listener class for updating of driver location.
 */

package com.ingogo.android.poll;

public interface IGUpdatePositionListener {

	public void positionUpdateSuccessfull(double latitude, double longitude);
	public void positionUpdateFailed(String errorMessage);
	public void initialFixObtained(double latitude,double longitude);
}
