package com.lucas.xaudio.audioplayer.events;


import com.lucas.xaudio.audioplayer.model.BaseAudioBean;

public class AudioLoadEvent {
  public BaseAudioBean mAudioBean;

  public AudioLoadEvent(BaseAudioBean audioBean) {
    this.mAudioBean = audioBean;
  }
}
