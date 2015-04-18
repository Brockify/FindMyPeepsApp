package com.skyrealm.brockyy.findmypeepsapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;


public class FriendsListActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    ArrayList<HashMap<String, String>> FriendsList;
    private String user;
    private String userBeingClicked;
    private String latitude;
    private String longitude;
    private Double userLatitude;
    private Double userLongitude;
    private String userComment;
    private String userUsername;
    private static final String TAG_FRIEND = "friend";
    private static final String TAG_LATITUDE = "latitude";
    private static final String TAG_LONGITUDE = "longitude";
    private static final String TAG_COMMENTS = "comments";
    private SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        //set title of the activity
        setTitle("Friends");


        //set onswipe refresh
        //set a swipe refresh layout
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(FriendsListActivity.this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        // Configure the swipe refresh layout
        //declare variables
        View friendsListView = findViewById(R.id.friendsListActivity);
        ListView friendsList = (ListView) findViewById(R.id.friendListView);

        //gets the username of user logged in
        user = getIntent().getExtras().getString("username");
        friendsListView.setOnTouchListener(new OnSwipeTouchListener(FriendsListActivity.this) {
            //calls on the swipeLeft method
            public void onSwipeLeft() {
                Intent intent = new Intent(FriendsListActivity.this, PendingFriendsActivity.class);
                intent.putExtra("username", user);
                startActivity(intent);
            }

            //calls on the swipeRight method
            public void onSwipeRight() {
                Intent intent = new Intent(FriendsListActivity.this, MainActivity.class);
                intent.putExtra("username", user);
                startActivity(intent);
            }
        });

        //set on swipe left and right for the listview too
        friendsList.setOnTouchListener(new OnSwipeTouchListener(FriendsListActivity.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(FriendsListActivity.this, PendingFriendsActivity.class);
                intent.putExtra("username", user);
                startActivity(intent);
            }

            public void onSwipeRight() {
                Intent intent = new Intent(FriendsListActivity.this, MainActivity.class);
                intent.putExtra("username", user);
                startActivity(intent);
            }

        });


        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                RelativeLayout vwParentRow = (RelativeLayout) v.getParent();
                final TextView tv = (TextView) vwParentRow.findViewById(R.id.username);
                final Button deleteButton = (Button) vwParentRow.findViewById(R.id.deleteButton);
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteButton.setText("Deleted");

                    }
                });
                userBeingClicked = tv.getText().toString();
                userUsername = userBeingClicked;

                new getSpecificUserLocation().execute();
            }
        });


        //declare new FriendsList as ArrayList
        FriendsList = new ArrayList<HashMap<String, String>>();

        //Execute the AsynchronusTask for the post request
        new getFriendsList().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
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
        if(id == R.id.action_logout) {
            Intent ii = new Intent(FriendsListActivity.this, Login.class);
            finish();
            // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
            startActivity(ii);
            return true;
        }
        if(id == R.id.action_profile) {
            Intent ii = new Intent(FriendsListActivity.this, Profile.class);
            ii.putExtra("username", user);
            finish();
            // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
            startActivity(ii);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        FriendsList.clear();
        ListView pendingListView = (ListView) findViewById(R.id.friendListView);
        pendingListView.setAdapter(null);
        swipeLayout.setRefreshing(true);
        new getFriendsList().execute();
        swipeLayout.setRefreshing(false);
    }

    class getFriendsList extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            //start the post to the database
            String responseBody = null;
            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://skyrealmstudio.com/GetPendingFriendsList.php");

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("Username", user));

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

    class getSpecificUserLocation extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params) {
            //start the post to the database
            String responseBody = null;
            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://skyrealmstudio.com/GetSpecificUserLocation.php");

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("Username", userBeingClicked));

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

            //JSON the string that is got from the post.
            String jsonStr = responseBody;

            Log.d("Response: ", "> " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONArray jsonArr = new JSONArray(jsonStr);
                    for (int i = 0; i < jsonArr.length(); i++) {
                        {

                            JSONObject c = jsonArr.getJSONObject(i);

                            String userClickedLatitude = c.getString(TAG_LATITUDE);
                            String userClickedLongitude = c.getString(TAG_LONGITUDE);
                            String comment = c.getString(TAG_COMMENTS);
                            longitude = userClickedLongitude;
                            latitude = userClickedLatitude;
                            userComment = comment;
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {

            if (latitude.isEmpty() || longitude.isEmpty()) {
                Toast.makeText(getApplicationContext(), "User has not updated their location.", Toast.LENGTH_LONG).show();

            } else {
                userLatitude = Double.parseDouble(latitude);
                userLongitude = Double.parseDouble(longitude);
                List<Address> addresses = null;
                Geocoder geocoder = new Geocoder(FriendsListActivity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(userLatitude, userLongitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String address = addresses.get(0).getAddressLine(0);

                Toast.makeText(getApplicationContext(), "Address: " + address, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(FriendsListActivity.this, MainActivity.class);
                intent.putExtra("otherLat", userLatitude);
                intent.putExtra("otherLong", userLongitude);
                intent.putExtra("isTrue", true);
                intent.putExtra("username", user);
                intent.putExtra("otherComment", userComment);
                intent.putExtra("userUsername", userUsername);
                startActivity(intent);
            }
        }
    }
}
