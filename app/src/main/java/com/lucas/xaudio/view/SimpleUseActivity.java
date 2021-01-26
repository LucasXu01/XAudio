package com.lucas.xaudio.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lucas.xaudio.R;
import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.mediaplayer.model.AudioBean;

import androidx.appcompat.app.AppCompatActivity;

public class SimpleUseActivity extends AppCompatActivity {


    private Button bt_play_net_music;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_use);

        bt_play_net_music = findViewById(R.id.bt_play_net_music);


        //播放网络mp3音频
        bt_play_net_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioBean audioBean1 = new AudioBean("http://music.163.com/song/media/outer/url?id=1459783374.mp3");
                XAudio.getInstance().addAudio(audioBean1);
                XAudio.getInstance().playAudio();
            }
        });

    }


    @Override
    protected void onDestroy() {
        XAudio.getInstance().releaseAudio();
        super.onDestroy();
    }

}