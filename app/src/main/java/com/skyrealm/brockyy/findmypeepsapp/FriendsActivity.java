package com.skyrealm.brockyy.findmypeepsapp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class FriendsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //Does the swipe command for swiping right
        OnSwipeTouchListener swipeRight;

        View friendView = (View) findViewById(R.id.friendsActivity);

        friendView.setOnTouchListener(new OnSwipeTouchListener(FriendsActivity.this) {
            //calls on the swipeRight method
            public void onSwipeRight() {
                Intent intent = new Intent(FriendsActivity.this, MainActivity.class);
                startActivity(intent);
            }

        });
        //end the swipe command


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
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
