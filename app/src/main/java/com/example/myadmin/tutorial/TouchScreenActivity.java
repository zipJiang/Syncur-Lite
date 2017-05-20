package com.example.myadmin.tutorial;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
//import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.io.DataOutputStream;
import java.io.IOException;

public class TouchScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_screen);

        /* This part gives us the message passed to us by the intent */
        Intent intent = getIntent();
        String message = intent.getStringExtra(Main.EXTRA_MESSAGE);
        /* End message extraction */

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        /* Seems that we dont have to use Connectivity Manager */
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        /* Check whether wifi is connected */
        //boolean isWifiConn = (networkInfo != null &&
        //       networkInfo.isConnected() && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI));
        boolean isWifiConn = networkInfo.isConnected();
        if(isWifiConn) {
            /* Proceed to next Stage */
            /* Send data to the specified ip address */
            /* Hard-coded once, should be optimized. */
            networkThread nt = new networkThread(port, message);
            nt.start();
            try{
                nt.join();
            }
            catch(InterruptedException e) {
                System.out.println("Interrupted.");
            }
            dStream = nt.dos;
            try{
                dStream.writeBytes("TouchScreen\n");
            }
            catch(IOException e) {
                System.out.println("Failed to write to dStream.");
            }
        }
        else {
            /* these naiive place holder should be changed to log recorder */
            System.out.println("No wifi connection detected.");
        }
        MySurfaceView mysurfaceView = (MySurfaceView)findViewById(R.id.touchScreen);
        mysurfaceView.setOutputStream(dStream);
        mysurfaceView.setPort(port);
    }

    /* Here start my own code */
    //private VelocityTracker mVelocityTracker = null;
    private static int port = 1765;
    private static DataOutputStream dStream = null;

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch(action) {
            case MotionEvent.ACTION_DOWN:
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
                try {
                    dStream.writeBytes(fXV.toString() + " " + fYV.toString());
                }
                catch(IOException e) {
                    System.out.println("write to dStream failed.");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }
    */
}
