package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGBookingModel;
import com.ingogo.android.model.IGCreditCardInformation;
import com.ingogo.android.model.IGReceiptInformationModel;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.webservices.beans.request.IGProcessPaymentRequestBean;
import com.ingogo.android.webservices.beans.response.IGPrintReceiptResponseBean;
import com.ingogo.android.webservices.beans.response.IGProcessPaymentResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGProcessPaymentApiListener;

public class IGProcessPaymentApi extends IGBaseApi implements IGApiInterface,
IGApiListener {
	private IGProcessPaymentApiListener _listener;
	private long _localityId;
	
	public IGProcessPaymentApi(IGProcessPaymentApiListener apiListener, IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
		_localityId = IngogoApp.getSharedApplication().getLocalityId();
	} 
	
	public void processPayment(String bookId, String fare, String totalFare, IGCreditCardInformation cardInfo, String suburbName) {
		String mobileNumber =IngogoApp.getSharedApplication()
								.getUserId();
		String password = IngogoApp.getSharedApplication()
								.getPassword();
		String lat = String.valueOf(IGLocationListener.getCurrentLatitude());
		String lon = String.valueOf(IGLocationListener.getCurrentLongitude());
		
		IGProcessPaymentRequestBean requestBean = new IGProcessPaymentRequestBean(mobileNumber, password, bookId,lat, lon, fare, totalFare, cardInfo, "", _localityId, suburbName);
		
		Log.e("Process payment = ",requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGProcessPaymentResponseBean.class,
				requestBean.toJsonString(), this);

		
	}
	
	public void processPayment(String bookId, String fare, String totalFare, IGCreditCardInformation cardInfo,  String cardDetails, String suburbName) {
		String mobileNumber =IngogoApp.getSharedApplication()
								.getUserId();
		String password = IngogoApp.getSharedApplication()
								.getPassword();
		String lat = String.valueOf(IGLocationListener.getCurrentLatitude());
		String lon = String.valueOf(IGLocationListener.getCurrentLongitude());
		
		IGProcessPaymentRequestBean requestBean = new IGProcessPaymentRequestBean(mobileNumber, password, bookId,lat, lon, fare, totalFare, cardInfo, cardDetails, _localityId, suburbName);
		
		Log.e("Process payment = ",requestBean.toJsonString());
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGProcessPaymentResponseBean.class,
				requestBean.toJsonString(), this);

		
	}

	
	@Override
	public void onResponseReceived(Map<String, Object> response) {
		// TODO Auto-generated method stub
		Log.i("PROCESS PAYMENT SUCCESSFUL RESPONSE", response.toString());
		IGProcessPaymentResponseBean respBean;
		IGBookingModel bookingModel = null;
		IGReceiptInformationModel receiptInfoModel = null;
		
		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGProcessPaymentResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			try {
				bookingModel = respBean.getBookingSumary();
			} catch (NullPointerException e) {
				bookingModel = null;
			}
			try {
				receiptInfoModel = respBean.getReceiptInformation();
			} catch (NullPointerException e) {
				receiptInfoModel = null;
			}
			
			_listener.processPaymentCompleted(bookingModel, respBean.getReceiptInformationPageText(), receiptInfoModel);

		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {
			respBean = (IGProcessPaymentResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.processPaymentFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		Log.i("PROCESS PAYMENT FAILURE RESPONSE", errorResponse.toString());
		IGProcessPaymentResponseBean respBean;
		respBean = (IGProcessPaymentResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.processPaymentFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
		
		
	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		String apiUrl = IGApiConstants.kIngogoBaseURL + IGApiConstants.kProcessPaymentUrl;
		return apiUrl;	
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
