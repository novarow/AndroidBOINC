package edu.berkeley.boinc;

import java.util.ArrayList;

import edu.berkeley.boinc.adapter.PrefsListAdapter;
import edu.berkeley.boinc.adapter.PrefsListItemWrapper;
import edu.berkeley.boinc.adapter.PrefsListItemWrapperBool;
import edu.berkeley.boinc.adapter.PrefsListItemWrapperDouble;
import edu.berkeley.boinc.adapter.PrefsListItemWrapperText;
import edu.berkeley.boinc.adapter.TasksListAdapter;
import edu.berkeley.boinc.client.ClientStatus;
import edu.berkeley.boinc.client.Monitor;
import edu.berkeley.boinc.rpc.Result;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class TasksActivity extends Activity {
	
	private final String TAG = "TasksActivity";
	
	private ClientStatus status; //client status, new information gets parsed by monitor, changes notified by "clientstatus" broadcast. read Result from here, to get information about tasks.

	private ListView lv;
	private TasksListAdapter listAdapter;
	
	private ArrayList<Result> data = new ArrayList<Result>(); //Adapter for list data

	private BroadcastReceiver mClientStatusChangeRec = new BroadcastReceiver() {
		
		private final String TAG = "TasksActivity-Receiver";
		@Override
		public void onReceive(Context context,Intent intent) {
			Log.d(TAG,"onReceive");
			loadData(); // refresh list view
		}
	};
	private IntentFilter ifcsc = new IntentFilter("edu.berkeley.boinc.clientstatuschange");
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasks_layout); 
		
		//get singleton client status from monitor
		status = Monitor.getClientStatus();
		
		//setup list and adapter
		ArrayList<Result> tmp = status.getTasks();
		if(tmp!=null) { //can be null before first monitor status cycle (e.g. when not logged in or during startup)
			data = tmp;
		}
		lv = (ListView) findViewById(R.id.listview);
        listAdapter = new TasksListAdapter(TasksActivity.this,R.id.listview,data);
        lv.setAdapter(listAdapter);
        
        Log.d(TAG,"onCreate");
	}
	
	public void onResume() {
		super.onResume();
		//register noisy clientStatusChangeReceiver here, so only active when Activity is visible
		Log.d(TAG+"-onResume","register receiver");
		registerReceiver(mClientStatusChangeRec,ifcsc);
		loadData();
	}
	
	public void onPause() {
		//unregister receiver, so there are not multiple intents flying in
		Log.d(TAG+"-onPause","remove receiver");
		unregisterReceiver(mClientStatusChangeRec);
		super.onPause();
	}
	
	
	private void loadData() {
		Log.d(TAG,"loadData");
		listAdapter.notifyDataSetChanged(); //force list adapter to refresh
	}

}
