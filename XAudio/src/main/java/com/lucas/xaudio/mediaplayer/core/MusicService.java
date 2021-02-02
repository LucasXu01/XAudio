package com.lucas.xaudio.mediaplayer.core;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.extra.AudioServiceListener;
import com.lucas.xaudio.mediaplayer.BroadcastReceiver.NotificationReceiver;
import com.lucas.xaudio.mediaplayer.events.AudioFavouriteEvent;
import com.lucas.xaudio.mediaplayer.events.AudioLoadEvent;
import com.lucas.xaudio.mediaplayer.events.AudioPlayModeEvent;
import com.lucas.xaudio.mediaplayer.events.AudioProgressEvent;
import com.lucas.xaudio.mediaplayer.events.AudioReleaseEvent;
import com.lucas.xaudio.mediaplayer.view.MusicNotificationHelper;
import com.lucas.xaudio.mediaplayer.view.NotificationHelperListener;

import androidx.annotation.Nullable;

import static com.lucas.xaudio.XAudio.ACTION_STATUS_BAR;


/**
 * 音乐后台服务,并更新notification状态
 */
public class MusicService extends Service implements NotificationHelperListener {

    private static String DATA_AUDIOS = "AUDIOS";
    private static String ACTION_START = "ACTION_START";
    private NotificationReceiver mReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerBroadcastReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MusicNotificationHelper.getInstance().init(this, XAudio.getInstance().getNotificationIntent());
        return super.onStartCommand(intent, flags, startId);
    }



    private void registerBroadcastReceiver() {
        if (mReceiver == null) {
            mReceiver = new NotificationReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_STATUS_BAR);
            registerReceiver(mReceiver, filter);
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onNotificationInit() {
        //service与Notification绑定
        startForeground(MusicNotificationHelper.NOTIFICATION_ID, MusicNotificationHelper.getInstance().getNotification());

        XAudio.getInstance().addAudioServiceListener(new AudioServiceListener() {
            @Override
            public void onAudioPause() {
                //更新notifacation为暂停状态
                MusicNotificationHelper.getInstance().showPauseStatus();
            }

            @Override
            public void onAudioStart() {
                //更新notifacation为播放状态
                MusicNotificationHelper.getInstance().showPlayStatus();
            }

            @Override
            public void onAudioProgress(AudioProgressEvent event) {

            }

            @Override
            public void onAudioModeChange(AudioPlayModeEvent event) {

            }

            @Override
            public void onAudioLoad(AudioLoadEvent event) {
                //更新notifacation为load状态
                MusicNotificationHelper.getInstance().showLoadStatus(event.mAudioBean);
            }

            @Override
            public void onAudioFavouriteEvent(AudioFavouriteEvent event) {
                //更新notifacation收藏状态
                MusicNotificationHelper.getInstance().changeFavouriteStatus(event.isFavourite);
            }

            @Override
            public void onAudioReleaseEvent(AudioReleaseEvent event) {
                //移除notifacation
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBroadcastReceiver();
    }


}
