package com.mobi.mobilitapp.Uploading;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mobi.mobilitapp.R;

import java.io.File;
import java.text.DecimalFormat;

import static java.lang.String.valueOf;

/**
 * Created by Fran Medina on 19/05/17.
 */

public class UploadService extends IntentService {

    public static final String FILEPATH = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + "/MobilitApp/sensors";
    public static final String TAG = "UploadService";
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private int serverCode;

    public UploadService() {
        // Used to name the worker thread
        super(UploadService.class.getName());
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Gets data from the incoming Intent
        String dataString = intent.getStringExtra("UP");

        // Do work here, based on the contents of dataString
        File dir = new File(FILEPATH);
        File[] files = dir.listFiles();
        Log.v(TAG, "Number of files: " + files.length);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Uploading files")
                .setContentText("Upload in progress")
                .setSmallIcon(R.drawable.ic_logo)
                .setWhen(System.currentTimeMillis());

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < files.length; i++) {
                            if (files[i].isFile()) {
                                DecimalFormat format = new DecimalFormat("#");
                                String val = valueOf((format.format((i + 0.0)/(double)files.length * 100)));
                                mBuilder.setProgress(files.length, i, false)
                                        .setContentText("Upload in progress: " + val + "%");
                                mNotificationManager.notify(1, mBuilder.build());
                                Intent intentUpdate = new Intent();
                                intentUpdate.setAction("UPDATE");
                                intentUpdate.addCategory("DEFAULT");
                                intentUpdate.putExtra("KEY", i);
                                sendBroadcast(intentUpdate);

                                Log.v("NUMERO DE ITERACIÓN", valueOf(i + 1) + " of " + valueOf(files.length));

                                try {
                                    UploadToServer mFile = new UploadToServer(files[i].toString());
                                    serverCode = mFile.call();
                                    Log.d(TAG, "Selected file " + files[i].toString());
                                    if (serverCode != 200) {
                                        // The server is down
                                        mBuilder.setContentTitle("ERROR UPLOADING")
                                                .setProgress(0, 0, false)
                                                .setContentText("...The server is down...");
                                        mNotificationManager.notify(1,mBuilder.build());
                                        return;
                                    }
                                    else {
                                        File deleteFile = new File(files[i].toString());
                                        Boolean delete = deleteFile.delete();
                                        if (delete) {
                                            Log.v(TAG, "The file " + files[i].toString()
                                                    + " has been deleted\nServer code: " + serverCode);
                                            // Return Result
                                            Intent intentResponse = new Intent();
                                            intentResponse.setAction("RESPONSE");
                                            intentResponse.addCategory("DEFAULT");
                                            intentResponse.putExtra("OUT", "OK");
                                            sendBroadcast(intentResponse);
                                        }
                                    }
                                } catch (Exception ex) {
                                    Log.e(TAG, "Upload fail");
                                }

                                /* Rok Slamek - for backup we don't erase files */
                                // When the Thread has finish we can erase the files and server response OK
                                /*if (serverCode == 200) {
                                    File deleteFile = new File(files[i].toString());
                                    Boolean delete = deleteFile.delete();
                                    if (delete) {
                                        Log.v(TAG, "The file " + files[i].toString()
                                                + " has been deleted\nServer code: " + serverCode);
                                        // Return Result
                                        Intent intentResponse = new Intent();
                                        intentResponse.setAction("RESPONSE");
                                        intentResponse.addCategory("DEFAULT");
                                        intentResponse.putExtra("OUT", "OK");
                                        sendBroadcast(intentResponse);
                                    }
                                } else {
                                    // Return Result
                                    Intent intentResponse = new Intent();
                                    intentResponse.setAction("RESPONSE");
                                    intentResponse.addCategory("DEFAULT");
                                    intentResponse.putExtra("OUT", "KO");
                                    sendBroadcast(intentResponse);

                                }*/

                            }
                        }
                        mBuilder.setContentText("Upload Complete")
                                .setProgress(0, 0, false);
                        mNotificationManager.notify(1, mBuilder.build());
                    }
                }
                ).start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
