package com.ingogo.android.webservices.beans.response;

import java.util.HashMap;

public class IGIssueReasonsResponseBean extends IGBaseResponseBean {
	private HashMap< String, String > reasons;

	public HashMap< String, String > getReasons() {
		return reasons;
	}

	public void setReasons(HashMap< String, String > reasons) {
		this.reasons = reasons;
	}
}
