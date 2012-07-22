package edu.berkeley.boinc;

import java.util.ListIterator;

import edu.berkeley.boinc.receiver.LogReceiver;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class DebugActivity extends Activity {
	
	private final String TAG = "DebugActivity";
	
	private TextView logText;
	private Integer logHubPointer = 0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug_layout);
		
		//broadcast test
        Intent testLog = new Intent();
        testLog.setAction("edu.berkeley.boinc.log");
        testLog.putExtra("tag", "DebugActivity");
        testLog.putExtra("message", "layout created");
        sendBroadcast(testLog);
		
		
		logText=(TextView)this.findViewById(R.id.logtext);
		Log.d(TAG,"onCreate finished");
	}
	
	public void onResume() {
		Log.d(TAG,"onResume");
		populateText();
	}
	
	public void refresh(View view) {
		Log.d(TAG,"refresh");
		populateText();
	}
	
	public void clear(View view) {
		Log.d(TAG,"clear");
		logText.setText("");
		logHubPointer = 0;
		LogReceiver.logHub.clear();
	}
	
	private void populateText() {
		super.onResume();
		Log.d(TAG,"populateText");
		ListIterator<String> hubIt = LogReceiver.logHub.listIterator(logHubPointer);
		while(hubIt.hasNext()){
			String message = hubIt.next();
			Log.d(TAG,"new Entry: " + message);
			logText.append("\n"+message);
			logHubPointer++;
		}
		
	}
	
}
