package com.sychev.wifilkietalkie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sychev.wifilkietalkie.data.SettingStore;
import com.sychev.wifilkietalkie.data.UserItem;
import com.sychev.wifilkietalkie.engine.AudioEngine;
import com.sychev.wifilkietalkie.engine.NetworkEngine;
import com.sychev.wifilkietalkie.engine.NetworkHeartBeatReceiver;
import com.sychev.wifilkietalkie.engine.NetworkHeartbeat;
import com.sychev.wifilkietalkie.view.TalkieActivity;
import com.sychev.wifilkietalkie.view.UserListActivity;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AudioEngine.DataHandler {

    private static final String TAG = "Main";
    private static final int REQUEST_MIC_PEMISSION_ID = 1;
    private AudioEngine mAudioEngine = null;
    private NetworkEngine mNetworkEngine = null;
    private List<UserItem> mUserItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SettingStore.getInstance().init(this);
        NetworkEngine.getInstance().init(this);

        EditText nameEdit = findViewById(R.id.nameEdit);
        nameEdit.setText(SettingStore.getInstance().getName());

        AppCompatButton start = findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingStore.getInstance().setUserName(nameEdit.getText().toString());
                Intent intent = new Intent(MainActivity.this, UserListActivity.class);
                startActivity(intent);
            }
        });

//        mNetworkEngine = new NetworkEngine(this);
//
//        mUserItemList = mNetworkEngine.getUserList();
//        mAudioEngine = new AudioEngine(this);
    }

    private void checkMicPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MIC_PEMISSION_ID );
        }

    }

    @Override
    public void sendData(byte[] array, int size) {
//        if
//
//
//        if (audioEngine != null)
//            audioEngine.playData(array, size);
    }
}