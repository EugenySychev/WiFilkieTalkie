package com.sychev.wifilkietalkie.engine;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.sychev.wifilkietalkie.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetworkHeartbeat extends Thread {

    private static final String TAG = "NwHeartBeat";

    private boolean mEnabled = false;
    private final Context mContext;
    private String mName;

    public NetworkHeartbeat(Context context, String name) {
        mContext = context;
        mName = name;
    }

    public void setEnabled(boolean enabled)
    {
        if (!mEnabled && enabled) {
            mEnabled = true;
            start();
        }
        mEnabled = enabled;
    }

    public void setUserName(String name) {
        mName = name;
    }

    @Override
    public void run() {

        while (mEnabled) {
            sendCheckData();

            try {
                Thread.sleep(Constants.HEARTBEAT_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCheckData() {
        String mess = mName + " is online";
        sendBroadcast(mess);
        Log.d(TAG, "Send mess " + mess);
    }

    public void sendBroadcast(String messageStr) {

//        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            InetAddress broadcast = NetworkEngine.getInstance().getBroadcastAddress();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, Constants.HEARTBEAT_PORT);
            socket.send(sendPacket);
            System.out.println(getClass().getName() + "Broadcast packet sent to: " + broadcast.getHostAddress());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    public void begin() {
        mEnabled = true;
        start();
    }
}
