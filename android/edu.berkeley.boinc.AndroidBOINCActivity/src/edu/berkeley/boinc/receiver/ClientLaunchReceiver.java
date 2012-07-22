package edu.berkeley.boinc.receiver;

import edu.berkeley.boinc.AndroidBOINCActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClientLaunchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ClientLaunchReceiver", "onReceive");
		//gets triggered when launch starts and finishes
		
		if(intent.getBooleanExtra("finished", false)) { // client launch finished
			AndroidBOINCActivity.client.settingUp = false;
			AndroidBOINCActivity.client.launched = true;
			Log.d("ClientLaunchReceiver", "client setup finished");

			//start monitor
			AndroidBOINCActivity.client.startMonitor();
		}
		else { //client launch started
			
			//get the client flags straight...
			AndroidBOINCActivity.client.broken=false;
			AndroidBOINCActivity.client.computing=false;
			AndroidBOINCActivity.client.launched=false;
			AndroidBOINCActivity.client.executing=false;
			AndroidBOINCActivity.client.settingUp=true;
			Log.d("ClientLaunchReceiver", "client setup started");
		}
	}
}
