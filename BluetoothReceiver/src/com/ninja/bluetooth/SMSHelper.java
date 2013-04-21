package com.ninja.bluetooth;

import android.telephony.SmsManager;
import android.util.Log;

public class SMSHelper {

	private static SmsManager smsManager = SmsManager.getDefault();
	private static final String TAG = "SMSHelper";
	
	public static void sendSMS(String text){
		Log.d(TAG, "Sending SMS : " + text);
		smsManager.sendTextMessage(Constants.BUZZ_GUARDIAN_NUMBER, null, text, null, null);
	}

}
