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
import android.widget.TextView;
import android.widget.Toast;

import com.skyrealm.brockyy.findmypeepsapp.JSONParser;

public class Profile extends Activity implements OnClickListener{
    String user;
    private EditText userchange, userverify;
    TextView usernameTextView;
    private Button bChange, bDelete;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://skyrealmstudio.com/ChangeUser.php";
    private static final String LOGIN_URL2 = "http://skyrealmstudio.com/Delete.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userchange = (EditText)findViewById(R.id.Newuser);
        userverify = (EditText)findViewById(R.id.Verify);
        bChange = (Button)findViewById(R.id.Change);
        bChange.setOnClickListener(this);
        bDelete = (Button)findViewById(R.id.Delete);
        bDelete.setOnClickListener(this);
        user = getIntent().getExtras().getString("username");
        final View mainView = findViewById(R.id.Profileview);
        usernameTextView = (TextView)findViewById(R.id.usernameTextView);



        OnSwipeTouchListener swipeListener;
        mainView.setOnTouchListener(new OnSwipeTouchListener(Profile.this) {
            public void onSwipeLeft() {
                Intent intent = new Intent(Profile.this, MainActivity.class);
                intent.putExtra("username", user);
                startActivity(intent);
            }
        });

        usernameTextView.setText(user);


    }


    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.Change:
                new AttemptChange().execute();
            break;
            case R.id.Delete:
                new AttemptDelete().execute();

            default:
                break;
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
            pDialog = new ProgressDialog(Profile.this);
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

                usernameTextView.setText(user);
                Toast.makeText(Profile.this, message, Toast.LENGTH_LONG).show();
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
            pDialog = new ProgressDialog(Profile.this);
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
                    Intent ii = new Intent(Profile.this, Login.class);
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


                Toast.makeText(Profile.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}