package com.example.gerardmarrugat.accelerometer;


import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by Gerard Marrugat on 05/01/2016.
 */

public class SendData_BK extends AsyncTask<File, Void, Void> {

    private String url_server;

    private static final String DataTAG = "DataTAG";

    public SendData_BK (String url){
        this.url_server = url;
    }


    @Override
    protected Void doInBackground(File... params) {

        //Log.v(DataTAG, params[0].toString());

        HttpClient httpClient = new DefaultHttpClient();

        //Define the url in the post object
        HttpPost httpPost = new HttpPost(this.url_server);
        //Creates an entity that contains the file we want to upload to the server
        FileEntity reqentity = new FileEntity(params[0],"text/plain");

        try {
            Log.v(DataTAG,"Entity content" + reqentity.getContent().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Define the request entity in the post object
        httpPost.setEntity(reqentity);

        try {

            HttpResponse httpResponse = httpClient.execute(httpPost);

            Log.v(DataTAG,"Transfer Done");

            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            //do something with httpResponse

            final StringBuilder out = new StringBuilder();
            String line;

            while ((line = rd.readLine()) != null) {
                out.append(line + "\n");
                Log.v(DataTAG,"out" + out.toString());
            }

            rd.close();

            Log.d(DataTAG, "serverResponse: " + out.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
