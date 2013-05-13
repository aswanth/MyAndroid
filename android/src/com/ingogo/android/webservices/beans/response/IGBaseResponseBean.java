/**
 * 
 */
package com.ingogo.android.webservices.beans.response;

import com.ingogo.android.webservices.beans.IGBaseBean;

/**
 * @author midhun
 * 
 */

public class IGBaseResponseBean extends IGBaseBean {

	private String responseCode;
	private IGResponseMessagesBean responseMessages;

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public IGResponseMessagesBean getResponseMessages() {
		return responseMessages;
	}

	public void setResponseMessages(IGResponseMessagesBean responseMessages) {
		this.responseMessages = responseMessages;
	}

}
