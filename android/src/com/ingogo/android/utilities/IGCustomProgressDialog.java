package com.ingogo.android.utilities;

import android.app.ProgressDialog;
import android.content.Context;

public class IGCustomProgressDialog extends ProgressDialog {

	public IGCustomProgressDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void onBackPressed() {
		return;
	}
}
