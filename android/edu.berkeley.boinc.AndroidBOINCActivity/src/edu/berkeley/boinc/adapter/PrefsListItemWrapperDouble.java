package edu.berkeley.boinc.adapter;

import edu.berkeley.boinc.R;
import android.content.Context;
import android.util.Log;

public class PrefsListItemWrapperDouble extends PrefsListItemWrapper {
	
	private final String TAG = "PrefsListItemWrapperDouble";

	public String header = "";
	public Double status;
	
	public PrefsListItemWrapperDouble(Context ctx, Integer ID, Double status) {
		super(ctx, ID);
		this.status = status;
		mapStrings(ID);
	}
	
	private void mapStrings(Integer id) {
		switch (id) {
		case R.string.prefs_disk_max_pct_header:
			header = ctx.getString(R.string.prefs_disk_max_pct_header);
			break;
		case R.string.prefs_disk_min_free_gb_header:
			header = ctx.getString(R.string.prefs_disk_min_free_gb_header);
			break;
		case R.string.prefs_daily_xfer_limit_mb_header:
			header = ctx.getString(R.string.prefs_daily_xfer_limit_mb_header);
			break;
		default:
			Log.d(TAG, "map failed!");
		}
	}
}
