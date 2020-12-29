package com.sychev.wifilkietalkie.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.sychev.wifilkietalkie.Constants;
import com.sychev.wifilkietalkie.R;
import com.sychev.wifilkietalkie.data.SettingStore;
import com.sychev.wifilkietalkie.data.UserItem;
import com.sychev.wifilkietalkie.engine.AudioEngine;
import com.sychev.wifilkietalkie.engine.NetworkEngine;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements UserListAdapter.ItemClickListener, NetworkEngine.NetworkHandler, AudioEngine.DataHandler {

    private static final String TAG = "UserList";
    private List<UserItem> mUserList;
    private UserListAdapter mAdapter;
    private Handler mHandler;
    private AudioEngine mAudioEngine;

    private final byte[] mReceivedBuffer = new byte[1024 * 1024 * 30];
    private int mReceivedSize = 0;
    private UserItem mCurrentItem = null;
    private boolean isBusy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mUserList = NetworkEngine.getInstance().getUserList();
        RecyclerView mRecyclerView = findViewById(R.id.user_list_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UserListAdapter(mUserList, this);
        mAdapter.setOnClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull @NotNull RecyclerView rv, @NonNull @NotNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                int index = -1;
                if (childView != null)
                    index = rv.getChildAdapterPosition(childView);

                if (index >= 0)
                    pushUser(index, e.getAction() == MotionEvent.ACTION_DOWN);

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull @NotNull RecyclerView rv, @NonNull @NotNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        NetworkEngine.getInstance().setupName(SettingStore.getInstance().getName());
        NetworkEngine.getInstance().setOnline(true);
        NetworkEngine.getInstance().setNetworkHandler(this);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == Constants.UPDATE_USER_STATE)
                    mAdapter.notifyDataSetChanged();
                super.handleMessage(msg);
            }
        };
        NetworkEngine.getInstance().setUiHandler(mHandler);

        mAudioEngine = new AudioEngine(this);
    }

    private void pushUser(int index, boolean pttState) {
        Log.d(TAG, "Send is " + pttState);
        UserItem item = mUserList.get(index);
        if (item != null) {
            item.setActionState(pttState ? UserItem.ActionState.TALK : UserItem.ActionState.NONE);
            mAdapter.notifyDataSetChanged();
            isBusy = pttState;
            if (pttState) {
                mCurrentItem = item;
                mAudioEngine.startStreaming();
            } else {
                mCurrentItem = null;
                mAudioEngine.stopStreaming();
            }
        }

        if (!pttState && mReceivedSize > 0) {
            Log.d(TAG, "Released player, can play data size " + mReceivedBuffer.length);
            mAudioEngine.startPlayer();
            mAudioEngine.playData(mReceivedBuffer, mReceivedSize);
            mAudioEngine.stopPlayer();
            mReceivedSize = 0;
        }

    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void beatHandle() {
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void receivedStreamData(InetAddress from, byte[] data, int length) {

        System.arraycopy(data, 0, mReceivedBuffer,mReceivedSize, length);
        mReceivedSize += length;
        if (!isBusy) {
            Log.d(TAG, "Receiver not busy, play data " + mReceivedBuffer.length);
            mAudioEngine.startPlayer();
            if (mReceivedBuffer.length > 0) {
                mAudioEngine.playData(mReceivedBuffer, mReceivedSize);
            }
            mAudioEngine.stopPlayer();
            mReceivedSize = 0;
        } else {
            Log.d(TAG, "Receiver busy, write to buffer, size is " + mReceivedBuffer.length);
        }
    }

    @Override
    public void recordedData(byte[] array, int size) {
        InetAddress address = null;
        if (mCurrentItem != null)
            address = mCurrentItem.getUserAddress();

        if (address != null)
        {

            Log.d(TAG, "Sending data to " + mCurrentItem.getUserAddress().getHostAddress());

            Log.d(TAG, address.getHostName());
            NetworkEngine.getInstance().sendAudioData(address, array, size);
        }
    }
}