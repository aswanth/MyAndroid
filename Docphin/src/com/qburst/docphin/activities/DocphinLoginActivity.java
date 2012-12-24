package com.qburst.docphin.activities;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.qburst.docphin.R;
import com.qburst.docphin.activities.base.DocphinBaseActivity;
import com.qburst.docphin.api.DocphinLoginAPI;
import com.qburst.docphin.apilisteners.DocphinLoginAPIListener;
import com.qburst.docphin.app.DocphinApp;
import com.qburst.docphin.utilities.DocphinUtilities;

public class DocphinLoginActivity extends DocphinBaseActivity implements
		DocphinLoginAPIListener {
	private Button _login;
	private EditText _usernameField;
	private EditText _passwordField;
	private String username;
	private String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initViews();
	}

	private void initViews() {
		_login = (Button) findViewById(R.id.buttonLogin);
		_usernameField = (EditText) findViewById(R.id.editTextUsername);
		_passwordField = (EditText) findViewById(R.id.editTextPassword);

		_login.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				username = _usernameField.getText().toString();
				password = _passwordField.getText().toString();
				// TODO: Use user inputs for login
				DocphinLoginAPI loginAPI = new DocphinLoginAPI(
						DocphinLoginActivity.this, "jon+qburst@docphin.com",
						"qburstphone1");
				loginAPI.login();
				showProgressDialog(DocphinLoginActivity.this);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	public void userLoggedInSuccessfully(String sessionToken) {
		if (sessionToken != null) {
			DocphinApp.getSharedApplication().saveSessionToken(sessionToken);
			long currentTimeInMilliseconds = Calendar.getInstance()
					.getTimeInMillis();
			DocphinApp.getSharedApplication().saveLastLoginTime(
					"" + currentTimeInMilliseconds);
			dismissProgressDialog();
			Intent intent = new Intent(DocphinLoginActivity.this,
					DocphinMyMessageActivity.class);
			startActivity(intent);
			this.finish();
		}
	}

	public void userLoginFailed(String error) {
		dismissProgressDialog();
		DocphinUtilities.showToast(error, DocphinLoginActivity.this);

	}
}
