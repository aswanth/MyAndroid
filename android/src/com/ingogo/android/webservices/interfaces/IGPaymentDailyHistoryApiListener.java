package com.ingogo.android.webservices.interfaces;

import java.util.ArrayList;

import com.ingogo.android.model.IGPaymentDailySummaryModel;

public interface IGPaymentDailyHistoryApiListener {

	public void paymentDailyHistoryFetchingCompleted(
			ArrayList<IGPaymentDailySummaryModel> paymentDailySummaries,
			String paidToAccount);

	public void paymentDailyHistoryFetchingFailed(String errorMessage);
}
