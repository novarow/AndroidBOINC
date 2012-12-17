package edu.berkeley.boinc.manager;

import edu.berkeley.boinc.client.ClientStatusData;
import edu.berkeley.boinc.client.ClientStatusMonitor;
import edu.berkeley.boinc.manager.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class SimpleViewActivity  extends Activity {

	private final String TAG = "SimpleViewActivity"; 
	
	private ClientStatusMonitor monitor;
	public static ClientStatusData client;
	
	private Boolean mIsBound;

	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been established, getService returns the Monitor object that is needed to call functions.
	        monitor = ((ClientStatusMonitor.LocalBinder)service).getService();
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
        setContentView(R.layout.activity_main);  
         
        Log.d(TAG, "onCreate"); 
        
        bindMonitorService();
    }
    
	@Override
	protected void onDestroy() {
    	Log.d(TAG, "onDestroy");
	    super.onDestroy();
	    
	    unbindMonitorService();
	}
	
	private void bindMonitorService() {
		// Service has to be started "sticky" by the first instance that uses it. It causes the service to stay around, even when all Activities are destroyed (on purpose or by the system)
		// check whether service already started by BootReceiver is done within the service.
		startService(new Intent(this,ClientStatusMonitor.class));
		
	    // Establish a connection with the service, onServiceConnected gets called when
		bindService(new Intent(this, ClientStatusMonitor.class), mConnection, 0);
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
