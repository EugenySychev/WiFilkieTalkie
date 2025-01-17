package com.sychev.wifilkietalkie.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sychev.wifilkietalkie.Constants;
import com.sychev.wifilkietalkie.R;
import com.sychev.wifilkietalkie.data.SettingStore;
import com.sychev.wifilkietalkie.data.UserItem;
import com.sychev.wifilkietalkie.engine.AudioEngine;
import com.sychev.wifilkietalkie.engine.NetworkEngine;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements UserListAdapter.ItemClickListener, NetworkEngine.NetworkHandler, AudioEngine.DataHandler {

    private static final String TAG = "UserList";
    private List<UserItem> mUserList;
    private UserListAdapter mAdapter;
    private Handler mHandler;
    private AudioEngine mAudioEngine;

    private final byte[] mReceivedBuffer = new byte[1024 * 1024 * 300];
    private int mReceivedSize = 0;
    private UserItem mCurrentItem = null;
    private boolean isBusy = false;
    private int mCurrentIndex = -1;
    private boolean mVolumePttEnabled;

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

        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
        }
        AppCompatImageButton ptt = findViewById(R.id.user_list_ptt_button);
        ptt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                pushUser(event.getAction() == MotionEvent.ACTION_DOWN ? true : false);
                if (event.getAction() == MotionEvent.ACTION_UP)
                    ptt.performClick();
                return false;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            // TODO: add handle stop exchange
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(UserListActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void pushUser(boolean pttState) {
        Log.d(TAG, "Send is " + pttState);
        isBusy = pttState;
        if (pttState) {
            mAudioEngine.startStreaming();
        } else {
            mAudioEngine.stopStreaming();
        }

        if (!pttState && mReceivedSize > 0 && !mAudioEngine.isPlaying()) {
            Log.d(TAG, "Released player, can play data size " + mReceivedSize);
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
    public void setActive(int position, boolean isChecked) {
        mUserList.get(position).setUserActive(isChecked);
    }

    @Override
    public void beatHandle() {
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void receivedStreamData(InetAddress from, byte[] data, int length) {

        System.arraycopy(data, 0, mReceivedBuffer, mReceivedSize, length);
        mReceivedSize += length;
        if (!isBusy && !mAudioEngine.isPlaying()) {
            Log.d(TAG, "Receiver not busy, play data " + mReceivedSize);
            mAudioEngine.startPlayer();
            if (mReceivedBuffer.length > 0) {
                mAudioEngine.playData(mReceivedBuffer, mReceivedSize);
            }
            mAudioEngine.stopPlayer();
            mReceivedSize = 0;
        } else {
            Log.d(TAG, "Receiver busy, write to buffer, size is " + mReceivedSize);
        }
    }

    @Override
    public void recordedData(byte[] array, int  size) {
        InetAddress address = null;
        try {
            address = NetworkEngine.getInstance().getBroadcastAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (address != null) {
            Log.d(TAG, "Sending data size " + size);

            Log.d(TAG, address.getHostName());
            NetworkEngine.getInstance().sendAudioData(address, array, size);
        }
    }
}