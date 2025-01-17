package com.sychev.wifilkietalkie.engine;

import android.util.Log;

import com.sychev.wifilkietalkie.Constants;
import com.sychev.wifilkietalkie.data.UserItem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NetworkHeartBeatReceiver extends Thread {

    private static final String TAG = "NetHBReceiver";
    private boolean mEnabled = false;
    private UserHBHandler mHandler;


    public interface UserHBHandler {
        void receivedUser(UserItem item);
    }

    public void setHandler(UserHBHandler handler) {
        mHandler = handler;
    }

    public void begin() {
        mEnabled = true;
        start();
    }

    @Override
    public void run() {
        DatagramSocket serverSocketUDP = null;

        try {
            InetAddress myHostAddr = InetAddress.getByName("0.0.0.0");
            serverSocketUDP = new DatagramSocket(null);
            serverSocketUDP.setReuseAddress(true);
            serverSocketUDP.bind(new InetSocketAddress("0.0.0.0", Constants.HEARTBEAT_PORT));
            serverSocketUDP.setBroadcast(true);
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        final byte[] receiveData = new byte[1024];


        while (mEnabled) {
//            Log.d(TAG, "Waiting for Broadcast request in ServerUDP.");

            final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                serverSocketUDP.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] sendData = new byte[1024];
            InetAddress address = receivePacket.getAddress();
            int port = receivePacket.getPort();
//            try {
//                if (!receivePacket.getAddress().getHostAddress().equals(NetworkEngine.getInstance().getLocalIpAddress())) {
//                    Log.d(TAG, "Local ip is " + NetworkEngine.getInstance().getLocalIpAddress().getHostAddress());
                    String req = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    Log.d(TAG, "Received UDP message : " + req + " from: " + receivePacket.getAddress().getHostAddress());
                    if (req.contains(" is online")) {
                        UserItem item = new UserItem();
                        item.setUserAddress(receivePacket.getAddress());
                        item.setUserName(req.replaceAll(" is online", ""));
                        Log.d(TAG, "Handled online user receive");
                        if (mHandler != null)
                            mHandler.receivedUser(item);
                    }

//                }
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
        }// while ends

        super.run();
    }

    public List<UserItem> getReceivedList() {
        return new ArrayList<>();
    }

    public void end() {
        mEnabled = false;
    }
}
