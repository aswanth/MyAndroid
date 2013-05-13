package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGAcceptJobResponseBean;

public interface IGAcceptJobListener {
public void acceptJobCompleted(IGAcceptJobResponseBean responseObj);
public void acceptJobFailed(String message);
}
