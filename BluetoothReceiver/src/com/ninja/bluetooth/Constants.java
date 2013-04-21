package com.ninja.bluetooth;

public class Constants {
	public static final String PANIC_BUTTON_MAC_ADDRESS = "00:06:66:4B:45:8A";
	public static final String STANDARD_SERIAL_PORT_UUID_STRING = "00001101-0000-1000-8000-00805f9b34fb"; 
	
	public static final String BUZZ_GUARDIAN_NUMBER = "+16787758612"; // BuzzGuardian Helpline Number
	public static final String EMERGENCY_TEXT = "=== BuzzGuardian ===\n EMERGENCY\n";
	public static final String CANCEL_TEXT = "=== BuzzGuardian ===\n CANCEL\n";
	public static final String TRACKING_TEXT = "=== BuzzGuardian ===\n TRACKING\n";
	
	public static final String EMERGENCY_MESSAGE = "EMERGENCY";
	public static final String CANCEL_MESSAGE = "CANCEL";
	public static final String TRACKING_MESSAGE = "TRACKING";
	public static final String ERROR_MESSAGE = "UNKNOWN MESSAGE FROM ARDUINO";
	
	public static final int TRACKING_PERIODICITY = 60000; 

	// Message types sent from the BluetoothConnection Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
}
