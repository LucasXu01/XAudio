package com.lucas.audioSample;

import android.app.Application;

import com.lucas.xaudio.XAudio;


public class myApp extends Application {



    @Override
    public void onCreate() {
        super.onCreate();

        //音频SDK初始化
        XAudio.getInstance().init(this);

    }




}
