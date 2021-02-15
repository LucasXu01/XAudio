package com.lucas.xaudio.extra;

import com.lucas.xaudio.audioplayer.core.AudioController;
import com.lucas.xaudio.audioplayer.events.AudioFavouriteEvent;
import com.lucas.xaudio.audioplayer.model.AudioBean;

import java.util.ArrayList;

public interface ITXAudio {

    /**
     * 添加音频 
     * 注意：必须先setQueen，才能继续add，否则会报queen null
     */
    ITXAudio setAudioQueen(ArrayList<AudioBean> queue);
    ITXAudio addAudio(AudioBean bean);
    ITXAudio addAudio(ArrayList<AudioBean> queue);
    
    /**
     * 播放 音频
     */
    void playAudio();

    /**
     * 暂停 音频
     */
    void pauseAudio();

    /**
     * 重新播放 音频
     */
    void resumeAudio();

    /**
     * 播放/暂停 音频
     */
    void playOrPauseAudio();

    /**
     * 释放 音频
     */
    void releaseAudio();

    /**
     * 设置音频的播放进度
     */
    void seekTo(int progress);
    
    /**
     * 切换到音频
     */
    void setPlayMode(AudioController.PlayMode playMode);

    /**
     * 获取当前的播放模式
     */
    AudioController.PlayMode getPlayMode();

    /**
     * 对外提供是否播放中状态
     */
    boolean isStartState();

    /**
     * 喜欢曲子
     */
    void onAudioFavouriteEvent(AudioFavouriteEvent event);

    /**
     * 移除某个曲子
     */
    void removeAudio(AudioBean audioBean);

    /**
     * 移除所有曲子
     */
    void clearAudio();




}
