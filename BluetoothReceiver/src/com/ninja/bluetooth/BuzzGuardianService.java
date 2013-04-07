package com.ninja.bluetooth;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class BuzzGuardianService extends Service {

	// Debugging
	private static final String TAG = "BuzzGuardianService";
	private static final boolean D = true;

	// Reference to LocationManager
	private LocationManager mLocationManager = null;

	// Reference to BluetoothConnection
	private BluetoothConnection btConnection = null;
	

	// location co-ordinates of mobile device
	private double latitude;
	private double longitude;

	private void setupLocationDetection() {
		mLocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
	
		LocationListener mlocListener = new MyLocationListener();
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, mlocListener);
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
	}

	/* Location Listener */
	public class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {

			latitude = loc.getLatitude();
			longitude = loc.getLongitude();
			//sendSMS(latitude, longitude);
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

	public void sendSMS(double latitude, double longitude) {
		String text = Constants.ALERT_TEXT + "latitude = " + latitude
				+ "\nlongitude= " + longitude;
		SMSHelper.sendSMS(text);
	}

	public BuzzGuardianService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		setupLocationDetection();
		btConnection = BluetoothConnection.getBluetoothConnection(this, mHandler);
		Log.d(TAG, "BuzzGuardianService Log created");
	}

	// The Handler that gets information back from the BluetoothConnection
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "BuzzGuardianService : " + msg.toString());
			sendSMS(latitude, longitude);
		}
	};

	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "BuzzGuardianService starting", Toast.LENGTH_SHORT)
				.show();

		Log.d(TAG, "BuzzGuardianService Log starting");
		btConnection.start();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// Return null as we are not binding the service to anything
		return null;
	}

}
