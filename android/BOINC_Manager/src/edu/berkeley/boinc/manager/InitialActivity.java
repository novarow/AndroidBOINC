package edu.berkeley.boinc.manager;

import edu.berkeley.boinc.client.RemoteClientService;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class InitialActivity extends Activity {
	
	private RemoteClientService rcs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        

        //TODO init ClientService
        
      //TODO allow and handle callback ; proceed with starting monitor and loading meaningful activity
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_initial, menu);
        return true;
    }
}
