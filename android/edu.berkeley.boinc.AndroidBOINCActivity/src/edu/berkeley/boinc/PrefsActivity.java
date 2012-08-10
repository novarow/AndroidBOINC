package edu.berkeley.boinc;

import java.util.ArrayList;

import edu.berkeley.boinc.adapter.PrefsListAdapter;
import edu.berkeley.boinc.adapter.PrefsListItemWrapper;
import edu.berkeley.boinc.adapter.PrefsListItemWrapperBool;
import edu.berkeley.boinc.adapter.PrefsListItemWrapperDouble;
import edu.berkeley.boinc.adapter.PrefsListItemWrapperText;
import edu.berkeley.boinc.client.Monitor;
import edu.berkeley.boinc.rpc.GlobalPreferences;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class PrefsActivity extends Activity implements OnClickListener {
	
	private final String TAG = "PrefsActivity";
	
	private Monitor monitor;
	private Boolean mIsBound = false;
	
	private ListView lv;
	private PrefsListAdapter listAdapter;
	
	private ArrayList<PrefsListItemWrapper> data = new ArrayList<PrefsListItemWrapper>(); //Adapter for list data
	private GlobalPreferences clientPrefs = null; //preferences of the client, read on every onResume via RPC
	private AppPreferences appPrefs = null; //Android specific preferences, singleton of monitor
	
	private Dialog dialog; //Dialog for input on non-Bool preferences
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doBindService();
	}
	
	public void onResume() {
		super.onResume();
		//gets called every time Activity comes to front, therefore also after onCreate
		if(clientPrefs == null) { //no data available, first call
			setContentView(R.layout.prefs_layout_loading);
		}
		if(mIsBound) { //update prefs in case Monitor is already bound. use case: user navigates back to prefs tab -> refresh
			loadSettings();
		}
	}
	
	/*
	 * Service binding part
	 * only necessary, when function on monitor instance has to be called
	 * currently in Prefs- and DebugActivity
	 * 
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(TAG,"onServiceConnected");
	        monitor = ((Monitor.LocalBinder)service).getService();
		    mIsBound = true;
			appPrefs = Monitor.getAppPrefs();
		    Log.d(TAG, "prefs available");
		    initPrefsLayout();
		    
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        monitor = null;
	        mIsBound = false;
	    }
	};

	private void doBindService() {
		if(!mIsBound) {
			getApplicationContext().bindService(new Intent(this, Monitor.class), mConnection, 0); //calling within Tab needs getApplicationContext() for bindService to work!
		}
	}

	private void doUnbindService() {
	    if (mIsBound) {
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}
	
	private void readPrefs() {
		clientPrefs = monitor.getPrefs(); //read prefs from client via rpc
	}
	
	private void initPrefsLayout() {

		readPrefs(); //update preferences
		
		setContentView(R.layout.prefs_layout);
		lv = (ListView) findViewById(R.id.listview);
        listAdapter = new PrefsListAdapter(PrefsActivity.this,R.id.listview,data);
        lv.setAdapter(listAdapter);

		//parse app prefs
		data.add(0, new PrefsListItemWrapperText(this,R.string.prefs_project_email_header,appPrefs.getEmail()));
		data.add(1, new PrefsListItemWrapperText(this,R.string.prefs_project_pwd_header,appPrefs.getPwd()));
		data.add(2, new PrefsListItemWrapperBool(this,R.string.prefs_autostart_header,appPrefs.getAutostart()));
		//parse client prefs
		data.add(3, new PrefsListItemWrapperBool(this,R.string.prefs_run_on_battery_header,clientPrefs.run_on_batteries));
		data.add(4, new PrefsListItemWrapperBool(this,R.string.prefs_network_wifi_only_header,clientPrefs.network_wifi_only));
		data.add(5, new PrefsListItemWrapperDouble(this,R.string.prefs_disk_max_pct_header,clientPrefs.disk_max_used_pct));
		data.add(6, new PrefsListItemWrapperDouble(this,R.string.prefs_disk_min_free_gb_header,clientPrefs.disk_min_free_gb));
		data.add(7, new PrefsListItemWrapperDouble(this,R.string.prefs_daily_xfer_limit_mb_header,clientPrefs.daily_xfer_limit_mb));
	}
	
	private void loadSettings() {
		readPrefs(); //update preferences
		
		((PrefsListItemWrapperText)data.get(0)).status = appPrefs.getEmail();
		((PrefsListItemWrapperText)data.get(1)).status = appPrefs.getPwd();
		((PrefsListItemWrapperBool)data.get(2)).setStatus(appPrefs.getAutostart());
		((PrefsListItemWrapperBool)data.get(3)).setStatus(clientPrefs.run_on_batteries);
		((PrefsListItemWrapperBool)data.get(4)).setStatus(clientPrefs.network_wifi_only); 
		((PrefsListItemWrapperDouble)data.get(5)).status = clientPrefs.disk_max_used_pct;
		((PrefsListItemWrapperDouble)data.get(6)).status = clientPrefs.disk_min_free_gb;
		((PrefsListItemWrapperDouble)data.get(7)).status = clientPrefs.daily_xfer_limit_mb;
		
		listAdapter.notifyDataSetChanged(); //force list adapter to refresh
	}
	
	/*
	 * Gets triggered by change of checkboxes. (Boolean prefs)
	 */
	public void onCbClick (View view) {
		Log.d(TAG,"onCbClick");
		Integer ID = (Integer) view.getTag();
		CheckBox source = (CheckBox) view;
		Boolean isSet = source.isChecked();
		
		switch (ID) {
		case R.string.prefs_autostart_header: //app pref
			appPrefs.setAutostart(isSet);
			break;
		case R.string.prefs_run_on_battery_header: //client pref
			clientPrefs.run_on_batteries = isSet;
			monitor.setPrefs(clientPrefs);
			break;
		case R.string.prefs_network_wifi_only_header: //client pref
			clientPrefs.network_wifi_only = isSet;
			monitor.setPrefs(clientPrefs);
			break;
		}
		loadSettings();
	}
	
	public void onItemClick (View view) {
		Integer ID = (Integer) view.getTag();
		Log.d(TAG,"onItemClick " + ID);
		showDialog(ID);
	}
	
	/*
	 * Gets called when showDialog is triggered
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		dialog = new Dialog(this); //instance new dialog
		dialog.setContentView(R.layout.prefs_layout_dialog);
		String title = "Enter new ";
		Button button = (Button) dialog.findViewById(R.id.buttonPrefSubmit);
		button.setOnClickListener(this);
		EditText edit = (EditText) dialog.findViewById(R.id.Input);
		//customize:
		switch (id) {
		case R.string.prefs_project_email_header:
			title += "eMail address";
			edit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			break;
		case R.string.prefs_project_pwd_header:
			title += "password";
			edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
			button.setText("Login!");
			break;
		case R.string.prefs_disk_max_pct_header:
			title += "disk space limit (%)";
			edit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
			break;
		case R.string.prefs_disk_min_free_gb_header:
			title += "free disk space (GB)";
			edit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
			break;
		case R.string.prefs_daily_xfer_limit_mb_header:
			title += "transfer limit (MB)";
			edit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
			break;
		default:
			Log.d(TAG,"onCreateDialog, couldnt match ID");
			break;
			
		}
		dialog.setTitle(title + ":");
		button.setId(id); //set input id, for evaluation in onClick
		return dialog;
	}

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    Log.d(TAG,"onDestroy()");
	    doUnbindService();
	}

	/*
	 * Gets called when Dialog's confirm button is clicked
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG,"dialogDismiss");
		Button button = (Button) v;
		Integer id = button.getId();
		EditText input = (EditText) dialog.findViewById(R.id.Input);
		String tmp = input.getText().toString();
		Log.d(TAG,"onClick with input " + tmp);
		try {
			switch (id) {
			case R.string.prefs_project_email_header:
				//TODO cant be reached, clickable turned of in PrefsListAdapter
				appPrefs.setEmail(tmp);
				break;
			case R.string.prefs_project_pwd_header:
				appPrefs.setPwd(tmp);
				//TODO logout (detach)
				//TODO login (attach)
				//TODO cant be reached, clickable turned of in PrefsListAdapter
				break;
			case R.string.prefs_disk_max_pct_header:
				tmp=tmp.replaceAll(",","."); //replace e.g. European decimal seperator "," by "."
				clientPrefs.disk_max_used_pct = Double.parseDouble(tmp);
				monitor.setPrefs(clientPrefs);
				break;
			case R.string.prefs_disk_min_free_gb_header:
				tmp=tmp.replaceAll(",","."); //replace e.g. European decimal seperator "," by "."
				clientPrefs.disk_max_used_gb = Double.parseDouble(tmp);
				monitor.setPrefs(clientPrefs);
				break;
			case R.string.prefs_daily_xfer_limit_mb_header:
				tmp=tmp.replaceAll(",","."); //replace e.g. European decimal seperator "," by "."
				clientPrefs.daily_xfer_limit_mb = Double.parseDouble(tmp);
				monitor.setPrefs(clientPrefs);
				break;
			default:
				Log.d(TAG,"onClick (dialog submit button), couldnt match ID");
				break;
			
			}
			dialog.dismiss();
			loadSettings();
		} catch (Exception e) { //e.g. when parsing fails
			Log.e(TAG, "Exception in dialog onClick", e);
			Toast toast = Toast.makeText(getApplicationContext(), "wrong format!", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	


}
