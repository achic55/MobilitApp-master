package com.mobi.mobilitapp.Capture;

import android.os.AsyncTask;

/**
 * Created by fran on 5/10/17.
 */

public class OneMinute extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... strings) {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }



        return "Executed";
    }
}
