package edu.berkeley.boinc.manager;

import edu.berkeley.boinc.client.ClientStatus;
import edu.berkeley.boinc.client.ClientStatusMonitor;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class InitialActivity extends Activity {
	
	private final String TAG = "InitialActivity"; 
	
	private ClientStatusMonitor monitor;
	public static ClientStatus client;
	
	private Boolean mIsBound;

	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been established, getService returns the Monitor object that is needed to call functions.
	        monitor = ((ClientStatusMonitor.LocalBinder)service).getService();
	        Log.d(TAG, "onServiceConnected: ClientStatusMonitor bound.");
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This should not happen
	        monitor = null;
	        Toast.makeText(getApplicationContext(), "service disconnected", Toast.LENGTH_SHORT).show();
	    }
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        Log.d(TAG, "onCreate"); 
        
        bindMonitorService();
        

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_initial, menu);
        return true;
    }
    

    @Override
	protected void onDestroy() {
    	Log.d(TAG, "onDestroy");
	    super.onDestroy();
	    
	    unbindMonitorService();
	}
	
	private void bindMonitorService() {
	    // Establish a connection with the service, onServiceConnected gets called when finished.
		Intent i = new Intent(this,edu.berkeley.boinc.client.ClientStatusMonitor.class);
		this.startService(i);
		this.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}
	
	private void unbindMonitorService() {
	    if (mIsBound) {
	        // Detach existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}
}
