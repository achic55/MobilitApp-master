package com.mobi.mobilitapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class History extends ActionBarActivity {
	Toolbar toolbar;
	ProgressBar progressBar;
	Handler h;
	FrameLayout fl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.history);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		progressBar = (ProgressBar) findViewById(R.id.progress_spinner);
		fl = (FrameLayout) findViewById(R.id.containerhist);
		//Make progress bar appear when you need it
		progressBar.setVisibility(View.VISIBLE);

		h = new Handler();

		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				new Populate().execute();
			}
		}, 100);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	public class Populate extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			getSupportFragmentManager().beginTransaction().add(R.id.containerhist, new HistoryFragment()).commit();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progressBar.setVisibility(View.GONE);
			fl.setVisibility(View.VISIBLE);

		}
	}

}
