package edu.berkeley.boinc.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {  

    @Override
    public void onReceive(Context context, Intent intent) {
    	
    	// check whether preference "autostart" is enabled is done within the service
    	Intent startServiceIntent = new Intent(context, Monitor.class);
    	startServiceIntent.putExtra("autostart", true);
    	context.startService(startServiceIntent);
    }
}

