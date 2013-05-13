/*
 * Package Name : com.ingogo.android.activities
 * Author : Ingogo
 * Copyright : Ingogo @ 2010-2011
 * Description : This activity receives intent filters and launches activity associated activities.
 */

package com.ingogo.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.ingogo.android.app.IGConstants;

public class IGIntentLauncherActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		Uri data = getIntent().getData();
		String type = data.getQueryParameter(IGConstants.kLaunchType);
		if (type.contains(IGConstants.kForgotPasswordType)) {

			String uri = data.getPath();
			String passPhrase = data.getQueryParameter(IGConstants.kPassphrase);
			Log.i("Got passphrase as", passPhrase + " from " + uri);

			String userId = data.getQueryParameter(IGConstants.kUserId);

			Intent intent = new Intent(this, IGForgotPasswordActivity.class);
			intent.putExtra(IGConstants.kPassphrase, passPhrase);
			intent.putExtra(IGConstants.kUserId, userId);
			startActivity(intent);
			finish();

		} else {
			finish();
		}

	}

}
