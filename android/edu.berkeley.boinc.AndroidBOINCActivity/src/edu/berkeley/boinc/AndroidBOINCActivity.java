package edu.berkeley.boinc;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle; 
import android.os.StrictMode;
import android.util.Log;  
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class AndroidBOINCActivity extends TabActivity {
	
	private static final String TAG = "AndroidBOINCActivity"; 
	
	public static BOINClient client;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main);  
         
        Log.d(TAG, "onCreate");  
        
        //disable strict mode for now
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        //initialize BOINClient and run it
        AndroidBOINCActivity.client = new BOINClient(this);
        AndroidBOINCActivity.client.setup();
        
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
    
    //temporary! until re-connect is implemented
    public void onPause(){
    	Log.d(TAG, "onPause");
    	client.shutdown();
    	Log.d(TAG, "client shut down");
    	super.onPause();
    }
}