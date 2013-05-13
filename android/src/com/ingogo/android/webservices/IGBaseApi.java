/**
 * 
 * 
 */

package com.ingogo.android.webservices;

import java.util.HashMap;
import java.util.Map;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

/**
 * @author midhun
 * 
 */

public class IGBaseApi {

	private Object _errorMsgObject;
	private Map<String, Object> _networkErrorResponse;
	private Map<String, Object> _networkTimeoutErrorResponse;
	private Map<String, Object> _internalServerErrorResponse;
	private Map<String, Object> _authenticationErrorResponse;
	IGExceptionApiListener _excptnListener;

	/**
	 * This function checks for specific network, internal server error  and network timeout related
	 * errors and handles it by redirecting it to related listeners.
	 * 
	 * @param ResponseObject
	 * @return void
	 * 
	 */

	public void detectErrorMessage(Map<String, Object> response) {

		Log.i("ERROR FOUND", "ENTERED DETECT-ERROR-MESSAGE");

		if (response != null) {

			_networkErrorResponse = new HashMap<String, Object>();
			_networkTimeoutErrorResponse = new HashMap<String, Object>();
			_internalServerErrorResponse = new HashMap<String, Object>();
			_authenticationErrorResponse = new HashMap<String, Object>();

			_errorMsgObject = response.get(IGApiConstants.kErrorMsgKey);
			String networkErrorMessage = IGApiConstants.kNetworkErrorMessage;
			String networkTimeoutErrorMessage = IGApiConstants.kNetworkTimeoutErrorMessage;
			String internalServerErrorMessage = IGApiConstants.kInternalServerErrorMessage;
			String authenticationErrorMessage = IGApiConstants.kAuthenticationErrorMessage;
			_networkErrorResponse.put(IGApiConstants.kApiFailedMsgKey,
					networkErrorMessage);
			_networkTimeoutErrorResponse.put(IGApiConstants.kApiFailedMsgKey,
					networkTimeoutErrorMessage);
			_internalServerErrorResponse.put(IGApiConstants.kApiFailedMsgKey,
					internalServerErrorMessage);	
			_authenticationErrorResponse.put(IGApiConstants.kApiFailedMsgKey,
					authenticationErrorMessage);

			if (_errorMsgObject
					.equals(IGApiConstants.kNetworkErrorExceptionKey)) {
				_excptnListener
						.onNetWorkUnavailableResponse(_networkErrorResponse);

			} else if (_errorMsgObject
					.equals(IGApiConstants.kTimeOutErrorExceptionKey)) {
				_excptnListener
						.onRequestTimedoutResponse(_networkTimeoutErrorResponse);

			}else if(_errorMsgObject
					.equals(IGApiConstants.kInternalServerErrorExceptionKey)){
				
				_excptnListener.onInternalServerErrorResponse(_internalServerErrorResponse);
			}else if(_errorMsgObject.equals(IGApiConstants.kAuthenticationErrorExceptioney)) {
				_excptnListener.onAuthenticationErrorResponse(_authenticationErrorResponse);
			}
		} else {
			_excptnListener.onNullResponseRecieved();
		}
	}



}
