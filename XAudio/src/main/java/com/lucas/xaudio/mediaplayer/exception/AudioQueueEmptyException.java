package com.lucas.xaudio.mediaplayer.exception;

/**
 * 播放队列为空异常
 */
public class AudioQueueEmptyException extends RuntimeException {

  public AudioQueueEmptyException(String error) {
    super(error);
  }
}
