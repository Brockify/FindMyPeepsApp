package com.skyrealm.brockyy.findmypeepsapp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class Profile extends ActionBarActivity {
    private String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

            View Prof = findViewById(R.id.Profileview);
            final TextView usernameTextView = (TextView) findViewById(R.id.usernameTextView);

            user = getIntent().getExtras().getString("username");
            Prof.setOnTouchListener(new OnSwipeTouchListener(Profile.this) {
                //calls on the swipeRight method
                public void onSwipeRight() {
                    Intent intent = new Intent(Profile.this, MainActivity.class);
                    intent.putExtra("username", user);
                    startActivity(intent);
                }

            });
            usernameTextView.setText(user);
        }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
