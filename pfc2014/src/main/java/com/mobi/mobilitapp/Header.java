package com.mobi.mobilitapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mobi.mobilitapp.DrawerAdapter.RowType;

public class Header implements Item {
	private final String name;
	private Context c;

	public Header(Context c, String name) {
		this.name = name;
		this.c = c;
	}

	@Override
	public int getViewType() {
		return RowType.HEADER_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
		if (convertView == null) {
			view = (View) inflater.inflate(R.layout.header, null);
			// Do some initialization
		} else {
			view = convertView;
		}

		TextView text = (TextView) view.findViewById(R.id.separator);
		text.setCompoundDrawablesWithIntrinsicBounds(c.getResources().getDrawable(R.drawable.ic_logo), null, null, null);
		text.setText(name);
		return view;
	}

}