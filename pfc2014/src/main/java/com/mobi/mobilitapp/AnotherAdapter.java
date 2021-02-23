package com.mobi.mobilitapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class AnotherAdapter extends ArrayAdapter<String> {

	private String[] files;
	private String[] files2;
	private int layoutResourceId;
	private Context context;

	public AnotherAdapter(Context context, int resource, String[] files, String[] files2) {
		super(context, resource, files);

		this.layoutResourceId = resource;
		this.context = context;
		this.files = files;
		this.files2 = files2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Typeface mFontreg = Typeface.createFromAsset(this.getContext().getAssets(), "fonts/Montserrat-Regular.ttf");
		Typeface mFontbold = Typeface.createFromAsset(this.getContext().getAssets(), "fonts/Montserrat-Bold.ttf");
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
		}
		TextView tv = (TextView) row.findViewById(R.id.text1);
		tv.setTypeface(mFontbold);
		TextView tv2 = (TextView) row.findViewById(R.id.text2);
		tv.setTypeface(mFontreg);
		tv.setText(files[position]);
		tv2.setText(files2[position]);
		return row;
	}
}
