package edu.berkeley.boinc.receiver;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LogReceiver extends BroadcastReceiver {

	public static ArrayList<String> logHub = new ArrayList<String>();
	@Override
	public void onReceive(Context context, Intent intent) {
		String tag = intent.getStringExtra("tag");
		if(tag == null) {
			tag = "unknown";
		}
		String message = intent.getStringExtra("message");
		String entry = tag + " - " + message;
		//Log.d("LogReceiver", entry);
		LogReceiver.logHub.add(entry);
	}

}
