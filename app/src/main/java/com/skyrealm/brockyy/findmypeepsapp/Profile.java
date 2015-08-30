package com.skyrealm.brockyy.findmypeepsapp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class Profile extends Activity implements OnClickListener {
    String user;
    private EditText biotxt;
    TextView usernameTextView;
    private Button bSend, top, middle, bottom;
    String encodedString;
    RequestParams params = new RequestParams();
    String imgPath, fileName;
    Bitmap bitmap, origmap;
    private static int RESULT_LOAD_IMG = 1;
    private int checkbit = 0;
    private int checkbutts = 0;
    // Progress Dialog
    private ProgressDialog pDialog;
    ProgressDialog prgDialog;
    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String LOGIN_URL = "http://skyrealmstudio.com/cgi-bin/Bio.py";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "Message";
    String Number;
    private Double latitude;
    private Double longitude;
    private String lastUpdated;
    android.os.Handler mainHandler;
    int seconds;
    int newSeconds;
    GPSTracker gps;
    private static Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        biotxt = (EditText) findViewById(R.id.biotext);
        bSend = (Button) findViewById(R.id.SendBio);
        bSend.setOnClickListener(this);
        user = getIntent().getExtras().getString("username");
        final View mainView = findViewById(R.id.Profileview);
        usernameTextView = (TextView) findViewById(R.id.status_usernameTextView);
        String tempuser = user.toLowerCase();
        OnSwipeTouchListener swipeListener;
        seconds = getIntent().getExtras().getInt("seconds");
        Number = getIntent().getExtras().getString("Number");
        gps = new GPSTracker(Profile.this);
        mainView.setOnTouchListener(new OnSwipeTouchListener(Profile.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(Profile.this, MainActivity.class);
                intent.putExtra("username", user);
                intent.putExtra("Number", Number);
                startActivity(intent);
            }
        });
        mainHandler = new Handler(Looper.getMainLooper());


        usernameTextView.setText(user);
        // show The Image
        new DownloadImageTask((ImageView) findViewById(R.id.imgView))
                .execute("http://skyrealmstudio.com/img/" + tempuser + "orig.jpg");

        if (seconds != 0) {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                int i = 0;

                @Override
                public void run() {
                    i++;
                    //do something
                    if (i % seconds == 0) {
                        //run the script on the main thread
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                seconds = 60;
                                new getLocation().execute();
                            } // This is your code
                        };
                        mainHandler.post(myRunnable);
                    } else {
                        newSeconds = (seconds - (i % seconds));
                        System.out.println("Seconds = " + newSeconds);
                    }
                }
            };
            timer.schedule(task, 0, 1000);
        }

    }


    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.SendBio:
                new AttemptSend().execute();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent ii = new Intent(Profile.this, MainActivity.class);
        ii.putExtra("username", user);
        ii.putExtra("Number", Number);
        if (timer != null)
            timer.cancel();
        ii.putExtra("seconds", newSeconds);
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

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }
    public static Bitmap scaleImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        int MAX_IMAGE_DIMENSION = 512;
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        String type = context.getContentResolver().getType(photoUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.equals("image/png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }
    // When Image is selected from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data
                checkbit = 1;
                Uri selectedImage = data.getData();
                bitmap = scaleImage(this,selectedImage);
                origmap = bitmap;

                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();
                final CheckBox checkBox = (CheckBox) findViewById(R.id.image);
                checkBox.setChecked(true);
                ImageView imgView = (ImageView) findViewById(R.id.imgView);
                imgView.setImageBitmap(bitmap);

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                int amorpmint = c.get(Calendar.AM_PM);
                String amorpm;
                if (amorpmint == 0)
                {
                    amorpm = "AM";
                } else {
                    amorpm = "PM";
                }

                lastUpdated = df.format(c.getTime()) + " " + amorpm;
                SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
                String time = timef.format(c.getTime()) + " " + amorpm;

                fileName = user + "orig";
                // Put file name in Async Http Post Param which will used in Php web app
                params.put("filename", fileName);
                params.put("username", user);
                params.put("Time", time);
                params.put("LastUpdated", lastUpdated);

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }
    public void SetTop(View v) {
        if (checkbit != 0) {
            if (origmap.getWidth() >= origmap.getHeight()) {

                bitmap = Bitmap.createBitmap(
                        origmap,
                        0,
                        0,
                        origmap.getHeight(),
                        origmap.getHeight()
                );

            } else {

                bitmap = Bitmap.createBitmap(
                        origmap,
                        0,
                        0,
                        origmap.getWidth(),
                        origmap.getWidth()
                );
            }
            checkbutts = 1;
            ImageView imgView = (ImageView) findViewById(R.id.imgView);
            imgView.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this, "Image must be selected from gallery before cropping", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void SetMiddle(View v) {
        if (checkbit != 0) {
        if (origmap.getWidth() >= origmap.getHeight()){

            bitmap = Bitmap.createBitmap(
                    origmap,
                    origmap.getWidth()/2 - origmap.getHeight()/2,
                    0,
                    origmap.getHeight(),
                    origmap.getHeight()
            );

        }else{

            bitmap = Bitmap.createBitmap(
                    origmap,
                    0,
                    origmap.getHeight()/2 - bitmap.getWidth()/2,
                    origmap.getWidth(),
                    origmap.getWidth()
            );
        }
            checkbutts = 1;
        ImageView imgView = (ImageView) findViewById(R.id.imgView);
        imgView.setImageBitmap(bitmap);
    }else{
            Toast.makeText(this, "Image must be selected from gallery before cropping", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void SetBottom(View v) {
        if (checkbit != 0) {
        if (origmap.getWidth() >= origmap.getHeight()){

            bitmap = Bitmap.createBitmap(
                    origmap,
                    origmap.getWidth() - bitmap.getHeight(),
                    0,
                    origmap.getHeight(),
                    origmap.getHeight()
            );

        }else{

            bitmap = Bitmap.createBitmap(
                    origmap,
                    0,
                    origmap.getHeight() - bitmap.getWidth(),
                    origmap.getWidth(),
                    origmap
                            .getWidth()
            );
        }
            checkbutts = 1;
        ImageView imgView = (ImageView) findViewById(R.id.imgView);
        imgView.setImageBitmap(bitmap);
    }else{
            Toast.makeText(this, "Image must be selected from gallery before cropping", Toast.LENGTH_LONG)
                    .show();
        }
    }

    // When Upload button is clicked
    public void uploadImage(View v) {
        // When Image is selected from Gallery

        if(checkbutts != 0) {
            if (imgPath != null && !imgPath.isEmpty()) {
                prgDialog.setMessage("Uploading Image");
                prgDialog.show();
                // Convert image to String using Base64
                encodeImagetoString();
                // When Image is not selected from Gallery
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "You must select image from gallery before you try to upload",
                        Toast.LENGTH_LONG).show();
            }
        }else{
                Toast.makeText(this, "Photo must be cropped before uploading", Toast.LENGTH_LONG)
                        .show();
        }
    }

    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {

            }

            ;

            @Override
            protected String doInBackground(Void... params) {

                //bitmap = Bitmap.createScaledBitmap(bitmap, original_width, original_height, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {

                // Put converted Image string into Async Http Post param
                params.put("image", encodedString);
                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload() {
        makeHTTPCall();
    }

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {

        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post("http://skyrealmstudio.com/cgi-bin/upload_image.py",
                params, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(String response) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        Intent ii = new Intent(Profile.this, Profile.class);
                        ii.putExtra("username", user);
                        ii.putExtra("Number", Number);
                        ii.putExtra("seconds", newSeconds);
                        if(timer != null)
                        {
                            timer.cancel();
                        }
                        finish();
                        startActivity(ii);
                        Toast.makeText(getApplicationContext(), response,
                                Toast.LENGTH_LONG).show();

                    }

                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc
                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(),
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. server is not running\n HTTP Status code : "
                                            + statusCode, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }

    }


    class AttemptSend extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         *
         */
        String response = null;
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Profile.this);
            pDialog.setMessage("Updating Bio");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // here Check for success tag
            int success;
            String text = biotxt.getText().toString();
            try {
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                int amorpmint = c.get(Calendar.AM_PM);
                String amorpm;
                if (amorpmint == 0)
                {
                    amorpm = "AM";
                } else {
                    amorpm = "PM";
                }

                lastUpdated = df.format(c.getTime()) + " " + amorpm;
                SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
                String time = timef.format(c.getTime()) + " " + amorpm;
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("biog", text));
                params.add(new BasicNameValuePair("user", user));
                params.add(new BasicNameValuePair("numbers", Number));
                params.add(new BasicNameValuePair("Time", time));
                params.add(new BasicNameValuePair("LastUpdated", lastUpdated));

                Log.d("request!", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // checking  log for json response
                Log.d("Registry attempt", json.toString());
                response = json.getString(TAG_MESSAGE);
                // success tag for json
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Bio Sent!", json.toString());

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
                usernameTextView.setText(user);
                Toast.makeText(Profile.this, message, Toast.LENGTH_LONG).show();
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
            geocoder = new Geocoder(Profile.this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                address = addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
            String time = timef.format(c.getTime()) + " " + amorpm;
            //website to post too
            String htmlUrl = "http://skyrealmstudio.com/cgi-bin/updatelocation.py";

            //send the post and execute it
            HTTPSendPost postSender = new HTTPSendPost();
            postSender.Setup(user, longitude, latitude, address, htmlUrl, "Auto updating", lastUpdated, time);
            postSender.execute();
            //done executing post

            //finished getting the street address-----------------------------------------
            return null;
        }
        // end showing it on the map ------------------------------------------------------------------------

        //this happens whenever an async task is done
        public void onPostExecute(Void result) {
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
}