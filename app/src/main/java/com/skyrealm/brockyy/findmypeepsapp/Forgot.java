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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skyrealm.brockyy.findmypeepsapp.JSONParser;

public class Forgot extends Activity implements OnClickListener{
    private EditText useremailforg;
    private Button bForgot;
    MainActivity setupLogin = new MainActivity();
    GPSTracker gps = new GPSTracker(this);
    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://skyrealmstudio.com/cgi-bin/forgot.py";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Forgot Password");
        setContentView(R.layout.activity_forgot);
        useremailforg = (EditText)findViewById(R.id.forgotuser);
        bForgot = (Button)findViewById(R.id.forgotbutton);
        bForgot.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.forgotbutton:
                if(gps.haveNetworkConnection()) {
                    new Attemptreset().execute();
                }else{
                    gps.LoginAlert();
                }
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {

        Intent ii = new Intent(Forgot.this,Login.class);
        finish();
        // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
        startActivity(ii);

    }

    class Attemptreset extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Forgot.this);
            pDialog.setMessage("Resetting...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag


            int success;

            String ue = useremailforg.getText().toString();


            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("useremail", ue));

                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // checking  log for json response

                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Intent ii = new Intent(Forgot.this,Login.class);
                    finish();
                    // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                    startActivity(ii);
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
                Toast.makeText(Forgot.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}