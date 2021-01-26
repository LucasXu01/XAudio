package com.lucas.audioSample.view;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lucas.audioSample.R;
import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.mediaplayer.model.AudioBean;
import com.lucas.audioSample.custom.CustomMusicService;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.ArrayList;
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

        initData();
        initMethod();

    }

    //点击事件
    private void initMethod() {

        PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
//                            Toast.makeText(MainActivity.this, "All permissions are granted", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        //简单使用
        bt_simple_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SimpleUseActivity.class));
            }
        });

        //简单的音乐播放器
        bt_music_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RecordSampleActivity.class));
            }
        });

        //简单的音乐播放器
        bt_xmusic_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XMusicPlayerActivity.start(MainActivity.this);
            }
        });

    }

    //初始化音乐数据
    private void initData() {
        ArrayList<AudioBean> audioBeanList = new ArrayList<>();

        AudioBean audioBean1 = new AudioBean("100001",
                "http://qiniu.yanfriends.com/obj_w5zDlMODwrDDiGjCn8Ky_3087361631_ea94_69e0_6a9e_c9e5b3d7a25d66f5b6b28ec92cdec944.mp3", "焰火青年",
                "华北浪革",
                "废柴",
                "2019年盛夏，华北平原的小镇青年开始大声唱出自己的歌词，说说自己的心里话，第一张EP《废柴》。",
                "http://qiniu.yanfriends.com/5d77a37b54143_426809737_1568121723.jpg",
                "4:09"
        );

        AudioBean audioBean2 = new AudioBean("100002",
                "http://qiniu.yanfriends.com/%E9%9D%92%E8%8A%B1%E7%93%B7.mp3", "青花瓷",
                "周杰伦",
                "我很忙",
                "再忙 … 也要陪你听一首歌！",
                "http://qiniu.yanfriends.com/T002R300x300M000002eFUFm2XYZ7z_1.jpg",
                "4:00"
        );

        AudioBean audioBean3 = new AudioBean("100003",
                "http://sr-sycdn.kuwo.cn/resource/n2/33/25/2629654819.mp3", "小情歌",
                "五月天",
                "小幸运",
                "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                "http://qiniu.yanfriends.com/timg.jpeg",
                "4:26"
        );

        audioBeanList.add(audioBean1);
        audioBeanList.add(audioBean2);
        audioBeanList.add(audioBean3);


        XAudio.getInstance()
                .setNotificationIntent(new Intent(this, XMusicPlayerActivity.class)) //可选
                .setAutoService()  //可选：不调用setAutoService不会有服务，不带参为自带的服务，或者根据需要填写自己的Service参
                .setAutoService(new CustomMusicService())
                .setAudioQueen(audioBeanList);
    }


}