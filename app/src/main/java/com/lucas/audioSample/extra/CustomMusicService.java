package com.lucas.audioSample.extra;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.lucas.audioSample.BroadcastReceiver.NotificationReceiver;
import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.AudioServiceListener;
import com.lucas.xaudio.audioplayer.events.AudioFavouriteEvent;
import com.lucas.xaudio.audioplayer.events.AudioLoadEvent;
import com.lucas.xaudio.audioplayer.events.AudioPlayModeEvent;
import com.lucas.xaudio.audioplayer.events.AudioProgressEvent;
import com.lucas.xaudio.audioplayer.NotificationHelperListener;
import com.lucas.audioSample.ui.MusicPlayerActivity;

import androidx.annotation.Nullable;

import static com.lucas.xaudio.XAudio.ACTION_STATUS_BAR;


/**
 * 自定义的音乐后台服务
 * 更新notification状态
 */
public class CustomMusicService extends Service implements NotificationHelperListener {

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
        MusicNotificationHelper.getInstance().init(this, new Intent(this, MusicPlayerActivity.class));
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
            public void onAudioReleaseEvent() {
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
