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

package edu.berkeley.boinc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ClientService extends Service{
	
	private final String TAG = "BOINC ClientService";
	
	private Boolean started = false;
	
	private String clientName; 
	private String authFileName; 
	private String clientPath; 
	
	private Process clientProcess;
	//private NotificationManager mNM;
	
	/*
	 * returns this class, allows clients to access this service's AIDL methods.
	 */
	private final IClientService.Stub mBinder = new IClientService.Stub() {
		@Override
		public String getAuthToken(List<String> URLs, String packageName) throws RemoteException {
			Log.d(TAG,"getAuthToken");
			// TODO store URLs and packageName, implement uninstall->detach
			return readAuthKey();
		}
	};
	
    @Override
    public IBinder onBind(Intent intent) {
    	// return the AIDL interface stub.
        return mBinder;
    }
	
	@Override
    public void onCreate() {
		Log.d(TAG,"onCreate()");
		//initialization of components gets performed in onStartCommand
		clientName = getResources().getString(R.string.client_name); 
		authFileName = getResources().getString(R.string.auth_file_name); 
		clientPath = getResources().getString(R.string.client_path); 
    }
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {	
    	//this gets called when by BootReceiver calls startService(intent)
    	Log.d(TAG, "onStartCommand");
    	
    	Boolean autostart = false;
    	try {
    		autostart = intent.getBooleanExtra("autostart", false); //if true, received intent is for autostart and got fired by the BootReceiver on start up.
    	}
    	catch (NullPointerException e) { // occurs, when onStartCommand is called with a null intent. Occurs on re-start, if START_STICKY is used. 
    		Log.d(TAG,"NullPointerException, intent flags: " + flags);
    	}
		
    	/* start client */
		Log.d(TAG, "values: intent-autostart " + autostart + " - started " + started);
		if(!started) {
			started = setupClient();
			Log.d(TAG, "setupClient returned: " + started);
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
     * Show a notification while service is running.
     
    private void showNotification() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.boinc, getString(R.string.autostart_notification_header), System.currentTimeMillis());

        // Set current view for notification panel
        notification.setLatestEventInfo(getApplicationContext(), getString(R.string.autostart_notification_header), getString(R.string.autostart_notification_text), null);

        // Send notification
        mNM.notify(1234, notification);
    }*/
    
    private Boolean setupClient() {
		//setup client
		Boolean success = false;
		
		
        success = installClient(false);
        if(success) {
        	Log.d(TAG,"installed");
        }
        else {
        	Log.d(TAG,"installation failed");
        	return success;
        }
        
        //run client
        success = runClient();
        if(success) {
        	Log.d(TAG,"started");
			//showNotification();
        }
        else {
        	Log.d(TAG,"start failed");
        	return success;
        }
        return success;
	}
    
    /*
	 * called by setupClient()
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
     * called by setupClient()
     * executes the BOINC client using the Java Runtime.exec method
     */
    private Boolean runClient() {
    	Boolean success = false;
    	try { 
        	//starts a new process which executes the BOINC client 
        	clientProcess = Runtime.getRuntime().exec(clientPath + clientName, null, new File(clientPath));
        	success = true;
    	}
    	catch (IOException ioe) {
    		Log.d(TAG, "starting BOINC client failed with Exception: " + ioe.getMessage());
    		Log.e(TAG, "IOException", ioe);
    	}
    	return success;
    }
    
    /*
     * called by getAuthToken()
     * reads the authentication and returns String
     */
    private String readAuthKey() {
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
	
		return authKey;
    }
}
