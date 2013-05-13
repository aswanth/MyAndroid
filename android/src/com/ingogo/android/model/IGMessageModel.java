package com.ingogo.android.model;

import java.io.Serializable;

public class IGMessageModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5099108194863257332L;

	private String sent;
    private String content;
    
	public String getSent() {
		return sent;
	}
	public void setSent(String sent) {
		this.sent = sent;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
