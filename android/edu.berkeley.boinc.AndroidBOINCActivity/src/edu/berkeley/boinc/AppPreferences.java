package edu.berkeley.boinc;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AppPreferences {
	
	private final String PREFS = "PREFS";
	private final String TAG = "AppPreferences";
	private SharedPreferences prefs;
	
	private String email;
	private String pwd;
	private String md5; // holds projects authentication token, as looked up during login
	private Boolean autostart;
	
	public void readPrefs (Context ctx) {
		if(prefs == null) {
			prefs = ctx.getSharedPreferences(PREFS, 0);
		}
		//second parameter of reading function is the initial value after installation of AndroidBOINC on new device.
		autostart = prefs.getBoolean("autostart", false);
		email = prefs.getString("email", "");
		pwd = prefs.getString("pwd", "");
		md5 = prefs.getString("md5", "");
		
		Log.d(TAG, "appPrefs read successful." + autostart + email + pwd + md5);
	}
	
	public void setEmail(String email) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("email", email);
		editor.commit();
		this.email = email;
	}
	
	public void setPwd(String pwd) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("pwd", pwd);
		editor.commit();
		this.pwd = pwd;
	}
	
	public void setMd5(String md5) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("md5", md5);
		editor.commit();
		this.md5 = md5;
	}
	
	public void setAutostart(Boolean as) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("autostart", as);
		editor.commit();
		this.autostart = as;
	}
	
	public String getEmail () {
		return this.email;
	}
	
	public String getPwd () {
		return this.pwd;
	}
	
	public String getMd5 () {
		return this.md5;
	}
	
	public Boolean getAutostart () {
		return this.autostart;
	}
}
