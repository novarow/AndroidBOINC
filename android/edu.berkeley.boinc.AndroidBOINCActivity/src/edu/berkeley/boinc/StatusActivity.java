package edu.berkeley.boinc;

import edu.berkeley.boinc.client.ClientStatus;
import edu.berkeley.boinc.client.Monitor;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class StatusActivity extends Activity {
	
	private final String TAG = "StatusActivity";
	
	private BroadcastReceiver mClientStatusChangeRec = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context,Intent intent) {
			String action = intent.getAction();
			Log.d(TAG+"-localClientStatusRecNoisy","received action " + action);
			loadLayout();
		}
	};
	private IntentFilter ifcsc = new IntentFilter("edu.berkeley.boinc.clientstatuschange");
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void onResume() {
		//register noisy clientStatusChangeReceiver here, so only active when Activity is visible
		Log.d(TAG+"-onResume","register noisy receiver");
		registerReceiver(mClientStatusChangeRec,ifcsc);
		
		//refresh layout
		loadLayout();
		
		super.onResume();
	}
	
	public void onPause() {
		//unregister receiver, so there are not multiple intents flying in
		Log.d(TAG+"-onPause","remove noisy receiver");
		unregisterReceiver(mClientStatusChangeRec);
		super.onStop();
	}
	
	private void loadLayout() {
		Log.d(TAG,"loadLayout()");
		ClientStatus status = Monitor.getClientStatus();
		switch(status.setupStatus){
		case 0:
			Log.d(TAG,"layout: status_layout_launching");
			setContentView(R.layout.status_layout_launching);
			break;
		case 1:
			Log.d(TAG,"client's setup status: ready! determine run status...");
			setContentView(R.layout.status_layout_suspended); //TEMPORARY
			break;
		case 2:
			setContentView(R.layout.status_layout_error);
			Log.d(TAG,"layout: status_layout_error");
			break;
		}
	}
	
	//gets called when user clicks on retry of error_layout
	//has to be public in order to get triggered by layout component
	public void reinitClient(View view) {
		Log.d(TAG,"reinitClient");
		//AndroidBOINCActivity.monitor.setup(); //start over with setup of client
		loadLayout(); //load new layout
	}
	
	public void disableComputation(View view) {
		Log.d(TAG,"disableComputation");
		//AndroidBOINCActivity.monitor.setRunMode(3); //run mode 3 = never
		loadLayout(); //load new layout
	}
	
	public void enableComputation(View view) {
		Log.d(TAG,"enableComputation");
		//AndroidBOINCActivity.monitor.setRunMode(2); //run mode 2 = auto
		loadLayout(); //load new layout
	}

}
