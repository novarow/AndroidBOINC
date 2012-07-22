package edu.berkeley.boinc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import edu.berkeley.boinc.definitions.CommonDefs;
import edu.berkeley.boinc.rpc.CcStatus;
import edu.berkeley.boinc.rpc.ProjectAttachReply;
import edu.berkeley.boinc.rpc.RpcClient;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class BOINClient {
	
	private final String TAG = "BOINClient";
	private Context ctx;
	
	//---------------------client status
	//Client status flags
	public Boolean settingUp = false; //client is in set up routine
	public Boolean executing = false; //client process is executing (could still be initializing)
	public Boolean launched = false; //client completed setup and is now available for RPCs
	public Boolean broken = false; //communication to client is broken
	public Boolean computing = false; //client is computing BOINC task
	public Integer suspendReason = 0; //reason why computing got suspended
	public Boolean computingEnabled = false; //run mode of client (2 auto = true, 3 never = false)
	//client status RPC
	private CcStatus status;
	//tasks status
	private ArrayList<edu.berkeley.boinc.rpc.Result> tasks; //holds information to task in client
	//---------------------end client status
	

	private Process client;
	
	private RpcClient rpc = new RpcClient();
	
	public BOINClient(Context ctx) {
		this.ctx = ctx;
	}
	
	public void setup(){
		(new ClientLaunchAsync(ctx)).execute(new Integer[0]);
	}
	
	public void shutdown(){
		Boolean success = rpc.quit();
		Log.d(TAG,"graceful client shutdown returned " + success);
		if(!success) {
			client.destroy();
		}
	}
	
	public void setRunMode(Integer mode) {
		Boolean success = rpc.setRunMode(mode,0);
		Log.d(TAG,"run mode set to " + mode + " returned " + success);
	}
	
	public synchronized void setClientStatus(ArrayList<edu.berkeley.boinc.rpc.Result> newTasks, edu.berkeley.boinc.rpc.CcStatus newStatus) { //synchronized wrapper for client status complex vars
		this.tasks = newTasks;
		this.status = newStatus;
	}
	public synchronized ArrayList<edu.berkeley.boinc.rpc.Result> getTasks() {
		return this.tasks;
	}
	public synchronized edu.berkeley.boinc.rpc.CcStatus getStatus() {
		return this.status;
	}
	
	public void startMonitor() {
		Log.d(TAG,"startMonitor");
		(new ClientMonitorAsync(ctx)).execute(new Integer[0]);
	}
	
	private final class ClientMonitorAsync extends AsyncTask<Integer,String,Boolean> {

		private final String TAG = "BOINClient monitor";
		private Context ctx;

		public ClientMonitorAsync(Context ctx) {
			this.ctx = ctx;
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			Log.d(TAG+"-doInBackground","monitor started.");
			
			while(true) {
				Log.d(TAG+"-doInBackground","monitor loop...");
				
				/*
				if(!rpc.connectionAlive()) { //check whether connection is still alive
					//runs cc_status on RPC?
					return false;
				}
				*/
				//Log.d(TAG, "getHostInfo");
				//rpc.getHostInfo();
				//Log.d(TAG, "getProjectStatus");
				//rpc.getProjectStatus();
				//Log.d(TAG, "getActiveResults");
				//rpc.getActiveResults();  
				
				Log.d(TAG, "getResults");
				ArrayList<edu.berkeley.boinc.rpc.Result> tasks = rpc.getResults();
				Log.d(TAG, "getCcStatus");
				edu.berkeley.boinc.rpc.CcStatus status = rpc.getCcStatus();
				if((tasks==null)||(status==null)) { //connection problem
					return false;
				} else {
					Log.d(TAG,"task_mode: " + status.task_mode + " task_suspend_reason: " + status.task_suspend_reason);
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
					}
					setClientStatus(tasks,status);
			        Intent clientStatus = new Intent();
			        clientStatus.setAction("edu.berkeley.boinc.clientstatus");
					ctx.sendBroadcast(clientStatus);
				}
	    		try {
	    			Thread.sleep(10000); //sleep
	    		}catch(Exception e){}
			}
		}

		@Override
		protected void onProgressUpdate(String... arg0) {
			Log.d(TAG+"-onProgressUpdate",arg0[0]);
			AndroidBOINCActivity.logMessage(ctx, TAG, arg0[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			AndroidBOINCActivity.logMessage(ctx, TAG, "client connection broken!");
			Log.d(TAG+" - onPostExecute","client connection broken!");
			
			//kill client process
			try {
				shutdown();
			}catch(Exception e) {}
			
	        Intent clientError = new Intent();
	        clientError.setAction("edu.berkeley.boinc.clienterror");
			ctx.sendOrderedBroadcast(clientError,null);
		}
	}
	
	private final class ClientLaunchAsync extends AsyncTask<Integer,String,Boolean> {

		private final String TAG = "BOINClient setup";
		
		private final String clientName = "boinc_client"; 
		private final String authFileName = "gui_rpc_auth.cfg";
		private String clientPath = "/data/data/edu.berkeley.boinc/client/";
		private Context ctx;
		
		private final Integer maxDuration = 30; //maximum polling duration
		
		public ClientLaunchAsync(Context ctx) {
			this.ctx = ctx;
		}
		
		@Override
		protected void onPreExecute() {
			
			//publish event, that client is launching
	        Intent clientLaunch = new Intent();
	        clientLaunch.setAction("edu.berkeley.boinc.clientlaunch");
	        clientLaunch.putExtra("finished", false);
			ctx.sendOrderedBroadcast(clientLaunch,null);
		}
		
		@Override
		protected Boolean doInBackground(Integer...arg0) {
			
			Boolean manualAppSetup = false; //debug!
			
			Boolean success = false;
			
	        //setup client
	        success = setupClient(true);
	        if(success) {
	        	publishProgress("installed. (1/5)");
	        }
	        else {
	        	publishProgress("installation failed!");
	        	return success;
	        }
			
	        //setup app
	        if(manualAppSetup){
		        success = setupApp();
		        if(success) {
		        	publishProgress("app installed. (1.5/5)");
		        }
		        else {
		        	publishProgress("app installation failed!");
		        	return success;
		        }
	        }
	        
	        //run client
	        success = runClient();
	        if(success) {
	        	publishProgress("started. (2/5)");
	        	executing = true;
	        }
	        else {
	        	publishProgress("start of client failed!");
	        	return success;
	        }
	        
	        //connect in loop
	        success = connect();
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
	        
	        //finished - return value goes to onPostExecute
			return success;
		}
		
		@Override
		protected void onProgressUpdate(String... arg0) {
			Log.d(TAG+"-onProgressUpdate",arg0[0]);
			AndroidBOINCActivity.logMessage(ctx, TAG, arg0[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			AndroidBOINCActivity.logMessage(ctx, TAG, "finished " + success);
			Log.d(TAG+" - onPostExecute","success: " + success);
			
			if(success) { //if setup successful, publish with clientlaunch event
		        Intent clientReady = new Intent();
		        clientReady.setAction("edu.berkeley.boinc.clientlaunch");
		        clientReady.putExtra("finished", true);
				ctx.sendOrderedBroadcast(clientReady,null);
			}
			else { //setup failed, publish error
		        Intent clientError = new Intent();
		        clientError.setAction("edu.berkeley.boinc.clienterror");
				ctx.sendOrderedBroadcast(clientError,null);
			}
			
			//mark end of set up process
			settingUp = false;
			
		}
		
	    private Boolean setupClient(Boolean overwrite){
	    	Boolean success = false;
	    	publishProgress("starting...");
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
	    		InputStream assets = ctx.getAssets().open(clientName); 
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
	    }
	    
	    private Boolean runClient() {
	    	Boolean success = false;
	    	try { 
	        	//starts a new process which executes the BOINC client 
	        	client = Runtime.getRuntime().exec(clientPath + clientName);
	        	success = true;
	    	}
	    	catch (IOException ioe) {
	    		Log.d(TAG, "starting BOINC client failed with Exception: " + ioe.getMessage());
	    		Log.e(TAG, "IOException", ioe);
	    	}
	    	return success;
	    }
	    
	    private Boolean connect() {
    		try {
    			Thread.sleep(5000); //sleep for five seconds
    		}catch(Exception e){}
	    	Boolean success = false;
	    	Integer loop = 0;
	    	while(!success && (6 > loop)) {
	    		success = rpc.open("127.0.0.1", 31416);
	    		loop++;
	    		try {
	    			Thread.sleep(5000); //sleep for five seconds
	    		}catch(Exception e){}
	    	}
	    	return success;
	    }
	    
	    private Boolean authorize() {
	    	//read authentication key from file
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
	    
	    private Boolean attachProject() {
	    	Log.d(TAG, "attachProject");
	    	//rpc.projectAttach("http://setiathome.berkeley.edu/", "cdd28f1747e714dc61cf731cb47f4205", "SETI@home");
	    	return rpc.projectAttach(ctx.getString(R.string.project_url), ctx.getString(R.string.project_auth_token), ctx.getString(R.string.project_name));
	    }
	    
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

    
    
    public void shutdown(View view) {
    	Log.d(TAG, "shutdown");
    	executing = false;
    	launched = false;
    	computing = false;
    	client.destroy();
    }

}
