package com.lucas.xaudio.app;

import android.app.Application;

import com.lucas.xaudio.XAudio;


public class XMsuicApp extends Application {



    @Override
    public void onCreate() {
        super.onCreate();

        //音频SDK初始化
        XAudio.getInstance().init(this);

    }




}
