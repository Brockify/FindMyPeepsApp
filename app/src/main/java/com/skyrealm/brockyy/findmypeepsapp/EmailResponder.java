package com.skyrealm.brockyy.findmypeepsapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brock on 3/15/15.
 */
public class EmailResponder extends AsyncTask<String,Double, String> {

    private String user;
    private String htmlUrl ="http://findmypeeps.skyrealmstudio.com/emailtest.php";
    private String message;
    private String subject;
    private String check;
    static String response = null;

    public void registermail(String user) {

        this.user = user;
        this.message="<html >\n" +
                "<head>\n" +
                "<title>Thank you for registering!</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\t\t\n" +
                "\t\t<h1>Congratulations!</h1>\n" +
                "<p>\n" +
                "Your \"Find My Peeps\" account has been created successfully. We are proud to welcome you to our wonderful community.<br><br>\n" +
                "\n" +
                "You can now sign in to \"Find My Peeps\" with your username and password on any Android device. <br><br>\n" +
                "\n" +
                "Thank you for choosing to join this amazing community, <br>\n" +
                "FMP Team \n" +
                "</body>\n" +
                "</html> ";
        this.subject="Thank you for registering!";
        this.check="reg";

    }

    protected String doInBackground(String... params) {
        switch(check){
            case "reg":

            //send a post to the database if the user requests so------------------------------------------------
// Creating HTTP client
            HttpClient httpClient = new DefaultHttpClient();
            // Creating HTTP Post
            HttpPost httpPost = new HttpPost(htmlUrl);

            // Building post parameters
            // key and value pair
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("username", user));
            nameValuePair.add(new BasicNameValuePair("message", (this.message)));
            nameValuePair.add(new BasicNameValuePair("subject", (this.subject)));
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
            }
                break;
        }

        return response;
    }

}


