package edu.berkeley.boinc.receiver;

import edu.berkeley.boinc.AndroidBOINCActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClientErrorReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ClientErrorReceiver", "onReceive");
		
		//set corresponding status flags of static client
		AndroidBOINCActivity.client.broken = true; 
		
		//shutdown client (flags for launched and executing are set there)
		AndroidBOINCActivity.client.shutdown();

	}

}
