package edu.berkeley.boinc.receiver;

import edu.berkeley.boinc.client.ClientStatus;
import edu.berkeley.boinc.client.Monitor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClientStatusChangeReceiver extends BroadcastReceiver {
	
	private ClientStatus status;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ClientStatusChangeReceiver", "onReceive");
		
		//set status of client
		status = Monitor.getClientStatus();

	}

}
