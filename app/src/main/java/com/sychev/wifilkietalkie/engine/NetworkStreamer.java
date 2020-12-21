package com.sychev.wifilkietalkie.engine;

import android.provider.ContactsContract;

import com.sychev.wifilkietalkie.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class NetworkStreamer extends Thread {

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
            mTransmitterSocket = new DatagramSocket(null);
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
        if (mTransmitterSocket != null) {
            DatagramPacket sendPacket = new DatagramPacket(data, length, to, mPort);
            try {
                mTransmitterSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void enableReceiver(boolean enable) {
        if (mReceiverEnabled && !enable) {
            mReceiverEnabled = false;
        } else if (!mReceiverEnabled && enable) {
            start();
        }
    }


    @Override
    public void run() {
        DatagramSocket serverSocketUDP = null;
        while (mReceiverEnabled) {
            try {
                serverSocketUDP = new DatagramSocket(null);
                serverSocketUDP.setReuseAddress(true);
                serverSocketUDP.bind(new InetSocketAddress(mPort));
                serverSocketUDP.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            final byte[] receiveData = new byte[mBufferSize];

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
