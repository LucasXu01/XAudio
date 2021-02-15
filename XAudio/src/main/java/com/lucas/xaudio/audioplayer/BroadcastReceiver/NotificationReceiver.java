package com.lucas.xaudio.audioplayer.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.audioplayer.core.AudioController;

/**
 * 接收Notification发送的广播
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }
        String extra = intent.getStringExtra(XAudio.EXTRA);
        switch (extra) {
            case XAudio.EXTRA_PLAY:
                //处理播放暂停事件,可以封到AudioController中
                AudioController.getInstance().playOrPause();
                break;
            case XAudio.EXTRA_PRE:
                AudioController.getInstance().previous(); //不管当前状态，直接播放
                break;
            case XAudio.EXTRA_NEXT:
                AudioController.getInstance().next();
                break;
            case XAudio.EXTRA_FAV:
                AudioController.getInstance().changeFavourite();
                break;
            case XAudio.EXTRA_CLOSE:
                XAudio.getInstance().pauseAudio();
                Intent intent2 = new Intent(XAudio.getInstance().getContext(), XAudio.getInstance().getService().getClass());
                XAudio.getInstance().getContext().stopService(intent2);
                break;
        }
    }
}