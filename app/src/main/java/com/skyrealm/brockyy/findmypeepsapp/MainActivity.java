package com.skyrealm.brockyy.findmypeepsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
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


public class MainActivity extends ActionBarActivity implements OnMapReadyCallback {
    String user;
    double latitude;
    double longitude;
    String address;
    String comments;
    Marker userMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Locations Screen");
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
        //DECLARATIONS-----------------------------------------------------------------------
        final TextView usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        final Button btnShowLocation = (Button) findViewById(R.id.getLocationButton);
        final View mainView = findViewById(R.id.mainActivity);
        final Switch shareSwitch = (Switch) findViewById(R.id.shareSwitch);
        user = getIntent().getExtras().getString("username");

        //END DECLARATIONS-------------------------------------------------------------------
        //If the update location button is clicked------------------------------------------
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (shareSwitch.isChecked()) {
                    new getLocation().execute();
                }
            }
        });

        // ends the button click------------------------------------------------------
        //declare an OnSwipeListener and then call on the onSwipeLeft function--------
        OnSwipeTouchListener swipeListener;
        mainView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(MainActivity.this, FriendsListActivity.class);
                intent.putExtra("username", user);
                // EXAMPLE:
                // intent.putExtra("latitude", latitudeText.getText().toString());
                startActivity(intent);
            }
        });

        usernameTextView.setText(user);

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GPSTracker gps = new GPSTracker(MainActivity.this);
      //If the update location button is clicked------------------------------------------
        latitude = gps.getLatitude();
        longitude = gps.getLongitude();

        if(latitude == 0|| longitude == 0)
        {
            //show a dialog box
            showLocationAlert();
        } else {
            //get the location and put it on the map
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            address = addresses.get(0).getAddressLine(0);


            LatLng userCurrentLocation = new LatLng(latitude, longitude);
            googleMap.setMyLocationEnabled(true);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 13));


            userMarker = googleMap.addMarker(new MarkerOptions()
                    .title("Your location is: " + address)
                    .position(userCurrentLocation));

        }
    }

    public class getLocation extends AsyncTask<Void, Void, Void>{


        GPSTracker gps = new GPSTracker(MainActivity.this);

        @Override
        protected Void doInBackground(Void... params) {
            final Switch shareSwitch = (Switch) findViewById(R.id.shareSwitch);
            final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);

            //If the update location button is clicked------------------------------------------
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
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

        public void onPostExecute(Void result)
        {
            if(latitude == 0 || longitude == 0)
            {

            } else {
                MapFragment googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
                TextView addressTextView = (TextView) findViewById(R.id.addressTextView);
                EditText commentEditText = (EditText) findViewById(R.id.commentEditText);


                if (gps.canGetLocation()) {
                    Toast.makeText(getApplicationContext(), "Your Location is -\nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                } else {
                    gps.showSettingsAlert();
                }
                addressTextView.setText(address);
                commentEditText.setText(null);
            }

        }
        //--------------------------------------------Finish getLocation()-----------------------------------

    }

    public void showLocationAlert()
    {
        //show a dialog box
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Location services");
        alertDialog.setMessage("Go to Settings>Location to turn on location.");
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }
}


