package com.sychev.wifilkietalkie.engine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sychev.wifilkietalkie.Constants;
import com.sychev.wifilkietalkie.data.UserItem;
import com.sychev.wifilkietalkie.view.UserListActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NetworkEngine implements NetworkHeartBeatReceiver.UserHBHandler, NetworkHeartbeat.HeartBeatHandler, NetworkStreamer.DataStreamHandler {

    @SuppressLint("StaticFieldLeak")
    private static NetworkEngine mInstance = null;
    private Context mContext = null;

    private final NetworkHeartbeat checkSender;
    private final NetworkHeartBeatReceiver checkReceiver;
    private final List<UserItem> mUsersList = new ArrayList<>();
    private NetworkHandler mHandler;
    private NetworkStreamer mStreamer = null;
    private String TAG = "NetworkEngine";
    private Handler mUiHandler;


    @Override
    public void receivedData(InetAddress from, byte[] data, int size) {
        if (mHandler != null)
            mHandler.receivedStreamData(from, data, size);
    }

    public void setUiHandler(Handler handler) {
        mUiHandler = handler;
    }


    public interface NetworkHandler {
        void beatHandle();
        void receivedStreamData(InetAddress from, byte[] data, int length);
    }

    public void setNetworkHandler(NetworkHandler handler) {
        mHandler = handler;
    }

    public boolean isReady() {
        return mContext != null;
    }

    public static NetworkEngine getInstance() {
        if (mInstance == null)
            mInstance = new NetworkEngine();
        return mInstance;
    }

    NetworkEngine() {
        checkReceiver = new NetworkHeartBeatReceiver();
        checkReceiver.setHandler(this);
        checkSender = new NetworkHeartbeat(null);
        checkSender.setHeartBeatHandler(this);
        mStreamer = new NetworkStreamer(Constants.STREAM_PORT, Constants.STREAM_BUFFER_SIZE, this);
    }

    public void init(Context context) {
        mContext = context;
    }

    public void setupName(String name) {
        checkSender.setUserName(name);
    }

    public void setOnline(boolean online) {
        if (online) {
            checkSender.begin();
            checkReceiver.begin();
            mStreamer.enableReceiver(true);
        } else {
            checkSender.end();
            checkReceiver.end();
            mStreamer.enableReceiver(false);
        }
    }

    public List<UserItem> getUserList() {
        return mUsersList;
    }

    public InetAddress getLocalIpAddress() throws UnknownHostException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        byte[] quads = new byte[]{127, 0, 0, 1};
        if (wifi != null) {
            DhcpInfo dhcp = wifi.getDhcpInfo();
            if (dhcp != null) {
                for (int k = 0; k < 4; k++)
                    quads[k] = (byte) ((dhcp.ipAddress >> k * 8) & 0xFF);
            }
        }
        return InetAddress.getByAddress(quads);
    }

    public InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        byte[] quads = new byte[]{-1, -1, -1, -1};
        if (wifi != null) {
            DhcpInfo dhcp = wifi.getDhcpInfo();
            if (dhcp != null) {
                int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
                for (int k = 0; k < 4; k++)
                    quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            }
        }
        return InetAddress.getByAddress(quads);
    }

    @Override
    public void receivedUser(UserItem receivedItem) {
        boolean isInList = false;
        for (UserItem item : mUsersList) {
            if (item.getUserName().equals(item.getUserName())) {
                item.resetNotReceivedCounter();
                isInList = true;
                break;
            }
        }
        if (!isInList)
            mUsersList.add(receivedItem);

    }

    @Override
    public void heartBeat() {
        for (UserItem item : mUsersList) {
//            Log.d(TAG, "Check " + item.getUserName() + " not received count " + item.getNotReceivedCount());
            item.setOnline(item.getNotReceivedCount() < Constants.OFFLINE_COUNTER);
            item.increaseNotReceivedCounter();
        }

        if (mUiHandler != null) {
            Message msg = mUiHandler.obtainMessage();
            msg.what = Constants.UPDATE_USER_STATE;
            mUiHandler.sendMessage(msg);
        }
    }

    public void sendAudioData(InetAddress to, byte[] data, int size) {
        if (mStreamer != null) {
            mStreamer.sendData(data, size, to);
        }
    }
}
