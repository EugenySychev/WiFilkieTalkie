package com.sychev.wifilkietalkie.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.sychev.wifilkietalkie.R;
import com.sychev.wifilkietalkie.data.UserItem;
import com.sychev.wifilkietalkie.engine.NetworkEngine;

import java.util.List;

public class UserListActivity extends AppCompatActivity implements UserListAdapter.ItemClickListener {

    private List<UserItem> mUserList;
    private UserListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        mUserList = NetworkEngine.getInstance().getUserList();
        RecyclerView recyclerView = findViewById(R.id.user_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UserListAdapter(mUserList, this);
        mAdapter.setOnClickListener(this);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        UserItem item = mUserList.get(position);

    }
}