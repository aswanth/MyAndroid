package com.ingogo.android.test;

import android.util.Log;
import junit.framework.Assert;

public class IngogoLoginTestSuit extends IngogoBaseTestSuit {

	
	public void testCase1() throws Exception{
		
		solo.waitForActivity("IGSignupActivity");
		solo.enterText(0, "ingogo");
		solo.enterText(1, "ingogo");
		solo.enterText(2, "ingogo");
		solo.clickOnImageButton(0);
		Assert.assertTrue(true);

	}
		
	public void testCase2() throws Exception{
		solo.waitForActivity("IGSignupActivity");
		solo.enterText(0, "8112345641");
		solo.enterText(1, "1334");
		solo.enterText(2, "T112");
		solo.clickOnImageButton(0);
		solo.waitForDialogToClose(1000);
		Assert.assertTrue(solo.searchText("Jobs"));
		
	}
	
	public void testCase3() throws Exception{
		
//		testCase2();
//		solo.clickOnMenuItem("TRIP HISTORY SUMMARY");
//		solo.waitForActivity("IGPaymentHistorySummaryActivity");
//		Assert.assertTrue(solo.searchText("Payment History"));
//		solo.goBack();
//		
//		solo.clickOnMenuItem("PRINT REFERRAL");
//		solo.waitForActivity("IGPrintReferralActivity");
//		Assert.assertTrue(solo.searchText("Refer a friend"));
//		solo.goBack();
//		
//		solo.clickOnMenuItem("CASH RECEIPT");
//		solo.waitForActivity("IGCashReceiptActivity");
//		Assert.assertTrue(solo.searchText("Cash Receipt"));
//		solo.goBack();
//		solo.goBack();
		
		solo.waitForActivity("IGSignupActivity");
		solo.enterText(0, "8112345641");
		solo.enterText(1, "1234");
		solo.enterText(2, "T111");
		solo.clickOnImageButton(0);
		solo.waitForActivity("IGJobsActivity");
		Assert.assertTrue(solo.searchText("Password"));
		
		

	}
	
	public void testCase4() throws Exception{
		testCase3();
		solo.clickOnMenuItem("LOG OUT");
		Assert.assertTrue(solo.searchText("Password"));

	}
}
