package com.skyrealm.brockyy.findmypeepsapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.skyrealm.brockyy.findmypeepsapp.GPSTracker;
import java.util.*;


public class MainActivity extends ActionBarActivity {
//hi how are you? Tester
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btnShowLocation;

        GPSTracker gps;

        setContentView(R.layout.activity_main);

        btnShowLocation = (Button) findViewById(R.id.getLocationButton);
        final TextView latitudeText = (TextView) findViewById(R.id.latTextView);
        final TextView longitudeText = (TextView) findViewById(R.id.longTextView);


        //The OnClickListener is so that a GUI object reacts when a user clicks it.
        btnShowLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                GPSTracker gps = new GPSTracker(MainActivity.this);

                if(gps.canGetLocation())
                {
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    latitudeText.setText(Double.toString(latitude));
                    longitudeText.setText(Double.toString(longitude));

                    Toast.makeText(getApplicationContext(), "Your Location is -\nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    gps.showSettingsAlert();
                }
            }
        });
        // end the OnClick listener




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
}
