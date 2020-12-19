package com.sychev.wifilkietalkie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.sychev.wifilkietalkie.data.UserItem;
import com.sychev.wifilkietalkie.engine.NetworkEngine;
import com.sychev.wifilkietalkie.engine.NetworkHeartBeatReceiver;
import com.sychev.wifilkietalkie.engine.NetworkHeartbeat;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

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


    }
}