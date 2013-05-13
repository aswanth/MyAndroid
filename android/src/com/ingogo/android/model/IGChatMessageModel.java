package com.ingogo.android.model;

import java.io.Serializable;
import java.util.Date;

public class IGChatMessageModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8654598077693842018L;
	private Date sent;
    private String content;
    
	public Date getSent() {
		return sent;
	}
	public void setSent(Date sent) {
		this.sent = sent;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
