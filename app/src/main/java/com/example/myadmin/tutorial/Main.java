package com.example.myadmin.tutorial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.*;

import static android.app.PendingIntent.getActivity;
import static android.view.Gravity.CENTER;
import static android.widget.ListPopupWindow.WRAP_CONTENT;

public class Main extends AppCompatActivity {

    static final String EXTRA_MESSAGE = "com.example.myadmin.Main.MESSAGE";
    public static int width;
    public static int height;
    private static final String TAG = "Main";
    private static int port = 1788;
    static networkThread mnetworkThread = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void toSensorMode(View view) {
        final EditText editText = (EditText)findViewById(R.id.ip);
        String message = editText.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                editText.setText("", TextView.BufferType.EDITABLE);
            }
        });

        /* Connect to WIFI here, before entering the new window */
        mnetworkThread = new networkThread(port, message);
        /* We will check here for message format */
        try{
            /* First check for invalid character */
            int dotCount = 0;
            for(int i = 0; i != message.length(); ++i) {
                char currChar = message.charAt(i);
                if(currChar == '.') {
                    ++dotCount;
                }
                else if(currChar < '0' || currChar > '9') {
                    throw new Exception("Unidentified ipAddress Character");
                }
            }
            if(dotCount != 3) {
                throw new Exception("Not valid ipv4 AddressFormat");
            }
        }
        catch(Exception e) {
            String m = e.getMessage();
            Log.d(TAG, m);
            /* Display Message in a Pop-up window */
            builder.setMessage(m)
                    .setTitle(R.string.error);

            AlertDialog dialog = builder.create();
            dialog.show();
            return ;
        }

        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        /* Seems that we don't have to use Connectivity Manager */
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        /* Check whether wifi is connected */
        boolean isWifiConn = networkInfo.isConnected();
        if(isWifiConn) {
            /* Proceed to next Stage */
            /* Send data to the specified ip address */
            /* This part is error prone and should be handled */
            mnetworkThread.start();

            try{
                mnetworkThread.join();
            }
            catch(InterruptedException e) {
                String m = "Network Thread Interrupted.";
                Log.d(TAG, m);

                /* Display Message in a Pop-up window */
                /* Display Message in a Pop-up window */
                builder.setMessage(m)
                        .setTitle(R.string.error);

                AlertDialog dialog = builder.create();
                dialog.show();

                return ;
            }

            try{

                if(mnetworkThread.dos == null) {
                    throw new Exception("Failed to create stable socket.");
                }
                mnetworkThread.dos.writeBytes("SensorMode\n");

            }
            catch(Exception e) {
                String m = e.getMessage();
                Log.d(TAG, m);
                /* Display Message in a Pop-up window */
                builder.setMessage(m)
                        .setTitle(R.string.error);

                AlertDialog dialog = builder.create();
                dialog.show();
                return ;
            }

            Intent intent = new Intent(this, SensorActivity.class);
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        }
        else {
            /* these naiive place holder should be changed to log recorder */
            String m = "No wifi connection detected.";
            Log.d(TAG, m);
            /* Display Message in a Pop-up window */
            builder.setMessage(m)
                    .setTitle(R.string.error);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    public void toTouchMode(View view) {
        final EditText editText = (EditText)findViewById(R.id.ip);
        String message = editText.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                editText.setText("", TextView.BufferType.EDITABLE);
            }
        });

        /* Connect to WIFI here, before entering the new window */
        mnetworkThread = new networkThread(port, message);
        /* We will check here for message format */
        try{
            /* First check for invalid character */
            int dotCount = 0;
            for(int i = 0; i != message.length(); ++i) {
                char currChar = message.charAt(i);
                if(currChar == '.') {
                    ++dotCount;
                }
                else if(currChar < '0' || currChar > '9') {
                    throw new Exception("Unidentified ipAddress Character");
                }
            }
            if(dotCount != 3) {
                throw new Exception("Not valid ipv4 AddressFormat");
            }
        }
        catch(Exception e) {
            String m = e.getMessage();
            Log.d(TAG, m);
            /* Display Message in a Pop-up window */
            builder.setMessage(m)
                    .setTitle(R.string.error);

            AlertDialog dialog = builder.create();
            dialog.show();
            return ;
        }

        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        /* Seems that we don't have to use Connectivity Manager */
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        /* Check whether wifi is connected */
        boolean isWifiConn = networkInfo.isConnected();
        if(isWifiConn) {
            /* Proceed to next Stage */
            /* Send data to the specified ip address */
            /* This part is error prone and should be handled */
            mnetworkThread.start();

            try{
                mnetworkThread.join();
            }
            catch(InterruptedException e) {
                String m = "Network Thread Interrupted.";
                Log.d(TAG, m);

                /* Display Message in a Pop-up window */
                /* Display Message in a Pop-up window */
                builder.setMessage(m)
                        .setTitle(R.string.error);

                AlertDialog dialog = builder.create();
                dialog.show();

                return ;
            }

            try{

                if(mnetworkThread.dos == null) {
                    throw new Exception("Failed to create stable socket.");
                }
                mnetworkThread.dos.writeBytes("TouchMode\n");

            }
            catch(Exception e) {
                String m = e.getMessage();
                Log.d(TAG, m);
                /* Display Message in a Pop-up window */
                builder.setMessage(m)
                        .setTitle(R.string.error);

                AlertDialog dialog = builder.create();
                dialog.show();
                return ;
            }

            Intent intent = new Intent(this, TouchScreenActivity.class);
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        }
        else {
            /* these naiive place holder should be changed to log recorder */
            String m = "No wifi connection detected.";
            Log.d(TAG, m);
            /* Display Message in a Pop-up window */
            builder.setMessage(m)
                    .setTitle(R.string.error);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

}