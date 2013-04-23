package com.ninja.bluetooth;

import java.sql.Date;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Background service that listens to Arduino device (Panic Button) and sends appropriate messages to the backend
 * @author natraj
 */
public class BuzzGuardianService extends Service {

	// Debugging
	private static final String TAG = "BUZZ_GUARDIAN_SERVICE";

	// Reference to BluetoothConnection
	private BluetoothConnection btConnection = null;

	private LocationHelper locationHelper = LocationHelper.getLocationHelper(this);
	private boolean isTracking = false; // true if we are tracking the location of mobile device
	private TrackerThread trackerThread; // reference to the Tracker thread

	@Override
	public void onCreate() {
		btConnection = BluetoothConnection.getBluetoothConnection(this,
				mHandler);
		locationHelper.setupLocationDetectors();
		locationHelper.setupLocationDetection(new LocationProcessor() {

			@Override
			public void process(double latitude, double longitude) {
			}
			
		});
		trackerThread = new TrackerThread();
		Log.d(TAG, "BuzzGuardianService Log created");
	}

	// The Handler that gets information back from the BluetoothConnection
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String message = (String)msg.obj;
			Log.d(TAG, "Received message from Arduino : " + message);
			
			/* 
			 * EMERGENCY MESSAGE: Send EMERGENCY SMS and call startTracking()
			 * CANCEL MESSAGE: Send CANCEL SMS and call stopTracking()
			 */
			if(Constants.EMERGENCY_MESSAGE.equals(message)){
				sendSMS(Constants.EMERGENCY_TEXT, locationHelper.getLatitude(), locationHelper.getLongitude());
				startTracking();
			}
			else if(Constants.CANCEL_MESSAGE.equals(message)){
				sendSMS(Constants.CANCEL_TEXT, locationHelper.getLatitude(), locationHelper.getLongitude());
				stopTracking();
			}
			else{
				//In case of error ignore
			}
			
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "BuzzGuardianService starting", Toast.LENGTH_SHORT)
				.show();

		Log.d(TAG, "BuzzGuardianService Log starting");
		btConnection.connectToPanicButton();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// Return null as we are not binding the service to anything
		return null;
	}

	@Override
	/**
	 * stop and teminate LocationHelper processes
	 */
	public void onDestroy() {
		locationHelper.stop();
		btConnection.stop();
		if(trackerThread.isAlive()){
			trackerThread.interrupt();
		}
	}

	public void sendSMS(String msg, double latitude, double longitude) {
		String text = msg + "latitude= " + latitude
				+ "\nlongitude= " + longitude + "\ntimestamp= "
				+ (new Date(System.currentTimeMillis())).toGMTString();
		SMSHelper.sendSMS(text);
	}

	public void setTracking(boolean isTracking) {
		this.isTracking = isTracking;
	}

	/**
	 * start the Tracking Thread which will send location updates every 60
	 * seconds
	 */
	private void startTracking() {
		setTracking(true);
		if (!trackerThread.isAlive() || trackerThread.isInterrupted()) {
			try {
				trackerThread.start();
			} catch (IllegalThreadStateException e) {

			}
		}
	}

	/**
	 * stop the Tracking Thread from sending location updates
	 */
	private void stopTracking() {
		setTracking(false);
		trackerThread.interrupt();
	}

	/**
	 * Sends periodic tracking location updates to the server until interrupted
	 */
	public class TrackerThread extends Thread {

		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					while (true) {
						Thread.sleep(Constants.TRACKING_PERIODICITY);
						sendTrackingSMS();
					}
				}
			} catch (InterruptedException e) {
				// if a cancel request is received
				setTracking(false);
				Thread.currentThread().interrupt();
			}
		}

		private void sendTrackingSMS() {
			sendSMS(Constants.TRACKING_TEXT, locationHelper.getLatitude(),
					locationHelper.getLongitude());
		}

	}
}
