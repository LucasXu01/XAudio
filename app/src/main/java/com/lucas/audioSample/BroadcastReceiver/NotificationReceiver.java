package com.lucas.audioSample.BroadcastReceiver;

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
                //处理播放暂停事件
                XAudio.getInstance().playOrPauseAudio();
                break;
            case XAudio.EXTRA_PRE:
                //上一首
                XAudio.getInstance().previousAudio();
                break;
            case XAudio.EXTRA_NEXT:
                //下一首
                XAudio.getInstance().nextAudio();
                break;
            case XAudio.EXTRA_FAV:
                // 收藏、喜欢

                break;
            case XAudio.EXTRA_CLOSE:
                XAudio.getInstance().pauseAudio();
                Intent intent2 = new Intent(XAudio.getInstance().getContext(), XAudio.getInstance().getService().getClass());
                XAudio.getInstance().getContext().stopService(intent2);
                break;
        }
    }
}