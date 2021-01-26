package com.lucas.xaudio.mediaplayer.events;


import com.lucas.xaudio.mediaplayer.core.AudioController;

/**
 * 播放模式切换事件
 */
public class AudioPlayModeEvent {
  public AudioController.PlayMode mPlayMode;

  public AudioPlayModeEvent(AudioController.PlayMode playMode) {
    this.mPlayMode = playMode;
  }
}
