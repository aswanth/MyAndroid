package com.ingogo.android.webservices.beans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class IGBaseBean {
	
	
	public IGBaseBean() {
		
	}
	
	public String toJsonString() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}
	
}
