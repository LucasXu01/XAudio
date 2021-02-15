package com.lucas.xaudio.audioplayer.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;


import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.audioplayer.events.AudioCompleteEvent;
import com.lucas.xaudio.audioplayer.events.AudioErrorEvent;
import com.lucas.xaudio.audioplayer.events.AudioLoadEvent;
import com.lucas.xaudio.audioplayer.events.AudioPauseEvent;
import com.lucas.xaudio.audioplayer.events.AudioProgressEvent;
import com.lucas.xaudio.audioplayer.events.AudioReleaseEvent;
import com.lucas.xaudio.audioplayer.events.AudioStartEvent;
import com.lucas.xaudio.audioplayer.model.AudioBean;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

/**
 * 播放器事件源
 */
public class AudioPlayer
    implements MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    AudioFocusManager.AudioFocusListener {

  private static final String TAG = "AudioPlayer";
  private static final int TIME_MSG = 0x01;
  private static final int TIME_INVAL = 100;
  //真正负责播放的核心MediaPlayer子类
  private CustomMediaPlayer mMediaPlayer;
  private WifiManager.WifiLock mWifiLock;
  //音频焦点监听器
  private AudioFocusManager mAudioFocusManager;
  private boolean isPausedByFocusLossTransient;
  //播放进度更新handler
  private Handler mHandler = new Handler(Looper.getMainLooper()) {
    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case TIME_MSG:
          //暂停也要更新进度，防止UI不同步，只不过进度一直一样
          if (getStatus() == CustomMediaPlayer.Status.STARTED
              || getStatus() == CustomMediaPlayer.Status.PAUSED) {
            //UI类型处理事件
            EventBus.getDefault()
                .post(new AudioProgressEvent(getStatus(), getCurrentPosition(), getDuration()));
            sendEmptyMessageDelayed(TIME_MSG, TIME_INVAL);
          }
          break;
      }
    }
  };

  /**
   * 完成唯一的mediaplayer初始化
   */
  public AudioPlayer() {
    init();
  }

  @Override public void audioFocusGrant() {
    //重新获得焦点
    setVolumn(1.0f, 1.0f);
    if (isPausedByFocusLossTransient) {
      resume();
    }
    isPausedByFocusLossTransient = false;
  }

  @Override public void audioFocusLoss() {
    //永久失去焦点，暂停
    if (mMediaPlayer != null) pause();
  }

  @Override public void audioFocusLossTransient() {
    //短暂失去焦点，暂停
    if (mMediaPlayer != null) pause();
    isPausedByFocusLossTransient = true;
  }

  @Override public void audioFocusLossDuck() {
    //瞬间失去焦点,
    setVolumn(0.5f, 0.5f);
  }

  @Override public void onBufferingUpdate(MediaPlayer mp, int percent) {

  }

  @Override public void onCompletion(MediaPlayer mp) {
    //发送播放完成事件,逻辑类型事件
    EventBus.getDefault().post(new AudioCompleteEvent());
  }

  @Override public boolean onError(MediaPlayer mp, int what, int extra) {
    //发送当次播放实败事件,逻辑类型事件
    EventBus.getDefault().post(new AudioErrorEvent());
    return false;
  }

  @Override public void onPrepared(MediaPlayer mp) {
    start();
  }

  //初始化播放器相关对象
  private void init() {
    mMediaPlayer = new CustomMediaPlayer();
    // 使用唤醒锁
    mMediaPlayer.setWakeMode(XAudio.getInstance().getContext(), PowerManager.PARTIAL_WAKE_LOCK);
    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mMediaPlayer.setOnCompletionListener(this);
    mMediaPlayer.setOnPreparedListener(this);
    mMediaPlayer.setOnBufferingUpdateListener(this);
    mMediaPlayer.setOnErrorListener(this);
    // 初始化wifi锁
    mWifiLock = ((WifiManager) XAudio.getInstance().getContext()
        .getApplicationContext()
        .getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
    // 初始化音频焦点管理器
    mAudioFocusManager = new AudioFocusManager(XAudio.getInstance().getContext(), this);
  }

  //获取播放器状态
  public CustomMediaPlayer.Status getStatus() {
    if (mMediaPlayer != null) {
      return mMediaPlayer.getState();
    } else {
      return CustomMediaPlayer.Status.STOPPED;
    }
  }

  /*
   * prepare以后自动调用start方法,外部不能调用
   */
  private void start() {
    // 获取音频焦点,保证我们的播放器顺利播放
    if (!mAudioFocusManager.requestAudioFocus()) {
      Log.e(TAG, "获取音频焦点失败");
    }
    mMediaPlayer.start();
    // 启用wifi锁
    mWifiLock.acquire();
    //更新进度
    mHandler.sendEmptyMessage(TIME_MSG);
    //发送start事件，UI类型处理事件
    EventBus.getDefault().post(new AudioStartEvent());
  }

  /**
   * 对外提供的加载音频的方法
   */
  public void load(AudioBean audioBean) {
    try {
      mMediaPlayer.reset();
      mMediaPlayer.setDataSource(audioBean.mUrl);
      mMediaPlayer.prepareAsync();
      //发送加载音频事件，UI类型处理事件
      EventBus.getDefault().post(new AudioLoadEvent(audioBean));
    } catch (IOException e) {
      e.printStackTrace();
      EventBus.getDefault().post(new AudioErrorEvent());
    }
  }

  /**
   * 对外提供的播放方法
   */
  public void resume() {
    if (getStatus() == CustomMediaPlayer.Status.PAUSED) {
      start();
    }
  }

  /**
   * 对外暴露pause方法
   */
  public void pause() {
    if (getStatus() == CustomMediaPlayer.Status.STARTED) {
      mMediaPlayer.pause();
      // 关闭wifi锁
      if (mWifiLock.isHeld()) {
        mWifiLock.release();
      }
      // 取消音频焦点
      if (mAudioFocusManager != null) {
        mAudioFocusManager.abandonAudioFocus();
      }
      //停止发送进度消息
      //mHandler.removeCallbacksAndMessages(null);
      //发送暂停事件,UI类型事件
      EventBus.getDefault().post(new AudioPauseEvent());
    }
  }

  /**
   * 销毁唯一mediaplayer实例,只有在退出app时使用
   */
  public void release() {
    if (mMediaPlayer == null) {
      return;
    }
    mMediaPlayer.release();
    mMediaPlayer = null;
    // 取消音频焦点
    if (mAudioFocusManager != null) {
      mAudioFocusManager.abandonAudioFocus();
    }
    // 关闭wifi锁
    if (mWifiLock.isHeld()) {
      mWifiLock.release();
    }
    mWifiLock = null;
    mAudioFocusManager = null;
    mHandler.removeCallbacksAndMessages(null);
    //发送销毁播放器事件,清除通知等
    EventBus.getDefault().post(new AudioReleaseEvent());
  }

  /**
   * 获取当前音乐总时长,更新进度用
   */
  public int getDuration() {
    if (getStatus() == CustomMediaPlayer.Status.STARTED
        || getStatus() == CustomMediaPlayer.Status.PAUSED) {
      return mMediaPlayer.getDuration();
    }
    return 0;
  }

  public int getCurrentPosition() {
    if (getStatus() == CustomMediaPlayer.Status.STARTED
        || getStatus() == CustomMediaPlayer.Status.PAUSED) {
      return mMediaPlayer.getCurrentPosition();
    }
    return 0;
  }

  public void setVolumn(float left, float right) {
    if (mMediaPlayer != null) mMediaPlayer.setVolume(left, right);
  }

  /**
   * 设置音频的播放进度
   * @param progress
   */
  public void seekTo(int progress) {
    if (mMediaPlayer != null) mMediaPlayer.seekTo(progress);
  }

}


