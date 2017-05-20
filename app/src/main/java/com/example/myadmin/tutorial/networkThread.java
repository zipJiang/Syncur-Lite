package com.example.myadmin.tutorial;

import java.io.DataOutputStream;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Security;

/**
 * Created by myadmin on 16/05/2017.
 */
public class networkThread extends Thread {
    public int port;
    public DataOutputStream dos;
    private String ipAdd;
    @Override
    public void run() {
        byte[] ipAddress = new byte[4];
        int[] ipAddressInt = new int[4];
        int locationA = ipAdd.indexOf(".", 0);
        int locationB = ipAdd.indexOf(".", locationA + 1);
        int locationC = ipAdd.indexOf(".", locationB + 1);
        ipAddressInt[0] = Integer.parseInt(ipAdd.substring(0, locationA));
        ipAddressInt[1] = Integer.parseInt(ipAdd.substring(locationA + 1, locationB));
        ipAddressInt[2] = Integer.parseInt(ipAdd.substring(locationB + 1, locationC));
        ipAddressInt[3] = Integer.parseInt(ipAdd.substring(locationC + 1, ipAdd.length()));

        for(int i = 0; i != 4; ++i) {
            ipAddress[i] = (byte)ipAddressInt[i];
        }

        /* End hard coding */
        InetAddress targetAddress = null;
        try {
            targetAddress = InetAddress.getByAddress(ipAddress);
        }
        catch(UnknownHostException e) {
            System.out.println("Unknown Host!");
        }
        try {
            /* This part should be carefully coded */
            Socket connectionSock = new Socket(targetAddress, port);
            dos = new DataOutputStream(
                    connectionSock.getOutputStream()
            );
        }
        catch(IOException e) {
            System.out.println("Error handling read and write.");
        }
        catch(SecurityException e) {
            System.out.println("Security Check failed.");
        }
    }

    public networkThread(int p, String m) {
        this.port = p;
        this.ipAdd = m;
    }

}
