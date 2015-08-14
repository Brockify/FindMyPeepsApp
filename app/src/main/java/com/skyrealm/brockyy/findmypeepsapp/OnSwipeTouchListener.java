package com.skyrealm.brockyy.findmypeepsapp;

/**
 * Created by Brockyy on 3/10/2015.
 */

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompatBase;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects left and right swipes across a view.
 */


public class OnSwipeTouchListener implements View.OnTouchListener {

    private GestureDetector gestureDetector;
    boolean isSwiped = false;
    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeLeft() {
        isSwiped = true;
    }

    public void onSwipeRight() {
        isSwiped = false;
    }

    public boolean isSwiped()
    {
        return isSwiped;
    }
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float distanceX = e2.getX() - e1.getX();
                float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0) {
                        onSwipeRight();
                        return true;
                    }
                    else {
                        onSwipeLeft();
                        return true;
                    }
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
}