package com.mobi.mobilitapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class Preferences extends PreferenceFragment{


public static final String PREF_LANG_KEY="language_key";
private SharedPreferences.OnSharedPreferenceChangeListener listener;
    String id,nombreUsuario,apellidoUsuario,pesoUsuario,nacimientoUsuario,mailUsuario,generoUsuario;
    final String ruta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/";
    boolean guardarusuario;
    protected FragmentActivity mActivity;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);

        listener=new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(PREF_LANG_KEY)){
                    Preference langPref= findPreference(key);
                    langPref.setSummary(sharedPreferences.getString(key,""));
                    update_language1(false);
                }

            }
                };

		addPreferencesFromResource(R.xml.prefs);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();


        updatePreference(findPreference("peso"));
        updatePreference(findPreference("born"));
        updatePreference(findPreference("genero"));


        /*try {

            SharedPreferences prefsusuario,prefsGoogle,prefsFace,peso;
            prefsusuario = getActivity().getSharedPreferences("prefs",Context.MODE_PRIVATE);
            prefsGoogle = getActivity().getSharedPreferences("google_login",Context.MODE_PRIVATE);
            prefsFace = getActivity().getSharedPreferences("face_login",Context.MODE_PRIVATE);
            peso = getActivity().getSharedPreferences("com.mobi.mobilitapp_preferences", Context.MODE_PRIVATE);


            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    // Implementation
                    if (key.equals("born")) {
                        // check if a valid year and let the user know if it isn't.

                        if (Integer.parseInt(prefs.getString("born", "1900")) >= currentYear - 80 &&
                                Integer.parseInt(prefs.getString("born", "1900")) <= currentYear - 17) {
                            Log.v("prefs", "edad cambiada");

                        } else {
                            Log.v("prefs", "edad debe ser entre 2015-80 y 2015-17");
                            new AlertDialog.Builder(mActivity).setTitle("!").setMessage("La edad debe ser entre 17 y 80 años")
                                    .setNeutralButton("Close", null).show();
                            editor.putString("born", "0");
                            //prefs.edit().putString("born","1900");
                            editor.commit();
                            updatePreference(findPreference("born"));
                            return;



                        }
                    }
                    if (key.equals("peso")) {
                        // check if a valid year and let the user know if it isn't.

                        if (Double.parseDouble(prefs.getString("peso", "0")) >= 20 && Double.parseDouble(prefs.getString("peso", "0")) <= 200) {
                            Log.v("prefs", "peso cambiado");

                        } else {
                            Log.v("prefs", "peso debe estar entre 20-200 Kg");
                            new AlertDialog.Builder(mActivity).setTitle("!").setMessage("peso debe estar entre 20-200 Kg").setNeutralButton("Close", null).show();
                            editor.putString("peso", "0");
                            editor.commit();
                            updatePreference(findPreference("peso"));
                            return;

                        }
                    }

                    if (key.equals("silence")) {

                        CheckBoxPreference prefCheck = (CheckBoxPreference) findPreference("silence");


                        if (prefCheck.isChecked()){
                            prefsusuario.edit().putString("Silence","ON");
                            Toast.makeText(mActivity, "Silence Mode ON", Toast.LENGTH_LONG).show();
                        }else{
                            prefsusuario.edit().putString("Silence","OFF");
                            Toast.makeText(mActivity, "Silence Mode OFF", Toast.LENGTH_LONG).show();
                        }


                        return;
                    }


                try {
                        String result="KO";
                        id = prefsusuario.getString("id", "no_id");
                        nombreUsuario=prefsGoogle.getString("nombre", "");
                        apellidoUsuario=prefsGoogle.getString("apellidos", "");
                        pesoUsuario=peso.getString("peso", "0");
                        nacimientoUsuario=prefsGoogle.getString("birthday", "");
                        mailUsuario=prefsGoogle.getString("correo","");
                        generoUsuario=peso.getString("genero","");

                        if (nombreUsuario.equals("")){
                            nombreUsuario=prefsFace.getString("nombre", "not_set");
                        }
                        if (apellidoUsuario.equals("")){
                            apellidoUsuario=prefsFace.getString("apellidos", "not_set");
                        }
                        if (nacimientoUsuario.equals("")){
                            nacimientoUsuario=prefsFace.getString("birthday", "0000-00-00");
                            if (nacimientoUsuario.equals("0000-00-00")){
                                nacimientoUsuario = peso.getString("born","0000");
                                nacimientoUsuario = nacimientoUsuario+"-01-01";

                            }
                        }
                        if (mailUsuario.equals("")){
                            mailUsuario=prefsFace.getString("email", "");
                            if (mailUsuario.equals("")){
                                mailUsuario = "notset@notset.com";
                            }
                        }
                        if (generoUsuario.equals("")){
                            generoUsuario = "not_set";

                        }


                        if (id.equalsIgnoreCase("no_id")){
                            Toast.makeText(mActivity,"Inicia la App primero",Toast.LENGTH_LONG).show();
                            updatePreference(findPreference(key));

                        }else {

                            result = new SaveDatabase(ruta, id, nombreUsuario, apellidoUsuario, pesoUsuario, nacimientoUsuario, mailUsuario, generoUsuario, true).execute().get();
                            if (result.equalsIgnoreCase("OK")) {
                                Toast.makeText(mActivity, "Saved Preferences", Toast.LENGTH_LONG).show();
                                updatePreference(findPreference(key));


                            } else {
                                Toast.makeText(mActivity.getBaseContext(), "Failed save preferences", Toast.LENGTH_LONG).show();
                            }
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            };

            prefs.registerOnSharedPreferenceChangeListener(listener);
        }catch (Exception e){

            Log.v("parse","parse: "+ e.toString());

        }*/
	}
    @Override
	public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);

    }

    @Override
    public void onPause(){
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);

    }
    private void updatePreference(Preference preference){

        if (preference instanceof ListPreference){
            ListPreference listpref = (ListPreference) preference;
            preference.setSummary(listpref.getEntry());
        }
        if (preference instanceof EditTextPreference){
            EditTextPreference textpref = (EditTextPreference) preference;
            textpref.setSummary(textpref.getText());
        }
    }

    public void update_language1(Boolean on_create_call){
        //chosen language
       String currValue;
       if(findPreference(PREF_LANG_KEY).toString().contains("(Auto)")){
         currValue="(Auto)";
        }
        else{
             currValue=findPreference(PREF_LANG_KEY).toString();
        }
        String the_language_short_version;

        //manually change locale if it's not AUTO
        if (!currValue.equals("(Auto)")) {
            the_language_short_version = currValue.split("[\\(\\)]")[1];
        }else{
            //set BACK language to AUTO
            the_language_short_version = Locale.getDefault().getISO3Language() ;
        }

        Locale locale = new Locale(the_language_short_version);
        Locale.setDefault(locale);
        Configuration config = this.getResources().getConfiguration();
        config.locale = locale;
        this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());


        //we restart ONLY if we update language from preferences
		if (on_create_call==Boolean.FALSE){
			mActivity.recreate();
		}

    }




}
