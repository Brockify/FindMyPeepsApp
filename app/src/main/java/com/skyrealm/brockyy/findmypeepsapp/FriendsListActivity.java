package com.skyrealm.brockyy.findmypeepsapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FriendsListActivity extends ActionBarActivity {

    ArrayList<HashMap<String, String>> FriendsList;
    private static final String TAG_FRIEND = "friend";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        setTitle("Friends");
        View friendsListView = findViewById(R.id.friendsListActivity);
        friendsListView.setOnTouchListener(new OnSwipeTouchListener(FriendsListActivity.this) {
            //calls on the swipeRight method
            public void onSwipeLeft() {
                Intent intent = new Intent(FriendsListActivity.this, PendingFriendsActivity.class);
                startActivity(intent);
            }

            public void onSwipeRight()
            {
                Intent intent = new Intent(FriendsListActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        FriendsList = new ArrayList<HashMap<String, String>>();

        new getFriendsList().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class getFriendsList extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            //start the post to the database
            String responseBody = null;
            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://brocksportfolio.com/GetPendingFriendsList.php");

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("Username", "Brock"));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                response = httpClient.execute(httpPost);
                responseBody = EntityUtils.toString(response.getEntity());
                // writing response to log
                Log.d("Http Response:", response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //end the post response
            String jsonStr = responseBody;

            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONArray jsonArr = new JSONArray(jsonStr);
                    for (int i = 0; i < jsonArr.length(); i++) {
                        {

                            JSONObject c = jsonArr.getJSONObject(i);
                            String Friend = c.getString(TAG_FRIEND);
                            HashMap<String, String> user = new HashMap<String, String>();
                            user.put(TAG_FRIEND, Friend);
                            FriendsList.add(user);
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            ListView list = (ListView) findViewById(R.id.friendListView);
            ListAdapter adapter = new SimpleAdapter(
                    FriendsListActivity.this, FriendsList,
                    R.layout.friends_list_items, new String[] {TAG_FRIEND}, new int[] { R.id.username});
            list.setAdapter(adapter);
        }

    }
}
