package com.ingogo.android.webservices.interfaces;

import com.ingogo.android.webservices.beans.response.IGLoginResponseBean;

public interface IGLoginApiListener {
	
	public void loginSuccessfully ( IGLoginResponseBean loginResponse );
	public void failedToLogin( String erroMessage);

}
