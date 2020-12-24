package com.sychev.wifilkietalkie.data;

import java.net.InetAddress;

public class UserItem {
    private String mUserName;
    private InetAddress mUserAddress;
    private int mNotReceivedCount;
    private boolean mIsOnline;
    private ActionState mActionState;

    public enum ActionState {
        NONE,
        TALK,
        LISTEN
    }
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

    public int getNotReceivedCount() {
        return mNotReceivedCount;
    }

    public void setNotReceivedCount(int notReceivedCount) {
        this.mNotReceivedCount = notReceivedCount;
    }

    public boolean isOnline() {
        return mIsOnline;
    }

    public void setOnline(boolean mIsOnline) {
        this.mIsOnline = mIsOnline;
    }

    public void increaseNotReceivedCounter() {
        mNotReceivedCount++;
    }

    public void resetNotReceivedCounter() {
        mNotReceivedCount = 0;
    }

    public ActionState getActionState() {
        return mActionState;
    }

    public void setActionState(ActionState actionState) {
        mActionState = actionState;
    }


}
