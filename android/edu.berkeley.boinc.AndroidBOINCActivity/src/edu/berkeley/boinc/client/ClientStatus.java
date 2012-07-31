package edu.berkeley.boinc.client;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import edu.berkeley.boinc.rpc.CcStatus;

/*
 * Singleton that holds the client status data, as determined by the Monitor.
 * To get instance call Monitor.getClientStatus()
 */
public class ClientStatus {
	
	private final String TAG = "ClientStatus";

	private Context ctx; // application context in order to fire broadcast events
	
	//setup status, set by "setupClient" method of ClientMonitorAsync
	// 0 = client is in setup routine (default)
	// 1 = client is launched and available for RPC (connected and authorized)
	// 2 = client is in a permanent error state, there are not attempts to fix it (otherwise 0)
	public Integer setupStatus = 0;
	
	//running status, set by rpc parser of ClientMonitorAsync
	public Boolean computing = false; //client is computing BOINC task
	public Integer computingSuspendReason = 0; //reason why computing got suspended
	public Boolean network = false;
	public Integer networkSuspendReason = 0;
	public Boolean computingRunModeEnabled = true; //run mode of client (2 auto = true, 3 never = false)
	public Boolean networkRunModeEnabled = true; //run mode of client (2 auto = true, 3 never = false)
	
	
	
	//client status RPC
	private CcStatus status;
	//tasks status
	private ArrayList<edu.berkeley.boinc.rpc.Result> tasks; //holds information to task in client
	
	public ClientStatus(){
	}
	
	public synchronized void fire() {
		if(ctx!=null) {
	        Intent clientChanged = new Intent();
	        clientChanged.setAction("edu.berkeley.boinc.clientstatuschange");
			ctx.sendBroadcast(clientChanged,null);
		}else {
			Log.d(TAG,"cant fire, not context set!");
		}
	}
	
	public void setCtx(Context tctx) {
		this.ctx = tctx;
	}
	
}
