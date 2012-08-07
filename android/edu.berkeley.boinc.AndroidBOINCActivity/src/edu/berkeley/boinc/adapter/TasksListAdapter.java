package edu.berkeley.boinc.adapter;

import java.util.ArrayList;

import edu.berkeley.boinc.R;
import edu.berkeley.boinc.definitions.CommonDefs;
import edu.berkeley.boinc.rpc.Result;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TasksListAdapter extends ArrayAdapter<Result>{
	
	private final String TAG = "TasksListAdapter";
	private ArrayList<Result> entries;
    private Activity activity;
 
    public TasksListAdapter(Activity a, int textViewResourceId, ArrayList<Result> entries) {
        super(a, textViewResourceId, entries);
        this.entries = entries;
        this.activity = a;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
        View v = convertView;
        LayoutInflater vi = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(R.layout.tasks_layout_listitem, null);
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.progressBar);
		TextView header = (TextView) v.findViewById(R.id.taskName);
		TextView status = (TextView) v.findViewById(R.id.taskStatus);
		TextView progress = (TextView) v.findViewById(R.id.taskProgress);
        
    	Result listItem = entries.get(position);
    	
		pb.setIndeterminate(false);
    	
    	switch (determineTaskState(listItem)) {
    	case 0: //task inactive
    		Log.d(TAG,"inactive");
    		pb.setProgressDrawable(this.activity.getResources().getDrawable((R.drawable.progressbar_stopped)));
    		break;
    	case 1: //task suspended
    		Log.d(TAG,"suspended");
    		pb.setProgressDrawable(this.activity.getResources().getDrawable((R.drawable.progressbar_paused)));
    		break;
    	case 2: //task active
    		Log.d(TAG,"active");
    		pb.setProgressDrawable(this.activity.getResources().getDrawable((R.drawable.progressbar_active)));
    		break;
    	default:
    		break;
    	}
    	

		
		//v.setTag(listItem.name);
    	String headerT = "Name: " + listItem.name;
		header.setText(headerT);
		pb.setProgress(Math.round(listItem.fraction_done * pb.getMax()));
		String pT = Math.round(listItem.fraction_done * 100) + "%";
		progress.setText(pT);
		
		String statusT = determineStatusText(listItem);
		status.setText(statusT);
    	
        return v;
    }
    
    private String determineStatusText(Result tmp) {
    	String text = "";
    	if(tmp.active_task) {
    		switch (tmp.active_task_state) {
    		case 0:
    			text = activity.getString(R.string.tasks_active_uninitialized);
    			break;
    		case 1:
    			text = activity.getString(R.string.tasks_active_executing);
    			break;
    		case 5:
    			text = activity.getString(R.string.tasks_active_abort_pending);
    			break;
    		case 8:
    			text = activity.getString(R.string.tasks_active_quit_pending);
    			break;
    		case 9:
    			text = activity.getString(R.string.tasks_active_suspended);
    			break;
    		}
    	} else {
    		switch (tmp.state) {
    		case 0:
    			text = activity.getString(R.string.tasks_result_new);
    			break;
    		case 1:
    			text = activity.getString(R.string.tasks_result_files_downloading);
    			break;
    		case 2:
    			text = activity.getString(R.string.tasks_result_files_downloaded);
    			break;
    		case 3:
    			text = activity.getString(R.string.tasks_result_compute_error);
    			break;
    		case 4:
    			text = activity.getString(R.string.tasks_result_files_uploading);
    			break;
    		case 5:
    			text = activity.getString(R.string.tasks_result_files_uploaded);
    			break;
    		case 6:
    			text = activity.getString(R.string.tasks_result_aborted);
    			break;
    		case 7:
    			text = activity.getString(R.string.tasks_result_upload_failed);
    			break;
    		}
    	}
    	return text;
    }
    
    private Integer determineTaskState(Result tmp) {
    	Integer state = -1;
    	/* Task states:
    	 * 0 inactive
    	 * 1 suspended
    	 * 2 running
    	 */
    	if(tmp.active_task) {
    		if(tmp.active_task_state == CommonDefs.PROCESS_SUSPENDED) {
    			state = 1;
    		} else {
    			state = 2;
    		}
    	} else {
    		state = 0;
    	}
    	Log.d(TAG,"active: " + tmp.active_task + " active state: " + tmp.active_task_state + " -> " + state);
    	return state;
    }
}
