package com.ingogo.android.webservices;

import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.model.IGPaymentDetailModel;
import com.ingogo.android.webservices.beans.request.IGPaymentHistoryDetailRequestBean;
import com.ingogo.android.webservices.beans.response.IGMapInfoResponseBean;
import com.ingogo.android.webservices.beans.response.IGPaymentHistoryDetailResponseBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentDetailApiListener;

public class IGPaymentDetailApi extends IGBaseApi implements IGApiInterface,
		IGApiListener {
	private IGPaymentDetailApiListener _listener;

	public IGPaymentDetailApi(IGPaymentDetailApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._excptnListener = excptnListener;
		this._listener = apiListener;
	}

	public void getPaymentDetail(String mobileNumber, String password,
			String bookingID) {
		IGPaymentHistoryDetailRequestBean requestBean = new IGPaymentHistoryDetailRequestBean(
				mobileNumber, password, bookingID);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(), IGPaymentHistoryDetailResponseBean.class,
				requestBean.toJsonString(), this);
		

	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.i("IG-FETCH-PAYMENT-HISTORY-DETAIL-RESPONSE", response.toString());
		IGPaymentHistoryDetailResponseBean respBean;

		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGPaymentHistoryDetailResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			IGPaymentDetailModel paymentDetail = this
					.processResponseBean(respBean);
			if (paymentDetail != null) {
				_listener.paymentDetailFetchingCompleted(paymentDetail);
			}
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGPaymentHistoryDetailResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.paymentDetailFetchingFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}
	}

	private IGPaymentDetailModel processResponseBean(
			IGPaymentHistoryDetailResponseBean respBean) {
		IGPaymentDetailModel paymentDetail = new IGPaymentDetailModel();
		paymentDetail.setBaseFee(respBean.getBaseFee());
		paymentDetail.setDropoffSuburb(respBean.getDropoffSuburb());
		paymentDetail.setMeterAmount(respBean.getMeterAmount());
		paymentDetail.setPickupAddress(respBean.getPickupAddress());
		paymentDetail.setPickupSuburb(respBean.getPickupSuburb());
		paymentDetail.setPointRevenue(respBean.getPointRevenue());
		paymentDetail.setServiceCredit(respBean.getServiceCredit());
		paymentDetail.setServiceFee(respBean.getServiceFee());
		paymentDetail.setSettlingAmount(respBean.getSettlingAmount());
		paymentDetail.setPassengerPaid(respBean.getPassengerPaid());
		paymentDetail.setBookingStatus(respBean.getBookingStatus());
		paymentDetail.setShareOfCreditCardFees(respBean
				.getShareOfCreditCardFees());
		paymentDetail.setWhen(respBean.getWhen());
		paymentDetail.setPaidByCorporateAccount(respBean.isPaidByCorporateAccount());
		return paymentDetail;
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		IGPaymentHistoryDetailResponseBean respBean;
		respBean = (IGPaymentHistoryDetailResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		respBean = (IGPaymentHistoryDetailResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.paymentDetailFetchingFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {

			this._excptnListener.onNullResponseRecieved();
		}
	}

	@Override
	public String buildURL() {
		String apiUrl = IGApiConstants.kIngogoBaseURL
				+ IGApiConstants.kPaymentDetailApiUrl;
		return apiUrl;
	}

	@Override
	public String buildURL(String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
