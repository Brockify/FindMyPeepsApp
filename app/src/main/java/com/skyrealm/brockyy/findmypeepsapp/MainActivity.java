package com.skyrealm.brockyy.findmypeepsapp;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //Global variables declaration
    String user;
    double latitude;
    double longitude;
    LatLng otherUserLocation;
    String address;
    String comments;
    Marker userMarker;
    Marker otherUserMarker;
    LatLng userCurrentLocation;
    int markerCounter = 0;
    MapFragment googleMap;
    boolean isTrue;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Locations Screen");
        //DECLARATIONS-----------------------------------------------------------------------
        final TextView usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        final Button btnShowLocation = (Button) findViewById(R.id.getLocationButton);
        final View mainView = findViewById(R.id.mainActivity);
        final Switch shareSwitch = (Switch) findViewById(R.id.shareSwitch);
        final Switch requestLocationSwitch = (Switch) findViewById(R.id.locationUpdateSwitch);
        final EditText intervalEditText = (EditText) findViewById(R.id.locationInterval);
        googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        user = getIntent().getExtras().getString("username");
        isTrue = getIntent().getExtras().getBoolean("isTrue");

        //build the google api client and connect too it (for the map)
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        //set the map when created
        googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        googleMap.getMapAsync(this);


        //If the update location button is clicked------------------------------------------
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update users location if they want that
                if (shareSwitch.isChecked()) {
                    new getLocation().execute();
                }
            }
        });

        //declare an OnSwipeListener and then call on the onSwipeLeft function--------
        mainView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(MainActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);
                // EXAMPLE:
                // intent.putExtra("latitude", latitudeText.getText().toString());
                startActivity(intent);
            }
        });

        //when the auto update request button is tapped on
        requestLocationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestLocationSwitch.isChecked())
                {
                    if(intervalEditText.getText().toString().matches("")){
                        Toast.makeText(getApplicationContext(), "Enter in a valid number.", Toast.LENGTH_LONG).show();
                    } else {
                        createLocationRequest();
                    }
                } else {

                }
            }
        });

        //set the users username
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

    //called when the activity is created (for the map)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        GPSTracker gps = new GPSTracker(MainActivity.this);
        //If the update location button is clicked------------------------------------------
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();

        //if latitude or longitude is 0, show an alert
        if (latitude != 0 || longitude != 0) {

            //get the current users location
            userCurrentLocation = new LatLng(latitude, longitude);
            googleMap.setMyLocationEnabled(true);
            //if the a user from friend list was not clicked, just set the zoom to the user
            if (!isTrue) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 13));

                //else show the users location that was clicked
            } else {
                double otherUserLat;
                double otherUserLong;
                String otherUserUsername;
                String otherUserComment;

                //get the extra
                otherUserLat = getIntent().getExtras().getDouble("otherLat");
                otherUserLong = getIntent().getExtras().getDouble("otherLong");
                otherUserUsername = getIntent().getExtras().getString("userUsername");
                otherUserComment = getIntent().getExtras().getString("otherComment");

                //add the other users location to the map
                otherUserMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(otherUserLat, otherUserLong))
                        .title(otherUserUsername + "Comments:" + otherUserComment));

                //zoom to show both the users location and the user clicked location
                otherUserLocation = new LatLng(otherUserLat, otherUserLong);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(userCurrentLocation);
                builder.include(otherUserLocation);
                LatLngBounds bounds = builder.build();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 22));
                //
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

    //FUNCTIONS FOR GOOGLE MAPS API ------------
    public void createLocationRequest()
    {
        final EditText updateInterval = (EditText) findViewById(R.id.locationInterval);
        int interval;

            interval = Integer.parseInt(updateInterval.getText().toString());
            interval = interval * 1000;
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(interval);
            mLocationRequest.setFastestInterval(interval);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

        }
    }
    @Override
    public void onLocationChanged(Location location) {
        final Switch locationUpdateSwitch = (Switch)findViewById(R.id.locationUpdateSwitch);
        if(locationUpdateSwitch.isChecked()) {
            mLastLocation = location;
            new getLocation().execute();
        } else {
        }
    }
//-------------------------------------------------

    //gets the location class (ASYNC)
    public class getLocation extends AsyncTask<Void, Void, Void>{


        GPSTracker gps = new GPSTracker(MainActivity.this);

        @Override
        protected Void doInBackground(Void... params) {
            final Switch shareSwitch = (Switch) findViewById(R.id.shareSwitch);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            address = addresses.get(0).getAddressLine(0);
            if (shareSwitch.isChecked()) {
                //website to post too
                String htmlUrl = "http://skyrealmstudio.com/updatelocation.php";

                //send the post and execute it
                HTTPSendPost postSender = new HTTPSendPost();
                postSender.Setup(user,longitude, latitude, address, htmlUrl, comments);
                postSender.execute();
                //done executing post
            }



            //finished getting the street address-----------------------------------------
            return null;
        }
        // end showing it on the map ------------------------------------------------------------------------

        //this happens whenever an async task is done
        public void onPostExecute(Void result)
        {
            userCurrentLocation = new LatLng(latitude, longitude);


            if(latitude == 0 || longitude == 0)
            {

            } else {
                MapFragment googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
                TextView addressTextView = (TextView) findViewById(R.id.addressTextView);
                EditText commentEditText = (EditText) findViewById(R.id.commentEditText);


                if (gps.canGetLocation()) {
                    //if it is the first time clicking get location
                if(markerCounter==0) {
                    Toast.makeText(getApplicationContext(), "Your Location is -\nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                    addressTextView.setText(address);
                    commentEditText.setText(null);
                    userMarker = googleMap.getMap().addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title("Your location is:" + address + "\nComment:" + comments));
                    markerCounter++;

                    //else it is not the first time
                    } else {
                    userMarker.remove();
                    Toast.makeText(getApplicationContext(), "Your Location is -\nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                    addressTextView.setText(address);
                    commentEditText.setText(null);
                    userMarker = googleMap.getMap().addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title("Your location is:" + address + "\nComment:" + comments));

                }
                        } else {
                    gps.showSettingsAlert();
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



}
