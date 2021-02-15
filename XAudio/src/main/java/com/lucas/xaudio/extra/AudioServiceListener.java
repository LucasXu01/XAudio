package com.lucas.xaudio.extra;


import com.lucas.xaudio.audioplayer.events.AudioFavouriteEvent;
import com.lucas.xaudio.audioplayer.events.AudioLoadEvent;
import com.lucas.xaudio.audioplayer.events.AudioPlayModeEvent;
import com.lucas.xaudio.audioplayer.events.AudioProgressEvent;
import com.lucas.xaudio.audioplayer.events.AudioReleaseEvent;

/**
 * 音频基本础对外接口
 *
 */
public interface AudioServiceListener{

    void onAudioPause();

    void onAudioStart();

    void onAudioProgress(AudioProgressEvent event);

    void onAudioModeChange(AudioPlayModeEvent event);

    void onAudioLoad(AudioLoadEvent event);

    void onAudioFavouriteEvent(AudioFavouriteEvent event);

    void onAudioReleaseEvent(AudioReleaseEvent event);


}
