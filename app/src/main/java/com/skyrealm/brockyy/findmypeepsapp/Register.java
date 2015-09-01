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
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
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
    int verif = 0;
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

        String pass = passreg.getText().toString();
        String Verify = verpasser.getText().toString();
        verpasser.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if(passreg.getText().toString().equals( verpasser.getText().toString())) {
                    verpasser.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
                } else {
                    verpasser.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
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




                int success;
                String pass = passreg.getText().toString();
                String user = userreg.getText().toString();
                String email = emailreg.getText().toString();
                String very = verpasser.getText().toString();
            if (pass.matches("") || user.matches("") || email.matches("") || very.matches("")) {
                return "One or more fields are empty!";
            }else{
                if(passreg.getText().toString().equals( verpasser.getText().toString())){
                try {

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("user", user));
                    params.add(new BasicNameValuePair("pass", pass));
                    params.add(new BasicNameValuePair("email", email));



                    JSONObject json = jsonParser.makeHttpRequest(
                            LOGIN_URL, "POST", params);

                    // checking  log for json response

                    // success tag for json
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
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