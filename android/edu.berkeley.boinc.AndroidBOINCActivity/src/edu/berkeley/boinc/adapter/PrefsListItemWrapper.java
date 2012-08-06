package edu.berkeley.boinc.adapter;

import android.content.Context;

public class PrefsListItemWrapper {
	
	public Context ctx;
	
	public Integer ID;
	
	public PrefsListItemWrapper (Context ctx, Integer ID) {
		this.ctx = ctx;
		this.ID = ID;
	}
}
