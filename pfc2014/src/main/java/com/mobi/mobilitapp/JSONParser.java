package com.mobi.mobilitapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;


 
import android.util.Log;


public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public JSONParser() {
 
    }
    // function get json from url
    // by making HTTP POST or GET mehtod
    public JSONObject makeHttpRequest(String url, String method, List<NameValuePair> params) {
        // Making HTTP request (POST/GET request)
        // url: Uniform Resource Locator to connect with
        // method: POST/GET
        // params: values to send/receive from connection
        try {
            // check for request method (POST/GET)
            if(method == "POST"){
                // request method is POST -- Definition: change the state of the objects in server

                //create HttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                //create HttpPOST Object with url
                HttpPost httpPost = new HttpPost(url);
                //Encode contents to POST/GET Object in an entity
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                //Log
                Log.v("json:","url:"+httpPost.toString());
                //Execute POST request
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //Receive response as inputStream
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
                // These two lines could be written as follows: is = httpResponse.getEntity().getContent();

            }else if(method == "GET"){
                // request method is GET -- Definition: get the state of the objects in server

                //create HttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                //Encode params
                String paramString = URLEncodedUtils.format(params, "utf-8");
                //Add paramString to url
                url += "?" + paramString;
                Log.v("json:","url:"+url);
                //Creates HttpGET Object with url
                HttpGet httpGet = new HttpGet(url);
                //Execute GET request
                HttpResponse httpResponse = httpClient.execute(httpGet);
                //Receive response as inputStream
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }           
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //Convert InputStream into String
            // is contents the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
                Log.v("json: ",line);
            }

            is.close();

         // Convert jsonObject to String
            json = sb.toString().substring(0, sb.toString().length()-1);

        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
 
    }
}