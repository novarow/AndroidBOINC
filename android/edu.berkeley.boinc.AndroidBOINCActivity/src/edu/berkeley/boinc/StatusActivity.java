package edu.berkeley.boinc;

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
	
	private BroadcastReceiver localClientStatusRecQuiet = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context,Intent intent) {
			//gets triggered every time the client setup status changes (clientlaunch, clientrunning, clienterror)
			String action = intent.getAction();
			Log.d(TAG+"-localClientStatusRecQuiet","received action " + action);
			loadLayout();
		}
	};
	private BroadcastReceiver localClientStatusRecNoisy = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context,Intent intent) {
			//gets triggered every time the client status changes (clientstatus)
			String action = intent.getAction();
			Log.d(TAG+"-localClientStatusRecNoisy","received action " + action);
			loadLayout();
		}
	};
	private IntentFilter ifcl = new IntentFilter("edu.berkeley.boinc.clientlaunch");
	private IntentFilter ifcr = new IntentFilter("edu.berkeley.boinc.clientcomputing");
	private IntentFilter ifce = new IntentFilter("edu.berkeley.boinc.clienterror");
	private IntentFilter ifcs = new IntentFilter("edu.berkeley.boinc.clientstatus");
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		loadLayout();
		
        //register less noisy status receiver permanently for this activity
		//this receiver has lower priority than BroadcastReceivers in edu.berkeley.boinc.reiceiver
        registerReceiver(localClientStatusRecQuiet,ifcl);
        registerReceiver(localClientStatusRecQuiet,ifcr);
        registerReceiver(localClientStatusRecQuiet,ifce);
	}
	
	public void onResume() {
		//register noisy client status receiver here, so only active when Activity is being visible
		Log.d(TAG+"-onResume","register noisy receiver");
		registerReceiver(localClientStatusRecNoisy,ifcs);
		
		//refresh layout
		loadLayout();
		
		super.onStart();
	}
	
	public void onPause() {
		//unregister receiver, so there are not multiple intents flying in
		Log.d(TAG+"-onPause","remove noisy receiver");
		unregisterReceiver(localClientStatusRecNoisy);
		super.onStop();
	}
	
	private void loadLayout() {
		Log.d(TAG,"loadLayout()");
		if(AndroidBOINCActivity.client.broken) { //client connection is broken
			setContentView(R.layout.status_layout_error);
			Log.d(TAG,"layout: status_layout_error");
		} else { // client connection is not broken
			if(AndroidBOINCActivity.client.launched) { // client finished launching
				if(AndroidBOINCActivity.client.computingEnabled) { //client is in run mode "auto", so not paused by user
					if(AndroidBOINCActivity.client.computing) {
						Log.d(TAG,"layout: status_layout_computing");
						setContentView(R.layout.status_layout_computing);
					}else {
						Log.d(TAG,"layout: status_layout_suspended");
						setContentView(R.layout.status_layout_suspended);
					}
				}
				else { //client is in run mode "never", force paused by user
					Log.d(TAG,"layout: status_layout_computing_disabled");
					setContentView(R.layout.status_layout_computing_disabled);
				}
			}
			else { //client is launching
				Log.d(TAG,"layout: status_layout_launching");
				setContentView(R.layout.status_layout_launching);
			}
		}
	}
	
	//gets called when user clicks on retry of error_layout
	//has to be public in order to get triggered by layout component
	public void reinitClient(View view) {
		Log.d(TAG,"reinitClient");
		AndroidBOINCActivity.client.setup(); //start over with setup of client
		loadLayout(); //load new layout
	}
	
	public void disableComputation(View view) {
		Log.d(TAG,"disableComputation");
		AndroidBOINCActivity.client.setRunMode(3); //run mode 3 = never
		loadLayout(); //load new layout
	}
	
	public void enableComputation(View view) {
		Log.d(TAG,"enableComputation");
		AndroidBOINCActivity.client.setRunMode(2); //run mode 2 = auto
		loadLayout(); //load new layout
	}

}
