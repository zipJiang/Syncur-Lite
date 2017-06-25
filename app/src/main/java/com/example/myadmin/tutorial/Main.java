package com.example.myadmin.tutorial;

/*
 *  这个类对应layout文件夹中的activity_main活动
 *  该事件是整个程序的login activity，主要负责
 *  处理ip地址的设置以及连接模式的选择，活动要求
 *  在输入不合法和未能连接的ip地址时报错但是不退出，
 *  而是重新请求输入IP地址，以保证整个程序的稳定性
 */
import android.app.DialogFragment;
import android.graphics.Color;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Button;
import android.graphics.Typeface;

import android.widget.*;

public class Main extends AppCompatActivity {

    /* 这个是该活动的附加签名，利用这个字符串来标识intent来自这个活动的实例 */
    static final String EXTRA_MESSAGE = "com.example.myadmin.Main.MESSAGE";
    //public static int width;
    //public static int height;
    private static final String TAG = "Main";
    /* 这是随机选择的port， 唯一的要求是与server端的port一致 */
    static int port = 1700;
    /*
     * 网络服务需要由独立的线程来处理，这个线程的实例定义为 Main 类中的 static 成员
     * 这样既方便于在 activity_main 中检验线程实例是否正常，也保证了多 activity
     * 实例共用一个网络线程
     */
    static networkThread mnetworkThread = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        /* 这里是对系统UI的初始化 */
        setContentView(R.layout.activity_main);
        EditText editText = (EditText)findViewById(R.id.ip);
        Button button1 = (Button)findViewById(R.id.sensorMode);
        Button button2 = (Button)findViewById(R.id.touchMode);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"JosefinSans.ttf");
        editText.setTypeface(typeface);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        button1.setTypeface(typeface);
        button2.setTypeface(typeface);
        //editText.setBackgroundColor(Color.BLUE);
        editText.setBackgroundColor(Color.GRAY);


    }
    /* 触屏模式：在点击对应的 button 后被激活，首先检查ip地址的合法性、
       网络连接的正常与否并尝试连接对应的 socket 如果超时连接仍失败
       或者未通过验证，则删除 ip 地址栏的内容并要求用户重新输入ip地址
     */
    public void toSensorMode(View view) {
        final EditText editText = (EditText)findViewById(R.id.ip);
        String message = editText.getText().toString();
        /* 由于 AlertDialog.Builder 的要求类似，所以只在程序最开始
            生成一遍，之后都用相同的 builder 生成alert dialog
         */
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
//          /* 检验IP地址的形式 */
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
            builder.setMessage(m)
                    .setTitle(R.string.error);

            AlertDialog dialog = builder.create();
            dialog.show();
            return ;
        }
        /* 由于原先使用的网络连接检验方法被标注为deprecated，所以现在改为利用简单的检验是否具有可使用的wifi作为标准 */
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        /* Seems that we don't have to use Connectivity Manager */
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo == null) {
            /* these naiive place holder should be changed to log recorder */
            String m = "No wifi connection detected.";
            /* Display Message in a Pop-up window */
            builder.setMessage(m)
                    .setTitle(R.string.error);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
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
            /* 如果已经成功完成网络套接字接口的写入，则启动对应的鼠标控制模式活动 */
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
        /* 基本的逻辑与 Sensor 模式类似。*/
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

    @Override
    protected void onResume() {
        /* 重写了父类中的 onResume 函数保证每次重新登入时会自动填写ip */
        super.onResume();
        EditText editText = (EditText)findViewById(R.id.ip);
        /* Set text back to the original port */
        if(mnetworkThread != null) {
            editText.setText(mnetworkThread.getIP(), TextView.BufferType.EDITABLE);
        }
    }

    /* 这个函数用来调用snackBar，snackBar会弹出一个调节灵敏度的窗口（Dialog） */
    public void activateSnackBar(View view) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout11), R.string.show, 2000);
        mySnackbar.setAction(R.string.fire, new Main.myClickListener());
        mySnackbar.show();
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