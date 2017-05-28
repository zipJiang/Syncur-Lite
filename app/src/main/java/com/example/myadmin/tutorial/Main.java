package com.example.myadmin.tutorial;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Button;
import android.graphics.Typeface;


public class Main extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.myadmin.Main.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText editText = (EditText)findViewById(R.id.ip);
        Button button1 = (Button)findViewById(R.id.sensorMode);
        Button button2 = (Button)findViewById(R.id.touchMode);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"JosefinSans.ttf");
        editText.setTypeface(typeface);
        button1.setTypeface(typeface);
        button2.setTypeface(typeface);
        //editText.setBackgroundColor(Color.BLUE);
        editText.setBackgroundColor(Color.GRAY);
    }
    public void toSensorMode(View view) {
        Intent intent = new Intent(this, SensorActivity.class);
        EditText editText = (EditText)findViewById(R.id.ip);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void toTouchMode(View view) {
        Intent intent = new Intent(this, TouchScreenActivity.class);
        EditText editText = (EditText)findViewById(R.id.ip);
        String message = editText.getText().toString();

        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

}