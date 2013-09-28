package com.ninja.bluetooth;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity of the application that contains a menu option to start/stop the BuzzGuardianService.
 */
public class BuzzGuardianActivity extends Activity {

	// Debugging
	private static final String TAG = "BuzzGuardianActivity";
	private static final boolean D = true;

	// Intent request codes
	private static final int REQUEST_ENABLE_BT = 3;

	// Layout Views
	private TextView mTitle;

	// Local Bluetooth Adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	// Menu items
	private static final int START_SERVICE = 0;
	private static final int STOP_SERVICE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) {
			Log.d(TAG, "+++ ON CREATE +++");
		}
		
		setLayoutAndTitle();
		getBlueToothAdapter();
	}

	private void setLayoutAndTitle() {
		// Set up the window layout
		setContentView(R.layout.main);		
	}

	private void getBlueToothAdapter() {
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D){
			Log.d(TAG, "++ ON START ++");
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
	}
	
	/**
	 * checks if a service is running in the background
	 * @param klass Class of the service being queried
	 * @return true is service is running
	 */
	private boolean isServiceRunning(Class klass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (klass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		generateMenu(menu);
		return true;
	}
	
	@Override
	/**
	 * For API Level >= 11, we can use invalidateOptionsMenu() instead
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		return onCreateOptionsMenu(menu);
	}
	
	private void generateMenu(Menu menu) {
		menu.clear();
		boolean isServiceRunning = isServiceRunning(BuzzGuardianService.class);
		if (!isServiceRunning) {
			menu.add(Menu.NONE, START_SERVICE, 0, "Start Service");
		} else {
			menu.add(Menu.NONE, STOP_SERVICE, 0, "Stop Service");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case START_SERVICE:
			// start the BuzzGuardian service
			Intent intent = new Intent(this, BuzzGuardianService.class);
			startService(intent);
			return true;
		case STOP_SERVICE:
			if(isServiceRunning(BuzzGuardianService.class)){
				stopService(new Intent(this, BuzzGuardianService.class));
			}
			return true;
		}
		return false;
	}
}
