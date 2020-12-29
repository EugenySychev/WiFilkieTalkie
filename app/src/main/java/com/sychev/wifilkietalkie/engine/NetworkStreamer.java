package com.sychev.wifilkietalkie.engine;

import android.provider.ContactsContract;
import android.util.Log;

import com.sychev.wifilkietalkie.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkStreamer extends Thread {

    private static final String TAG = "NetworkStreamer";
    private final int mPort;
    private final int mBufferSize;
    private DataStreamHandler mHandler;
    private boolean mReceiverEnabled = false;
    private DatagramSocket mTransmitterSocket = null;
    private InetAddress mLocalAddress = null;

    public NetworkStreamer(int port, int bufferSize, DataStreamHandler handler) {
        mPort = port;
        mBufferSize = bufferSize;
        mHandler = handler;
        try {
            mTransmitterSocket = new DatagramSocket(new InetSocketAddress(mPort));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public interface DataStreamHandler {
        void receivedData(InetAddress from, byte[] data, int size);
    }

    public void setLocalAddress(InetAddress address) {
        mLocalAddress = address;
    }

    public void sendData(byte[] data, int length, InetAddress to) {
        Log.d(TAG, "Send by socket " + (mTransmitterSocket != null));
        if (mTransmitterSocket != null) {
            Log.d(TAG, "Prepare data for sending");
            InetAddress broadcast = null;
            try {
                broadcast = NetworkEngine.getInstance().getBroadcastAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }

            DatagramPacket sendPacket = new DatagramPacket(data, length, to, mPort + 1);
            try {
                mTransmitterSocket.send(sendPacket);
                Log.d(TAG, "Send " + sendPacket.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void enableReceiver(boolean enable) {
        if (mReceiverEnabled && !enable) {
            mReceiverEnabled = false;
        } else if (!mReceiverEnabled && enable) {
            mReceiverEnabled = true;
            start();
        }
    }


    @Override
    public void run() {
        DatagramSocket serverSocketUDP = null;
        try {
            serverSocketUDP = new DatagramSocket(new InetSocketAddress(mPort + 1));
//            serverSocketUDP.setReuseAddress(true);
//            serverSocketUDP.bind(new InetSocketAddress("0.0.0.0", mPort));
//            serverSocketUDP.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        final byte[] receiveData = new byte[mBufferSize];
        Log.d(TAG, "Stream receiver started");

        while (mReceiverEnabled && serverSocketUDP != null) {
            Log.d(TAG, "Received data " + receiveData.length );
            final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                serverSocketUDP.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mHandler != null && receivePacket.getAddress() != mLocalAddress)
                mHandler.receivedData(receivePacket.getAddress(), receivePacket.getData(), receivePacket.getLength());
        }
    }
}
