package com.example.myadmin.tutorial;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.support.v4.view.VelocityTrackerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;


public class TouchScreenActivity extends AppCompatActivity {

    private static final String TAG = "TouchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_screen);

        /* End message extraction */
        MySurfaceView mysurfaceView = (MySurfaceView)findViewById(R.id.touchScreen);
        mysurfaceView.setOutputStream(Main.mnetworkThread.dos);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Main.mnetworkThread.off();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        /* Seems that we don't have to use Connectivity Manager */
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo == null) {
            /* these naiive place holder should be changed to log recorder */
            String m = "No wifi connection detected.";
            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
            finish();
            return;
        }
        /* Check whether wifi is connected */
        boolean isWifiConn = networkInfo.isConnected();

        if(!isWifiConn) {
            String m = "Network Error occurred, reconnection needed.";
            Log.d(TAG, m);


            Intent intent = new Intent(this, Main.class);
            startActivity(intent);
            finish();
            return;
        }

        if (!Main.mnetworkThread.isConnected()) {
            Main.mnetworkThread = new networkThread(Main.mnetworkThread.port, Main.mnetworkThread.getIP());
            Main.mnetworkThread.start();
            try {
                Main.mnetworkThread.join();
            } catch (InterruptedException e) {
                String m = "Network Error occurred, reconnection needed.";
                Log.d(TAG, m);


                Intent intent = new Intent(this, Main.class);
                startActivity(intent);
                finish();
                return;
            }
            /* End message extraction */
            MySurfaceView mysurfaceView = (MySurfaceView)findViewById(R.id.touchScreen);
            mysurfaceView.setOutputStream(Main.mnetworkThread.dos);
            try {
                Main.mnetworkThread.dos.writeBytes("TouchMode\n");
            }
            catch(IOException e) {
                String m = e.getMessage();
                Log.d(TAG, m);


                Intent intent = new Intent(this, Main.class);
                startActivity(intent);
                finish();
                return;
            }
        }
    }
}
