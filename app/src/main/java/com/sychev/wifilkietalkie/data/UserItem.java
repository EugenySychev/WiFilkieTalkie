package com.sychev.wifilkietalkie.data;

import java.net.InetAddress;

public class UserItem {
    private String mUserName;
    private InetAddress mUserAddress;
    private int mNotReceivedCount;

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public InetAddress getUserAddress() {
        return mUserAddress;
    }

    public void setUserAddress(InetAddress userAddress) {
        this.mUserAddress = userAddress;
    }

    public int getmNotReceivedCount() {
        return mNotReceivedCount;
    }

    public void setmNotReceivedCount(int mNotReceivedCount) {
        this.mNotReceivedCount = mNotReceivedCount;
    }
}
