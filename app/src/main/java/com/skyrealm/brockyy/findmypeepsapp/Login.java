package com.skyrealm.brockyy.findmypeepsapp;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.skyrealm.brockyy.findmypeepsapp.R;

public class Login extends Activity implements OnClickListener, View.OnTouchListener {
    private EditText users, pass;

    private Button bLogin, bRegister;
    private TextView tvforgot;
    private boolean Regist = false;
    // Progress Dialog
    private ProgressDialog pDialog;
    MainActivity setupLogin = new MainActivity();
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://www.skyrealmstudio.com/cgi-bin/login.py";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private String username;
    private String password;
    private Toast backtoast;
    GPSTracker gps = new GPSTracker(this);
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    public int checks;


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        users = (EditText) findViewById(R.id.username);
        pass = (EditText) findViewById(R.id.password);
        bLogin = (Button) findViewById(R.id.login);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.remember);
        bLogin.setOnClickListener(this);
        bRegister = (Button) findViewById(R.id.registerlog);
        bRegister.setOnClickListener(this);
        tvforgot = (TextView) findViewById(R.id.forgotbut);
        tvforgot.setOnTouchListener(this);
        saveLoginCheckBox = (CheckBox) findViewById(R.id.remember);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            users.setText(loginPreferences.getString("username", ""));
            pass.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.forgotbut:
                Intent ii = new Intent(Login.this, Forgot.class);
                finish();
                startActivity(ii);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.login:




                username = users.getText().toString();
                password = pass.getText().toString();

                if (saveLoginCheckBox.isChecked()) {
                    loginPrefsEditor.putBoolean("saveLogin", true);
                    loginPrefsEditor.putString("username", username);
                    loginPrefsEditor.putString("password", password);
                    loginPrefsEditor.commit();
                } else {
                    loginPrefsEditor.clear();
                    loginPrefsEditor.commit();
                }
                if(gps.haveNetworkConnection())
                {
                    new AttemptLogin().execute();
                }else
                {
                    gps.LoginAlert();
                }


                break;
            case R.id.registerlog:

                Intent ii = new Intent(Login.this, Register.class);
                finish();
                startActivity(ii);

                break;
            default:
                break;
        }
    }


    @Override
    public void onBackPressed() {

        if (backtoast != null && backtoast.getView().getWindowToken() != null) {
            finish();
        } else {
            backtoast = Toast.makeText(this, "Press back to exit", Toast.LENGTH_SHORT);
            backtoast.show();
        }

    }


    class AttemptLogin extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setMessage("Attempting to login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag
            int success;

            boolean failure = false;


            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));

                Log.d("request!", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // checking  log for json response
                Log.d("Login attempt", json.toString());

                // success tag for json
                success = json.getInt(TAG_SUCCESS);


                if (success == 1) {


                    Log.d("Successfully Login!", json.toString());
                    Intent ii = new Intent(Login.this, MainActivity.class);
                    ii.putExtra("username", users.getText().toString());


                    finish();

                    startActivity(ii);

                    return json.getString(TAG_MESSAGE);
                } else {

                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }



        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null) {
                Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();


            }

        }
    }
}