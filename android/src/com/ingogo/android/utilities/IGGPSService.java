package com.ingogo.android.utilities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

public class IGGPSService extends Service {

	public static LocationManager locationManager;
	public static LocationListener locationListener = new IGLocationListener();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("SERVICE STATUS", "ON CREATE");
		IGLocationListener.clearGPSData();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

	}
	private boolean isGpsAvailable() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
		Log.i("SERVICE STATUS", "ON DESTROY");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Log.i("SERVICE STATUS", "ON START");
	}

}
