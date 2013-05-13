/*
 * Package Name : com.ingogo.android.poll
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : Listener class for Incoming available jobs used by IGJobsActivity.
 */

package com.ingogo.android.poll;

import java.util.ArrayList;

import com.ingogo.android.model.IGJob;

public interface IGAvailableJobsListener {

	public void availableJobsUpdated(ArrayList<IGJob> jobList);
	public void availableJobsUpdateFailed(String errorMessage);
	public void getDriverStatus(boolean status);
	public void getPreviousJobMessage(String message);
}
