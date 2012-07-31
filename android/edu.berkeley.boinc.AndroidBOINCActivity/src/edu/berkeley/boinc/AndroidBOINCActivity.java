package edu.berkeley.boinc;

import edu.berkeley.boinc.client.ClientStatus;
import edu.berkeley.boinc.client.Monitor;
import edu.berkeley.boinc.client.Monitor.LocalBinder;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle; 
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;  
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class AndroidBOINCActivity extends TabActivity {
	
	private static final String TAG = "AndroidBOINCActivity"; 
	
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

	        // Tell the user about this for our demo.
	        Toast.makeText(getApplicationContext(), "service connected", Toast.LENGTH_SHORT).show();
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
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
		Log.d(TAG,"doBindService()");
		bindService(new Intent(this, Monitor.class), mConnection, Context.BIND_AUTO_CREATE);
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
        
        //set application context to service singleton
        Monitor.getClientStatus().setCtx(this);
        
        //temporary - disable strict mode
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        

        
        //bind monitor service
        doBindService();
        
        TabHost tabHost = getTabHost();
        TabSpec statusSpec = tabHost.newTabSpec("Status");
        statusSpec.setIndicator("Status", getResources().getDrawable(R.drawable.icon_status_tab));
        Intent statusIntent = new Intent(this,StatusActivity.class);
        statusSpec.setContent(statusIntent);
        TabSpec tasksSpec = tabHost.newTabSpec("Tasks");
        tasksSpec.setIndicator("Tasks", getResources().getDrawable(R.drawable.icon_tasks_tab));
        Intent tasksIntent = new Intent(this,TasksActivity.class);
        tasksSpec.setContent(tasksIntent);
        TabSpec prefsSpec = tabHost.newTabSpec("Preferences");
        prefsSpec.setIndicator("Preferences", getResources().getDrawable(R.drawable.icon_prefs_tab));
        Intent prefsIntent = new Intent(this,PrefsActivity.class);
        prefsSpec.setContent(prefsIntent);
        TabSpec msgsSpec = tabHost.newTabSpec("Messages");
        msgsSpec.setIndicator("Messages", getResources().getDrawable(R.drawable.icon_msgs_tab));
        Intent msgsIntent = new Intent(this,MsgsActivity.class);
        msgsSpec.setContent(msgsIntent);
        TabSpec debugSpec = tabHost.newTabSpec("Debug");
        debugSpec.setIndicator("Debug", getResources().getDrawable(R.drawable.icon_debug_tab));
        Intent debugIntent = new Intent(this,DebugActivity.class);
        debugSpec.setContent(debugIntent);
        tabHost.addTab(statusSpec);
        tabHost.addTab(tasksSpec);
        tabHost.addTab(msgsSpec);
        tabHost.addTab(prefsSpec);
        tabHost.addTab(debugSpec);
        
        Log.d(TAG,"tab layout setup done");

        

        
        AndroidBOINCActivity.logMessage(this, TAG, "on Create finished");
       
    }
    
    public static void logMessage(Context ctx, String tag, String message) {
        Intent testLog = new Intent();
        testLog.setAction("edu.berkeley.boinc.log");
        testLog.putExtra("message", message);   
        testLog.putExtra("tag", tag);
        ctx.sendBroadcast(testLog);
    }
}