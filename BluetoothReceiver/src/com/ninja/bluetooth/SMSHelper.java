package com.ninja.bluetooth;

import android.telephony.SmsManager;

public class SMSHelper {

	private static SmsManager smsManager = SmsManager.getDefault();
	
	public static void sendSMS(String text){
		smsManager.sendTextMessage(Constants.BUZZ_GUARDIAN_NUMBER, null, text, null, null);
	}

}
