package edu.berkeley.boinc.adapter;

import java.util.ArrayList;

import edu.berkeley.boinc.R;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class PrefsListAdapter extends ArrayAdapter<PrefsListItemWrapper>{
	
	private final String TAG = "PrefsListAdapter";
	private ArrayList<PrefsListItemWrapper> entries;
    private Activity activity;
 
    public PrefsListAdapter(Activity a, int textViewResourceId, ArrayList<PrefsListItemWrapper> entries) {
        super(a, textViewResourceId, entries);
        this.entries = entries;
        this.activity = a;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
        View v = convertView;
        LayoutInflater vi = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
    	PrefsListItemWrapper listItem = entries.get(position);
    	
    	if(listItem instanceof PrefsListItemWrapperBool) {
    		v = vi.inflate(R.layout.prefs_layout_listitem_bool, null);
    		CheckBox header = (CheckBox) v.findViewById(R.id.checkbox);
    		header.setText(((PrefsListItemWrapperBool) listItem).header);
    		header.setTag(listItem.ID); //set ID as tag to checkbox, since checkbox is clicked
        	header.setChecked(((PrefsListItemWrapperBool) listItem).getStatus());
    		TextView status = (TextView) v.findViewById(R.id.status);
    		status.setText(((PrefsListItemWrapperBool) listItem).status_text);
    	} else if(listItem instanceof PrefsListItemWrapperDouble) {
    		v = vi.inflate(R.layout.prefs_layout_listitem, null);
    		v.setTag(listItem.ID); //set ID as tag to view, since root layout defines onClick method
    		TextView header = (TextView) v.findViewById(R.id.header);
    		header.setText(((PrefsListItemWrapperDouble) listItem).header);
    		TextView status = (TextView) v.findViewById(R.id.status);
    		status.setText(((PrefsListItemWrapperDouble) listItem).status.toString());
    		
    	} else if(listItem instanceof PrefsListItemWrapperText) {
    		v = vi.inflate(R.layout.prefs_layout_listitem, null);
    		v.setTag(listItem.ID);
    		TextView header = (TextView) v.findViewById(R.id.header);
    		header.setText(((PrefsListItemWrapperText) listItem).header);
    		TextView status = (TextView) v.findViewById(R.id.status);
    		status.setText(((PrefsListItemWrapperText) listItem).display);
    		
    	}
    	
        return v;
    }
}
