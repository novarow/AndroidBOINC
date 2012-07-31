package edu.berkeley.boinc.receiver;

import edu.berkeley.boinc.AndroidBOINCActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClientComputingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ClientRunningReceiver", "onReceive");
		
		//triggerd when computing is either stopped or started.
		//TODO
		
		//set status of client
		//AndroidBOINCActivity.monitor.computing = false; //client is computing BOINC task
		//AndroidBOINCActivity.monitor.suspendReason = 0; //reason why computing got suspended
	}

}
