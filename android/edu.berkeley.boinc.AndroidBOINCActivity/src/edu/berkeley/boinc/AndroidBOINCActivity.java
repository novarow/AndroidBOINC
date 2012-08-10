package edu.berkeley.boinc;

import edu.berkeley.boinc.client.ClientStatus;
import edu.berkeley.boinc.client.Monitor;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle; 
import android.os.IBinder;
import android.util.Log;  
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class AndroidBOINCActivity extends TabActivity {
	
	private final String TAG = "AndroidBOINCActivity"; 
	private final Boolean debugTab = true;
	
	private Monitor monitor;
	public static ClientStatus client;
	
	private Boolean mIsBound;
	

	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        monitor = ((Monitor.LocalBinder)service).getService();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        monitor = null;
	        Toast.makeText(getApplicationContext(), "service disconnected",
	                Toast.LENGTH_SHORT).show();
	    }
	};

	void doBindService() {
		// Service has to be started "sticky" by the first instance that uses it. It causes the service to stay around, even when all Activities are destroyed (on purpose or by the system)
		// check whether service already started by BootReiver is done within the service.
		startService(new Intent(this,Monitor.class));
		
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
		bindService(new Intent(this, Monitor.class), mConnection, 0);
	    mIsBound = true;
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}

	@Override
	protected void onDestroy() {
    	Log.d(TAG, "onDestroy");
	    super.onDestroy();
	    doUnbindService();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main);  
         
        Log.d(TAG, "onCreate"); 
        
        //bind monitor service
        doBindService();
        
        TabHost tabHost = getTabHost();
        
        TabSpec statusSpec = tabHost.newTabSpec(getResources().getString(R.string.tab_status));
        statusSpec.setIndicator(getResources().getString(R.string.tab_status), getResources().getDrawable(R.drawable.icon_status_tab));
        Intent statusIntent = new Intent(this,StatusActivity.class);
        statusSpec.setContent(statusIntent);
        tabHost.addTab(statusSpec);
        
        TabSpec tasksSpec = tabHost.newTabSpec(getResources().getString(R.string.tab_tasks));
        tasksSpec.setIndicator(getResources().getString(R.string.tab_tasks), getResources().getDrawable(R.drawable.icon_tasks_tab));
        Intent tasksIntent = new Intent(this,TasksActivity.class);
        tasksSpec.setContent(tasksIntent);
        tabHost.addTab(tasksSpec);
        
        TabSpec transSpec = tabHost.newTabSpec(getResources().getString(R.string.tab_transfers));
        transSpec.setIndicator(getResources().getString(R.string.tab_transfers), getResources().getDrawable(R.drawable.icon_trans_tab));
        Intent transIntent = new Intent(this,TransActivity.class);
        transSpec.setContent(transIntent);
        tabHost.addTab(transSpec);
        
        TabSpec prefsSpec = tabHost.newTabSpec(getResources().getString(R.string.tab_preferences));
        prefsSpec.setIndicator(getResources().getString(R.string.tab_preferences), getResources().getDrawable(R.drawable.icon_prefs_tab));
        Intent prefsIntent = new Intent(this,PrefsActivity.class);
        prefsSpec.setContent(prefsIntent);
        tabHost.addTab(prefsSpec);
        
        TabSpec msgsSpec = tabHost.newTabSpec(getResources().getString(R.string.tab_messages));
        msgsSpec.setIndicator(getResources().getString(R.string.tab_messages), getResources().getDrawable(R.drawable.icon_msgs_tab));
        Intent msgsIntent = new Intent(this,MsgsActivity.class);
        msgsSpec.setContent(msgsIntent);
        tabHost.addTab(msgsSpec);
        
        if(debugTab) {
	        TabSpec debugSpec = tabHost.newTabSpec(getResources().getString(R.string.tab_debug));
	        debugSpec.setIndicator(getResources().getString(R.string.tab_debug), getResources().getDrawable(R.drawable.icon_debug_tab));
	        Intent debugIntent = new Intent(this,DebugActivity.class);
	        debugSpec.setContent(debugIntent);
	        tabHost.addTab(debugSpec);
        }
        Log.d(TAG,"tab layout setup done");

        AndroidBOINCActivity.logMessage(this, TAG, "tab setup finished");
       
    }
    
    public static void logMessage(Context ctx, String tag, String message) {
        Intent testLog = new Intent();
        testLog.setAction("edu.berkeley.boinc.log");
        testLog.putExtra("message", message);   
        testLog.putExtra("tag", tag);
        ctx.sendBroadcast(testLog);
    }
}