package com.mobi.mobilitapp.CrashDetection.HandlersAndServices;

import android.content.Context;
import android.support.v4.content.ContextCompat;

/**
 * Created by toni_ on 20/06/2016.
 */
public interface IEmergencyManager {
    void reStart();

    boolean isEmergency();

    boolean isAskedAboutEmergency();

    void setAskedAboutEmergency(boolean askedAboutEmergency);

    void askAboutEmergency(Context context);

    boolean isCallingEmergencies();

    void setCallingEmergencies(boolean callingEmergencies);

    void beepSound();

}
