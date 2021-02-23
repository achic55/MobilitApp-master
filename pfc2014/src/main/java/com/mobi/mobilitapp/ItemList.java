package com.mobi.mobilitapp;

import com.mobi.mobilitapp.DrawerAdapter.RowType;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

public class ItemList implements Item {
	private final int str1;
	private Context c;

	public ItemList(Context c, int text1) {
		this.str1 = text1;
		this.c = c;
	}

	@Override
	public int getViewType() {
		return RowType.LIST_ITEM.ordinal();
	}

	@Override
	public View getView(LayoutInflater inflater, View convertView) {
		View view;
		if (convertView == null) {
			view = (View) inflater.inflate(R.layout.menulateral, null);
		} else {
			view = convertView;
		}

		TextView text1 = (TextView) view.findViewById(R.id.tvtext1);


		text1.setText(str1);
		if (str1 == R.string.show_location_history) {
			text1.setCompoundDrawablesWithIntrinsicBounds(c.getResources().getDrawable(R.drawable.files), null, null, null);
		} else if (str1 == R.string.graph) {
			text1.setCompoundDrawablesWithIntrinsicBounds(c.getResources().getDrawable(R.drawable.statistics), null, null, null);
		} else if (str1 == R.string.clean) {
			text1.setCompoundDrawablesWithIntrinsicBounds(c.getResources().getDrawable(R.drawable.eraser), null, null, null);
		} else if (str1 == R.string.perfil) {
			text1.setCompoundDrawablesWithIntrinsicBounds(c.getResources().getDrawable(R.drawable.register), null, null, null);
		} else if (str1 == R.string.sesion) {
			text1.setCompoundDrawablesWithIntrinsicBounds(c.getResources().getDrawable(R.drawable.login), null, null, null);
		
	} else if (str1 == R.string.traffic) {
		text1.setCompoundDrawablesWithIntrinsicBounds(c.getResources().getDrawable(R.drawable.traffic), null, null, null);
	} else if (str1 == R.string.help) {
        text1.setCompoundDrawablesWithIntrinsicBounds(c.getResources().getDrawable(R.drawable.help), null, null, null);
    }
		return view;
	}

}