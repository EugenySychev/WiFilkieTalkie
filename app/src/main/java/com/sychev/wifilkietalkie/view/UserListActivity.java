package com.sychev.wifilkietalkie.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sychev.wifilkietalkie.Constants;
import com.sychev.wifilkietalkie.R;
import com.sychev.wifilkietalkie.data.SettingStore;
import com.sychev.wifilkietalkie.data.UserItem;
import com.sychev.wifilkietalkie.engine.AudioEngine;
import com.sychev.wifilkietalkie.engine.NetworkEngine;

import org.jetbrains.annotations.NotNull;

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

    private final byte[] mReceivedBuffer = new byte[1024 * 1024 * 30];
    private int mReceivedSize = 0;
    private UserItem mCurrentItem = null;
    private boolean isBusy = false;
    private int mCurrentIndex = -1;

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
                if (childView != null) {
                    index = rv.getChildAdapterPosition(childView);
                }

                boolean pttState = false;
                if (index >= 0) {
                    if (index != mCurrentIndex) {
                        pushUser(mCurrentIndex, false);
                        mCurrentIndex = index;
                    }

                    if (e.getAction() == MotionEvent.ACTION_DOWN ||
                            e.getAction() == MotionEvent.ACTION_UP) {
                        pttState = e.getAction() == MotionEvent.ACTION_DOWN;
                        pushUser(index, pttState);
                    }
                } else {
                    pushUser(mCurrentIndex, false);
                    mCurrentIndex = index;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull @NotNull RecyclerView
                                             rv, @NonNull @NotNull MotionEvent e) {
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
        }

        ;
        NetworkEngine.getInstance().setUiHandler(mHandler);

        mAudioEngine = new AudioEngine(this);

        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
        }
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

    private void pushUser(int index, boolean pttState) {
        Log.d(TAG, "Send is " + pttState);
        if (index >= 0) {
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

        System.arraycopy(data, 0, mReceivedBuffer, mReceivedSize, length);
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

        if (address != null) {

            Log.d(TAG, "Sending data to " + mCurrentItem.getUserAddress().getHostAddress());

            Log.d(TAG, address.getHostName());
            NetworkEngine.getInstance().sendAudioData(address, array, size);
        }
    }
}