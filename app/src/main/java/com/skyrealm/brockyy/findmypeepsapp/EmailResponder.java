package com.skyrealm.brockyy.findmypeepsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 6/25/2015.
 */
public class EmailResponder extends AsyncTask<String,Double, String> {

    private String user;
    private String subject;
    private String message;
    private String sub;
    static String response = null;

    public void registermail(String user, String subject, String message) {

        this.sub = "registermail";
        this.subject = subject;
        this.user = user;
        this.message = message;


    }

    protected String doInBackground(String... params) {
        switch (sub) {
            case "registermail":
                //send a post to the database if the user requests so------------------------------------------------
                // Creating HTTP client
                HttpClient httpClient = new DefaultHttpClient();
                // Creating HTTP Post
                HttpPost httpPost = new HttpPost("http://findmypeeps.skyrealmstudio.com/emailtest.php");

                // Building post parameters
                // key and value pair
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("Username", user));
                nameValuePair.add(new BasicNameValuePair("subject", this.subject));
                nameValuePair.add(new BasicNameValuePair("message", this.message));

                // Url Encoding the POST parameters
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                } catch (UnsupportedEncodingException e) {
                    // writing error to Log
                    e.printStackTrace();
                }

                // Making HTTP Request
                try {
                    HttpResponse response = httpClient.execute(httpPost);

                    // writing response to log
                    Log.d("Http Response:", response.toString());
                } catch (ClientProtocolException e) {
                    // writing exception to log
                    e.printStackTrace();
                } catch (IOException e) {
                    // writing exception to log
                    e.printStackTrace();

                    break;


                }


        }
        return response;
    }
}



