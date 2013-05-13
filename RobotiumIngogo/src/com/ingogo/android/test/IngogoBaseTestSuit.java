package com.ingogo.android.test;

import android.test.ActivityInstrumentationTestCase2;

import com.ingogo.android.activities.IGLoginActivity;
import com.jayway.android.robotium.solo.Solo;

public class IngogoBaseTestSuit extends
		ActivityInstrumentationTestCase2<IGLoginActivity> {

	protected Solo solo;
	public IngogoBaseTestSuit() {
		super(IGLoginActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		solo = new Solo(getInstrumentation(), getActivity());
	}

	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();
		super.tearDown();
	}

}
