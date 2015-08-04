package com.skyrealm.brockyy.findmypeepsapp;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.loopj.android.http.RequestParams;

public class FProfile extends Activity implements OnClickListener{
    String user;
    String friend;
    TextView usernameTextView, BioView, commentView;
    String comment;
    String Bio;
    RequestParams params = new RequestParams();

    // Progress Dialog
    private ProgressDialog pDialog;
    ProgressDialog prgDialog;
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://skyrealmstudio.com/cgi-bin/GetFriend.py";
    private static final String TAG_MESSAGE = "bio";
    private static final String TAG_COMMENT = "comment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fprofile);
        friend = getIntent().getExtras().getString("otherUser");
        user = getIntent().getExtras().getString("username");
        final View mainView = findViewById(R.id.Profileview);
        usernameTextView = (TextView)findViewById(R.id.friendTextView);
        BioView = (TextView)findViewById(R.id.friendBio);
        commentView = (TextView)findViewById(R.id.friendlastcomment);

        String tempuser = friend.toLowerCase();
        OnSwipeTouchListener swipeListener;
        mainView.setOnTouchListener(new OnSwipeTouchListener(FProfile.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(FProfile.this, MainActivity.class);
                intent.putExtra("username", user);
                startActivity(intent);
            }
        });

        usernameTextView.setText(friend);
        // show The Image
        new DownloadImageTask((ImageView) findViewById(R.id.imgView))
                .execute("http://skyrealmstudio.com/img/" + tempuser + "orig.jpg");
        new AttemptGrabs().execute();
    }



    public void onClick(View v) {
    }

    @Override
    public void onBackPressed() {
        Intent ii = new Intent(FProfile.this, MainActivity.class);
        ii.putExtra("username", user);
        finish();
        startActivity(ii);
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    class AttemptGrabs extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(FProfile.this);
            pDialog.setMessage("Loading Profile...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag
            try {

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("friend", friend));

                Log.d("request!", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // checking  log for json response
                Log.d("Registry attempt", json.toString());

                // success tag for json
                    Bio = json.getString(TAG_MESSAGE);
                    comment = json.getString(TAG_COMMENT);


                return json.getString(TAG_MESSAGE);
            } catch (JSONException e) {
                e.printStackTrace();
            }



            return null;
        }
        /**
         * Once the background process is done we need to  Dismiss the progress dialog asap
         * **/
        protected void onPostExecute(String message) {
            BioView.setText(Bio);
            commentView.setText(comment);
            pDialog.dismiss();
        }
    }

}