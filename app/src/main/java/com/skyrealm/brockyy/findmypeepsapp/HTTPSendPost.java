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
public class HTTPSendPost extends AsyncTask<String,Double, String> {

    private String user;
    private double latitude;
    private double longitude;
    private String lastUpdated;
    private String address;
    private String htmlUrl;
    private String pendingUser;
    private String comments;
    private String friend;
    private int YesOrNo;
    static String response = null;

    public void Setup(String user,double longitude, double latitude, String address, String htmlUrl, String comments, String lastUpdated) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.htmlUrl = htmlUrl;
        this.comments = comments;
        this.user = user;
        this.lastUpdated = lastUpdated;

    }
    public void SetUpOnlyUrl(String user, String htmlUrl, String pendingUser, int yesorno)
    {
        this.htmlUrl = htmlUrl;
        this.pendingUser = pendingUser;
        this.YesOrNo = yesorno;
        this.user = user;
    }
    public void setUpOnDeleteFriend(String user,String friend, String htmlUrl)
    {
        this.user = user;
        this.friend = friend;
        this.htmlUrl = htmlUrl;
    }

    protected String doInBackground(String... params) {
        if (htmlUrl.equals("http://skyrealmstudio.com/cgi-bin/updatelocation.py")) {
            //send a post to the database if the user requests so------------------------------------------------
// Creating HTTP client
            HttpClient httpClient = new DefaultHttpClient();
            // Creating HTTP Post
            HttpPost httpPost = new HttpPost(htmlUrl);

            // Building post parameters
            // key and value pair
            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("Username", user));
            nameValuePair.add(new BasicNameValuePair("Latitude", Double.toString(this.latitude)));
            nameValuePair.add(new BasicNameValuePair("Longitude", Double.toString(this.longitude)));
            nameValuePair.add(new BasicNameValuePair("Address", this.address));
            nameValuePair.add(new BasicNameValuePair("Comments", this.comments));
            nameValuePair.add(new BasicNameValuePair("LastUpdated", this.lastUpdated));

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
        } else if (htmlUrl.equals("http://www.skyrealmstudio.com/cgi-bin/AcceptOrDenyFriendRequest.py")) {

            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(htmlUrl);

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("Username", user));
            nameValuePair.add(new BasicNameValuePair("Friend", this.pendingUser));
            nameValuePair.add(new BasicNameValuePair("YesOrNo", Integer.toString(YesOrNo)));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                response = httpClient.execute(httpPost);

                // writing response to log
                Log.d("Http Response:", response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else if (htmlUrl.equals("http://www.skyrealmstudio.com/cgi-bin/DeleteFriend.py")) {

            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(htmlUrl);

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("userLoggedIn", user));
            nameValuePair.add(new BasicNameValuePair("friend", this.friend));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                response = httpClient.execute(httpPost);

                // writing response to log
                Log.d("Http Response:", response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

}


