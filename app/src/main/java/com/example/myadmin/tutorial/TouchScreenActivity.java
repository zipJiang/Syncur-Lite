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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_screen);

        /* This part gives us the message passed to us by the intent */
        Intent intent = getIntent();
        String message = intent.getStringExtra(Main.EXTRA_MESSAGE);
        /* End message extraction */
        MySurfaceView mysurfaceView = (MySurfaceView)findViewById(R.id.touchScreen);
        mysurfaceView.setOutputStream(Main.mnetworkThread.dos);
        mysurfaceView.setPort(port);
    }

    /* Here start my own code */
    private static int port = 1788;

}
