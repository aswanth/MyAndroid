package com.ingogo.android.webservices;

import java.util.ArrayList;
import java.util.Map;

import android.util.Log;

import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.model.IGPaymentDailySummaryModel;
import com.ingogo.android.webservices.beans.request.IGPaymentDailyHistoryRequestBean;
import com.ingogo.android.webservices.beans.response.IGMapInfoResponseBean;
import com.ingogo.android.webservices.beans.response.IGPaymentDailyHistoryResponseBean;
import com.ingogo.android.webservices.beans.response.IGPaymentDailySummaryBean;
import com.ingogo.android.webservices.interfaces.IGApiInterface;
import com.ingogo.android.webservices.interfaces.IGApiListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGPaymentDailyHistoryApiListener;

public class IGPaymentDailyHistoryApi extends IGBaseApi implements
		IGApiInterface, IGApiListener {
	IGPaymentDailyHistoryApiListener _listener = null;

	public IGPaymentDailyHistoryApi(
			IGPaymentDailyHistoryApiListener apiListener,
			IGExceptionApiListener excptnListener) {
		this._listener = apiListener;
		this._excptnListener = excptnListener;
	}

	public void getPaymentDailyHistorySummaries(String username,
			String password, long paymentId, String paymentStatus) { // GET req.

		IGPaymentDailyHistoryRequestBean requestBean = new IGPaymentDailyHistoryRequestBean(
				username, password, paymentId);
		IGBaseWebserviceThreadPool.getSharedInstance().addWerbserviceTask(
				buildURL(paymentStatus), IGPaymentDailyHistoryResponseBean.class,
				requestBean.toJsonString(), this);
		
	}

	@Override
	public void onResponseReceived(Map<String, Object> response) {
		Log.i("IG-FETCH-PAYMENT-DAILY-HISTORY-RESPONSE", response.toString());
		IGPaymentDailyHistoryResponseBean respBean;

		if (response.containsKey(IGApiConstants.kSuccessMsgKey)) {
			respBean = (IGPaymentDailyHistoryResponseBean) response
					.get(IGApiConstants.kSuccessMsgKey);
			IngogoApp.getSharedApplication().setDriverName(
					respBean.getDriverName());
			IngogoApp.getSharedApplication().setPlateNumber(
					respBean.getPlateNo());

			ArrayList<IGPaymentDailySummaryModel> paymentDailySummaries = this
					.processResponseBean(respBean);
			if (paymentDailySummaries != null) {
				_listener.paymentDailyHistoryFetchingCompleted(
						paymentDailySummaries, respBean.getPaidToAccount());
			}
		} else if (response.containsKey(IGApiConstants.kApiFailedMsgKey)) {

			respBean = (IGPaymentDailyHistoryResponseBean) response
					.get(IGApiConstants.kApiFailedMsgKey);
			if (respBean != null) {
				_listener.paymentDailyHistoryFetchingFailed(respBean
						.getResponseMessages().errorMessagesToString());
			}

		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse) {
		IGPaymentDailyHistoryResponseBean respBean;
		respBean = (IGPaymentDailyHistoryResponseBean) errorResponse
				.get(IGApiConstants.kApiFailedMsgKey);
		if (respBean != null) {
			_listener.paymentDailyHistoryFetchingFailed(respBean
					.getResponseMessages().errorMessagesToString());
		} else {
			this._excptnListener.onNullResponseRecieved();
		}

	}

	@Override
	public String buildURL(String paymentStatus) {
		String apiUrl = null;
		if (paymentStatus.trim().equalsIgnoreCase(
				IGApiConstants.kPaymentStatus.trim())) {
			apiUrl = IGApiConstants.kIngogoBaseURL
					+ IGApiConstants.kPaymentSettledDailySummaryApiUrl;
		} else {
			apiUrl = IGApiConstants.kIngogoBaseURL
					+ IGApiConstants.kPaymentPendingDailySummaryApiUrl;
		}
		return apiUrl;
	}

	private ArrayList<IGPaymentDailySummaryModel> processResponseBean(
			IGPaymentDailyHistoryResponseBean respBean) {
		ArrayList<IGPaymentDailySummaryModel> paymentArray = null;
		if (respBean.getPaymentDailySummaryList() != null) {
			paymentArray = new ArrayList<IGPaymentDailySummaryModel>();
			for (IGPaymentDailySummaryBean payBean : respBean
					.getPaymentDailySummaryList()) {

				IGPaymentDailySummaryModel paymentDailySummary = new IGPaymentDailySummaryModel();
				paymentDailySummary.setBookingId(String.valueOf(payBean
						.getBookingId()));
				paymentDailySummary.setWhen(String.valueOf(payBean.getWhen()));
				paymentDailySummary.setMeterAmount(String.valueOf(payBean
						.getMeterAmount()));
				paymentDailySummary.setSettlingAmount(String.valueOf(payBean
						.getSettlingAmount()));

				paymentArray.add(paymentDailySummary);
			}
		}

		return paymentArray;

	}

	@Override
	public String buildURL() {
		// TODO Auto-generated method stub
		return null;
	}

}
