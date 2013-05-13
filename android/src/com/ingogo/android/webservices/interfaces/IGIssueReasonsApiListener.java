package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGIssueReasonsResponseBean;

public interface IGIssueReasonsApiListener {
	public void retrieveReasonsCompleted(IGIssueReasonsResponseBean issueDetails);
    public void retrieveReasonsFailed(String errorMessage);

}
