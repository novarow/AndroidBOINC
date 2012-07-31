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
			//AndroidBOINCActivity.monitor.settingUp = false;
			//AndroidBOINCActivity.monitor.launched = true;
			Log.d("ClientLaunchReceiver", "client setup finished");

			//start monitor
			//AndroidBOINCActivity.monitor.startMonitor();
		}
		else { //client launch started
			
			//get the client flags straight...
			//AndroidBOINCActivity.monitor.broken=false;
			//AndroidBOINCActivity.monitor.computing=false;
			//AndroidBOINCActivity.monitor.launched=false;
			//AndroidBOINCActivity.monitor.executing=false;
			//AndroidBOINCActivity.monitor.settingUp=true;
			Log.d("ClientLaunchReceiver", "client setup started");
		}
	}
}
