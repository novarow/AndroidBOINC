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

import edu.berkeley.boinc.IClientService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class RemoteClientService {
	
	private final String TAG = "ClientService";
	
	private Context ctx;
	private Activity act;
	
	private IClientService mIClientService;
	private Boolean isBound = false;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected (ComponentName className, IBinder service) {
			mIClientService = IClientService.Stub.asInterface(service);
			Log.d(TAG,"service connected.");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mIClientService = null;
			Log.d(TAG,"service disconnected.");
		}
	};
	
	private String authToken;

	public RemoteClientService(Context ctx, Activity act) {
		this.ctx = ctx;
		this.act = act;
		
        //TODO check whether clients exists (package edu.berkeley.boinc unique?)
		
		
        doBindService();
        //TODO check authorization status
		
		//TODO call visitor (activity method)
	}
	
	private void doBindService() {
		// Service has to be started "sticky" by the first instance that uses it. It causes the service to stay around, even when all Activities are destroyed (on purpose or by the system)
		// check whether service already started by BootReceiver is done within the service.
		Intent i = new Intent();
		i.setClassName("edu.berkeley.boinc", "edu.berkeley.boinc.ClientService");
		ctx.startService(i);
		
	    // Establish a connection with the service, onServiceConnected gets called when
		ctx.bindService(i, mConnection, 0);
	    isBound = true;
	}

	private void doUnbindService() {
	    if (isBound) {
	        // Detach existing connection.
	        ctx.unbindService(mConnection);
	        isBound = false;
	    }
	}
    
    public String getAuthToken() {
    	return "";
    }
}
