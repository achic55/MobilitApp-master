package com.mobi.mobilitapp.CrashDetection.HandlersAndServices;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;

import com.mobi.mobilitapp.CrashDetection.Acitivities.EmergencyMenuActivity;
import com.mobi.mobilitapp.R;

import java.util.Locale;

/**
 * Created by toni_ on 20/05/2016.
 */
public class EmergencyManager implements IEmergencyManager {
    private boolean emergency;
    private boolean askedAboutEmergency;
    private boolean callingEmergencies;
    ToneGenerator toneG;
    String language;

    public EmergencyManager() {
        this.emergency = false;
        this.callingEmergencies = false;
        this.askedAboutEmergency = false;
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        language = Locale.getDefault().getDisplayLanguage();
    }

    @Override
    public void reStart()
    {
        this.emergency = false;
        this.callingEmergencies = false;
        this.askedAboutEmergency = false;
    }

    @Override
    public boolean isEmergency() {
        return emergency;
    }

    @Override
    public boolean isAskedAboutEmergency() {
        return askedAboutEmergency;
    }

    @Override
    public void setAskedAboutEmergency(boolean askedAboutEmergency) {
        this.askedAboutEmergency = askedAboutEmergency;
    }

    @Override
    public void askAboutEmergency(final Context context){
        if(emergency == false){
            emergency = true;
            Intent intent = new Intent();
            intent.setClass(context, EmergencyMenuActivity.class); //Menu de Emergencia
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @Override
    public boolean isCallingEmergencies() {
        return callingEmergencies;
    }

    @Override
    public void setCallingEmergencies(boolean callingEmergencies) {
        this.callingEmergencies = callingEmergencies;
    }

    @Override
    public void beepSound(){
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
    }

}
