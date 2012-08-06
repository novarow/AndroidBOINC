package edu.berkeley.boinc.adapter;

import edu.berkeley.boinc.R;
import android.content.Context;
import android.util.Log;

public class PrefsListItemWrapperText extends PrefsListItemWrapper {
	
	private final String TAG = "PrefsListItemWrapperText";

	public String header = "";
	public String status;
	public String display;
	
	public PrefsListItemWrapperText(Context ctx, Integer ID, String status) {
		super(ctx, ID);
		this.status = status;
		mapStrings(ID);
	}
	
	private void mapStrings(Integer id) {
		switch (id) {
		case R.string.prefs_project_email_header:
			header = ctx.getString(R.string.prefs_project_email_header);
			display = status;
			break;
		case R.string.prefs_project_pwd_header:
			header = ctx.getString(R.string.prefs_project_pwd_header);
			display = "*********"; 
			break;
		default:
			Log.d(TAG, "map failed!");
		}
	}
}
