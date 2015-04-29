package com.skyrealm.brockyy.findmypeepsapp;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skyrealm.brockyy.findmypeepsapp.R;

public class Login extends Activity implements OnClickListener{
    private EditText user, pass;
    private Button bLogin, bRegister;
    private boolean Regist = false;
    // Progress Dialog
    private ProgressDialog pDialog;
    MainActivity setupLogin = new MainActivity();
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://skyrealmstudio.com/login.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private String username;
    private Toast backtoast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        user = (EditText)findViewById(R.id.username);
        pass = (EditText)findViewById(R.id.password);
        bLogin = (Button)findViewById(R.id.login);
        bLogin.setOnClickListener(this);

        bRegister = (Button)findViewById(R.id.registerlog);
        bRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.login:
                new AttemptLogin().execute();
                break;
            case R.id.registerlog:

                    Intent ii = new Intent(Login.this, Register.class);
                    finish();
                    // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                    startActivity(ii);

                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {

            if(backtoast!=null&&backtoast.getView().getWindowToken()!=null) {
                finish();
            } else {
                backtoast = Toast.makeText(this, "Press back to exit", Toast.LENGTH_SHORT);
                backtoast.show();
            }

    }




    class AttemptLogin extends AsyncTask<String, String, String> {

        boolean failure = false;

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
            username = user.getText().toString();
            String password = pass.getText().toString();
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
                    Intent ii = new Intent(Login.this,MainActivity.class);
                    ii.putExtra("username", user.getText().toString());
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
                Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();

            }
        }
    }
}