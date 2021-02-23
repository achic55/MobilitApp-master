package com.mobi.mobilitapp;

import java.util.Arrays;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LoginFragment extends Fragment implements OnClickListener, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private LoginButton authButton;
	private SignInButton signin;
	private GoogleApiClient mPlusClient;
	private ConnectionResult mConnectionResult;
	private ProgressDialog mConnectionProgressDialog;
	Person p;
	String acc;
	private UiLifecycleHelper uiHelper;
	SharedPreferences prefsGoogle, prefsFace, first, prefs;
	SharedPreferences.Editor editorG;
	TextView tv, tVnever;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v("aki", "aki_logincreateview");
		View rootView = inflater.inflate(R.layout.login_lay, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v("aki", "aki_activitycreatedfrag");
		prefsGoogle = getActivity().getSharedPreferences("google_login", Context.MODE_PRIVATE);
		prefsFace = getActivity().getSharedPreferences("face_login", Context.MODE_PRIVATE);
		first = getActivity().getSharedPreferences("prefs", 0);
		prefs = getActivity().getSharedPreferences("com.mobi.mobilitapp_preferences", 0);

		signin = (SignInButton) getActivity().findViewById(R.id.sign_in_button);
		authButton = (LoginButton) getActivity().findViewById(R.id.authButton);
		tv = (TextView) getActivity().findViewById(R.id.tVtarde);
		//tVnever = (TextView) getActivity().findViewById(R.id.tVnever);

		signin.setOnClickListener(this);
		tv.setOnClickListener(this);
//		tVnever.setOnClickListener(this);
		authButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

		mPlusClient = new GoogleApiClient.Builder(getActivity(), this, this).addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
		mConnectionProgressDialog = new ProgressDialog(getActivity());
		mConnectionProgressDialog.setMessage(getResources().getString(R.string.login));
		mConnectionProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("aki", "aki_destroy_login");
		Session.getActiveSession().closeAndClearTokenInformation();
		uiHelper.onDestroy();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.v("aki", "aki_pause_login");
		uiHelper.onPause();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}
		uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.v("aki", "aki_start_login");
		mPlusClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v("aki", "aki_stop_login");
		mPlusClient.disconnect();
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			Log.v("aki", "aki_call");
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.v("aki", "aki_result");
		if (requestCode == AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST && resultCode == -1) {
			mConnectionResult = null;
			mConnectionProgressDialog.show();
			mPlusClient.connect();
		}
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		Log.v("aki", "aki_onsessionstate");
		if (state.isOpened()) {
			Log.v("aki", "aki_opened");
			Request.newMeRequest(session, new Request.GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user, Response response) {
					// TODO Auto-generated method stub
					if (response != null) {
						SharedPreferences.Editor edi = prefsFace.edit();
						if (user.getBirthday() != null)
							edi.putString("birthday", user.getBirthday());
						edi.putString("email", user.asMap().get("email").toString());
						edi.putString("nombre", user.getFirstName());
						edi.putString("apellidos", user.getLastName());
						edi.putString("genero", user.asMap().get("gender").toString());

						edi.commit();

					}

				}
			}).executeAsync();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent i = new Intent();
			getActivity().setResult(Activity.RESULT_OK, i);
			getActivity().finish();
            Log.v("aki", "aki_facebook");

		} else if (state.isClosed()) {
			Log.v("aki", "aki_closed");

			session.closeAndClearTokenInformation();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.v("aki", "aki_failed " + result.getErrorCode());
		if (mConnectionProgressDialog.isShowing()) {
			if (result.hasResolution()) {
				try {
					result.startResolutionForResult(getActivity(), AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
				} catch (SendIntentException e) {
					mPlusClient.connect();
				}
			}
		}
		mConnectionResult = result;

	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.v("aki", "aki_connected");
		mConnectionProgressDialog.dismiss();

		p = Plus.PeopleApi.getCurrentPerson(mPlusClient);
		acc = Plus.AccountApi.getAccountName(mPlusClient);
		editorG = prefsGoogle.edit();

		editorG.putString("correo", acc);
		editorG.putString("birthday", p.getBirthday());
		editorG.putString("nombre", p.getName().getGivenName());
		editorG.putString("apellidos", p.getName().getFamilyName());
		editorG.putInt("genero", p.getGender());
		editorG.commit();
		signOut();
		Intent i = new Intent();
		getActivity().setResult(Activity.RESULT_OK, i);
		getActivity().finish();

	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.sign_in_button && !mPlusClient.isConnected()) {
			// Log.v("aki", "aki_onclick "+mConnectionResult.getErrorCode());
			if (mConnectionResult == null) {
				mConnectionProgressDialog.show();
			} else {
				try {
					mConnectionResult.startResolutionForResult(getActivity(),
							AppUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
				} catch (SendIntentException e) {
					// Intenta la conexión de nuevo.
					mConnectionResult = null;
					mPlusClient.connect();
				}
			}
		} else if (v.getId() == R.id.tVtarde) {

            Intent i = new Intent();
            getActivity().setResult(Activity.RESULT_OK, i);
            getActivity().finish();
        }


	}

	public void signOut() {
		if (mPlusClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mPlusClient);
			mPlusClient.disconnect();
			mPlusClient.connect();
		}
	}

}
