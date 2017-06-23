package com.example.myadmin.tutorial;


import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//import android.support.v4.view.VelocityTrackerCompat;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;


public class TouchScreenActivity extends AppCompatActivity {

    /* coeff 定义在TouchScreenActivity中，因为后期主要优化这个 */
    private static final String TAG = "TouchActivity";
    static float coeff = (float)0.2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_screen);

        /* End message extraction */
        MySurfaceView mysurfaceView = (MySurfaceView)findViewById(R.id.touchScreen);
        mysurfaceView.setOutputStream(Main.mnetworkThread.dos);
    }

    /* 这个函数用来调用snackBar，snackBar会弹出一个调节灵敏度的窗口（Dialog） */
    public void activateSnackBar(View view) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.show, 2000);
        mySnackbar.setAction(R.string.fire, new myClickListener());
        mySnackbar.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Main.mnetworkThread.off();
    }

    @Override
    protected void onResume() {
        /* 这里的设计逻辑是，用户切出应用然后切回应用，应该保证自动重连到服务器 */
        super.onResume();

        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        /* Seems that we don't have to use Connectivity Manager */
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        /* 重新连接要求wifi已经连接成功 */
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

        /* 重连默认IP */
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

    /* 为了方便这个调节灵敏度Dialog中的参数使用，
     * 将其定义为内部类
     */
    public class myClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showDialog();
        }

        void showDialog() {
            DialogFragment newFragment = myDialogFrag.newInstance(R.string.sensi);
            newFragment.show(getFragmentManager(), "dialog");
        }
    }


}
