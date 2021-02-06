package com.lucas.audioSample.view;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.lucas.audioSample.R;
import com.lucas.xaudio.recorder.AudioRecord;
import com.lucas.xaudio.recorder.AudioRecordConfig;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AudioRecorderActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = AudioRecorderActivity.class.getSimpleName();

    public enum AudioRecorderActivityPState {
        PREPARE,
        PLAYING,
        PAUSED,
        STOP,
        RELEASE
    }

    private RadioGroup mRGOutputFormat;
    private Button mBtnStart;
    private Button mBtnPause;
    private Button mBtnStop;

    private AudioRecord mAudioRecord;
    private AudioRecordConfig mConfig;
    private String path;
    private boolean isPaused = false;  //是否暂停
    private AudioRecorderActivityPState mState = AudioRecorderActivityPState.RELEASE;
    private boolean isPlaying = false;
    private ExecutorService mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        mExecutor = Executors.newCachedThreadPool();

        mRGOutputFormat = findViewById(R.id.mr_rgOutputFormat);
        mBtnStart = findViewById(R.id.mr_btnStart);
        mBtnPause = findViewById(R.id.mr_btnPause);
        mBtnStop = findViewById(R.id.mr_btnStop);

        mBtnStart.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);

        mRGOutputFormat.setOnCheckedChangeListener(this);

    }

    void prepare() {
        if (mState == AudioRecorderActivityPState.RELEASE) {
            mConfig = new AudioRecordConfig(
                    MediaRecorder.AudioSource.MIC,
                    AudioRecordConfig.SampleRate.SAMPPLERATE_44100,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioRecordConfig.OutputFormat.WAV);
            mAudioRecord = new AudioRecord(mConfig, PathUtils.getExternalStoragePath() + "/XAudio/", UUID.randomUUID().toString());
            mAudioRecord.prepare();
            mState = AudioRecorderActivityPState.PREPARE;
        }
    }

    void start() {

        prepare();

        if (mState == AudioRecorderActivityPState.PLAYING) {
            Toast.makeText(this, "AudioRecord is Recording.", Toast.LENGTH_LONG).show();
        }
        if (mState == AudioRecorderActivityPState.RELEASE) {
            Toast.makeText(this, "Please click the stop button.", Toast.LENGTH_LONG).show();
        }
        mAudioRecord.start();
        mState = AudioRecorderActivityPState.PLAYING;
    }

    void pause() {
        if (mState == AudioRecorderActivityPState.PLAYING) {
            isPaused = true;
            mAudioRecord.pause();
            mBtnPause.setText("resume");
            mState = AudioRecorderActivityPState.PAUSED;
            return;
        } else {
            Toast.makeText(this, "null", Toast.LENGTH_LONG).show();
        }
    }

    void resume() {
        if (mState == AudioRecorderActivityPState.PAUSED) {
            isPaused = false;
            mAudioRecord.resume();
            mBtnPause.setText("pause");
            mState = AudioRecorderActivityPState.PLAYING;
        }
    }

    void stop() {
        if (mState == AudioRecorderActivityPState.PLAYING || mState == AudioRecorderActivityPState.PAUSED) {
            mAudioRecord.stop();
            mState = AudioRecorderActivityPState.STOP;
            release();
        }
        prepare();
    }

    void release() {
        mState = AudioRecorderActivityPState.RELEASE;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.mr_btnStart:
                start();
                break;
            case R.id.mr_btnPause:
                if (isPaused) {
                    resume();
                } else {
                    pause();
                }
                break;
            case R.id.mr_btnStop:
                stop();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
        }
        if (mAudioRecord != null) {
            mAudioRecord.release();
        }

        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if (mState == AudioRecorderActivityPState.PLAYING || mState == AudioRecorderActivityPState.PAUSED) {
            Toast.makeText(this, "Please click the stop button.", Toast.LENGTH_LONG).show();
            return;
        } else {
            switch (id) {
                case R.id.mr_rbOutputFormat_mp3:
//                    outputFormat = AudioRecordConfig.OutputFormat.MP3;
                    Log.d(TAG, "onCheckedChanged: mp3");
                    break;

            }
        }


    }
}