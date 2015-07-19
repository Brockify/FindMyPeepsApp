package com.skyrealm.brockyy.findmypeepsapp;


import android.app.ProgressDialog;
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
    double latitude;
    double longitude;
    LatLng otherUserLocation;
    String address;
    String comments;
    Marker otherUserMarker;
    LatLng userCurrentLocation;
    int markerCounter = 0;
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
    Spinner spinner;
    boolean locationUpdatingOrNot = false;
    double otherUserLat;
    double otherUserLong;
    String otherUserUsername;
    String otherUserComment;
    private static final String TAG_FRIEND = "friend";
    private static final String TAG_LATITUDE = "latitude";
    private static final String TAG_LONGITUDE = "longitude";
    private static final String TAG_COMMENTS = "comments";
    ArrayList<HashMap<String, String>> FriendsList = new ArrayList<HashMap<String, String>>();
    List<MarkerOptions> markers;



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

        //create new gps with MainActivity as context
        gps = new GPSTracker(this);

        //set the map when created
        googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);

        //sets a listener for the map
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

        //setup the spinner
        spinner = (Spinner) findViewById(R.id.updateSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.autoUpdate, R.layout.custom_spinner);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //get the spinners onItemClick
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                //if (spinner.getSelectedItem().equals("Minutes")) {
                //  if (locationUpdatingOrNot)
                //    stopLocationUpdates();
                //} else {
                //  createLocationRequest();
                //}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        //declare an OnSwipeListener and then call on the onSwipeLeft function--------
        mainView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(MainActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);

                startActivity(intent);
            }
        });


        new getFriendsList().execute();

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
        if (gps.canConnect()) {
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
                String urlTest = "http://skyrealmstudio.com/img/"+otherUserUsername+".jpg";
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
                String urlTest = "http://skyrealmstudio.com/img/"+user+".jpg";
                new getLocation().execute();
                break;
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

    //FUNCTIONS FOR GOOGLE MAPS API ------------
    public void createLocationRequest() {
        final Spinner spinner = (Spinner) findViewById(R.id.updateSpinner);
        int interval;

        locationUpdatingOrNot = true;

        interval = Integer.parseInt(spinner.getSelectedItem().toString());
        interval = interval * 60000;
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
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

        boolean alertSettingsFlag = false;

        GPSTracker gps = new GPSTracker(MainActivity.this);

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

            if (!gps.canConnect()) {
                alertSettingsFlag = true;
            } else {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);

                final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);

                //If the update location button is clicked------------------------------------------
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                comments = commentEditText.getText().toString();

                //getting the street address---------------------------------------------------;
                Geocoder geocoder;
                List<Address> addresses = null;
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

                alertSettingsFlag = false;
            }

            //finished getting the street address-----------------------------------------
            return null;
        }
        // end showing it on the map ------------------------------------------------------------------------

        //this happens whenever an async task is done
        public void onPostExecute(Void result) {
            pDialog.dismiss();
            if (alertSettingsFlag) {
                gps.showSettingsAlert();
            } else {
                googleMap.getMap().setMyLocationEnabled(true);

                userCurrentLocation = new LatLng(latitude, longitude);


                MapFragment googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
                TextView addressTextView = (TextView) findViewById(R.id.addressTextView);
                EditText commentEditText = (EditText) findViewById(R.id.commentEditText);

                //if the address comes back null send a toast
                if (address == null) {
                    Toast.makeText(getApplicationContext(), "Could not update location! Try again.", Toast.LENGTH_LONG).show();
                } else {
                    String urlTest = "http://skyrealmstudio.com/img/"+user+".jpg";
                    //if it is the first time clicking get location
                    if (markerCounter == 0) {

                        Toast.makeText(getApplicationContext(), "Updated location!", Toast.LENGTH_LONG).show();
                        addressTextView.setText(address);
                        commentEditText.setText(null);
                        new DownloadImageTask().execute(urlTest);
                        userCurrentLocation = new LatLng(latitude, longitude);
                        googleMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 13));

                        new DownloadImageTask().execute(urlTest, user);
                        markerCounter++;
                        //else it is not the first time
                    } else {
                        Toast.makeText(getApplicationContext(), "Updated location!", Toast.LENGTH_LONG).show();
                        new DownloadImageTask().execute(urlTest, user);
                        addressTextView.setText(address);
                        commentEditText.setText(null);

                    }
                }

            }
        }
        //--------------------------------------------Finish getLocation()-----------------------------------
    }

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
    class getFriendsList extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            //start the post to the database
            MarkerOptions tempMarker;
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

                    for(int i =0; i < FriendsList.size(); i++)
                    {
                                            //start the post to the database
                        String responseBody1 = null;
                        httpClient = new DefaultHttpClient();

                        httpPost = new HttpPost("http://skyrealmstudio.com/GetSpecificUserLocation.php");

                        List<NameValuePair> nameValuePair1 = new ArrayList<NameValuePair>();
                        String friendToFind = FriendsList.get(i).values().toString();
                        nameValuePair1.add(new BasicNameValuePair("Username", friendToFind));


                        try {
                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair1));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        try {
                            response = httpClient.execute(httpPost);
                            responseBody1 = EntityUtils.toString(response.getEntity());
                            // writing response to log
                            Log.d("Http Response:", response.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //end the post response

                        //JSON the string that is got from the post.
                         jsonStr = responseBody1;

                        Log.d("Response: ", "> " + jsonStr);
                        if (jsonStr != null){
                            jsonArr = new JSONArray(jsonStr);


                            JSONObject c = jsonArr.getJSONObject(i);


                           tempMarker = new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(c.getString(TAG_LATITUDE)), Double.parseDouble(c.getString(TAG_LONGITUDE))))
                                    .title(friendToFind)
                                    .snippet(c.getString(TAG_COMMENTS));
                            markers.add(i, tempMarker);
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        public void onPostExecute(Void Result)
        {
            for(int i = 0; i < markers.size(); i++)
            {
                googleMap.getMap().addMarker(new MarkerOptions().title(markers.get(i).getTitle())
                      .position(new LatLng(markers.get(i).getPosition().latitude, markers.get(i).getPosition().longitude)));
            }
        }
    }

}

