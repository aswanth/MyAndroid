package com.QLog.qlogtest;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class QLogActivity extends Activity {

	private Button _debugButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qlog);
		QLog.setupLogging(getApplication());
		_debugButton = (Button) findViewById(R.id.debugbutton);
		_debugButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(QLogActivity.this,
						QLogListActivity.class);
				startActivity(intent);

			}
		});

		 for(int i = 0; i < 1000; i++){
		QLog.i("QLogTest", "Testing", true);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.qlog, menu);
		return true;
	}

}
