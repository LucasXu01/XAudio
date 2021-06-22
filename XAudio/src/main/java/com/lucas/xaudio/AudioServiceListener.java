package com.lucas.xaudio;


import com.lucas.xaudio.audioplayer.events.AudioFavouriteEvent;
import com.lucas.xaudio.audioplayer.events.AudioLoadEvent;
import com.lucas.xaudio.audioplayer.events.AudioPlayModeEvent;
import com.lucas.xaudio.audioplayer.events.AudioProgressEvent;

/**
 * 音频基本础对外接口
 *
 */
public interface AudioServiceListener{

    default void onAudioStart(){

    }

    default void onAudioPause(){

    }

    default void onAudioProgress(AudioProgressEvent event){

    }

    default void onAudioModeChange(AudioPlayModeEvent event){

    }

    default void onAudioLoad(AudioLoadEvent event){

    }

    default void onAudioFavouriteEvent(AudioFavouriteEvent event){

    }

    default void onAudioReleaseEvent(){

    }

    default void onAudioCompleteEvent(){
    }

    default void onAudioErrorEvent(){
    }



}
