package edu.berkeley.boinc.adapter;

import edu.berkeley.boinc.R;
import android.content.Context;
import android.util.Log;

public class PrefsListItemWrapperBool extends PrefsListItemWrapper {
	
	private final String TAG = "PrefsListItemWrapperBool";

	public String header = "";
	private String status_true = "";
	private String status_false = "";
	public String status_text;
	private Boolean status;
	
	public PrefsListItemWrapperBool(Context ctx, Integer ID, Boolean status) {
		super(ctx, ID);
		this.status = status;
		mapStrings(ID);
		setStatusMessage();
	}
	
	private void mapStrings(Integer id) {
		switch (id) {
		case R.string.prefs_autostart_header:
			header = ctx.getString(R.string.prefs_autostart_header);
			status_true = ctx.getString(R.string.prefs_autostart_true);
			status_false = ctx.getString(R.string.prefs_autostart_false);
			break;
		case R.string.prefs_run_on_battery_header:
			header = ctx.getString(R.string.prefs_run_on_battery_header);
			status_true = ctx.getString(R.string.prefs_run_on_battery_true);
			status_false = ctx.getString(R.string.prefs_run_on_battery_false);
			break;
		case R.string.prefs_network_wifi_only_header:
			header = ctx.getString(R.string.prefs_network_wifi_only_header);
			status_true = ctx.getString(R.string.prefs_network_wifi_only_true);
			status_false = ctx.getString(R.string.prefs_network_wifi_only_false);
			break;
		default:
			Log.d(TAG, "map failed!");
		}
	}
	
	public void setStatus(Boolean newStatus) {
		this.status = newStatus;
		setStatusMessage();
	}
	
	public Boolean getStatus() {
		return this.status;
	}
	
	private void setStatusMessage() {
		if(status) {
			status_text = status_true;
		} else {
			status_text = status_false;
		}
		
	}
}
