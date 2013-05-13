package com.ingogo.android.activities;

import android.os.Bundle;
import android.view.Menu;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;

public class IGPaymentBaseActivity extends IGBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		IGUpdatePositionPollingTask.ignoreStaleState = true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		// Disable the logout, jobs and account info activities on login page.
		if (IngogoApp.getSharedApplication().isLoggedIn()) {
			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.LOGOUT,
					IGConstants.ORDER_NONE, getString(R.string.logout_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.TRIP_HISTORY_SUMMARY,
					IGConstants.ORDER_NONE,
					getString(R.string.trip_history_summary_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PRINT_REFFERAL, IGConstants.ORDER_NONE,
					getString(R.string.print_referal_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.CASH_RECEIPT, IGConstants.ORDER_NONE,
					getString(R.string.cash_receipt_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PAYMENT_OPTION, IGConstants.ORDER_NONE,
					getString(R.string.payment_option_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.JOBS,
					IGConstants.ORDER_NONE, getString(R.string.jobs_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.HELP,
					IGConstants.ORDER_NONE, getString(R.string.help_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.PRACTICE_SWIPE, IGConstants.ORDER_NONE,
					getString(R.string.practice_swipe_menu_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.REPRINT_RECEIPT, IGConstants.ORDER_NONE,
					getString(R.string.reprint_receipt_title));
			menu.addSubMenu(IGConstants.GROUP_ZERO,
					menuEnumerator.ACCOUNT_SETTINGS, IGConstants.ORDER_NONE,
					getString(R.string.account_settings_title));
		}
		try {
		menu.findItem(menuEnumerator.PAYMENT_OPTION).setEnabled(false);
		}catch(NullPointerException e) {
			
		}

		return true;
	}

}
