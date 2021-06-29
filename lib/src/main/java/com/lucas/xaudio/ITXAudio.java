package com.lucas.xaudio;

import com.lucas.xaudio.audioplayer.core.AudioController;
import com.lucas.xaudio.audioplayer.model.BaseAudioBean;

import java.util.ArrayList;

public interface ITXAudio {

    /**
     * 添加音频 
     */
    ITXAudio addAudio(BaseAudioBean bean);
    ITXAudio addAudio(ArrayList<BaseAudioBean> queue);
    
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
     * 移除某个曲子
     */
    void removeAudio(BaseAudioBean audioBean);

    /**
     * 移除所有曲子
     */
    void clearAudioList();

    /**
     * 获得所有曲子
     * @return
     */
    ArrayList<BaseAudioBean> getAudioQueue();




}
