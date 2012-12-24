package com.qburst.docphin.apilisteners;

public interface DocphinLoginAPIListener {
	public void userLoggedInSuccessfully(String response);
	public void userLoginFailed(String error);
}
