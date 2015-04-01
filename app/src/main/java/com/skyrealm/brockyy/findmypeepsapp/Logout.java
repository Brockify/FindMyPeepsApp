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

import com.skyrealm.brockyy.findmypeepsapp.R;

public class Logout extends Activity implements OnClickListener{
    private Button bLogout;

    // Progress Dialog
    private ProgressDialog pDialog;
    MainActivity setupLogin = new MainActivity();


    @Override
    protected void onCreate(Bundle savedInstanceState) {


    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.logOut:
                Intent ii = new Intent(Logout.this, Login.class);
                finish();
                // this finish() method is used to tell android os that we are done with current //activity now! Moving to other activity
                startActivity(ii);

                break;
            default:
                break;
        }
    }

    class AttemptLogin extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... args) {
           return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {

            pDialog.dismiss();
            if (message != null){
                Toast.makeText(Logout.this, message, Toast.LENGTH_LONG).show();

            }
        }
    }
}