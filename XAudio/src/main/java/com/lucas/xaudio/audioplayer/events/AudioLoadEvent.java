package com.lucas.xaudio.audioplayer.events;


import com.lucas.xaudio.audioplayer.model.AudioBean;

public class AudioLoadEvent {
  public AudioBean mAudioBean;

  public AudioLoadEvent(AudioBean audioBean) {
    this.mAudioBean = audioBean;
  }
}
