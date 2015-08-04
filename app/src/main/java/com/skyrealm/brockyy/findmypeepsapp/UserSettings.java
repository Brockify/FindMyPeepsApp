package com.skyrealm.brockyy.findmypeepsapp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.skyrealm.brockyy.findmypeepsapp.JSONParser;

public class UserSettings extends Activity implements OnClickListener{
    String user;
    private EditText userchange, userverify, oldpasswrd, newpasswrd;
    TextView usernameTextView;
    private Button bChange, bDelete, bReset;



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
        final View mainView = findViewById(R.id.Profileview);
        OnSwipeTouchListener swipeListener;
        mainView.setOnTouchListener(new OnSwipeTouchListener(UserSettings.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(UserSettings.this, MainActivity.class);
                intent.putExtra("username", user);
                startActivity(intent);
            }
        });
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
        finish();
        startActivity(ii);
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

    class AttemptDelete extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
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
    class Attemptrest extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
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


}