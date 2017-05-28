package com.example.myadmin.tutorial;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.*;
import android.widget.TextView;

import java.io.*;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private static SensorManager mSensorManager;
    private static Sensor mMotion;
    private static DataOutputStream dStream;
    private static final String TAG = "SensorActivity";
    /* Hard coded */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
    }

    @Override
    public void onStart() {
        super.onStart();
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
            /* This should be handled later */
            System.out.println("No compatible sensor to be invoked.");
        }
        dStream = Main.mnetworkThread.dos;
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
        Main.mnetworkThread.off();
        finish();
        mSensorManager.unregisterListener(this);
    }
}