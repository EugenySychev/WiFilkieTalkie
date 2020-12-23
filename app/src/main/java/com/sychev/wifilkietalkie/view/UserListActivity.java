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
import com.sychev.wifilkietalkie.engine.NetworkEngine;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.List;

public class UserListActivity extends AppCompatActivity implements UserListAdapter.ItemClickListener, NetworkEngine.NetworkHandler {

    private static final String TAG = "UserList";
    private List<UserItem> mUserList;
    private UserListAdapter mAdapter;
    private Handler mHandler;

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
    }

    private void pushUser(int index, boolean pttState) {
        Log.d(TAG, "Send is " + pttState);
    }

    @Override
    public void onItemClick(View view, int position) {
        UserItem item = mUserList.get(position);
    }

    @Override
    public void beatHandle() {
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void receivedStreamData(InetAddress from, byte[] data, int length) {

    }
}