package com.ninja.bluetooth;

import android.util.Log;

public class Utils {

	/**
	 * @param data byte array of data read from Arduino
	 * @return message sent by the user
	 */
	public static String parseArduinoMessage(byte[] data) {
		String _data = new String(data);
		Log.d("UTILS", _data);
		
		String message;
		if (_data.contains(Constants.ARDUINO_EMERGENCY_MESSAGE)) {
			message = Constants.EMERGENCY_MESSAGE;
		} else if (_data.contains(Constants.ARDUINO_CANCEL_MESSAGE)) {
			message = Constants.CANCEL_MESSAGE;
		} else {
			message = Constants.ARDUINO_ERROR_MESSAGE;
		}

		return message;
	}
}
