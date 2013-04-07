package com.ninja.bluetooth;

public class Constants {
	public static final String BUZZ_GUARDIAN_NUMBER = "+16787758612";
	public static final String ALERT_TEXT = "=== BuzzGuardian ===\n HELP\n";

	// Message types sent from the BluetoothConnection Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
}
