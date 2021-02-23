package com.mobi.mobilitapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class PrefAct extends ActionBarActivity {
	
	Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.prefslay);
		toolbar=(Toolbar)findViewById(R.id.toolbarprefs);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		if(savedInstanceState==null)
		getFragmentManager().beginTransaction().replace(R.id.container_prefs, new Preferences()).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Log.v("aki", "aki_back");
		Intent i = new Intent();
		setResult(Activity.RESULT_OK, i);
		finish();	
	}

}
