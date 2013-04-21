package com.ninja.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

/**
 * Manages location detection for BuzzGuardian
 */
public class LocationHelper {

	private static final String TAG = "LOCATION_HELPER";
	
	private double latitude;
	private double longitude;
	private LocationManager mLocationManager;
	private MyLocationListener myLocationListener;
	private Context context;
	
	private static LocationHelper locationHelper = null;
	
	public static LocationHelper getLocationHelper(Context context){
		if(locationHelper == null){
			return new LocationHelper(context);
		}
		return locationHelper;
	}
	
	private LocationHelper(Context context){
		this.context = context;
	}
	
	/**
	 * setup location detection to determine user location
	 */
	public void setupLocationDetectors() {
		mLocationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		final boolean networkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!(gpsEnabled || networkProviderEnabled)) {
			Log.d(TAG, "Enabling Location Detection");
			enableLocationSettings();
		}
	}
	
	private void enableLocationSettings() {
		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.context.startActivity(settingsIntent);
	}
	
	public void setupLocationDetection(LocationProcessor processor) {
		myLocationListener = new MyLocationListener(processor);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
	}
	
	/**
	 * stop getting updates from LocationListener
	 */
	public void stop(){
		mLocationManager.removeUpdates(myLocationListener);
	}

	/**
	 * Listens for change of location and processes it
	 */
	public class MyLocationListener implements LocationListener {

		private LocationProcessor processor;
		
		public MyLocationListener(LocationProcessor processor){
			this.processor = processor;
		}
		
		

		public void setProcessor(LocationProcessor processor) {
			this.processor = processor;
		}

		@Override
		public void onLocationChanged(Location loc) {
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
			processor.process(latitude, longitude);
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
}
