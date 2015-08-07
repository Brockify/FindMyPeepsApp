package com.skyrealm.brockyy.findmypeepsapp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.skyrealm.brockyy.findmypeepsapp.JSONParser;

public class UserSettings extends Activity implements OnClickListener{
    String user;
    private EditText userchange, userverify, oldpasswrd, newpasswrd;
    TextView usernameTextView;
    private Button bChange, bDelete, bReset;
    private static Timer timer;
    //current user
    Double latitude;
    Double longitude;
    String lastUpdated = null;
    Marker userMarker = null;
    LatLng userCurrentLocation;
    GPSTracker gps;
    int seconds;
    int newSeconds;
    Switch autoUpdateSwitch;
    EditText minutesToUpdate;


    // Progress Dialog
    private ProgressDialog pDialog;
    ProgressDialog prgDialog;
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://skyrealmstudio.com/cgi-bin/changeuser.py";
    private static final String LOGIN_URL2 = "http://skyrealmstudio.com/cgi-bin/delete.py";
    private static final String LOGIN_URL3 = "http://skyrealmstudio.com/cgi-bin/resetpass.py";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userchange = (EditText)findViewById(R.id.Newuser);
        userverify = (EditText)findViewById(R.id.Verify);
        oldpasswrd = (EditText) findViewById(R.id.oldpass);
        newpasswrd = (EditText) findViewById(R.id.newpass);
        bChange = (Button)findViewById(R.id.Change);
        bChange.setOnClickListener(this);
        bDelete = (Button)findViewById(R.id.Delete);
        bDelete.setOnClickListener(this);
        bReset = (Button)findViewById(R.id.restpass);
        bReset.setOnClickListener(this);
        user = getIntent().getExtras().getString("username");
        seconds = getIntent().getExtras().getInt("seconds");
        autoUpdateSwitch = (Switch)findViewById(R.id.autoLocationSwitch);
        minutesToUpdate = (EditText) findViewById(R.id.minuteEditText);
        final View mainView = findViewById(R.id.myLayout);

