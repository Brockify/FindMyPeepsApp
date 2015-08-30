package com.skyrealm.brockyy.findmypeepsapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;

public class StatusActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String user;
    String Number;
    private static Timer timer;
    int seconds;
    int newSeconds;
    private Double latitude;
    private Double longitude;
    private String lastUpdated;
    android.os.Handler mainHandler;
    GPSTracker gps;
    SwipeRefreshLayout swipeLayout;
    ListView notificationList;
    ArrayList<String> notifications = new ArrayList <String>();
    ArrayList<Bitmap> userIcons = new ArrayList<Bitmap>();
    ArrayList<String> usernameArrayList = new ArrayList<String>();
    ArrayList<String> dateArrayList = new ArrayList<String>();
    ArrayList<String> timeArrayList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //setup the actionbar first
        getSupportActionBar().setDisplayOptions(DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.statusactivity_actionbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3366CC")));
        final View statusView = findViewById(R.id.statusActivity);
        notificationList = (ListView) findViewById(R.id.notificationsListView);
        //DECLARATION
        user = getIntent().getExtras().getString("username");
        Number = getIntent().getExtras().getString("Number");
        seconds = getIntent().getExtras().getInt("seconds");
        gps = new GPSTracker(StatusActivity.this);

        //On touch swipe listener for swipe right method
        statusView.setOnTouchListener(new OnSwipeTouchListener(StatusActivity.this) {
            //calls on the swipeRight method
            public void onSwipeRight() {
                Intent intent = new Intent(StatusActivity.this, PendingFriendsActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);
            }
        });
        //set on swipe left and right for the listview too
        notificationList.setOnTouchListener(new OnSwipeTouchListener(StatusActivity.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(StatusActivity.this, PendingFriendsActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);
            }
        });
        //set a swipe refresh layout
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(StatusActivity.this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

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
        notificationList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (notificationList != null && notificationList.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = notificationList.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = notificationList.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeLayout.setEnabled(enable);
            }
        });

        ImageButton pendingUserActionBarButton = (ImageButton) findViewById(R.id.pendingUserActionButton);
        pendingUserActionBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StatusActivity.this, PendingFriendsActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                if (timer != null)
                    timer.cancel();
                intent.putExtra("seconds", newSeconds);
                startActivity(intent);
            }
        });

        new getNotifications().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent ii = new Intent(StatusActivity.this, UserSettings.class);
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
            Intent ii = new Intent(StatusActivity.this, Login.class);
            if (timer != null)
                timer.cancel();
            startActivity(ii);
            finish();
            return true;
        }
        if (id == R.id.action_profile) {
            Intent ii = new Intent(StatusActivity.this, Profile.class);
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
    @Override
    public void onRefresh() {
        notifications.clear();
        ListView notificationListview = (ListView) findViewById(R.id.notificationsListView);
        notificationListview.setAdapter(null);
        new getNotifications().execute();
    }

    class getNotifications extends AsyncTask<String, Void, Void> {
        protected void onPreExecute() {
            super.onPreExecute();
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
        }

        @Override
        protected Void doInBackground(String... strings) {
            HttpResponse response;
            String responseStr = null;
            String notification;
            String usernames;
            JSONObject obj = null;
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://www.skyrealmstudio.com/cgi-bin/GetNotifications.py");
            JSONArray json = null;


            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", user));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                response = httpclient.execute(httppost);
                responseStr = EntityUtils.toString(response.getEntity());

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            System.out.println(responseStr);
            try {
                json = new JSONArray(responseStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int counter = 0; counter < json.length(); counter++)
                try {
                    notification = json.getJSONObject(counter).getString("notification");
                    usernames = json.getJSONObject(counter).getString("username");
                    char secondNum = notification.charAt(11);
                    char firstNum = notification.charAt(12);
                    String numberDone = String.valueOf(secondNum) + String.valueOf(firstNum);
                    String finalText;
                    String date;
                    String time;
                    if(Integer.parseInt(numberDone) > 12)
                    {
                        int numberTesting = Integer.parseInt(numberDone);
                        int finalNum;
                        finalNum = numberTesting - 12;
                        if (finalNum >= 10)
                        {
                            finalText = String.valueOf(finalNum);
                        } else {
                            finalText = "0" + String.valueOf(finalNum);
                        }

                        String finalNotification;
                        finalNotification = notification.substring(0,11) + finalText + notification.substring(13);
                        notification = finalNotification;
                    }
                    date = notification.substring(0, 10);
                    time = notification.substring(11, 22);
                    Bitmap userIcon = null;
                    String urldisplay = "http://skyrealmstudio.com/img/" + usernames.toLowerCase() + ".jpg";
                    InputStream in = null;
                    try {
                        in = new URL(urldisplay).openStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    userIcon = BitmapFactory.decodeStream(in);
                        //make the icon a circle,'
                    userIcon = userIcon.createScaledBitmap(userIcon, userIcon.getWidth(), userIcon.getHeight(), false);
                    usernameArrayList.add(usernames);
                    int length = usernames.length();
                   notification = notification.substring(24 + length + 1);
                    notifications.add(notification);
                    userIcons.add(userIcon);
                    dateArrayList.add(date);
                    String tempTime;
                    tempTime = time.substring(0, 1) + time.substring(1, 2);
                    if (Integer.parseInt(tempTime) < 10) {
                        time = time.substring(1, 5) + time.substring(8);
                    } else {
                        time = time.substring(0, 5) + time.substring(8);
                    }
                    timeArrayList.add(time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            CustomAdapter adapter = new CustomAdapter(StatusActivity.this, notifications, userIcons, usernameArrayList, dateArrayList, timeArrayList);
            ListView list = (ListView) findViewById(R.id.notificationsListView);
            list.setAdapter(adapter);
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                }
            });
        }
    }
    //gets the location class (ASYNC)
    class getLocation extends AsyncTask<Void, Void, Void> {
        String address;

        @Override
        protected Void doInBackground(Void... params) {

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
            geocoder = new Geocoder(StatusActivity.this, Locale.getDefault());

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
