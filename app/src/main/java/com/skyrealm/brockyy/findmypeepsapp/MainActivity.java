package com.skyrealm.brockyy.findmypeepsapp;

import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.skyrealm.brockyy.findmypeepsapp.GPSTracker;

import java.io.IOException;
import java.util.*;


public class MainActivity extends ActionBarActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DECLARATIONS--------------------------------
        final Button btnShowLocation = (Button) findViewById(R.id.getLocationButton);
        GPSTracker gps;
        final View mainView = (View) findViewById(R.id.mainActivity);
        //END DECLARATIONS----------------------------

        //If the update location button is clicked------------------------------------------
        btnShowLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getLocation();
            }
        });
        // ends the button click------------------------------------------------------

        //declare an OnSwipeListener and then call on the onSwipeLeft function--------
        OnSwipeTouchListener swipeListener;
        mainView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this)
        {
        public void onSwipeLeft()
        {
                Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
            startActivity(intent);
        }
        });
        //ends the OnSwipeListener-----------------------------------------------------

    }


        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    //--------------------------------------------getLocation()Function-----------------------------------
    public void getLocation()
    {
       Button btnShowLocation = (Button) findViewById(R.id.getLocationButton);
        final TextView latitudeText = (TextView) findViewById(R.id.latTextView);
        final TextView longitudeText = (TextView) findViewById(R.id.longTextView);
        //If the update location button is clicked------------------------------------------
                GPSTracker gps = new GPSTracker(MainActivity.this);
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

                if(gps.canGetLocation())
                {

                    latitudeText.setText(Double.toString(latitude));
                    longitudeText.setText(Double.toString(longitude));

                    Toast.makeText(getApplicationContext(), "Your Location is -\nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                } else {
                    gps.showSettingsAlert();
                }

        //getting the street address---------------------------------------------------;
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = addresses.get(0).getAddressLine(0);
        TextView addressTextView = (TextView) findViewById(R.id.addressTextView);

        addressTextView.setText(address);
        //finished getting the street address-----------------------------------------
            }
    //--------------------------------------------Finish getLocation()-----------------------------------
    }
