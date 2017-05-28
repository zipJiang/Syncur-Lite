package com.example.myadmin.tutorial;

import android.os.Parcelable;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.*;
import java.net.*;
import java.security.Security;

/**
 * Created by myadmin on 16/05/2017.
 */
public class networkThread extends Thread {
    public int port;
    public DataOutputStream dos;
    private String ipAdd;
    private static final String TAG = "networkThread";
    @Override
    public void run() {
        SocketAddress socketAddress = new InetSocketAddress(ipAdd , port);

        /* End hard coding */

        try {
            /* This part should be carefully coded */
            Socket connectionSock = new Socket();
            connectionSock.connect(socketAddress, 5000);
            dos = new DataOutputStream(
                    connectionSock.getOutputStream()
            );
        }
        catch(SocketTimeoutException e) {
            String m = "Connection timeout";
            Log.d(TAG, m);
            /* Explicitly set dos to null indicating a bad connection */
            dos = null;
            return ;
        }
        catch(IOException e) {
            String m = "Error handling read and write.";
            Log.d(TAG, m);
            /* Explicitly set dos to null indicating a bad connection */
            dos = null;
            return ;
        }
        catch(SecurityException e) {
            String m = "Security Failed.";
            Log.d(TAG, m);
            /* Explicitly set dos to null indicating a bad connection */
            dos = null;
            return ;
        }
    }

    public networkThread(int p, String m) {
        this.port = p;
        this.ipAdd = m;
    }

}
