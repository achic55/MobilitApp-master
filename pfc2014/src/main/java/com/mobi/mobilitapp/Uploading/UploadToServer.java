package com.mobi.mobilitapp.Uploading;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

/**
 * Created by Fran Medina on 20/05/17.
 */

public class UploadToServer implements Callable {

    private static final String TAG = "Upload2Server";
    private String filePath;
    private int serverCode = 0;
    private String serverMsg = null;


    public UploadToServer(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Integer call() {

        HttpURLConnection con = null;
        DataOutputStream dos = null;
        String URLServer = "http://mobilitat.upc.edu/csv/data.php";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        boolean isAlive;

        // ----- Method 1) PING to the server to know if it is alive
/*        try {
            Process connection = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");
            int serverValue = connection.waitFor();
            boolean reachable = (serverValue == 0);
            Log.v("SERVER_SERVER_RESPONSE", "" + reachable);
        } catch (IOException IOE) {
            IOE.printStackTrace();
        } catch (InterruptedException IE) {
            IE.printStackTrace();
        }*/

        // ----- Method 2) Catch an exception if the server is down
        try {
            URL home = new URL("http://mobilitat.upc.edu");
            URLConnection homeConn = home.openConnection();
            homeConn.connect();
            isAlive = true;
        } catch (Exception e) {
            Log.e(TAG, "The server is down...");
            isAlive = false;
        }

        if (isAlive) {

            try {
                FileInputStream fis = new FileInputStream(new File(filePath));
                Log.v(TAG, "SERVER: " + isAlive);
                Log.v(TAG, "Correct FIS: " + filePath);
                URL mURL = new URL(URLServer);
                con = (HttpURLConnection) mURL.openConnection();

                // Allows Inputs/Outputs over the connection. Do not use a cached copy
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setUseCaches(false);

                // Open an HTTP connection to the URL
                con.setRequestMethod("POST");
                con.setRequestProperty("Connection", "Keep-Alive");
                con.setRequestProperty("ENCTYPE", "multipart/form-data");
                con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                con.setRequestProperty("uploaded_file", filePath);

                dos = new DataOutputStream(con.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                        + filePath + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // Create a buffer of maximum size
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read the file and write it into form...
                bytesRead = fis.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fis.read(buffer, 0, bufferSize);
                }

                // Send multipart form data necessary after file data
                dos.writeBytes(lineEnd); // \r\n
                dos.writeBytes(twoHyphens + boundary + lineEnd); // --*****\r\n

                // Responses from the server (code and msg)
                serverCode = con.getResponseCode();
                serverMsg = con.getResponseMessage();
                Log.i(TAG, "HTTP Response is: " + serverMsg + ": " + serverCode);
                if (serverCode != 200) {
                    Log.v(TAG, "SERVER RESPONSE CODE ERROR" + serverCode);
                    serverCode = 0;
                }

                // Close the streams
                fis.close();
                dos.flush();
                dos.close();


            } catch (IOException ex) {
                ex.printStackTrace();
                serverCode = 0;
                return serverCode;
            }
            return serverCode;
        }

        else {
            return 0;
        }
    }
}
