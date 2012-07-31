package edu.berkeley.boinc;

import edu.berkeley.boinc.client.ClientStatus;
import edu.berkeley.boinc.client.Monitor;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class StatusActivity extends Activity {
	
	private final String TAG = "StatusActivity";
	
	private Monitor monitor;
	
	private Boolean mIsBound = false;

	private BroadcastReceiver mClientStatusChangeRec = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context,Intent intent) {
			String action = intent.getAction();
			Log.d(TAG+"-localClientStatusRecNoisy","received action " + action);
			loadLayout(); //initial layout set up
		}
	};
	private IntentFilter ifcsc = new IntentFilter("edu.berkeley.boinc.clientstatuschange");
	

	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(TAG,"onServiceConnected");
	        monitor = ((Monitor.LocalBinder)service).getService();
		    mIsBound = true;
		    loadLayout();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        monitor = null;
	    }
	};

	void doBindService() {
		if(!mIsBound) {
			getApplicationContext().bindService(new Intent(this, Monitor.class), mConnection, 0); //calling within Tab needs getApplicationContext() for bindService to work!
		}
	}

	void doUnbindService() {
	    if (mIsBound) {
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}
	
	public void onCreate(Bundle savedInstanceState) {
		//bind to monitor in order to call its functions and access ClientStatus singleton
		doBindService();
		super.onCreate(savedInstanceState);
	}
	
	public void onResume() {
		//register noisy clientStatusChangeReceiver here, so only active when Activity is visible
		Log.d(TAG+"-onResume","register noisy receiver");
		registerReceiver(mClientStatusChangeRec,ifcsc);
		loadLayout();
		super.onResume();
	}
	
	public void onPause() {
		//unregister receiver, so there are not multiple intents flying in
		Log.d(TAG+"-onPause","remove noisy receiver");
		unregisterReceiver(mClientStatusChangeRec);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    doUnbindService();
	}
	
	private void loadLayout() {
		Log.d(TAG,"loadLayout()");
		
		//load layout, if service is available and ClientStatus can be accessed.
		//if this is not the case, "onServiceConnected" will call "loadLayout" as soon as the service is bound
		if(mIsBound) {
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
	}
	
	//gets called when user clicks on retry of error_layout
	//has to be public in order to get triggered by layout component
	public void reinitClient(View view) {
		if(!mIsBound) return;
		Log.d(TAG,"reinitClient");
		monitor.restartMonitor(); //start over with setup of client
	}
	
	public void disableComputation(View view) {
		if(!mIsBound) return;
		Log.d(TAG,"disableComputation");
		//AndroidBOINCActivity.monitor.setRunMode(3); //run mode 3 = never
	}
	
	public void enableComputation(View view) {
		if(!mIsBound) return;
		Log.d(TAG,"enableComputation");
		//AndroidBOINCActivity.monitor.setRunMode(2); //run mode 2 = auto
	}

}
