package com.lucas.audioSample.view;

import android.os.Bundle;
import android.widget.Button;

import com.lucas.audioSample.R;
import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.audioplayer.model.AudioBean;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerSampleActivity extends AppCompatActivity {

    private Button bt_play_pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_sample);

        bt_play_pause = findViewById(R.id.bt_play_pause);

        XAudio.getInstance()
                .addAudio(new AudioBean("http://music.163.com/song/media/outer/url?id=1459783374.mp3"))
                .playAudio();

        // 播放、暂停网络 mp3 音频
        bt_play_pause.setOnClickListener(v->{
            XAudio.getInstance().playOrPauseAudio();
            bt_play_pause.setText(XAudio.getInstance().isStartState() ? "暂停" : "播放");
        });
    }

    @Override
    protected void onDestroy() {
        XAudio.getInstance().clearAudio();
        super.onDestroy();
    }
}