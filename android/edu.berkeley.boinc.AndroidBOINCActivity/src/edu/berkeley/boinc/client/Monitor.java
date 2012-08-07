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
import edu.berkeley.boinc.AppPreferences;
import edu.berkeley.boinc.R;
import edu.berkeley.boinc.definitions.CommonDefs;
import edu.berkeley.boinc.rpc.AccountIn;
import edu.berkeley.boinc.rpc.AccountOut;
import edu.berkeley.boinc.rpc.CcStatus;
import edu.berkeley.boinc.rpc.GlobalPreferences;
import edu.berkeley.boinc.rpc.Project;
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
	private static AppPreferences prefs; //hold the status of the app, controlled by AppPreferences
	
	public static ClientStatus getClientStatus() { //singleton pattern
		if (clientStatus == null) {
			clientStatus = new ClientStatus();
		}
		return clientStatus;
	}
	
	public static AppPreferences getAppPrefs() { //singleton pattern
		if (prefs == null) {
			prefs = new AppPreferences();
		}
		return prefs;
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
		
		getAppPrefs().readPrefs(this);
		
		getClientStatus().setCtx(this);
        
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
    
    public synchronized Integer lookupCredentials(String email, String pwd, Integer maxDuration) {
    	Integer retval = -1;
    	AccountIn credentials = new AccountIn();
    	credentials.email_addr = email;
    	credentials.passwd = pwd;
    	credentials.url = getString(R.string.project_url);
    	Boolean success = rpc.lookupAccount(credentials); //asynch
    	if(success) { //only continue if lookupAccount command did not fail
    		//get authentication token from lookupAccountPoll
    		Integer counter = 0;
    		Integer sleepDuration = 500; //in mili seconds
    		Integer maxLoops = maxDuration / sleepDuration;
    		Boolean loop = true;
    		while(loop && (counter < maxLoops)) {
    			loop = false;
    			try {
    				Thread.sleep(sleepDuration);
    			} catch (Exception e) {}
    			counter ++;
    			AccountOut auth = rpc.lookupAccountPoll();
    			if (auth.error_num == -204) {
    				loop = true; //no result yet, keep looping
    			}
    			else {
    				//final result ready
    				if(auth.error_num == 0) { //write usable results to AppPreferences
        				AppPreferences appPrefs = Monitor.getAppPrefs(); //get singleton appPrefs to save authToken
        				appPrefs.setEmail(email);
        				appPrefs.setPwd(pwd);
        				appPrefs.setMd5(auth.authenticator);
        				Log.d(TAG, "credentials verified");
    				}
    				retval = auth.error_num;
    			}
    		}
    	}
    	Log.d(TAG, "lookupCredentials returns " + retval);
    	return retval;
    }
    
    public synchronized Boolean attachProject(Integer maxDuration) {
    	Boolean success = false;

    	//get singleton appPrefs to read authToken
		AppPreferences appPrefs = Monitor.getAppPrefs(); 
		
		//make asynchronous call to attach project
    	success = rpc.projectAttach(getString(R.string.project_url), appPrefs.getMd5(), getString(R.string.project_name));
    	if(success) { //only continue if attach command did not fail
    		// verify success of projectAttach with poll function
    		success = false;
    		Integer counter = 0;
    		Integer sleepDuration = 500; //in mili seconds
    		Integer maxLoops = maxDuration / sleepDuration;
    		while(!success && (counter < maxLoops)) {
    			try {
    				Thread.sleep(sleepDuration);
    			} catch (Exception e) {}
    			counter ++;
    			ProjectAttachReply reply = rpc.projectAttachPoll();
    			Integer result = reply.error_num;
    			if(result == 0) {
    				success = true;
    	    		AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "project attached!");
    				// Client is attached to project, publish setupStatus 1 -> setup complete!
    				getClientStatus().setupStatus = 1;
    				getClientStatus().fire();
    			}
    		}
    	}
    	return success;
    }
    
	public synchronized void setRunMode(Integer mode) {
		Boolean success = rpc.setRunMode(mode,0);
		Log.d(TAG,"run mode set to " + mode + " returned " + success);
	}
	
	public synchronized GlobalPreferences getPrefs() {
		Log.d(TAG,"getPrefs");
		return rpc.getGlobalPrefsWorkingStruct();
	}
	
	public synchronized void setPrefs(GlobalPreferences globalPrefs) {
		rpc.setGlobalPrefsOverrideStruct(globalPrefs); //set new override settings
		rpc.readGlobalPrefsOverride(); //trigger reload of override settings
	}
	
	private final class ClientMonitorAsync extends AsyncTask<Integer,String,Boolean> {

		private final String TAG = "ClientMonitorAsync";
		
		private Boolean clientStarted = false;
		
		private final String clientName = getString(R.string.client_name); 
		private final String authFileName = getString(R.string.auth_file_name); 
		private String clientPath = getString(R.string.client_path); 
		
		private final Integer maxDuration = 30; //maximum polling duration
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.d(TAG+"-doInBackground","monitor started.");
			
			while(true) {
				Log.d(TAG+"-doInBackground","monitor loop...");
				
				
				if(!rpc.connectionAlive()) { //check whether connection is still alive
					//if connection is not working, either client has not been set up yet, user not attached to project, or client crashed.
					//in all cases trigger startUp again.
					if(!startUp()) { //synchronous execution in same thread -> blocks monitor until finished
						publishProgress("starting BOINC Client failed. Stop Monitor.");
						//cancel(true); 
						return false; //if startUp fails, stop monitor execution. restart has to be triggered by user.
					}
				}
				
				Log.d(TAG, "getCcStatus");
				edu.berkeley.boinc.rpc.CcStatus status = rpc.getCcStatus();
				Log.d(TAG, "getState");
				edu.berkeley.boinc.rpc.CcState state = rpc.getState();
				
				if((state!=null)&&(status!=null)) {
					Monitor.clientStatus.setClientStatus(status,state);
				} else {
					AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "client status connection problem");
				}
				
		        Intent clientStatus = new Intent();
		        clientStatus.setAction("edu.berkeley.boinc.clientstatus");
		        getApplicationContext().sendBroadcast(clientStatus);
	    		try {
	    			Thread.sleep(10000); //sleep
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
			Log.d(TAG+" - onPostExecute","monitor exit"); 
			Monitor.monitorActive = false;
		}
		

		
		private Boolean startUp() {
			
			//adapt client status and broadcast event
			getClientStatus().setupStatus = 0;
			getClientStatus().fire();
			
			//status control
			Boolean success = false;
			
			//try to connect, if client of another Manager lifecycle exists.
			Boolean connect =  connectClient();
			
			if(!connect) { //if connect did not work out, start new client instance and run connect attempts in loop
				Integer counter = 0;
				Integer max = 5; //max number of setup attempts
				Boolean setup = setupClient();
				if(!setup) {
					//setup failed, publish setupStatus 2 -> permanent error!
					getClientStatus().setupStatus = 2;
					getClientStatus().fire();
				}
				//try to connect to executed Client in loop
				while(!(connect=connectClient()) && (counter<max)) { //re-trys setting up the client several times, before giving up.
					AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "--- restart setup ---");
					counter++;
					try {
						Thread.sleep(5000);
					}catch (Exception e) {}
				}
				
				//connect still not succeeded. publish setupStatus 2 -> permanent error!
				if(!connect) {
					getClientStatus().setupStatus = 2;
					getClientStatus().fire();
				}
				
			}

			//client is connected.
			
			Boolean login = false;
			login = checkLogin();
			if(login) { // Client is attached to project, publish setupStatus 1 -> setup complete!
				getClientStatus().setupStatus = 1;
				getClientStatus().fire();
				//return true in order to start monitor
				success = true;
			} else { //client is not attached to project, publish setupStatus 3 -> wait for user input
				getClientStatus().setupStatus = 3;
				getClientStatus().fire();
			}
			
			//return success status, start monitor only if true.
			return success;
		}
		
		private Boolean connectClient() {
			Boolean success = false;
			
			publishProgress("connect client.");
			
	        success = connect();
	        if(success) {
	        	publishProgress("socket connection established (1/2)");
	        }
	        else {
	        	publishProgress("socket connection failed!");
	        	return success;
	        }
	        
	        //authorize
	        success = authorize();
	        if(success) {
	        	publishProgress("socket authorized. (2/2)");
	        }
	        else {
	        	publishProgress("socket authorization failed!");
	        	return success;
	        }
	        return success;
		}
		
		/*
		 * checks whether client is attached to project
		 */
		private Boolean checkLogin() {
			publishProgress("verify project login:");
			Boolean success = false;
			success = verifyProjectAttach();
	        if(success) {
	        	publishProgress("credentials verified. logged in!");
	        }
	        else {
	        	publishProgress("not logged in!");
	        	return success;
	        }
			return success;
		}
		
		/*
		private Boolean projectLogin() {
			publishProgress("project login...");
			Boolean success = false;

	        if(success) {
	        	publishProgress("credentials verified. logged in!");
	        }
	        else {
	        	publishProgress("not logged in!");
	        	return success;
	        }
			return success;
		}
		
		

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
		} */
		
		/*
		 * called by startUp()
		 * copies and executes the Client
		 */
		private Boolean setupClient() {
			//setup client
			Boolean success = false;
			
			shutdownExisitingClient();
	
			publishProgress("Client setup.");
			
	        success = installClient(true);
	        if(success) {
	        	publishProgress("installed. (1/2)");
	        }
	        else {
	        	publishProgress("installation failed!");
	        	return success;
	        }
	        
	        //run client
	        success = runClient();
	        if(success) {
	        	publishProgress("started. (2/2)");
	        	clientStarted = true; //mark that client process is running.
	        }
	        else {
	        	publishProgress("start failed!");
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
	    
	    private Boolean verifyProjectAttach() {
	    	Log.d(TAG, "verifyProjectAttach");
	    	Boolean success = false;
	    	ArrayList<Project> projects = rpc.getProjectStatus();
	    	Integer attachedProjectsAmount = projects.size();
	    	Log.d(TAG,"projects amount " + projects.size()); 
	    	if(attachedProjectsAmount > 0) { // there are attached projects
	    		success = true;
	    	}
	    	Log.d(TAG,"verifyProjectAttach about to return with " + success);
	    	return success;
	    }
	}
}
