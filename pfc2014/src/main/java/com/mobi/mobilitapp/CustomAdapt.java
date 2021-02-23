package com.mobi.mobilitapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;


public class CustomAdapt extends ArrayAdapter<String> {

	private String[] files;
	private int layoutResourceId;
	private Context context;

	public CustomAdapt(Context context, int resource, String[] files) {
		super(context, resource, files);
		// TODO Auto-generated constructor stub
		this.layoutResourceId = resource;
		this.context = context;
		this.files = files;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
		}
		TextView tv = (TextView) row.findViewById(R.id.tvfiles);
		Button b= (Button) row.findViewById(R.id.bDelete);
		tv.setText(files[position]);
		b.setVisibility(Button.INVISIBLE);
	
		return row;
	}

	
}