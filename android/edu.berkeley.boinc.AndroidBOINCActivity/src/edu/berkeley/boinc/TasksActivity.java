package edu.berkeley.boinc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class TasksActivity extends Activity {
	
	private final String TAG = "TasksActivity";

	private BroadcastReceiver localClientStatusRecNoisy = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context,Intent intent) {
			//gets triggered every time the client status changes (clientstatus)
			String action = intent.getAction();
			Log.d(TAG+"-localClientStatusRecNoisy","received action " + action);
		}
	};
	private IntentFilter ifcs = new IntentFilter("edu.berkeley.boinc.clientstatus");
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msgs_layout); 
	}
	
	public void onResume() {
		//register noisy client status receiver here, so only active when Activity is being visible
		Log.d(TAG+"-onResume","register noisy receiver");
		registerReceiver(localClientStatusRecNoisy,ifcs);
		super.onStart();
	}
	
	public void onPause() {
		//unregister receiver, so there are not multiple intents flying in
		Log.d(TAG+"-onPause","remove noisy receiver");
		unregisterReceiver(localClientStatusRecNoisy);
		super.onStop();
	}

}
