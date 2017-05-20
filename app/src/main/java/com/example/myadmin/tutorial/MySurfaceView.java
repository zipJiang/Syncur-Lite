package com.example.myadmin.tutorial;

import android.content.Context;

import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by myadmin on 18/05/2017.
 */
public class MySurfaceView extends View {
    private DataOutputStream dStream;
    private int port;
    private VelocityTracker mVelocityTracker = null;
    public MySurfaceView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void setOutputStream(DataOutputStream d) {
        dStream = d;
    }

    public void setPort(int p) {
        port = p;
    }

    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime = 0;
    private boolean secondTouch = false;


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                startClickTime = Calendar.getInstance().getTimeInMillis();
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                secondTouch = true;
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                mVelocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
                Log.d("", "X velocity: " +
                        VelocityTrackerCompat.getXVelocity(mVelocityTracker,
                                pointerId));
                Float fXV =
                        new Float(VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
                Log.d("", "Y velocity: " +
                        VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                                pointerId));
                Float fYV =
                        new Float(VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));
                /* And I should send data to the server */
                try {
                    dStream.writeBytes("MOVE:" + fXV.toString() + " " + fYV.toString() + "\n");
                }
                catch(IOException e) {
                    System.out.println("write to dStream failed.");
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                secondTouch = false;
                /* Generate a right click */
                long semiDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if(semiDuration < MAX_CLICK_DURATION){
                    try {
                        dStream.writeBytes("RIGHT:" + semiDuration + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if(clickDuration < MAX_CLICK_DURATION && !secondTouch) {
                    //click event has occurred
                    try {
                        dStream.writeBytes("CLICK:" + clickDuration + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else if(clickDuration < MAX_CLICK_DURATION && secondTouch) {
                    try {
                        dStream.writeBytes("RIGHT:" + clickDuration + "\n");
                    }
                    catch(IOException e) {
                        System.out.println("write to dStream failed.");
                    }
                }
                else {
                    ;
                }

            case MotionEvent.ACTION_CANCEL:
                secondTouch = false;
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
        }
        return true;
    }
}