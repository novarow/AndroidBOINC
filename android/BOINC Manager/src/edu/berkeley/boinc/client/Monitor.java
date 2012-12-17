/*******************************************************************************
 * This file is part of BOINC.
 * http://boinc.berkeley.edu
 * Copyright (C) 2012 University of California
 * 
 * BOINC is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * BOINC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with BOINC.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.berkeley.boinc.client;

import java.util.ArrayList;

import edu.berkeley.boinc.AppPreferences;
import edu.berkeley.boinc.rpc.AccountIn;
import edu.berkeley.boinc.rpc.AccountOut;
import edu.berkeley.boinc.rpc.CcState;
import edu.berkeley.boinc.rpc.CcStatus;
import edu.berkeley.boinc.rpc.GlobalPreferences;
import edu.berkeley.boinc.rpc.Message;
import edu.berkeley.boinc.rpc.Project;
import edu.berkeley.boinc.rpc.ProjectAttachReply;
import edu.berkeley.boinc.rpc.RpcClient;
import edu.berkeley.boinc.rpc.Transfer;
import edu.berkeley.boinc.manager.R;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class Monitor extends Service{
	
	private final String TAG = "BOINC Client Monitor Service";
	
	private static ClientStatus clientStatus; //holds the status of the client as determined by the Monitor
	private static AppPreferences appPrefs; //hold the status of the app, controlled by AppPreferences
	
	private Boolean monitorStarted = false;
	
	public static ClientStatus getClientStatus() { //singleton pattern
		if (clientStatus == null) {
			clientStatus = new ClientStatus();
		}
		return clientStatus;
	}
	
	public static AppPreferences getAppPrefs() { //singleton pattern
		if (appPrefs == null) {
			appPrefs = new AppPreferences();
		}
		return appPrefs;
	}
	
	//private NotificationManager mNM;
	
	public static Boolean monitorActive = false;
	
	private RpcClient rpc = new RpcClient();

	/*
	 * returns this class, allows clients to access this service's functions and attributes.
	 */
	public class LocalBinder extends Binder {
        public Monitor getService() {
            return Monitor.this;
        }
    }
	
	@Override
    public void onCreate() {
		Log.d(TAG,"onCreate()");
		//initialization of components gets performed in onStartCommand
    }
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {	
    	Log.d(TAG, "onStartCommand");
		
		getAppPrefs().readPrefs(this); //create singleton AppPreferences prefs with current application context
		
		/*
		 * start monitor if either
		 * the user's preference autostart is enabled and the intent carries the autostart flag (intent from BootReceiver)
		 * or it is not an autostart-intent (not from BootReceiver) and the service hasnt been started yet
		 */
		Log.d(TAG, "monitorStarted: " + monitorStarted);
		if(!monitorStarted){
			monitorStarted = true;
			Log.d(TAG, "starting service sticky & setup start of monitor...");
			
			getClientStatus().setCtx(this);
	        
	        (new ClientMonitorAsync()).execute(new Integer[0]); //start monitor in new thread
	    	
	        Log.d(TAG, "asynchronous monitor started!");
		}
		else {
			Log.d(TAG, "asynchronous monitor NOT started!");
		}
		/*
		 * START_NOT_STICKY is now used and replaced START_STICKY in previous implementations.
		 * Lifecycle events - e.g. killing apps by calling their "onDestroy" methods, or killing an app in the task manager - does not effect the non-Dalvik code like the native BOINC Client.
		 * Therefore, it is not necessary for the service to get reactivated. When the user navigates back to the app (BOINC Manager), the service gets re-started from scratch.
		 * Con: After user navigates back, it takes some time until current Client status is present.
		 * Pro: Saves RAM/CPU.
		 * 
		 * For detailed service documentation see
		 * http://android-developers.blogspot.com.au/2010/02/service-api-changes-starting-with.html
		 */
		return START_NOT_STICKY;
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

	
    public void restartMonitor() {
    	if(Monitor.monitorActive) { //monitor is already active, launch cancelled
    		//AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "monitor active - restart cancelled");
    	}
    	else {
        	Log.d(TAG,"restart monitor");
        	(new ClientMonitorAsync()).execute(new Integer[0]);
    	}
    }
    
    public void quitClient() {
    	(new ShutdownClientAsync()).execute();
    }
    
    public void attachProject(String email, String pwd) {
		Log.d(TAG,"attachProject");
		String[] param = new String[2];
		param[0] = email;
		param[1] = pwd;
		(new ProjectAttachAsync()).execute(param);
    }
    
    
	public void setRunMode(Integer mode) {
		//execute in different thread, in order to avoid network communication in main thread and therefore ANR errors
		(new WriteClientRunModeAsync()).execute(mode);
	}
	
	public void setPrefs(GlobalPreferences globalPrefs) {
		//execute in different thread, in order to avoid network communication in main thread and therefore ANR errors
		(new WriteClientPrefsAsync()).execute(globalPrefs);
	}
	
	private final class ClientMonitorAsync extends AsyncTask<Integer,String,Boolean> {

		private final String TAG = "ClientMonitorAsync";
		private final Boolean showRpcCommands = false;
		
		private Integer refreshFrequency = 3000; //frequency of which the monitor updates client status via RPC, to often can cause reduced performance!
		
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
				
				if(showRpcCommands) Log.d(TAG, "getCcStatus");
				CcStatus status = rpc.getCcStatus();
				if(showRpcCommands) Log.d(TAG, "getState"); 
				CcState state = rpc.getState();
				//TODO getState is quite verbose, optimize!
				if(showRpcCommands) Log.d(TAG, "getTransers");
				ArrayList<Transfer>  transfers = rpc.getFileTransfers();
				if(showRpcCommands) Log.d(TAG, "getGlobalPrefsWorkingStruct");
				GlobalPreferences clientPrefs = rpc.getGlobalPrefsWorkingStruct();
				//TODO only when debug tab:
				//TODO room for improvements, dont retrieve complete list every time, but only new messages.
				Integer count = rpc.getMessageCount();
				//Log.d(TAG, "message count: " + count);
				if(showRpcCommands) Log.d(TAG, "getMessages, count: " + count);
				ArrayList<Message> msgs = rpc.getMessages(count - 25); //get the most recent 25 messages
				
				if((state!=null)&&(status!=null)&&(transfers!=null)&&(clientPrefs!=null)) {
					Monitor.clientStatus.setClientStatus(status,state,transfers,clientPrefs,msgs);
				} else {
					//AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "client status connection problem");
				}
				
		        Intent clientStatus = new Intent();
		        clientStatus.setAction("edu.berkeley.boinc.clientstatus");
		        getApplicationContext().sendBroadcast(clientStatus);
	    		try {
	    			Thread.sleep(refreshFrequency); //sleep
	    		}catch(Exception e){}
			}
		}

		@Override
		protected void onProgressUpdate(String... arg0) {
			Log.d(TAG+"-onProgressUpdate",arg0[0]);
			//AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, arg0[0]);
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
				
				//TODO bind to BOINCClient???
				
				//try to connect to Client in loop
				while(!(connect=connectClient()) && (counter<max)) { //re-trys setting up the client several times, before giving up.
					//AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "--- restart setup ---");
					counter++;
					try {
						Thread.sleep(5000);
					}catch (Exception e) {}
				}
				
				//connect still not succeeded. publish setupStatus 2 -> permanent error!
				if(!connect) {
					getClientStatus().setupStatus = 2;
					getClientStatus().fire();
					return false;
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
	    	//TODO retrieve authKey
	    	
			//Log.d(TAG, "authKey: " + authKey);
			
			//trigger client rpc
			//return rpc.authorize(authKey); 
	    	return true;
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
	
	private final class ProjectAttachAsync extends AsyncTask<String,String,Boolean> {

		private final String TAG = "ProjectAttachAsync";
		
		private final Integer maxDuration = 3000; //maximum polling duration
		
		private String email;
		private String pwd;
		
		@Override
		protected void onPreExecute() {
			Log.d(TAG+"-onPreExecute","publish setupStatus 0"); //client is in setup routine... again.
			getClientStatus().setupStatus = 0;
			getClientStatus().fire();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			this.email = params[0];
			this.pwd = params[1];
			Log.d(TAG+"-doInBackground","login started with: " + email + "-" + pwd);
			
			Integer retval = lookupCredentials();
			Boolean success = false;
			switch (retval) {
			case 0:
				Log.d(TAG, "verified successful");
				success = true;
				break;
			case -206:
				Log.d(TAG, "password incorrect!");
				publishProgress("Password Incorrect!");
				break;
			case -136:
				Log.d(TAG, "eMail incorrect!");
				publishProgress("eMail Incorrect!");
				break;
			case -113:
				Log.d(TAG, "No internet connection!");
				publishProgress("No internet connection!");
				break;
			default:
				Log.d(TAG, "unkown error occured!");
				publishProgress("Unknown Error!");
				break;
			}
			
			if(!success) {
				Log.d(TAG, "verification failed - exit");
				return false;
			}
			
			Boolean attach = attachProject(); //tries credentials stored in AppPreferences singleton, terminates after 3000 ms in order to prevent "ANR application not responding" dialog
			if(attach) {
				publishProgress("Successful.");
			}
			return attach;
		}

		@Override
		protected void onProgressUpdate(String... arg0) {
			Log.d(TAG+"-onProgressUpdate",arg0[0]);
			//AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, arg0[0]);
			Toast toast = Toast.makeText(getApplicationContext(), arg0[0], Toast.LENGTH_SHORT);
			toast.show();
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			if(success) { //login successful
				Log.d(TAG,"login successful, restart monitor");
				restartMonitor();
			} else { //login failed
				Log.d(TAG,"login failed, publish");
				getClientStatus().setupStatus = 3;
				getClientStatus().fire();
			}
			
		}
		
		private Integer lookupCredentials() {
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
	    			if(auth==null) {
	    				return -1;
	    			}
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
	    
	    private Boolean attachProject() {
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
	    	    		//AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "project attached!");
	    				// Client is attached to project, publish setupStatus 1 -> setup complete!
	    				getClientStatus().setupStatus = 1;
	    				getClientStatus().fire();
	    			}
	    		}
	    	}
	    	return success;
	    }
	}

	private final class WriteClientPrefsAsync extends AsyncTask<GlobalPreferences,Void,Boolean> {

		private final String TAG = "WriteClientPrefsAsync";
		@Override
		protected Boolean doInBackground(GlobalPreferences... params) {
			Log.d(TAG, "doInBackground");
			Boolean retval1 = rpc.setGlobalPrefsOverrideStruct(params[0]); //set new override settings
			Boolean retval2 = rpc.readGlobalPrefsOverride(); //trigger reload of override settings
			if(retval1 && retval2) {
				Log.d(TAG, "successful.");
				return true;
			}
			return false;
		}
	}
	
	private final class WriteClientRunModeAsync extends AsyncTask<Integer,Void,Boolean> {

		private final String TAG = "WriteClientRunModeAsync";
		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.d(TAG, "doInBackground");
			Boolean success = rpc.setRunMode(params[0],0);
			Log.d(TAG,"run mode set to " + params[0] + " returned " + success);
			return success;
		}
	}
	
	private final class ShutdownClientAsync extends AsyncTask<Void,Void,Boolean> {

		private final String TAG = "ShutdownClientAsync";
		@Override
		protected Boolean doInBackground(Void... params) {
			Log.d(TAG, "doInBackground");
	    	Boolean success = rpc.quit();
	    	//AndroidBOINCActivity.logMessage(getApplicationContext(), TAG, "graceful shutdown returned " + success);
			if(!success) {
				//TODO quit BOINCClient service somehow?
			}
			return success;
		}
	}
}
