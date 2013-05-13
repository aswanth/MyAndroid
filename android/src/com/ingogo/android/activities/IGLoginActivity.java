package com.ingogo.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/*
 * Dummy activity to resolve the shortcut issue with same name as the launcher activity in Iteration-18
 * Changed name of login to signup activity.
 */
public class IGLoginActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Possible work around for market launches. See http://code.google.com/p/android/issues/detail?id=2373
		// for more details. Essentially, the market launches the main activity on top of other activities.
		// we never want this to happen. Instead, we check if we are the root and if not, we finish.
		if (!isTaskRoot()) {
		    final Intent intent = getIntent();
		    final String intentAction = intent.getAction(); 
		    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null && intentAction.equals(Intent.ACTION_MAIN)) {
		        finish();
		        return;       
		    }
		}
		Intent intent = getIntent();

		Bundle extras = intent.getExtras();
		boolean showCrashAlert = false;
		try {
			showCrashAlert = extras.getBoolean("show_crash_alert",false);

		}catch (Exception e) {
			showCrashAlert = false;		
		}
		
		Intent splashIntent = new Intent(IGLoginActivity.this,IGSplashActivity.class);
		splashIntent.putExtra("show_crash_alert", showCrashAlert);
		startActivity(splashIntent);
		finish();
	}

	
}
