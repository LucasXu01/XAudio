package com.lucas.xaudio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lucas.xaudio.audioplayer.core.AudioController;
import com.lucas.xaudio.audioplayer.events.AudioLoadEvent;
import com.lucas.xaudio.audioplayer.events.AudioPlayModeEvent;
import com.lucas.xaudio.audioplayer.events.AudioProgressEvent;
import com.lucas.xaudio.audioplayer.model.BaseAudioBean;
import com.lucas.xaudio.utils.XAudioUtils;


import java.util.ArrayList;
import java.util.List;

/**
 * author : LucasXu
 * time   : 2021/1/8 2:08 PM
 * des    : 唯一与外界通信的用户操作管理类
 */

public final class XAudio implements ITXAudio {

    public static final String ACTION_STATUS_BAR = "XAUDIO_NOTIFICATION_ACTIONS";
    public static final String EXTRA = "extra";
    public static final String EXTRA_PLAY = "play_pause";
    public static final String EXTRA_NEXT = "play_next";
    public static final String EXTRA_PRE = "play_previous";
    public static final String EXTRA_FAV = "play_favourite";
    public static final String EXTRA_CLOSE = "play_close";

    private static final String TAG = "XAudio";
    private static Context mContext; //SDK全局Context
    private static List<AudioServiceListener> sAudioServiceListeners = new ArrayList<>();
    private Service mService;
    private Intent notificationIntent;

    /**
     * 在主Application中调用
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
    }

    public static XAudio getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final XAudio sInstance = new XAudio();
    }

    // 添加音频的监听事件
    public void addAudioServiceListener(AudioServiceListener audioServiceListener) {
        Log.i(TAG, "addAudioServiceListener:" + sAudioServiceListeners.size() + "| l:" + audioServiceListener);
        if (audioServiceListener != null && !sAudioServiceListeners.contains(audioServiceListener)) {
            sAudioServiceListeners.add(audioServiceListener);
        }
    }

    // 移除音频的监听事件
    public void removeAudioServiceListener(AudioServiceListener audioServiceListener) {
        Log.i(TAG, "removeAudioServiceListener:" + sAudioServiceListeners.size() + "| l:" + audioServiceListener);
        if (audioServiceListener == null) {
            sAudioServiceListeners.clear();
        } else {
            sAudioServiceListeners.remove(audioServiceListener);
        }
    }

    /**
     * 设置通知栏的意图
     */
    public XAudio setNotificationIntent(Intent intent){
        this.notificationIntent = intent;
        return this;
    }
    public Intent getNotificationIntent(){
        return notificationIntent;
    }

    /**
     * 添加音频
     */
    public XAudio addAudio(BaseAudioBean bean) {
        AudioController.getInstance().addAudio(bean);
        return this;
    }

    public XAudio addAudio(ArrayList<BaseAudioBean> queue) {
        AudioController.getInstance().addAudio(queue);
        return this;
    }

    /**
     * 播放 音频
     */
    public void playAudio() {
        AudioController.getInstance().play();
        if (mService != null && !XAudioUtils.isServiceRunning(mContext, mService.getClass().getName())) {
            Intent intent = new Intent(XAudio.getInstance().getContext(), mService.getClass());
            XAudio.getInstance().getContext().startService(intent);
        }
    }

    /**
     * 暂停 音频
     */
    public void pauseAudio() {
        AudioController.getInstance().pause();
    }

    /**
     * 重新播放 音频
     */
    public void resumeAudio() {
        AudioController.getInstance().resume();
    }

    /**
     * 播放/暂停 音频
     */
    public void playOrPauseAudio() {
        AudioController.getInstance().playOrPause();
        if (mService != null && !XAudioUtils.isServiceRunning(mContext, mService.getClass().getName())) {
            Intent intent = new Intent(XAudio.getInstance().getContext(), mService.getClass());
            XAudio.getInstance().getContext().startService(intent);
        }
    }

    /**
     * 上一首
     */
    public void previousAudio(){
        AudioController.getInstance().previous();
    }

    /**
     * 下一首
     */
    public void nextAudio(){
        AudioController.getInstance().next();
    }

    /**
     * 释放 音频
     */
    public void releaseAudio() {
        AudioController.getInstance().release();
    }

    /**
     * 设置音频的播放进度
     */
    public void seekTo(int progress) {
        AudioController.getInstance().seekTo(progress);
    }


    /**
     * 切换到音频
     */
    public void setPlayMode(AudioController.PlayMode playMode) {
        AudioController.getInstance().setPlayMode(playMode);
    }

    /**
     * 获取当前的播放模式
     */
    public AudioController.PlayMode getPlayMode() {
        return AudioController.getInstance().getPlayMode();
    }

    /**
     * 对外提供是否播放中状态
     */
    public boolean isStartState() {
        return AudioController.getInstance().isStartState();
    }


    /**
     * 移除某个曲子
     */
    @Override
    public void removeAudio(BaseAudioBean audioBean) {
        AudioController.getInstance().removeAudio(audioBean);
    }

    /**
     * 移除所有曲子
     */
    @Override
    public void clearAudioList() {
        AudioController.getInstance().clearAudioList();
    }

    public Context getContext() {
        return mContext;
    }


    /**
     * 设置通知栏
     */
    public XAudio setNotification(Service mService) {
        this.mService = mService;
        return this;
    }



    /**
     * 设置服务
     * 可选：
     * 不调用setAutoService不会有服务，
     * 不带参为自带的服务，
     * 或者根据需要填写自己的Service参
     */
//    public XAudio setAutoService() {
//        this.mService = new MusicService();
//        return this;
//    }
    /**
     * 设置服务
     */
    public XAudio setAutoService(Service mService) {
        this.mService = mService;
        return this;
    }

    public Service getService(){
        return mService;
    }

    /**
     * ********** 以下是对外的音频状态变化接口 *************
     */
    public void onAudioStartEvent() {
        Log.e(TAG, "onAudioStartEvent: ");
        //开始播放状态
        for (AudioServiceListener l : sAudioServiceListeners) {
            l.onAudioStart();
        }
    }

    public void onAudioPauseEvent() {
        Log.e(TAG, "onAudioPauseEvent: ");
        //暂停状态
        for (AudioServiceListener l : sAudioServiceListeners) {
            l.onAudioPause();
        }
    }

    public void onAudioProgessEvent(AudioProgressEvent audioProgressEvent) {
        //更新时间
        for (AudioServiceListener l : sAudioServiceListeners) {
            l.onAudioProgress(audioProgressEvent);
        }
    }

    public void onAudioPlayModeEvent(AudioPlayModeEvent audioPlayModeEvent) {
        //更新播放模式
        for (AudioServiceListener l : sAudioServiceListeners) {
            l.onAudioModeChange(audioPlayModeEvent);
        }
    }

    public void onAudioLoadEvent(AudioLoadEvent audioLoadEvent) {
        for (AudioServiceListener l : sAudioServiceListeners) {
            l.onAudioLoad(audioLoadEvent);
        }
    }

    public void onAudioReleaseEvent() {
        for (AudioServiceListener l : sAudioServiceListeners) {
            l.onAudioReleaseEvent();
        }
    }

    public void onAudioCompleteEvent() {
        nextAudio();
        for (AudioServiceListener l : sAudioServiceListeners) {
            l.onAudioCompleteEvent();
        }
    }

    public void onAudioErrorEvent() {
        nextAudio();
        for (AudioServiceListener l : sAudioServiceListeners) {
            l.onAudioErrorEvent();
        }
    }
    /**
     * ******************** 以上 **********************
     */

}
