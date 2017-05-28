package com.example.myadmin.tutorial;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.*;

import static android.view.Gravity.CENTER;
import static android.widget.ListPopupWindow.WRAP_CONTENT;

public class Main extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.myadmin.Main.MESSAGE";
    private static final String TAG = "Main";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void toSensorMode(View view) {
        EditText editText = (EditText)findViewById(R.id.ip);
        String message = editText.getText().toString();
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
            /* Display Message in a Pop-up window */
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup errorLayout = (ViewGroup)inflater.inflate(R.layout.popup, null);
            TextView tv = (TextView)errorLayout.findViewById(R.id.errorText);
            tv.setText(m);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            final PopupWindow pw = new PopupWindow(errorLayout, (int)(width * 0.8), WRAP_CONTENT, true);
            /* Empty the input box */
            editText.setText("", TextView.BufferType.EDITABLE);
            pw.showAtLocation(findViewById(R.id.activity_main), CENTER, 0, 0);
            Button close = (Button) errorLayout.findViewById(R.id.okbutton);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View popupView) {
                    pw.dismiss();
                }
            });
            return ;
        }
        Intent intent = new Intent(this, SensorActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    public void toTouchMode(View view) {


        EditText editText = (EditText)findViewById(R.id.ip);
        String message = editText.getText().toString();
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
            /* Display Message in a Pop-up window */
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup errorLayout = (ViewGroup)inflater.inflate(R.layout.popup, null);
            TextView tv = (TextView)errorLayout.findViewById(R.id.errorText);
            tv.setText(m);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            final PopupWindow pw = new PopupWindow(errorLayout, (int)(width * 0.8), WRAP_CONTENT, true);
            /* Empty the input box */
            editText.setText("", TextView.BufferType.EDITABLE);
            pw.showAtLocation(findViewById(R.id.activity_main), CENTER, 0, 0);
            Button close = (Button) errorLayout.findViewById(R.id.okbutton);
            close.setOnClickListener(new View.OnClickListener() {
                public void onClick(View popupView) {
                    pw.dismiss();
                }
            });
            return ;
        }
        Intent intent = new Intent(this, TouchScreenActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}