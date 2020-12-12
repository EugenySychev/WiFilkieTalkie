package com.sychev.wifilkietalkie.engine;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.util.Log;

import com.sychev.wifilkietalkie.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class NetworkHeartbeat extends Thread {

    private static final String TAG = "NwHeartBeat";
    private static NetworkHeartbeat mInstance = null;

    private boolean mEnabled = false;
    private boolean mStarted = false;
    private Context mContext;

    public static synchronized NetworkHeartbeat getInstance() {
        if (mInstance == null)
            mInstance = new NetworkHeartbeat();
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void setEnabled(boolean enabled)
    {
        if (!mEnabled && enabled) {
            mEnabled = true;
            start();
        }
        mEnabled = enabled;
    }

    @Override
    public void run() {

        while (mEnabled) {
            mStarted = true;

            try {
                sendCheckData();

                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        super.run();
    }

    public void sendBroadcast(String messageStr) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(), Constants.PORT);
            socket.send(sendPacket);
            System.out.println(getClass().getName() + "Broadcast packet sent to: " + getBroadcastAddress().getHostAddress());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehowx

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}
