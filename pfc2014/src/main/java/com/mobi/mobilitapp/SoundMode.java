package com.mobi.mobilitapp;

import android.media.AudioManager;
import android.os.AsyncTask;


public class SoundMode extends AsyncTask<Void, Void, String> {
	
	AudioManager mAudioManager; 
	boolean mPhoneIsSilent;
	String message="";
			
	
	
public SoundMode(AudioManager mAudioManager, boolean mPhoneIsSilent){//leer de Json y guardar en database
		
	this.mAudioManager = mAudioManager;
	this.mPhoneIsSilent = mPhoneIsSilent;
	
	}
protected String doInBackground(Void...parameters) {
	
	
	if (mPhoneIsSilent= true){
		ModoNormal( mAudioManager, mPhoneIsSilent);
		return message = "normal";
	}
	else{
		ModoSilencio(mAudioManager,mPhoneIsSilent);
		return message = "silencio";
	}
	
}

public void ModoSilencio(AudioManager mAudioManager,boolean mPhoneIsSilent){
	
	// Change to silent mode
	mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	mPhoneIsSilent = true;
}

public void ModoNormal(AudioManager mAudioManager,boolean mPhoneIsSilent){
	// Change back to normal mode
	mAudioManager
			.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	mPhoneIsSilent = false;
	
}
}
