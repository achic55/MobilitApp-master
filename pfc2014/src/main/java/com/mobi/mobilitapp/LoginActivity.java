package com.mobi.mobilitapp;

import com.google.android.gms.common.api.GoogleApiClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class LoginActivity extends ActionBarActivity {

	SharedPreferences prefsGoogle, prefsFace, first, prefs;
	SharedPreferences.Editor editor;

	private MenuItem logout;
	String age;
	int agenum, gen;

	Toolbar toolbar;
	LoginFragment login;
	PerfilFragment perfil;

	FrameLayout fl_log, fl_perf;
	boolean show = false;
	GoogleApiClient gac;
	String nombre;
	boolean isGoogle = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Log VERBOSE messages and load the layout
		Log.v("aki", "aki_createlogin_act");
		setContentView(R.layout.login_layout);

		//Load the Toolbar
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		prefsGoogle = getSharedPreferences("google_login", Context.MODE_PRIVATE);
		prefsFace = getSharedPreferences("face_login", Context.MODE_PRIVATE);
		first = getSharedPreferences("prefs", 0);
		prefs = getSharedPreferences("com.mobi.mobilitapp_preferences", 0);

		//Load the container of the layout
		fl_log = (FrameLayout) findViewById(R.id.container_loginandperf);

		if (!first.getBoolean("firstRun", false)) {
			//Turn back to a upper level when Home button is clicked
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			//Enables Home Button
			getSupportActionBar().setHomeButtonEnabled(true);
			//
		}

		//google login?
		nombre = prefsGoogle.getString("nombre", "noname");
		isGoogle = true;
		//compares string nombre with "noname"
		//facebook login
		if (nombre.equalsIgnoreCase("noname")) {
			nombre = prefsFace.getString("nombre", "noname");
			isGoogle = false;
		}

		if (nombre.equalsIgnoreCase("noname")) {
			getSupportFragmentManager().beginTransaction().add(R.id.container_loginandperf, new LoginFragment(), "login").commit();
			show = false;

		} else {
			getSupportFragmentManager().beginTransaction().add(R.id.container_loginandperf, new PerfilFragment(), "perfil").commit();
			show = true;

		}

		getSupportFragmentManager().executePendingTransactions();
		
		if (!show) {
			login = (LoginFragment) getSupportFragmentManager().findFragmentByTag("login");
		} else {
			perfil = (PerfilFragment) getSupportFragmentManager().findFragmentByTag("perfil");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v("aki", "aki_result_act");

		login.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu2, menu);
		logout = menu.findItem(R.id.logout);
		logout.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemid = item.getItemId();

		switch (itemid) {

		case R.id.logout:

            SharedPreferences.Editor editor2 = prefs.edit();
            editor2.putString("born", "0");
            editor2.putString("peso", "0");
            editor2.commit();


            SharedPreferences.Editor editor3 = first.edit();
            editor3.putString("login","default").commit();

			SharedPreferences.Editor editor = prefsGoogle.edit();
			editor.clear().commit();

			SharedPreferences.Editor editor4 = prefsFace.edit();
			editor4.clear().commit();


			Intent back = new Intent();
			setResult(RESULT_OK, back);
			finish();

			break;

		case android.R.id.home:

			onBackPressed();

			break;

            case R.id.prf:
                Intent i = new Intent(this, PrefAct.class);
                startActivity(i);

		}


		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		logout = menu.findItem(R.id.logout);
		if (logout != null)
			if (show) {
				logout.setVisible(true);
			} else {
				logout.setVisible(false);
			}
		return super.onPrepareOptionsMenu(menu);
	}
}
