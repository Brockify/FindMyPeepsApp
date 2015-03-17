package com.skyrealm.brockyy.findmypeepsapp;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;


public class FriendsActivity extends ActionBarActivity{

    private static final String TAG_FROMUSER = "fromUser";
    JSONArray pendingRequests = null;
    ArrayList<HashMap<String, String>> pendingUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        //DECLARATION
        ListView list = (ListView) findViewById(R.id.friendslistView);
        OnSwipeTouchListener swipeRight;
        View friendView = findViewById(R.id.friendsActivity);



       // EXAMPLE:
       // final String latitude = getIntent().getExtras().getString("latitude");
        //On touch swipe listener for swipe right method
        friendView.setOnTouchListener(new OnSwipeTouchListener(FriendsActivity.this) {
            //calls on the swipeRight method
            public void onSwipeRight() {
                Intent intent = new Intent(FriendsActivity.this, MainActivity.class);
                startActivity(intent);
            }

        });
        //end the swipe command
        //send post request
        String htmlUrl = "http://brocksportfolio.com/GetPendingRequests.php";

        HTTPSendPost postSender = new HTTPSendPost();
        postSender.Setup(500, 050, "tesT", htmlUrl);
        postSender.execute();
        //end sending post request

        //create new class object
        GetPendingRequests pendingRequest = new GetPendingRequests();
        //do in background
        pendingRequest.doInBackground(pendingUsers, pendingRequests);

        //create array adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_friends, (java.util.List<String>) pendingRequests);

        //set list adapter
        list.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
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
}

class GetPendingRequests extends AsyncTask<Void, Void, Void> {

    private static final String TAG_FROMUSER = "fromUser";

    protected Void doInBackground(ArrayList<HashMap<String, String>> pendingUsers, JSONArray pendingRequests) {
        HTTPSendPost httpSendPost = new HTTPSendPost();
        String jsonStr = httpSendPost.doInBackground("http://www.brocksportfolio.com/GetPendingRequests.php");

        Log.d("Response: ", "> " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
               pendingRequests = jsonObj.getJSONArray(TAG_FROMUSER);
                for (int i = 0; i < pendingRequests.length(); i++) {
                    {

                        JSONObject c = pendingRequests.getJSONObject(i);
                        String fromUser = c.getString(TAG_FROMUSER);
                        HashMap<String, String> user = new HashMap<String, String>();
                        user.put(TAG_FROMUSER, fromUser);
                        pendingUsers.add(user);
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }
}



