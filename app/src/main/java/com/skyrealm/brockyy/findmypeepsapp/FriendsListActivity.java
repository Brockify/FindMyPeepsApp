package com.skyrealm.brockyy.findmypeepsapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class FriendsListActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    ArrayList<HashMap<String, String>> FriendsList;
    private String user;
    private String userBeingClicked;
    private Double latitude;
    private Double longitude;
    private String lastUpdated;
    String Number;
    Handler mainHandler;
    int seconds;
    int newSeconds;
    private Double userLatitude;
    private Double userLongitude;
    private String userComment;
    private String userUsername;
    final String[] userDelete = {null};
    private static final String TAG_FRIEND = "friend";
    private ProgressDialog pDialog;
    GPSTracker gps;
    private static Timer timer;
    private SwipeRefreshLayout swipeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        //setup the actionbar first
        getSupportActionBar().setDisplayOptions(android.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.friendsactivity_actionbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3366CC")));
        //set title of the activity
        seconds = getIntent().getExtras().getInt("seconds");

        //declare variables
        final ListView friendsList = (ListView) findViewById(R.id.friendListView);
        View friendsListView = findViewById(R.id.friendsListActivity);
        //gets the username of user logged in
        user = getIntent().getExtras().getString("username");
        Number = getIntent().getExtras().getString("Number");
        gps = new GPSTracker(FriendsListActivity.this);
        friendsListView.setOnTouchListener(new OnSwipeTouchListener(FriendsListActivity.this) {
            //calls on the swipeLeft method
            public void onSwipeLeft() {
                if (timer != null)
                    timer.cancel();
                Intent intent = new Intent(FriendsListActivity.this, PendingFriendsActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("seconds", newSeconds);
                intent.putExtra("Number", Number);
                finish();
                startActivity(intent);
            }

            //calls on the swipeRight method
            public void onSwipeRight() {
                if (timer != null)
                    timer.cancel();
                Intent intent = new Intent(FriendsListActivity.this, MainActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("seconds", newSeconds);
                intent.putExtra("Number", Number);
                finish();
                startActivity(intent);
            }
        });
        //set on swipe left and right for the listview too
        friendsList.setOnTouchListener(new OnSwipeTouchListener(FriendsListActivity.this) {
            public void onSwipeLeft() {
                if (timer != null)
                    timer.cancel();
                Intent intent = new Intent(FriendsListActivity.this, PendingFriendsActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("seconds", newSeconds);
                intent.putExtra("Number", Number);
                startActivity(intent);
            }

            public void onSwipeRight() {
                Intent intent = new Intent(FriendsListActivity.this, MainActivity.class);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("username", user);
                intent.putExtra("seconds", newSeconds);
                intent.putExtra("Number", Number);
                startActivity(intent);
            }

        });
        mainHandler = new Handler(Looper.getMainLooper());

        friendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                    TextView tv = (TextView) v.findViewById(R.id.username);

                    userBeingClicked = tv.getText().toString();

                    userUsername = userBeingClicked;
                    new getSpecificUserLocation().execute();
            }
        });
        if (seconds != 0) {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                int i = 0;

                @Override
                public void run() {
                    i++;
                    //do something
                    if (i % seconds == 0) {
                        //run the script on the main thread
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                seconds = 60;
                                new getLocation().execute();
                            } // This is your code
                        };
                        mainHandler.post(myRunnable);
                    }else {
                        newSeconds = (seconds - (i % seconds));
                        System.out.println("Seconds = " + newSeconds);
                    }
                }
            };
            timer.schedule(task, 0, 1000);

    }
        //set a swipe refresh layout
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(FriendsListActivity.this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

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
            Intent ii = new Intent(FriendsListActivity.this, UserSettings.class);
            if (timer != null)
                timer.cancel();
            ii.putExtra("username", user);
            ii.putExtra("seconds", newSeconds);
            ii.putExtra("Number", Number);
            finish();
            // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
            startActivity(ii);
            return true;
        }
        if (id == R.id.action_logout) {
            Intent ii = new Intent(FriendsListActivity.this, Login.class);
            if (timer != null)
                timer.cancel();
            startActivity(ii);
            finish();
            return true;
        }
        if (id == R.id.action_profile) {
            Intent ii = new Intent(FriendsListActivity.this, Profile.class);
            ii.putExtra("username", user);
            ii.putExtra("seconds", newSeconds);
            ii.putExtra("Number", Number);
            if (timer != null)
                timer.cancel();
            finish();
            // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
            startActivity(ii);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteFriend(View view)
    {
        RelativeLayout vwParentRow = (RelativeLayout)view.getParent();

        final Button deleteButton = (Button) vwParentRow.findViewById(R.id.deleteButton);
        final TextView userDeleteText = (TextView) vwParentRow.findViewById(R.id.username);
        final Button profileButton = (Button) vwParentRow.findViewById(R.id.profileButton);

        //sends a alert dialog making sure they want to delete the user
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        // send post
                        String htmlUrl = "http://www.skyrealmstudio.com/cgi-bin/DeleteFriend.py";
                        HTTPSendPost sendPost = new HTTPSendPost();
                        userDelete[0] = userDeleteText.getText().toString();
                        sendPost.setUpOnDeleteFriend(user, userDelete[0], htmlUrl);
                        sendPost.execute();
                        Toast.makeText(getApplicationContext(), userDelete[0] + " deleted.", Toast.LENGTH_LONG).show();
                        //set everything to be not visible
                        deleteButton.setVisibility(View.GONE);
                        profileButton.setVisibility(View.GONE);
                        userDeleteText.setVisibility(View.GONE);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        //

        //show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(FriendsListActivity.this);
        builder.setMessage("Are you sure you would like to delete " + userDeleteText.getText().toString() + " as a friend.").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
        //
    }

    public void profileButtonClicked(View view)
    {
        RelativeLayout vwParentRow = (RelativeLayout)view.getParent();

        final TextView otherUserText = (TextView) vwParentRow.findViewById(R.id.username);
        if (timer != null)
            timer.cancel();
        Intent intent = new Intent(FriendsListActivity.this, FProfile.class);
        intent.putExtra("username", user);
        intent.putExtra("otherUser", otherUserText.getText().toString());
        intent.putExtra("seconds", newSeconds);
        intent.putExtra("Number", Number);
        startActivity(intent);
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
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(FriendsListActivity.this);
            pDialog.setMessage("Getting friends...");
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            //start the post to the database
            String responseBody = null;
            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://skyrealmstudio.com/cgi-bin/GetFriendsList.py");

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("username", user));

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
            pDialog.dismiss();
        }
    }

    class getSpecificUserLocation extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(FriendsListActivity.this);
            pDialog.setMessage("Getting " + userBeingClicked + "'s location..");
            pDialog.setIndeterminate(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //start the post to the database
            String   responseBody = null;
            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://skyrealmstudio.com/cgi-bin/GetSpecificUsersLocation.py");

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

                            Double userClickedLatitude = c.getDouble("latitude");
                            Double userClickedLongitude = c.getDouble("longitude");
                            String comment = c.getString("comments");
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
            String address = null;
            if (latitude == null || longitude == null) {
                Toast.makeText(getApplicationContext(), "User has not updated their location.", Toast.LENGTH_LONG).show();

            } else {
                userLatitude = latitude;
                userLongitude = longitude;
                List<Address> addresses = null;
                Geocoder geocoder = new Geocoder(FriendsListActivity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(userLatitude, userLongitude, 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), userUsername + "'s address: " + address, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(FriendsListActivity.this, MainActivity.class);
                intent.putExtra("otherLat", userLatitude);
                intent.putExtra("otherLong", userLongitude);
                intent.putExtra("isOtherUserClicked", true);
                intent.putExtra("username", user);
                intent.putExtra("otherComment", userComment);
                intent.putExtra("userUsername", userUsername);
                intent.putExtra("Number", Number);
                finish();
                startActivity(intent);
                pDialog.dismiss();
            }
        }
    }
    //gets the location class (ASYNC)
    class getLocation extends AsyncTask<Void, Void, Void> {
        String address;

        @Override
        protected Void doInBackground(Void... params) {

            final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);

            //If the update location button is clicked------------------------------------------\
            latitude = gps.getLocation().getLatitude();
            longitude = gps.getLocation().getLongitude();

            //get time and date
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int amorpmint = c.get(Calendar.AM_PM);
            String amorpm;
            if (amorpmint == 0) {
                amorpm = "AM";
            } else {
                amorpm = "PM";
            }

            lastUpdated = df.format(c.getTime()) + " " + amorpm;

            //getting the street address---------------------------------------------------;
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(FriendsListActivity.this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                address = addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
            String time = timef.format(c.getTime()) + " " + amorpm;

            //website to post too
            String htmlUrl = "http://skyrealmstudio.com/cgi-bin/updatelocation.py";

            //send the post and execute it
            HTTPSendPost postSender = new HTTPSendPost();
            postSender.Setup(user, longitude, latitude, address, htmlUrl, "Auto updating", lastUpdated, time);
            postSender.execute();
            //done executing post

            //finished getting the street address-----------------------------------------
            return null;
        }
        // end showing it on the map ------------------------------------------------------------------------

        //this happens whenever an async task is done
        public void onPostExecute(Void result) {
            //if the address comes back null send a toast
            if (address == null) {
                Toast.makeText(getApplicationContext(), "Could not update location! Try again.", Toast.LENGTH_LONG).show();
            } else {
                //if it is the first time clicking get location
                Toast.makeText(getApplicationContext(), "Updated location!", Toast.LENGTH_LONG).show();
            }
            gps.stopUsingGps();

        }
    }
}
