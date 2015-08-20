package com.skyrealm.brockyy.findmypeepsapp;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skyrealm.brockyy.findmypeepsapp.JSONParser;

public class Register extends Activity implements OnClickListener{
    private EditText userreg, passreg, emailreg, verpasser;
    GPSTracker gps = new GPSTracker(this);
    private Button bRegister;
    MainActivity setupLogin = new MainActivity();
    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://skyrealmstudio.com/cgi-bin/register.py";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userreg = (EditText)findViewById(R.id.user);
        passreg = (EditText)findViewById(R.id.pass);
        emailreg = (EditText)findViewById(R.id.email);
        verpasser = (EditText) findViewById(R.id.verpass);
        bRegister = (Button)findViewById(R.id.registerbutton   );
        bRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.registerbutton:
                if(gps.haveNetworkConnection()) {
                    new AttemptRegister().execute();
                }else{
                    gps.LoginAlert();
                }
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {

        Intent ii = new Intent(Register.this,Login.class);
        // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
        finish();
        startActivity(ii);
    }

    class AttemptRegister extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Register.this);
            pDialog.setMessage("Registering...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag

            String pass = passreg.getText().toString();
            String Verify = verpasser.getText().toString();

            if(pass == Verify) {

                int success;

                String user = userreg.getText().toString();
                String email = emailreg.getText().toString();

                try {

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("user", user));
                    params.add(new BasicNameValuePair("pass", pass));
                    params.add(new BasicNameValuePair("email", email));


                    Log.d("request!", "starting");

                    JSONObject json = jsonParser.makeHttpRequest(
                            LOGIN_URL, "POST", params);

                    // checking  log for json response
                    Log.d("Registry attempt", json.toString());

                    // success tag for json
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        Log.d("You are Registered!", json.toString());
                        Intent ii = new Intent(Register.this, Login.class);
                        finish();
                        // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                        startActivity(ii);
                        return json.getString(TAG_MESSAGE);
                    } else {

                        return json.getString(TAG_MESSAGE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                return "Passwords must match!";

            }
            return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null){
                Toast.makeText(Register.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}