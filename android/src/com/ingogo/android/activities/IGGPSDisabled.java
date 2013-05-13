/**
 * 
 */
package com.ingogo.android.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ingogo.android.R;
import com.ingogo.android.app.IGConstants;

/**
 * @author dipu
 *
 */
public class IGGPSDisabled extends IGBaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.no_gps);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.LOGOUT,
				IGConstants.ORDER_NONE, getString(R.string.logout_title));
		menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.JOBS,
				IGConstants.ORDER_NONE, getString(R.string.jobs_menu_title));

		menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.HELP,
				IGConstants.ORDER_NONE, getString(R.string.help_title));
		menu.addSubMenu(IGConstants.GROUP_ZERO,
				menuEnumerator.TRIP_HISTORY_SUMMARY, IGConstants.ORDER_NONE,
				getString(R.string.trip_history_summary_title));
		menu.addSubMenu(IGConstants.GROUP_ZERO, menuEnumerator.ACCOUNT_SETTINGS,
				IGConstants.ORDER_NONE, getString(R.string.account_settings_title));

		// Disable the logout, jobs and account info activities on login page.
		menu.findItem(menuEnumerator.LOGOUT).setEnabled(false);
		menu.findItem(menuEnumerator.JOBS).setEnabled(false);
		menu.findItem(menuEnumerator.TRIP_HISTORY_SUMMARY).setEnabled(false);
		menu.findItem(menuEnumerator.ACCOUNT_SETTINGS).setEnabled(false);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case menuEnumerator.LOGOUT:

			break;
		case menuEnumerator.JOBS:

			break;

		case menuEnumerator.HELP:
			goToHelpPage();
			break;
		case menuEnumerator.ACCOUNT_SETTINGS:
			break;
			
		default:
			break;
		}
		return true;
	}
}
