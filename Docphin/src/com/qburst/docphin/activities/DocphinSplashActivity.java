package com.qburst.docphin.activities;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.qburst.docphin.R;
import com.qburst.docphin.app.DocphinApp;
import com.qburst.docphin.app.DocphinConstants;

public class DocphinSplashActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		// checkLoggedinStatus();

		showLoginView();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_splash, menu);
		return true;
	}

	private void showLoginView() {
		Intent mainIntent = new Intent(DocphinSplashActivity.this,
				DocphinLoginActivity.class);
		finish();
		startActivity(mainIntent);
	}

	private void goToMessageActivity() {
		Intent intent = new Intent(DocphinSplashActivity.this,
				DocphinMyMessageActivity.class);
		startActivity(intent);
	}

	private void checkLoggedinStatus() {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		String lastLoginTime = DocphinApp.getSharedApplication()
				.getLastLoginTime();
		if (DocphinApp.getSharedApplication().getLastLoginTime() != null) {
			if ((currentTime - Long.valueOf(lastLoginTime).longValue()) > DocphinConstants.appLoginDuration) {
				showLoginView();
			} else {
				goToMessageActivity();
			}
		} else {
			showLoginView();
		}

	}

}
