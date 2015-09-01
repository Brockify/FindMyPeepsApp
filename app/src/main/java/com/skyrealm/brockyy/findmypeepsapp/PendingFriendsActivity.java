package com.skyrealm.brockyy.findmypeepsapp;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class PendingFriendsActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

     static final String TAG_FROMUSER = "fromUser";
     private ArrayList<HashMap<String, String>> pendingUsers;
     private HttpResponse response;
     private String responseBody;
     private SwipeRefreshLayout swipeLayout;
     private String user;
    String Number;
    private Double latitude;
    private Double longitude;
    private String lastUpdated;
    android.os.Handler mainHandler;
    int seconds;
    int newSeconds;
    GPSTracker gps;
    private static Timer timer;
    ProgressDialog pDialog;
    EditText friendEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendingfriends);
        //setup the actionbar first
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.pendingactivity_actionbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3366CC")));

        pDialog = new ProgressDialog(PendingFriendsActivity.this);
        //declare variables
        Button addFriendButton = (Button) findViewById(R.id.addFriendButton);
        final View friendView = findViewById(R.id.friendsActivity);
        final ListView friendsList = (ListView) findViewById(R.id.friendslistView);
        //DECLARATION
        pendingUsers = new ArrayList<HashMap<String, String>>();
        user = getIntent().getExtras().getString("username");
        Number = getIntent().getExtras().getString("Number");
        seconds = getIntent().getExtras().getInt("seconds");
        gps = new GPSTracker(PendingFriendsActivity.this);

        //set a swipe refresh layout
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        new GetPendingRequests().execute();
        // EXAMPLE:
        // final String latitude = getIntent().getExtras().getString("latitude");
        //On touch swipe listener for swipe right method
        friendView.setOnTouchListener(new OnSwipeTouchListener(PendingFriendsActivity.this) {
            //calls on the swipeRight method
            public void onSwipeRight() {
                Intent intent = new Intent(PendingFriendsActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);

            }
            public void onSwipeLeft()
            {
                Intent intent = new Intent(PendingFriendsActivity.this, StatusActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);
            }
        });

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PendingFriendsActivity.this);
                builder.setView(R.layout.activity_popup_comment);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog dialoger = (Dialog) dialog;
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                friendEditText = (EditText) dialoger.findViewById(R.id.commentEditText) ;
                                if (friendEditText.length() > 0) {
                                    new addFriendClass().execute();
                                } else {
                                    Toast.makeText(PendingFriendsActivity.this, "Friend request not sent. Friend not found.", Toast.LENGTH_LONG).show();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                //build the dialog box
                builder.setTitle("Add Friend");
                builder.setPositiveButton("Send Request", dialogClickListener);
                builder.setNegativeButton("Cancel", dialogClickListener);
                builder.create();
                builder.show();
            }
        });
        //end the swipe command

        //set on swipe left and right for the listview too
        friendsList.setOnTouchListener(new OnSwipeTouchListener(PendingFriendsActivity.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(PendingFriendsActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);
            }
            public void onSwipeLeft()
            {
                Intent intent = new Intent(PendingFriendsActivity.this, StatusActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);
            }
        });
        mainHandler = new android.os.Handler(Looper.getMainLooper());


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
                    } else {
                        newSeconds = (seconds - (i % seconds));
                        System.out.println("Seconds = " + newSeconds);
                    }
                }
            };
            timer.schedule(task, 0, 1000);
        }

        //only allows the swipe if it is at the top of the list
        friendsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (friendsList != null && friendsList.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = friendsList.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = friendsList.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeLayout.setEnabled(enable);
            }
        });

        ImageButton friendUserActionBarButton = (ImageButton) findViewById(R.id.friendsActionBarButton);
        ImageButton notificationActionBarButton = (ImageButton) findViewById(R.id.notificationActionBarButton);

        friendUserActionBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PendingFriendsActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);
            }
        });
        notificationActionBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PendingFriendsActivity.this, StatusActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);
            }
        });
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
            Intent ii = new Intent(PendingFriendsActivity.this, UserSettings.class);
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
            Intent ii = new Intent(PendingFriendsActivity.this, Login.class);
            if (timer != null)
                timer.cancel();
            startActivity(ii);
            finish();
            return true;
        }
        if (id == R.id.action_profile) {
            Intent ii = new Intent(PendingFriendsActivity.this, Profile.class);
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

    //slide up to refresh
    @Override
    public void onRefresh() {
        pendingUsers.clear();
        ListView pendingListView = (ListView) findViewById(R.id.friendslistView);
        pendingListView.setAdapter(null);
        new GetPendingRequests().execute();
    }

    //Asynchronus class to do background data away from the main thread.
    class GetPendingRequests extends AsyncTask<Void, Void, Void> {
        protected void onPreExecute()
        {
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
        }

    //call what to do in the asynchronus task
        @Override
        protected Void doInBackground(Void... params) {
           //start the post to the database
            String responseBody = null;
            HttpResponse response;
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost("http://www.skyrealmstudio.com/cgi-bin/GetPendingList.py");

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("username", user));
            nameValuePair.add(new BasicNameValuePair("Number", Number));

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                response = httpClient.execute(httpPost);
                responseBody = EntityUtils.toString(response.getEntity());
                // writing response to log
            } catch (IOException e) {
                e.printStackTrace();
            }
        //end the post response

        //JSON the string that is got from the post.
            String jsonStr = responseBody;

            if (jsonStr != null) {
                try {
                    JSONArray jsonArr = new JSONArray(jsonStr);
                    for (int i = 0; i < jsonArr.length(); i++) {
                        {

                            JSONObject c = jsonArr.getJSONObject(i);
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
            //done
        }
    //done doing in the background

    //after the asynchronus post is done, execute this method
        @Override
        protected void onPostExecute(Void result) {
            {
                ListView pendingListView = (ListView) findViewById(R.id.friendslistView);
                //setup a list adapter and then set that on the list
                ListAdapter adapter = new SimpleAdapter(
                        PendingFriendsActivity.this, pendingUsers,
                        R.layout.pending_requests_list_item, new String[] {TAG_FROMUSER}, new int[] { R.id.name});
                pendingListView.setAdapter(adapter);
                //done setting on the list adapter
                swipeLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                });
            }
        }
    }

    //Whenever an ACCEPT button is clicked on the listView it will post to the server.
    public void addButtonClicked(View view)
    {

        //get the position of the button
        RelativeLayout vwParentRow = (RelativeLayout)view.getParent();
        TextView pending =(TextView)vwParentRow.findViewById(R.id.name);
        Button accepted = (Button)vwParentRow.findViewById(R.id.addButton);
        Button declined = (Button)vwParentRow.findViewById(R.id.declineButton);
        TextView acceptedOrDeclined = (TextView)vwParentRow.findViewById(R.id.acceptedOrDeclined);

        //extract the text
        String pendingUserText = pending.getText().toString();
        Toast.makeText(PendingFriendsActivity.this, pendingUserText + " added as a friend", Toast.LENGTH_LONG).show();

        //send the post
        HTTPSendPost httpPost = new HTTPSendPost();
        httpPost.SetUpOnlyUrl(user, "http://www.skyrealmstudio.com/cgi-bin/AcceptOrDenyFriendRequest.py", pendingUserText, 1, Number);
        httpPost.execute();
        //end post

        //make the buttons not visible
        accepted.setVisibility(View.GONE);
        declined.setVisibility(View.GONE);
        acceptedOrDeclined.setText("Friend added");
        acceptedOrDeclined.setVisibility(View.VISIBLE);
        pending.setVisibility(View.GONE);

    }


    public void declineButtonClicked(View view)
    {
        //get the position of the button
        RelativeLayout vwParentRow = (RelativeLayout)view.getParent();
        TextView pending =(TextView)vwParentRow.findViewById(R.id.name);
        Button accepted = (Button)vwParentRow.findViewById(R.id.addButton);
        Button declined = (Button)vwParentRow.findViewById(R.id.declineButton);
        TextView acceptedOrDeclined = (TextView)vwParentRow.findViewById(R.id.acceptedOrDeclined);

        //extract the text
        String pendingUserText = pending.getText().toString();
        Toast.makeText(PendingFriendsActivity.this, pendingUserText + " declined", Toast.LENGTH_LONG).show();

        //send the post
        HTTPSendPost httpPost = new HTTPSendPost();
        httpPost.SetUpOnlyUrl(user, "http://www.skyrealmstudio.com/cgi-bin/AcceptOrDenyFriendRequest.py", pendingUserText, 0, Number);
        httpPost.execute();
        //end post

        //make the buttons not visible
        accepted.setVisibility(View.GONE);
        declined.setVisibility(View.GONE);
        acceptedOrDeclined.setText("Friend declined");
        acceptedOrDeclined.setVisibility(View.VISIBLE);
        pending.setVisibility(View.GONE);
    }

    class addFriendClass extends AsyncTask<Void, Void, Boolean>
    {
        protected void onPreExecute()
        {
            pDialog = new ProgressDialog(PendingFriendsActivity.this);
            pDialog.setMessage("Adding friend...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //start the post to the database
            response = null;
            responseBody = null;
            HttpClient httpClient = new DefaultHttpClient();


            HttpPost httpPost = new HttpPost("http://www.skyrealmstudio.com/cgi-bin/SendFriendRequest.py");

            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("fromUser", user));
            nameValuePair.add(new BasicNameValuePair("toUser", friendEditText.getText().toString()));
            nameValuePair.add(new BasicNameValuePair("Number", Number));


            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                response = httpClient.execute(httpPost);
                responseBody = EntityUtils.toString(response.getEntity());

                // writing response to log
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Boolean Result)
        {
            if (responseBody.equals("\nFailed\n")) {
                //end the post response
                    Toast.makeText(PendingFriendsActivity.this, "Friend request not sent. Friend not found.", Toast.LENGTH_LONG).show();
            }else if(responseBody.equals("\nAlready your friend\n"))
            {
                    Toast.makeText(PendingFriendsActivity.this, "Friend request not sent. User already your friend.", Toast.LENGTH_LONG).show();
            }
          else if(responseBody.equals("\nCannot add yourself\n"))
            {
                    Toast.makeText(PendingFriendsActivity.this, "Friend request not sent. Cannot add yourself.", Toast.LENGTH_LONG).show();

            }else if(responseBody.equals("\nAlready pending\n"))
            {
                    Toast.makeText(PendingFriendsActivity.this, "Friend request not sent. User already has request from you.", Toast.LENGTH_LONG).show();
            } else {

                    Toast.makeText(PendingFriendsActivity.this, "Friend request send.", Toast.LENGTH_LONG).show();
                }
            pDialog.dismiss();
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
            geocoder = new Geocoder(PendingFriendsActivity.this, Locale.getDefault());

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
            postSender.Setup(user, longitude, latitude, address, htmlUrl, "Auto updating", lastUpdated, time, Number);
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




