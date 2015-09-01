package com.skyrealm.brockyy.findmypeepsapp;


import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import android.os.Bundle;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.model.people.Person;

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
import java.util.*;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, GoogleMap.OnInfoWindowClickListener {
    //Global variables declaration
    //current user
    String user;
    String Number;
    Double latitude;
    Double longitude;
    String lastUpdated = null;
    Marker userMarker = null;
    LatLng userCurrentLocation;
    int seconds;
    int newSeconds;
    private static Timer timer;
    //other user
    LatLng otherUserLocation;
    boolean isOtherUserClicked;
    Marker otherUserMarker;
    double otherUserLat;
    double otherUserLong;
    String otherUserUsername;
    String otherUserComment;
    //general
    String address;
    String comments;
    MapFragment googleMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private ProgressDialog pDialog;
    private Toast backtoast;
    GPSTracker gps;
    Intent MainIntent;
    LatLngBounds bounds;
    Button getLocationButton;
    private ArrayList<MarkerOptions> mMyMarkersArray = new ArrayList<MarkerOptions>();
    LatLngBounds friendsListBoundaries;
    Handler mainHandler;
    Bitmap icon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainIntent = new Intent(MainActivity.this, MainActivity.class);
        icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.action_logo);
        //setup the actionbar first
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.mainactivity_actionbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3366CC")));
        //DECLARATIONS-----------------------------------------------------------------------
        View mainView = findViewById(R.id.mainActivity);
        getLocationButton = (Button) findViewById(R.id.getLocationButton);
        googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        user = getIntent().getExtras().getString("username");
        Number = getIntent().getExtras().getString("Number");
        isOtherUserClicked = getIntent().getExtras().getBoolean("isOtherUserClicked");
        seconds = getIntent().getExtras().getInt("seconds");
        Log.d("Message:", "Seconds = " + seconds);
        //set OnClickListeners
        getLocationButton.setOnClickListener(this);

        //set Main Intent
        MainIntent = getIntent();

        //create new gps with MainActivity as context
        gps = new GPSTracker(MainActivity.this);

        //set the map when created
        googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);

        //sets a listener for the map
        //if a specific user is clicked, zoom into that user and your current location
        googleMap.getMap().setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            //called once the map is done loading
            public void onMapLoaded() {
                if (gps.isGPSEnabledOrNot()) {
                    latitude = gps.getLocation().getLatitude();
                    longitude = gps.getLocation().getLongitude();
                    userCurrentLocation = new LatLng(latitude, longitude);
                    if (isOtherUserClicked) {
                        //get the extras
                        otherUserLat = getIntent().getExtras().getDouble("otherLat");
                        otherUserLong = getIntent().getExtras().getDouble("otherLong");
                        otherUserUsername = getIntent().getExtras().getString("userUsername");
                        otherUserComment = getIntent().getExtras().getString("otherComment");
                        //zoom to show both the users location and the user clicked location
                        otherUserLocation = new LatLng(otherUserLat, otherUserLong);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(userCurrentLocation);
                        builder.include(otherUserLocation);
                        bounds = builder.build();
                        String urlTest = "http://skyrealmstudio.com/img/" + otherUserUsername.toLowerCase() + ".jpg";
                        new DownloadImageTask().execute(urlTest, otherUserUsername);
                        int padding = 50;
                        googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                        userMarker = googleMap.getMap().addMarker(new MarkerOptions().title(user).position(userCurrentLocation).icon(BitmapDescriptorFactory.fromBitmap(icon)));
                    } else {
                        //set friends on the map
                        new MarkerScript().execute();
                        userMarker = googleMap.getMap().addMarker(new MarkerOptions().title(user).position(userCurrentLocation).icon(BitmapDescriptorFactory.fromBitmap(icon)));

                    }
                }else {
                    gps.showSettingsAlert();
                }
            }
        });

        mainHandler = new Handler(Looper.getMainLooper());
        googleMap.getMapAsync(this);

        //build the google api client and connect too it (for the map)
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        //declare an OnSwipeListener and then call on the onSwipeLeft function--------
        mainView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                if (timer != null)
                    timer.cancel();
                Intent intent = new Intent(MainActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("seconds", newSeconds);
                intent.putExtra("Number", Number);
                startActivity(intent);
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
                                newSeconds = 0;
                                seconds = 60;
                                new getLocation().execute();
                            } // This is your code
                        };
                        mainHandler.post(myRunnable);
                    } else {
                        newSeconds = (seconds - (i % seconds));
                        System.out.println(newSeconds);
                    }
                }
            };
            timer.schedule(task, 0, 1000);
        }
        ImageButton friendActionButton = (ImageButton) findViewById(R.id.friendsActionBarButton);
        friendActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null)
                    timer.cancel();
                Intent intent = new Intent(MainActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("seconds", newSeconds);
                intent.putExtra("Number", Number);
                startActivity(intent);
            }
        });
    }

    //function to build the client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onBackPressed() {

        if (backtoast != null && backtoast.getView().getWindowToken() != null) {
            if (timer != null)
                timer.cancel();
            Intent intent = new Intent(this, Login.class);
            finish();
            startActivity(intent);
        } else {
            backtoast = Toast.makeText(this, "Press back to logout", Toast.LENGTH_SHORT);
            backtoast.show();
        }

    }

    //called when the activity is created (for the map)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        googleMap.setOnInfoWindowClickListener(this);
    }

    //get onClick codes
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //if Get Location button is clicked
            case R.id.getLocationButton:

                //build a dialog for sending the location
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(R.layout.activity_popup_comment);
                if (gps.isGPSEnabledOrNot()) {
                    //sends a alert dialog making sure they want to delete the user
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Dialog dialoger = (Dialog) dialog;
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    EditText commentEditText = (EditText) dialoger.findViewById(R.id.commentEditText) ;
                                    comments = commentEditText.getText().toString();
                                    new getLocation().execute();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    //build the dialog box
                    builder.setTitle("Send Location");
                    builder.setPositiveButton("Update Location", dialogClickListener);
                    builder.setNegativeButton("Cancel", dialogClickListener);
                    builder.create();
                    builder.show();
                } else {
                    gps.showSettingsAlert();
                }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent ii = new Intent(MainActivity.this, UserSettings.class);
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
            Intent ii = new Intent(MainActivity.this, Login.class);
            if (timer != null)
                timer.cancel();
            startActivity(ii);
            finish();
            return true;
        }
        if (id == R.id.action_profile) {
            Intent ii = new Intent(MainActivity.this, Profile.class);
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


    //when the map is connected
    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

        }
    }

    //when location is changed
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        new getLocation().execute();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }


