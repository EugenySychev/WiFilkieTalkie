package com.sychev.wifilkietalkie.engine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.sychev.wifilkietalkie.data.UserItem;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class NetworkEngine {

    @SuppressLint("StaticFieldLeak")
    private static NetworkEngine mInstance = null;
    private Context mContext = null;

    private NetworkHeartbeat checkSender;
    private NetworkHeartBeatReceiver checkReceiver;

    public boolean isReady() {
        return mContext != null;
    }

    public static NetworkEngine getInstance() {
        if (mInstance == null)
            mInstance = new NetworkEngine();
        return mInstance;
    }

    NetworkEngine() {}

    public void init(Context context) {
        mContext = context;
        checkReceiver = new NetworkHeartBeatReceiver();
        checkSender = new NetworkHeartbeat(mContext, null);
    }

    public void setupName(String name) {
        checkSender.setUserName(name);
    }

    public List<UserItem> getUserList() {
        return checkReceiver.getReceivedList();
    }

    public InetAddress getLocalIpAddress() throws UnknownHostException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        byte[] quads = new byte[] {127, 0, 0, 1} ;
        if (wifi != null)
        {
            DhcpInfo dhcp = wifi.getDhcpInfo();
            if (dhcp != null) {
                for (int k = 0; k < 4; k++)
                    quads[k] = (byte) ((dhcp.ipAddress >> k * 8) & 0xFF);
            }
        }
        return InetAddress.getByAddress(quads);
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        byte[] quads = new byte[] {-1, -1, -1, -1} ;
        if (wifi != null)
        {
            DhcpInfo dhcp = wifi.getDhcpInfo();
            if (dhcp != null) {
                int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
                for (int k = 0; k < 4; k++)
                    quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            }
        }
        return InetAddress.getByAddress(quads);
    }

}
