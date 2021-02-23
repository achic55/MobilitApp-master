package com.mobi.mobilitapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Gerard Marrugat on 28/02/2016.
 */

//This class is used to receive the Data Sensor send from Sensor_Listener class to LocationService

public class Data_Receiver extends ResultReceiver {

    private Receiver mReceiver;

    public Data_Receiver(Handler handler) {
        super(handler);
        // TODO Auto-generated constructor stub
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);

    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }


}