//-------------------------------------------------

    //gets the location class (ASYNC)
    public class getLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            gps = new GPSTracker(MainActivity.this);
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Sharing your location...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

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
            if (amorpmint == 0)
            {
                amorpm = "AM";
            } else {
                amorpm = "PM";
            }

            lastUpdated = df.format(c.getTime()) + " " + amorpm;
            SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
            String time = timef.format(c.getTime()) + " " + amorpm;
            //getting the street address---------------------------------------------------;
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                address = addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //website to post too
            String htmlUrl = "http://skyrealmstudio.com/cgi-bin/updatelocation.py";

            //send the post and execute it
            HTTPSendPost postSender = new HTTPSendPost();
            postSender.Setup(user, longitude, latitude, address, htmlUrl, comments, lastUpdated, time, Number);
            postSender.execute();
            //done executing post

            //finished getting the street address-----------------------------------------
            return null;
        }
        // end showing it on the map ------------------------------------------------------------------------

        //this happens whenever an async task is done
        public void onPostExecute(Void result) {
            LatLngBounds.Builder temp = new LatLngBounds.Builder();

            userCurrentLocation = new LatLng(latitude, longitude);

            MapFragment googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
            //if the address comes back null send a toast
            if (address == null) {
                Toast.makeText(getApplicationContext(), "Could not update location! Try again.", Toast.LENGTH_LONG).show();
            } else {
                String urlTest = "http://skyrealmstudio.com/img/" + user.toLowerCase() + ".jpg";
                //if it is the first time clicking get location
                Toast.makeText(getApplicationContext(), "Updated location!", Toast.LENGTH_LONG).show();
                new DownloadImageTask().execute(urlTest);
                userCurrentLocation = new LatLng(latitude, longitude);
                if (mMyMarkersArray.size() == 0) {
                    googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 60));
                } else {
                    temp.include(userCurrentLocation);
                    for (int counter = 0; counter < mMyMarkersArray.size(); counter++) {
                        LatLng user = new LatLng(mMyMarkersArray.get(counter).getPosition().latitude, mMyMarkersArray.get(counter).getPosition().longitude);
                        temp.include(user);
                    }
                    LatLngBounds bound = temp.build();
                    googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bound, 100));
                }


                new DownloadImageTask().execute(urlTest, user);
            }
            userMarker.remove();
            userMarker = googleMap.getMap().addMarker(new MarkerOptions().position(userCurrentLocation).title(user).icon(BitmapDescriptorFactory.fromBitmap(icon)));
            gps.stopUsingGps();
            pDialog.dismiss();

        }
    }
    //--------------------------------------------Finish getLocation()-----------------------------------

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    //gets the profile pictures of a user when clicked
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            //add the other users location to the map

            Bitmap bhalfsize = result.createScaledBitmap(result, result.getWidth(), result.getHeight(), false);
            bhalfsize = getCroppedBitmap(bhalfsize);

            otherUserMarker = googleMap.getMap().addMarker(new MarkerOptions()
                    .position(new LatLng(otherUserLat, otherUserLong))
                    .title(otherUserUsername)
                    .snippet(otherUserComment)
                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize)));

        }
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

    class MarkerScript extends AsyncTask<String, Void, Void> {
        int userCounter = 0;
        protected void onPreExecute()
        {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading friends onto map...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(String... strings) {
            HttpResponse response;
            String responseStr = null;
            String username;
            String comment;
            String latitude;
            String longitude;
            String lastUpdated;

            JSONObject obj = null;
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://www.skyrealmstudio.com/cgi-bin/MarkerScript.py");
            JSONArray json = null;


            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", user));
                nameValuePairs.add(new BasicNameValuePair("Number", Number));
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
                    //parse the data
                    username = json.getJSONObject(counter).getString("Username");
                    comment = json.getJSONObject(counter).getString("Comment");
                    latitude = json.getJSONObject(counter).getString("Latitude");
                    longitude = json.getJSONObject(counter).getString("Longitude");
                    lastUpdated = json.getJSONObject(counter).getString("LastUpdated");
                    Bitmap userIcon = null;
                        String urldisplay = "http://skyrealmstudio.com/img/" + username.toLowerCase() + ".jpg";
                        try {
                            InputStream in = new URL(urldisplay).openStream();
                            userIcon = BitmapFactory.decodeStream(in);
                            //make the icon a circle,'
                            userIcon = userIcon.createScaledBitmap(userIcon, userIcon.getWidth(), userIcon.getHeight(), false);
                            userIcon = getCroppedBitmap(userIcon);
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            e.printStackTrace();
                        }

                    //log the data
                    if (latitude.equals("User did not update location") || longitude.equals("User did not update location") || latitude.equals("") || longitude.equals("")) {

                    } else {
                        MarkerOptions markerOption = new MarkerOptions().position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))).title(username).snippet(lastUpdated + "-" + comment).icon(BitmapDescriptorFactory.fromBitmap(userIcon));
                        mMyMarkersArray.add(userCounter, markerOption);
                        userCounter++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }
        //insert data onto map and set the boundaries
        protected void onPostExecute(Void result) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(int counter = 0; counter < mMyMarkersArray.size(); counter++) {
                LatLng userLatLng= new LatLng(mMyMarkersArray.get(counter).getPosition().latitude, mMyMarkersArray.get(counter).getPosition().longitude);
                builder.include(userLatLng);
            }
            for(int counter = 0; counter < mMyMarkersArray.size(); counter++)
            {
                googleMap.getMap().addMarker(mMyMarkersArray.get(counter));
            }
                if (userMarker != null) {
                    userMarker.remove();
                    userMarker = googleMap.getMap().addMarker(new MarkerOptions().title(user).position(userCurrentLocation).icon(BitmapDescriptorFactory.fromBitmap(icon)));
                    builder.include(userCurrentLocation);
                }
            friendsListBoundaries = builder.build();
                googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(friendsListBoundaries, 100));
                pDialog.cancel();

        }
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        // When Play services not found in device
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // Show Error dialog to install Play services
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            } else {
                Toast.makeText(MainActivity.this, "This device doesn't support Play services, App will not work normally", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        } else {
            Toast.makeText(MainActivity.this, "This device supports Play services, App will work normally", Toast.LENGTH_LONG).show();
        }
        return true;
    }
}