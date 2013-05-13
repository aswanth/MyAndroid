package com.ingogo.android.utilities;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.ingogo.android.poll.IGUpdatePositionPollingTask;

public class IGLocationListener implements LocationListener{

	static Location currentLocation;
	public static float presentGPSAccuracy;
	public static boolean _initialFixObtained = false;
	public static double _latitude = 0.0;
	public static double _longitude = 0.0;
	
	/**
	 * To check whether initial location obtained or not.
	 * **/
	public static boolean isInitialFixObtained() {
		if ((_latitude == 0.0) || (_longitude == 0.0)) 
			return false;
		else
			return true;
	}
	
	

	/**
	 * Set to true when initial location obtained.Otherwise set to false.
	 * **/
	public static void clearGPSData() {
//		_latitude = 8.48596000;
//		_longitude = 76.94823;
		Log.e("POLLING TASK","Clear Gps data");
		_latitude = 0.0;
		_longitude = 0.0;
	}
	
	public static double getCurrentLatitude() {
//		return 8.52966594696;
		//return -33.852;
		return _latitude;
	}

	public static double getCurrentLongitude() {
//		return 76.9384689331;
	//	return 151.23;
		return _longitude;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		
		// Called when a new location is found by the network location
		// provider.

		if(!isInitialFixObtained()) {
			_latitude = location.getLatitude();
			_longitude = location.getLongitude();
			Log.e("POLLING TASK","initial fix obtained is called");
			IGUpdatePositionPollingTask.initialFixObtained();
		}
	
		_latitude = location.getLatitude();
		_longitude = location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String provider) {
		_initialFixObtained = false;
		Log.i("PROVIDER", "DISABLED:"+provider);
		clearGPSData();
	
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("PROVIDER", "ENABLED:"+provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i("EXTRAS", ""+extras);
		Log.i("Provider status", ""+status);
		
	}	
}

