package com.example.myadmin.tutorial;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.*;
import android.widget.TextView;

import java.io.*;

import java.util.List;


public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private static SensorManager mSensorManager;
    private static Sensor mMotion;
    private static int port = 1788;
    private static DataOutputStream dStream = null;
    /* Hard coded */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        /* This part gives us the message passed to us by the intent */
        Intent intent = getIntent();
        String message = intent.getStringExtra(Main.EXTRA_MESSAGE);
        /* End message extraction */

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        /* Check availability of Linear_Acceleration_Sensor */
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            // Success! There's a TYPE_LINEAR_ACCELERATION sensor.
            /* Check resolution and range for each sensor in the list */
            mMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        }
        else {
            // Failure! No linear_acceleration_monitor.
            System.out.println("No compatible sensor to be invoked.");
        }

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
                dStream.writeBytes("SensorMode\n");
            }
            catch(IOException e) {
                System.out.println("Failed to write to dStream.");
            }
        }
        else {
            /* these naiive place holder should be changed to log recorder */
            System.out.println("No wifi connection detected.");
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        Float axisX = new Float(event.values[0]);
        Float axisY = new Float(event.values[1]);
        Float axisZ = new Float(event.values[2]);
        TextView textViewX = (TextView)findViewById(R.id.cx);
        TextView textViewY = (TextView)findViewById(R.id.cy);
        TextView textViewZ = (TextView)findViewById(R.id.cz);
        // Do something with this sensor value.
        textViewX.setText(axisX.toString());
        textViewY.setText(axisY.toString());
        textViewZ.setText(axisZ.toString());

        /* Write data to dos */
        try {
            dStream.writeBytes("MOVE:" + axisX + " " + axisY + " " + axisZ + "\n");
        }
        catch(IOException e) {
            System.out.println("Bytes writing failed!");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mMotion, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}