package com.skyrealm.brockyy.findmypeepsapp;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {
    //Global variables declaration
    String user;
    Double latitude;
    Double longitude;
    LatLng otherUserLocation;
    String address;
    String comments;
    Marker otherUserMarker;
    LatLng userCurrentLocation;
    MapFragment googleMap;
    boolean isOtherUserClicked;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private ProgressDialog pDialog;
    private Toast backtoast;
    GPSTracker gps;
    Intent MainIntent;
    LatLngBounds bounds;
    Button getLocationButton;
    boolean locationUpdatingOrNot = false;
    double otherUserLat;
    double otherUserLong;
    String otherUserUsername;
    String otherUserComment;
    private ArrayList<MarkerOptions> mMyMarkersArray = new ArrayList<MarkerOptions>();
    LatLngBounds friendsListBoundaries;
    LocationManager lm;
    Location location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainIntent = new Intent(MainActivity.this, MainActivity.class);


        setTitle("Locations Screen");
        //DECLARATIONS-----------------------------------------------------------------------
        TextView usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        View mainView = findViewById(R.id.mainActivity);
        getLocationButton = (Button) findViewById(R.id.getLocationButton);
        googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        user = getIntent().getExtras().getString("username");
        isOtherUserClicked = getIntent().getExtras().getBoolean("isOtherUserClicked");

        //set OnClickListeners
        getLocationButton.setOnClickListener(this);

        //set Main Intent
        MainIntent = getIntent();

        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

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
                if (isOtherUserClicked) {
                    int padding = 0;
                    googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }
            }
        });

        googleMap.getMapAsync(this);

        //build the google api client and connect too it (for the map)
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        //declare an OnSwipeListener and then call on the onSwipeLeft function--------
        mainView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(MainActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);

                startActivity(intent);
            }
        });


       //get friends list and put friends on the map
        new MarkerScript().execute();


        //set username
        usernameTextView.setText(user);
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
            finish();
        } else {
            backtoast = Toast.makeText(this, "Press back to logout", Toast.LENGTH_SHORT);
            backtoast.show();
        }

    }

    //called when the activity is created (for the map)
    @Override
    public void onMapReady(GoogleMap googleMap) {
            if(gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                //get the current users location
                userCurrentLocation = new LatLng(latitude, longitude);
                googleMap.setMyLocationEnabled(false);

                //if the a user from friend list was not clicked, just set the zoom to the user
                if (!isOtherUserClicked) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 13));
                    //else show the users location that was clicked
                } else {


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
                    String urlTest = "http://skyrealmstudio.com/img/" + otherUserUsername + ".jpg";
                    new DownloadImageTask().execute(urlTest, otherUserUsername);

                    //
                }
            }
    }

    //get onClick codes
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //if Get Location button is clicked
            case R.id.getLocationButton:
                if (gps.haveNetworkConnection()) {
                    String urlTest = "http://skyrealmstudio.com/img/" + user + ".jpg";
                    new getLocation().execute();
                } else {
                    gps.showSettingsAlert();
                }
        }
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
        if (id == R.id.action_logout) {
            Intent ii = new Intent(MainActivity.this, Login.class);
            finish();
            // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
            startActivity(ii);
            return true;
        }
        if (id == R.id.action_profile) {
            Intent ii = new Intent(MainActivity.this, Profile.class);
            ii.putExtra("username", user);
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


//-------------------------------------------------

    //gets the location class (ASYNC)
    public class getLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Sharing your location...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

                final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);

                //If the update location button is clicked------------------------------------------\

                    latitude = gps.getLocation().getLatitude();
                    longitude = gps.getLocation().getLongitude();
                    comments = commentEditText.getText().toString();


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
                    String htmlUrl = "http://skyrealmstudio.com/updatelocation.php";

                    //send the post and execute it
                    HTTPSendPost postSender = new HTTPSendPost();
                    postSender.Setup(user, longitude, latitude, address, htmlUrl, comments);
                    postSender.execute();
                    //done executing post

            //finished getting the street address-----------------------------------------
            return null;
        }
        // end showing it on the map ------------------------------------------------------------------------

        //this happens whenever an async task is done
        public void onPostExecute(Void result) {
            LatLngBounds.Builder temp = new LatLngBounds.Builder();
                pDialog.dismiss();
                    googleMap.getMap().setMyLocationEnabled(true);

                    userCurrentLocation = new LatLng(latitude, longitude);


                    MapFragment googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
                    TextView addressTextView = (TextView) findViewById(R.id.addressTextView);
                    EditText commentEditText = (EditText) findViewById(R.id.commentEditText);

                    //if the address comes back null send a toast
                    if (address == null) {
                        Toast.makeText(getApplicationContext(), "Could not update location! Try again.", Toast.LENGTH_LONG).show();
                    } else {
                        String urlTest = "http://skyrealmstudio.com/img/" + user + ".jpg";
                        //if it is the first time clicking get location
                        Toast.makeText(getApplicationContext(), "Updated location!", Toast.LENGTH_LONG).show();
                        addressTextView.setText(address);
                        commentEditText.setText(null);
                        new DownloadImageTask().execute(urlTest);
                        userCurrentLocation = new LatLng(latitude, longitude);
                        if (mMyMarkersArray.size() == 0) {
                            googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 60));
                        } else {
                            temp.include(userCurrentLocation);
                            for (int counter = 0; counter < mMyMarkersArray.size(); counter++) {
                                LatLng user = new LatLng(mMyMarkersArray.get(0).getPosition().latitude, mMyMarkersArray.get(counter).getPosition().longitude);
                                temp.include(user);
                            }
                            LatLngBounds bound = temp.build();
                            googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(bound, 100));
                        }

                        new DownloadImageTask().execute(urlTest, user);
                    }
                gps.stopUsingGps();

                }
            }
        //--------------------------------------------Finish getLocation()-----------------------------------

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void stopLocationUpdates() {
        locationUpdatingOrNot = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
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

            Bitmap bhalfsize = result.createScaledBitmap(result, result.getWidth() / 7, result.getHeight() / 7, false);
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
        @Override
        protected Void doInBackground(String... strings) {
            HttpResponse response = null;
            String responseStr = null;
            String username = null;
            String comment = null;
            String latitude = null;
            String longitude = null;

            JSONObject obj = null;
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://www.skyrealmstudio.com/cgi-bin/MarkerScript.py");
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
            for (int counter = 0; counter < json.length(); counter++) {
                try {
                    //parse the data
                    username = json.getJSONObject(counter).getString("Username");
                    comment = json.getJSONObject(counter).getString("Comment");
                    latitude = json.getJSONObject(counter).getString("Latitude");
                    longitude = json.getJSONObject(counter).getString("Longitude");
                    //log the data
                    if (latitude.equals("User did not update location") || longitude.equals("User did not update location") || latitude.equals("") || longitude.equals("")) {

                    } else {
                        MarkerOptions markerOption = new MarkerOptions().position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))).title(username).snippet(comment);
                        mMyMarkersArray.add(userCounter, markerOption);
                        userCounter++;
                    }
                    //reset the variables for the next loop
                    username = null;
                    comment = null;
                    latitude = null;
                    longitude = null;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
            friendsListBoundaries = builder.build();
            for(int counter = 0; counter < mMyMarkersArray.size(); counter++)
            {
                googleMap.getMap().addMarker(mMyMarkersArray.get(counter));
            }
            googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(friendsListBoundaries, 100));
        }
    }

}

