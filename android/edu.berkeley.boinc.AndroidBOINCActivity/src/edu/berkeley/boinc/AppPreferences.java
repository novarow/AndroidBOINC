package edu.berkeley.boinc;

import edu.berkeley.boinc.rpc.Md5;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AppPreferences {
	
	private final String PREFS = "PREFS";
	private final String TAG = "AppPreferences";
	private SharedPreferences prefs;
	
	private String email;
	private String pwd;
	private String md5;
	private Boolean autostart;
	
	public void readPrefs (Context ctx) {
		if(prefs == null) {
			prefs = ctx.getSharedPreferences(PREFS, 0);
		}
		autostart = prefs.getBoolean("autostart", false);
		email = prefs.getString(email, "test@test.asdf");
		pwd = prefs.getString(pwd, "lol");
		md5 = prefs.getString(md5, "");
		
		Log.d(TAG, "read successful.");
	}
	
	private String getPasswdHash(String passwd, String email_addr) {
		return Md5.hash(passwd+email_addr);
	}
	
	public void setEmail(String email) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("email", email);
		editor.commit();
		this.email = email;
	}
	
	public void setPwd(String pwd) {
		String md5 = getPasswdHash(pwd, email);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("pwd", pwd);
		editor.putString("md5", md5);
		editor.commit();
		this.pwd = pwd;
		this.md5 = md5;
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
