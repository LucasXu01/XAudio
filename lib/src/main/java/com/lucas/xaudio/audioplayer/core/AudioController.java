package com.lucas.xaudio.audioplayer.core;


import android.util.Log;

import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.audioplayer.events.AudioPlayModeEvent;
import com.lucas.xaudio.audioplayer.exception.AudioQueueEmptyException;
import com.lucas.xaudio.audioplayer.model.BaseAudioBean;
import com.lucas.xaudio.recorder.XRecorder;


import java.util.ArrayList;
import java.util.Random;

/**
 * 控制播放逻辑类，注意添加一些控制方法时，要考虑是否需要增加Event,来更新UI
 */
public class AudioController {

    /**
     * 播放方式
     */
    public enum PlayMode {
        /**
         * 列表循环
         */
        LOOP,
        /**
         * 随机
         */
        RANDOM,
        /**
         * 单曲循环
         */
        REPEAT
    }
    private String TAG = AudioController.class.getSimpleName();
    private AudioPlayer mAudioPlayer;
    //播放队列,不能为空,不设置主动抛错
    private ArrayList<BaseAudioBean> mQueue = new ArrayList<>();
    private int mQueueIndex = 0;
    private PlayMode mPlayMode = PlayMode.LOOP;

    private AudioController() {
        mAudioPlayer = new AudioPlayer();
    }

    public static AudioController getInstance() {
        return AudioController.SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static AudioController instance = new AudioController();
    }

    private void addCustomAudio(int index, BaseAudioBean bean) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        mQueue.add(index, bean);
    }

    private int queryAudio(BaseAudioBean bean) {
        return mQueue.indexOf(bean);
    }

    private void load(BaseAudioBean bean) {
        mAudioPlayer.load(bean);
    }

    /*
     * 获取播放器当前状态
     */
    private CustomMediaPlayer.Status getStatus() {
        return mAudioPlayer.getStatus();
    }

    private BaseAudioBean getNextPlaying() {
        switch (mPlayMode) {
            case LOOP:
                mQueueIndex = (mQueueIndex + 1) % mQueue.size();
                return getPlaying(mQueueIndex);
            case RANDOM:
                mQueueIndex = new Random().nextInt(mQueue.size()) % mQueue.size();
                return getPlaying(mQueueIndex);
            case REPEAT:
                return getPlaying(mQueueIndex);
        }
        return null;
    }

    private BaseAudioBean getPreviousPlaying() {
        switch (mPlayMode) {
            case LOOP:
                mQueueIndex = (mQueueIndex + mQueue.size() - 1) % mQueue.size();
                return getPlaying(mQueueIndex);
            case RANDOM:
                mQueueIndex = new Random().nextInt(mQueue.size()) % mQueue.size();
                return getPlaying(mQueueIndex);
            case REPEAT:
                return getPlaying(mQueueIndex);
        }
        return null;
    }

    private BaseAudioBean getPlaying(int index) {
        if (mQueue != null && !mQueue.isEmpty() && index >= 0 && index < mQueue.size()) {
            return mQueue.get(index);
        } else {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
    }


    /**
     * 对外提供是否播放中状态
     */
    public boolean isStartState() {
        return CustomMediaPlayer.Status.STARTED == getStatus();
    }

    /**
     * 对外提提供是否暂停状态
     */
    public boolean isPauseState() {
        return CustomMediaPlayer.Status.PAUSED == getStatus();
    }

    public ArrayList<BaseAudioBean> getQueue() {
        return mQueue == null ? new ArrayList<BaseAudioBean>() : mQueue;
    }

    /**
     * 队列头添加播放歌曲
     * 添加队列歌曲，歌曲index不变
     */
    public void addAudio(BaseAudioBean bean) {
        this.addAudio(0, bean);
    }

    public void addAudio(ArrayList<BaseAudioBean> queue) {
        for (BaseAudioBean baseAudioBean :  queue){
            addAudio(baseAudioBean);
        }
    }

    public void addAudio(int index, BaseAudioBean bean) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        int query = queryAudio(bean);
        if (query <= -1) {
            //没添加过此id的歌曲，添加且直播番放
            addCustomAudio(index, bean);
//            setPlayIndex(index);
        } else {
            BaseAudioBean currentBean = getNowPlaying();
            if (!currentBean.id.equals(bean.id)) {
                //添加过且不是当前播放，播，否则什么也不干
//                setPlayIndex(query);
            }
        }
    }

    public void removeAudio(BaseAudioBean bean) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        for (BaseAudioBean audioBean : mQueue) {
            if (audioBean.id.equals(bean.id)) {
                mQueue.remove(bean);
            }
        }
    }

    public void clearAudioList() {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        mQueue.clear();
    }

    public PlayMode getPlayMode() {
        return mPlayMode;
    }

    public void setPlayMode(PlayMode playMode) {
        mPlayMode = playMode;
        //还要对外发送切换事件，更新UI
        XAudio.getInstance().onAudioPlayModeEvent(new AudioPlayModeEvent(mPlayMode));
    }

    public int getQueueIndex() {
        return mQueueIndex;
    }


    /**
     * 播放/暂停切换
     */
    public void playOrPause() {
        if (isStartState()) {
            pause();
        } else if (isPauseState()) {
            resume();
        }
    }

    /**
     * 播放指定index的歌曲
     */
    public void play(int index) {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
        if ( index > 0 && index <  mQueue.size() ) {
            mQueueIndex = index;
            play();
        }else {
            Log.e(TAG, "play: 播放指定index的歌曲错误  index超出Queue范围");
        }

    }

    /**
     * 加载当前index歌曲
     */
    public void play() {
        BaseAudioBean bean = getPlaying(mQueueIndex);
        load(bean);
    }

    /**
     * 加载next index歌曲
     */
    public void next() {
        BaseAudioBean bean = getNextPlaying();
        load(bean);
    }

    /**
     * 加载previous index歌曲
     */
    public void previous() {
        BaseAudioBean bean = getPreviousPlaying();
        load(bean);
    }

    /**
     * 对外提供获取当前播放时间
     */
    public int getNowPlayTime() {
        return mAudioPlayer.getCurrentPosition();
    }

    /**
     * 对外提供获取总播放时间
     */
    public int getTotalPlayTime() {
        return mAudioPlayer.getCurrentPosition();
    }

    /**
     * 对外提供的获取当前歌曲信息
     */
    public BaseAudioBean getNowPlaying() {
        return getPlaying(mQueueIndex);
    }

    public void resume() {
        mAudioPlayer.resume();
    }

    public void pause() {
        mAudioPlayer.pause();
    }

    public void seekTo(int progress) {
        mAudioPlayer.seekTo(progress);
    }

    public void release() {
        mAudioPlayer.release();
    }

}
