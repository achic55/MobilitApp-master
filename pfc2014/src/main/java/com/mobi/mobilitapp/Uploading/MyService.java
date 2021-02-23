package com.mobi.mobilitapp.Uploading;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;

/**
 * Created by fran on 19/05/17.
 */

public class MyService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
