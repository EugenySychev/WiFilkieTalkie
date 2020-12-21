package com.sychev.wifilkietalkie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.sychev.wifilkietalkie.data.UserItem;
import com.sychev.wifilkietalkie.engine.AudioEngine;
import com.sychev.wifilkietalkie.engine.NetworkEngine;
import com.sychev.wifilkietalkie.engine.NetworkHeartBeatReceiver;
import com.sychev.wifilkietalkie.engine.NetworkHeartbeat;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AudioEngine.DataHandler {

    private static final String TAG = "Main";
    private AudioEngine audioEngine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NetworkEngine.getInstance().init(this);
        NetworkEngine.getInstance().setupName("Jonie");
        NetworkEngine.getInstance().setOnline(true);

        TextView ipTextView = findViewById(R.id.ipTextView);
        TextView brTextView = findViewById(R.id.broadIpTextView);
        try {
            ipTextView.setText(NetworkEngine.getInstance().getLocalIpAddress().toString());
            brTextView.setText(NetworkEngine.getInstance().getBroadcastAddress().toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1234);
        }

        audioEngine = new AudioEngine(this);
        audioEngine.startStreaming();
    }

    @Override
    public void sendData(byte[] array, int size) {
        if (audioEngine != null)
            audioEngine.playData(array, size);
    }
}