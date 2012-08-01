package edu.berkeley.boinc.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import edu.berkeley.boinc.AndroidBOINCActivity;
import edu.berkeley.boinc.R;
import edu.berkeley.boinc.definitions.CommonDefs;
import edu.berkeley.boinc.rpc.CcStatus;
import edu.berkeley.boinc.rpc.GlobalPreferences;
import edu.berkeley.boinc.rpc.ProjectAttachReply;
import edu.berkeley.boinc.rpc.RpcClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class Monitor extends Service{
	
	private final String TAG = "BOINC Client Monitor Service";
	
	private static ClientStatus clientStatus; //holds the status of the client as determined by the Monitor
	public static ClientStatus getClientStatus() { //singleton pattern
		if (clientStatus == null) {
			clientStatus = new ClientStatus();
		}
		return clientStatus;
	}
	
	private NotificationManager mNM;
	
	public static Boolean monitorActive = false;
	private Process clientProcess;
	
	private RpcClient rpc = new RpcClient();

	/*
	 * returns this class, allows clients to access this service's functions and attributes.
	 */
	public class LocalBinder extends Binder {
        public Monitor getService() {
            return Monitor.this;
        }
    }
	
	/*
	 * gets called once, when startService is called from within an Activity
	 */
	@Override
    public void onCreate() {
		Log.d(TAG,"onCreate()");
        
        // test notification
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        
        
        (new ClientMonitorAsync()).execute(new Integer[0]);
    }
	
	/*
	 * gets called once, when startService is called from within an Activity
	 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	//this gets called after startService(intent)
    	Log.d(TAG, "onStartCommand");
        // returning START_STICKY causes service to run until it is explecitly closed
        return START_STICKY;
    }

    /*
     * this should not be reached
     */
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(1234);
        
		Boolean success = rpc.quit();
		Log.d(TAG,"graceful client shutdown returned " + success);
		if(!success) {
			clientProcess.destroy();
		}

        // Tell the user we stopped.
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
    }

    /*
     * gets called every-time an activity binds to this service, but not the initial start (onCreate and onStartCommand are called there)
     */
    @Override
    public IBinder onBind(Intent intent) {
    	//Log.d(TAG,"onBind");
        return mBinder;
    }
    private final IBinder mBinder = new LocalBinder();
    
    /*
     * Show a notification while this service is running.
     */
    private void showNotification() {
        Notification notification = new Notification(R.drawable.playw48, "service started", System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), AndroidBOINCActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(getApplicationContext(), "blub", "service started", contentIntent);

        // Send the notification.
        mNM.notify(1234, notification);
    }
	
    public void restartMonitor() {
    	if(Monitor.monitorActive) { //monitor is already active, launch cancelled
    		AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "monitor active - restart cancelled");
    	}
    	else {
        	Log.d(TAG,"restart monitor");
        	(new ClientMonitorAsync()).execute(new Integer[0]);
    	}
    }
    
	public void setRunMode(Integer mode) {
		Boolean success = rpc.setRunMode(mode,0);
		Log.d(TAG,"run mode set to " + mode + " returned " + success);
	}
	
	private final class ClientMonitorAsync extends AsyncTask<Integer,String,Boolean> {

		private final String TAG = "ClientMonitorAsync";
		
		private Boolean clientStarted = false;
		
		private final String clientName = "boinc_client"; 
		private final String authFileName = "gui_rpc_auth.cfg";
		private String clientPath = "/data/data/edu.berkeley.boinc/client/";
		
		private final Integer maxDuration = 30; //maximum polling duration
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.d(TAG+"-doInBackground","monitor started.");
			
			while(true) {
				Log.d(TAG+"-doInBackground","monitor loop...");
				
				
				if(!rpc.connectionAlive()) { //check whether connection is still alive
					//if connection is not working, either client has not been set up yet, or client crashed.
					//in both cases start client again
					setupClient(); //synchronous execution in same thread -> blocks monitor until finished
				}
				
				//Log.d(TAG, "getHostInfo");
				//rpc.getHostInfo();
				//Log.d(TAG, "getProjectStatus");
				//rpc.getProjectStatus();
				//Log.d(TAG, "getActiveResults");
				//rpc.getActiveResults();  
				
				//Log.d(TAG, "getResults");
				//ArrayList<edu.berkeley.boinc.rpc.Result> tasks = rpc.getResults();
				
				
				//prefs test
				//GlobalPreferences prefs = rpc.getGlobalPrefsWorkingStruct();
				//Log.d(TAG, "Java class holds network_wifi_only value: " + prefs.network_wifi_only); 
				
				Log.d(TAG, "getCcStatus");
				edu.berkeley.boinc.rpc.CcStatus status = rpc.getCcStatus();
				Log.d(TAG, "getState");
				edu.berkeley.boinc.rpc.CcState state = rpc.getState();
				
				if((state!=null)&&(status!=null)) {
					Monitor.clientStatus.setClientStatus(status,state);
				} else {
					AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "client status connection problem");
				}
					//connection problem! do nothing, loop will continue, "connectionAlive" fail, and the setup routine start.
					
					
					//Log.d(TAG,"task_mode: " + status.task_mode + " task_suspend_reason: " + status.task_suspend_reason);
					/*
					if(status.task_mode==CommonDefs.RUN_MODE_NEVER) { // run mode set to "never"
						Log.d(TAG,"disabled by user");
						computing = false;
						suspendReason = status.task_suspend_reason; //doesnt matter too much, because layout doesnt show
						computingEnabled = false;
					}
					else if (status.task_mode==CommonDefs.RUN_MODE_AUTO){ //client is in run mode "auto"
						suspendReason = status.task_suspend_reason;
						computingEnabled = true;
						if(suspendReason==CommonDefs.SUSPEND_NOT_SUSPENDED) { //not suspended, so computing
							Log.d(TAG,"computing");
							//TODO check whether there are active tasks?
							computing = true;
						}
						else { //suspended
							Log.d(TAG,"suspended");
							computing = false;
						}
					}*/
					//setClientStatus(tasks,status);
				
		        Intent clientStatus = new Intent();
		        clientStatus.setAction("edu.berkeley.boinc.clientstatus");
		        getApplicationContext().sendBroadcast(clientStatus);
	    		try {
	    			Thread.sleep(3000); //sleep
	    		}catch(Exception e){}
			}
		}

		@Override
		protected void onProgressUpdate(String... arg0) {
			Log.d(TAG+"-onProgressUpdate",arg0[0]);
			AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, arg0[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "client connection broken! (permanent)");
			Log.d(TAG+" - onPostExecute","client connection broken! (permanent)"); 
			Monitor.monitorActive = false;
		}
		

		
		private void setupClient() {
			
			//adapt client status and broadcast event
			getClientStatus().setupStatus = 0;
			getClientStatus().fire();
			
			//try to reconnect, if client of another Manager lifecycle exists.
			Boolean success =  reconnectClient();
			
			if(!success) { //if reconnect did not work out, loop through setup routine until successful or timeout
				success = false;
				Integer counter = 0;
				Integer max = 5; //max number of setup attempts
				while(!(success=setupClientRoutine()) && (counter<max)) { //re-trys setting up the client several times, before giving up.
					AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "--- restart setup ---");
					counter++;
				}
			}
	        
			//publish results
			AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "finished " + success);
			if(success) { //if setup successful, publish...
				getClientStatus().setupStatus = 1;
				getClientStatus().fire();
			}
			else { //setup failed several times, publish...
				getClientStatus().setupStatus = 2;
				getClientStatus().fire();
				
			}
		}
		
		/*
		 * called by setupClient()
		 * tries to reconnect to running BOINC client.
		 * This is needed, if previous execution of this Manager app terminated and BOINC client is still running on the device.
		 */
		private Boolean reconnectClient() {
			Boolean success = false;
			
			publishProgress("trying to re-connect client.");
			
	        success = connect();
	        if(success) {
	        	publishProgress("re-connected. (1/3)");
	        }
	        else {
	        	publishProgress("re-connection failed!");
	        	return success;
	        }
	        
	        //authorize
	        success = authorize();
	        if(success) {
	        	publishProgress("authorized. (2/3)");
	        }
	        else {
	        	publishProgress("authorization failed!");
	        	return success;
	        }
	        
	        //attach project
	        attachProject();
	        success = attachProjectPoll();
	        if(success) {
	        	publishProgress("project attached. (3/3)");
	        }
	        else {
	        	publishProgress("project attachment failed!");
	        	return success;
	        }
	        
	        publishProgress("client re-connected");
	        return success;
		}
		
		/*
		 * called by setupClient()
		 * walks through the steps necessary to get a working and interacting BOINC client.
		 */
		private Boolean setupClientRoutine() {
			//setup client
			Boolean success = false;
			
			shutdownExisitingClient();
	
	        success = installClient(true);
	        if(success) {
	        	publishProgress("installed. (1/5)");
	        }
	        else {
	        	publishProgress("installation failed!");
	        	return success;
	        }
	        
	        //run client
	        success = runClient();
	        if(success) {
	        	publishProgress("started. (2/5)");
	        	clientStarted = true; //mark that client process is running.
	        }
	        else {
	        	publishProgress("start of client failed!");
	        	return success;
	        }
	        
	        //connect in loop
    		try {
    			Thread.sleep(5000); //sleep for five seconds before connecting to BOINC client
    		}catch(Exception e){}
	    	success = false;
	    	Integer loop = 0;
	    	while(!success && (6 > loop)) {
	    		success = connect();
	    		loop++;
	    		try {
	    			Thread.sleep(5000); //sleep for five seconds
	    		}catch(Exception e){}
	    	}
	        if(success) {
	        	publishProgress("connected. (3/5)");
	        }
	        else {
	        	publishProgress("connection failed!");
	        	return success;
	        }
	        
	        //authorize
	        success = authorize();
	        if(success) {
	        	publishProgress("authorized. (4/5)");
	        }
	        else {
	        	publishProgress("authorization failed!");
	        	return success;
	        }
	        
	        //attach project
	        Boolean tmp = attachProject();
	        Log.d(TAG,"attach project returned: " + tmp);
	        success = attachProjectPoll();
	        if(success) {
	        	publishProgress("project attached. (5/5)");
	        }
	        else {
	        	publishProgress("project attachment failed!");
	        	return success;
	        }
	        
	        return success;
		}
		
		/*
		 * called by setupClientRoutine()
		 * checks whether client process exists (previous launch attempts) and kills it
		 */
		private void shutdownExisitingClient() {
			if(clientStarted){ //client has been started before, connection is broken
				publishProgress("shutdown of existing client");
				Boolean success = rpc.quit();
				Log.d(TAG+"-setupClient","graceful client shutdown returned " + success);
				if(!success) {
					clientProcess.destroy();
				}
				else{
		    		try {
		    			Thread.sleep(10000); //give client time for graceful shutdown
		    		}catch(Exception e){}
				}
			}
		}

		/*
		 * called by setupClientRoutine()
		 * copies the binaries of BOINC client from assets directory into storage space of this application
		 */
	    private Boolean installClient(Boolean overwrite){
	    	Boolean success = false;
	    	try {
	    		
	    		//end execution if no overwrite
	    		File boincClient = new File(clientPath+clientName);
	    		if (boincClient.exists() && !overwrite) {
	    			Log.d(TAG,"client exists, end setup of client");
	    			return true;
	    		}
	    		
	    		//delete old client
	    		if(boincClient.exists() && overwrite) {
	    			Log.d(TAG,"delete old client");
	    			boincClient.delete();
	    		}
	    		
	    		//check path and create it
	    		File clientDir = new File(clientPath);
	    		if(!clientDir.exists()) {
	    			clientDir.mkdir();
	    			clientDir.setWritable(true); 
	    		}
	    		
	    		//copy client from assets to clientPath
	    		InputStream assets = getApplicationContext().getAssets().open(clientName); 
	    		OutputStream data = new FileOutputStream(boincClient); 
	    		byte[] b = new byte [1024];
	    		int read; 
	    		while((read = assets.read(b)) != -1){ 
	    			data.write(b,0,read);
	    		}
	    		assets.close(); 
	    		data.flush(); 
	    		data.close();
	    		Log.d(TAG, "copy successful"); 
	    		boincClient.setExecutable(true);
	    		success = boincClient.canExecute();
	    		Log.d(TAG, "native client file in app space is executable: " + success);  
	    	}
	    	catch (IOException ioe) {  
	    		Log.d(TAG, "Exception: " + ioe.getMessage());
	    		Log.e(TAG, "IOException", ioe);
	    	}
	    	
	    	return success;
	    }
	    
	    /*
	    public Boolean setupApp(){
	    	Boolean success = false;
	    	Log.d(TAG, "setupApp");
	    	try {
	    		File app = new File(clientPath+"projects/isaac.ssl.berkeley.edu_test/uppercase_6.27_arm-android-linux-gnu");
	    		if(app.exists()) {
	    			app.delete();
	    			Log.d(TAG, "deleted");  
	    		}
	    		
	    		InputStream assets = ctx.getAssets().open("uc2"); 
	    		OutputStream data = new FileOutputStream(app); 
	    		byte[] b = new byte [1024];
	    		int read; 
	    		while((read = assets.read(b)) != -1){ 
	    			data.write(b,0,read);
	    		}
	    		assets.close(); 
	    		data.flush(); 
	    		data.close();
	    		Log.d(TAG, "copy successful"); 
	    		app.setExecutable(true);
	    		success = app.canExecute();
	    		Log.d(TAG, "native client file in app space is executable: " + success);  
	    	}
	    	catch (IOException ioe) {  
	    		Log.d(TAG, "Exception: " + ioe.getMessage());
	    		Log.e(TAG, "IOException", ioe);
	    	}
	    	return success;
	    }*/
	    
	    /*
	     * called by setupClientRoutine()
	     * executes the BOINC client using the Java Runtime exec method.
	     */
	    private Boolean runClient() {
	    	Boolean success = false;
	    	try { 
	        	//starts a new process which executes the BOINC client 
	        	clientProcess = Runtime.getRuntime().exec(clientPath + clientName);
	        	success = true;
	    	}
	    	catch (IOException ioe) {
	    		Log.d(TAG, "starting BOINC client failed with Exception: " + ioe.getMessage());
	    		Log.e(TAG, "IOException", ioe);
	    	}
	    	return success;
	    }
	    
	    /*
	     * called by setupClientRoutine() and reconnectClient()
	     * connects to running BOINC client.
	     */
	    private Boolean connect() {
	    	return rpc.open("127.0.0.1", 31416);
	    }
	    
	    /*
	     * called by setupClientRoutine() and reconnectClient()
	     * authorizes this application as valid RPC Manager by reading auth token from file and making RPC call.
	     */
	    private Boolean authorize() {
	    	File authFile = new File(clientPath+authFileName);
	    	StringBuffer fileData = new StringBuffer(100);
	    	char[] buf = new char[1024];
	    	int read = 0;
	    	try{
	    		BufferedReader br = new BufferedReader(new FileReader(authFile));
	    		while((read=br.read(buf)) != -1){
	    	    	String readData = String.valueOf(buf, 0, read);
	    	    	fileData.append(readData);
	    	    	buf = new char[1024];
	    	    }
	    		br.close();
	    	}
	    	catch (FileNotFoundException fnfe) {
	    		Log.e(TAG, "auth file not found",fnfe);
	    	}
	    	catch (IOException ioe) {
	    		Log.e(TAG, "ioexception",ioe);
	    	}

			String authKey = fileData.toString();
			Log.d(TAG, "authKey: " + authKey);
			
			//trigger client rpc
			return rpc.authorize(authKey); 
	    }
	    
	    /*
	     * called by setupClientRoutine() and reconnectClient()
	     * attaches the hard coded BOINC project to this client. RPC call does nothing, if client already attached to project.
	     */
	    private Boolean attachProject() {
	    	Log.d(TAG, "attachProject");
	    	//rpc.projectAttach("http://setiathome.berkeley.edu/", "cdd28f1747e714dc61cf731cb47f4205", "SETI@home");
	    	return rpc.projectAttach(getApplicationContext().getString(R.string.project_url), getApplicationContext().getString(R.string.project_auth_token), getApplicationContext().getString(R.string.project_name));
	    }
	    
	    /*
	     * called by setupClientRoutine() and reconnectClient()
	     * retrieves "attachProject" result in a polling loop
	     */
	    private Boolean attachProjectPoll() {
	    	Log.d(TAG, "attachProjectPoll");
	    	Boolean success = false;
	    	Integer loop = 0;
	    	while(!success && (maxDuration > loop)) {
		    	ProjectAttachReply reply = rpc.projectAttachPoll();
		    	Log.d(TAG,"ProjectAttachReply: " + reply.error_num);
		    	if(reply.error_num==0){
		    		Log.d(TAG, "Returned 0, project attached.");
		    		success = true;
		    	}
	    		loop++;
	    		try {
	    			Thread.sleep(1000); //sleep for one second
	    		}catch(Exception e){}
	    	}
	    	return success;
	    }
	}
}