         prefs = this.getSharedPreferences(
                "com.skyrealm.brockyy", Context.MODE_PRIVATE);
        OnSwipeTouchListener swipeListener;
        mainView.setOnTouchListener(new OnSwipeTouchListener(UserSettings.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(UserSettings.this, MainActivity.class);
                intent.putExtra("username", user);
                startActivity(intent);
            }
        });
        String minuteText = prefs.getString("minuteToUpdate", "not set");
        if (!minuteText.equals("not set")) {
            minutesToUpdate.setText(minuteText);
        }

        gps = new GPSTracker(UserSettings.this);
        if (timer != null)
        {
            autoUpdateSwitch.setChecked(true);
        }
        if (autoUpdateSwitch.isChecked())
        {
                timer = new Timer();
                TimerTask task = new TimerTask() {
                    int i = 0;

                    @Override
                    public void run() {
                        i++;
                        //do something
                        if (i % seconds == 0) {
                            new getLocation().execute();
                        } else {
                            newSeconds = (seconds - (i % seconds));
                            System.out.println("Seconds = " + newSeconds);
                        }
                    }
                };
                timer.schedule(task, 0, 1000);
        } else {
            newSeconds = 0;
        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.Change:
                new AttemptChange().execute();
                break;
            case R.id.Delete:
                new AttemptDelete().execute();
                break;
            case R.id.restpass:
                new Attemptrest().execute();
                break;
            default:
                break;
        }
    }
    @Override
    public void onBackPressed() {
        Intent ii = new Intent(UserSettings.this, MainActivity.class);
        ii.putExtra("username", user);
        if (autoUpdateSwitch.isChecked())
            ii.putExtra("seconds", newSeconds);
        else
            ii.putExtra("seconds", 0);
        if(timer != null)
            timer.cancel();
        finish();
        startActivity(ii);
    }

    public void autoUpdateSwitchClicked(View view)
    {
        minutesToUpdate = (EditText) findViewById(R.id.minuteEditText);
        if (minutesToUpdate.getText().toString().isEmpty()) {
            Toast.makeText(UserSettings.this, "Please enter in a value", Toast.LENGTH_LONG).show();
            autoUpdateSwitch.setChecked(false);
        } else {

            // Edit the saved preferences
            prefs.edit().putString("minuteToUpdate", minutesToUpdate.getText().toString()).apply();
            seconds = Integer.parseInt(minutesToUpdate.getText().toString()) * 60;
            if (autoUpdateSwitch.isChecked()) {
                timer = new Timer();
                TimerTask task = new TimerTask() {
                    int i = 0;

                    @Override
                    public void run() {
                        i++;
                        //do something
                        if (i % seconds == 0) {
                            new getLocation().execute();
                        } else {
                            newSeconds = (seconds - (i % seconds));
                            System.out.println("Seconds = " + newSeconds);
                        }
                    }
                };
                timer.schedule(task, 0, 1000);
            } else {
                timer.cancel();
                timer = null;
            }
        }
    }

    class AttemptChange extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UserSettings.this);
            pDialog.setMessage("Attempting to change Username...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag

            int success;
            String newuser = userchange.getText().toString();
            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("Newuser", newuser));
                params.add(new BasicNameValuePair("username", user));

                Log.d("request!", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // checking  log for json response
                Log.d("Registry attempt", json.toString());

                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    user = newuser;
                    Log.d("Username Changed!", json.toString());
                    return json.getString(TAG_MESSAGE);
                }else{

                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }



            return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null){
                Toast.makeText(UserSettings.this, message, Toast.LENGTH_LONG).show();
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
            geocoder = new Geocoder(UserSettings.this, Locale.getDefault());

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
            postSender.Setup(user, longitude, latitude, address, htmlUrl, "Auto updating", lastUpdated);
            postSender.execute();
            //done executing post

            //finished getting the street address-----------------------------------------
            return null;
        }
        // end showing it on the map ------------------------------------------------------------------------

        //this happens whenever an async task is done
        public void onPostExecute(Void result) {
            userCurrentLocation = new LatLng(latitude, longitude);


            MapFragment googleMap = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
            EditText commentEditText = (EditText) findViewById(R.id.commentEditText);

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

        class AttemptDelete extends AsyncTask<String, String, String> {
            /**
             * Before starting background thread Show Progress Dialog
             */
            boolean failure = false;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(UserSettings.this);
                pDialog.setMessage("Attempting to delete account...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }

            @Override
            protected String doInBackground(String... args) {
                // TODO Auto-generated method stub
                // here Check for success tag

                int success;
                String vuser = userverify.getText().toString();
                try {

                    List<NameValuePair> delete = new ArrayList<NameValuePair>();
                    delete.add(new BasicNameValuePair("Verify", vuser));
                    delete.add(new BasicNameValuePair("username", user));

                    Log.d("request!", "starting");

                    JSONObject json = jsonParser.makeHttpRequest(
                            LOGIN_URL2, "POST", delete);

                    // checking  log for json response
                    Log.d("Registry attempt", json.toString());

                    // success tag for json
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        Intent ii = new Intent(UserSettings.this, Login.class);
                        finish();

                        // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                        startActivity(ii);
                        Log.d("Account Deleted", json.toString());
                        return json.getString(TAG_MESSAGE);
                    } else {

                        return json.getString(TAG_MESSAGE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return null;
            }

            /**
             * Once the background process is done we need to  Dismiss the progress dialog asap
             **/
            protected void onPostExecute(String message) {

                pDialog.dismiss();
                if (message != null) {


                    Toast.makeText(UserSettings.this, message, Toast.LENGTH_LONG).show();
                }
            }
        }

        class Attemptrest extends AsyncTask<String, String, String> {
            /**
             * Before starting background thread Show Progress Dialog
             */
            boolean failure = false;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(UserSettings.this);
                pDialog.setMessage("Attempting to reset password...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }

            @Override
            protected String doInBackground(String... args) {
                // TODO Auto-generated method stub
                // here Check for success tag

                int success;
                String newpsw = newpasswrd.getText().toString();
                String oldpsw = oldpasswrd.getText().toString();
                String usr = user;
                try {

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("oldpass", oldpsw));
                    params.add(new BasicNameValuePair("newpass", newpsw));
                    params.add(new BasicNameValuePair("user", user));

                    Log.d("request!", "starting");

                    JSONObject json = jsonParser.makeHttpRequest(
                            LOGIN_URL3, "POST", params);

                    // checking  log for json response
                    Log.d("Registry attempt", json.toString());

                    // success tag for json
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        Log.d("Password Changed!", json.toString());
                        return json.getString(TAG_MESSAGE);
                    } else {

                        return json.getString(TAG_MESSAGE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return null;
            }

            /**
             * Once the background process is done we need to  Dismiss the progress dialog asap
             **/
            protected void onPostExecute(String message) {

                pDialog.dismiss();
                if (message != null) {

                    Toast.makeText(UserSettings.this, message, Toast.LENGTH_LONG).show();
                }
            }
        }
    }