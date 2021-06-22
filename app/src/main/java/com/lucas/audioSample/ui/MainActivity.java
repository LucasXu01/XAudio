package com.lucas.audioSample.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.lucas.audioSample.R;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button bt_simple_use;
    private Button bt_music_play;
    private Button bt_xmusic_play;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_simple_use = findViewById(R.id.bt_simple_use);
        bt_music_play = findViewById(R.id.bt_music_play);
        bt_xmusic_play = findViewById(R.id.bt_xmusic_play);

        PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            // Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show();
                        }
                    }
                });


        //简单使用
        bt_simple_use.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PlayerSampleActivity.class));
        });

        //简单的音乐播放器
        bt_music_play.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RecordSampleActivity.class));
        });

        //简单的音乐播放器
        bt_xmusic_play.setOnClickListener(v -> {
            MusicPlayerActivity.start(MainActivity.this);
        });


    }


}