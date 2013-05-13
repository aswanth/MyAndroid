package com.ingogo.android.webservices;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;

public class IGUpdateAccountApi {
	private IGResponseListener _responseListener;
	private String _accountName, _accountNumber, _bsb, _companyName, _abn;
	private long _bankAccountId;

	public IGUpdateAccountApi(IGResponseListener callingActivity,
			String companyName, String abn, String accountName,
			String accountNumber, String bsb, long bankAccountId) {
		this._responseListener = callingActivity;
		this._companyName = companyName;
		this._abn = abn;
		this._accountName = accountName;
		this._accountNumber = accountNumber;
		this._bsb = bsb;
		this._bankAccountId = bankAccountId;
	}

	public void updateAccountInfo() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kUpdateAccountApiURL;
		IGBaseWebService webservice = new IGBaseWebService(buildJSONRequest(),
				new Handler(), new IGCallbackWrapper(_responseListener),
				apiUrl, IGApiConstants.kUpdateAccountWebServiceId);
		webservice.setAuthorizationHeader(IngogoApp.getSharedApplication()
				.getUserId()
				+ ":"
				+ IngogoApp.getSharedApplication().getAccessToken());

		webservice.start();

	}

	private String buildJSONRequest() {
		JSONObject jObject = new JSONObject();
		try {
			JSONObject accountInfo = new JSONObject();
			accountInfo.put(IGApiConstants.kJSONCompanyNameKey, _companyName);
			accountInfo.put(IGApiConstants.kJSONABNKey, _abn);
			if (_accountName == null) {
				accountInfo.put(IGApiConstants.kJSONAccountNameKey,
						JSONObject.NULL);
			} else {
				accountInfo.put(IGApiConstants.kJSONAccountNameKey,
						_accountName);
			}
			if (_accountNumber == null) {
				accountInfo.put(IGApiConstants.kJSONAccountNumberKey,
						JSONObject.NULL);
			} else {
				accountInfo.put(IGApiConstants.kJSONAccountNumberKey,
						_accountNumber);
			}
			if (_bsb == null) {
				accountInfo.put(IGApiConstants.kJSONBSBKey, JSONObject.NULL);
			} else {
				accountInfo.put(IGApiConstants.kJSONBSBKey, _bsb);
			}
			if(_bankAccountId == 0) {
				accountInfo.put(IGApiConstants.kJSONBankAccountIDKey, JSONObject.NULL);
			} else {
				accountInfo.put(IGApiConstants.kJSONBankAccountIDKey, _bankAccountId);
			}
			jObject.put(IGApiConstants.kJSONAccountInfoKey, accountInfo);
			jObject.put(IGApiConstants.kJSONUsernameKey, IngogoApp
					.getSharedApplication().getUserId());
			jObject.put(IGApiConstants.kJSONPasswordKey, IngogoApp
					.getSharedApplication().getPassword());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("UPDATE ACCOUNT JOBJ", jObject.toString());
		return jObject.toString();

	}
}
