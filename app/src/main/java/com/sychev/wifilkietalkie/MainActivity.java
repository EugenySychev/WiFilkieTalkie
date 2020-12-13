package com.sychev.wifilkietalkie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.sychev.wifilkietalkie.engine.NetworkEngine;
import com.sychev.wifilkietalkie.engine.NetworkHeartBeatReceiver;
import com.sychev.wifilkietalkie.engine.NetworkHeartbeat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NetworkEngine.getInstance().init(this);

        NetworkHeartBeatReceiver heartBeatReceiver = new NetworkHeartBeatReceiver();
        NetworkHeartbeat heartbeat = new NetworkHeartbeat(this, "Johny");

//        heartbeat.setEnabled(true);

        Log.d(TAG, "Thread is " + heartbeat.getState());
        heartbeat.begin();
        heartBeatReceiver.begin();
    }
}