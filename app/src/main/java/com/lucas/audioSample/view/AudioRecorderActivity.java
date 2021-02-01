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

import com.lucas.audioSample.R;
import com.lucas.xaudio.recorder.AudioRecord;
import com.lucas.xaudio.recorder.AudioRecordConfig;
import com.lucas.xaudio.utils.FileUtils;

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
    private RadioGroup mRGSamplingRate;
    private RadioGroup mRGBitRate;
    private RadioGroup mRGChannel;
    private Button mBtnStart;
    private Button mBtnPause;
    private Button mBtnStop;

    private AudioRecord mAudioRecord;
    private AudioRecordConfig mConfig;
    private String path;
    private boolean isPaused = false;  //是否暂停
    private AudioRecorderActivityPState mState = AudioRecorderActivityPState.RELEASE;
    private AudioRecordConfig.OutputFormat outputFormat = AudioRecordConfig.OutputFormat.AAC;
    //    private AudioRecordConfig.OutputFormat outputFormat = AudioRecordConfig.OutputFormat.PCM;
//    private AudioRecordConfig.OutputFormat outputFormat = AudioRecordConfig.OutputFormat.WAV;
    private int sampleRate = AudioRecordConfig.SampleRate.SAMPPLERATE_44100;
    private int bitRate = AudioFormat.ENCODING_PCM_16BIT;
    private int channels = AudioFormat.CHANNEL_IN_STEREO;
    private boolean isPlaying = false;
    private ExecutorService mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        mExecutor = Executors.newCachedThreadPool();

        mRGOutputFormat = findViewById(R.id.mr_rgOutputFormat);
        mRGSamplingRate = findViewById(R.id.mr_rgSamplingRate);
        mRGBitRate = findViewById(R.id.mr_rgBitrate);
        mRGChannel = findViewById(R.id.mr_rgChannel);
        mBtnStart = findViewById(R.id.mr_btnStart);
        mBtnPause = findViewById(R.id.mr_btnPause);
        mBtnStop = findViewById(R.id.mr_btnStop);

        mBtnStart.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);

        mRGOutputFormat.setOnCheckedChangeListener(this);
        mRGBitRate.setOnCheckedChangeListener(this);
        mRGSamplingRate.setOnCheckedChangeListener(this);
        mRGChannel.setOnCheckedChangeListener(this);

    }

    void prepare() {
        if (mState == AudioRecorderActivityPState.RELEASE) {
            mConfig = new AudioRecordConfig(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channels,
                    bitRate,
                    outputFormat);
            mAudioRecord = new AudioRecord(mConfig, FileUtils.getAppPath(), "demo");
            mAudioRecord.prepare();
            updateState(AudioRecorderActivityPState.PREPARE);
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
        updateState(AudioRecorderActivityPState.PLAYING);
    }

    void pause() {
        if (mState == AudioRecorderActivityPState.PLAYING) {
            isPaused = true;
            mAudioRecord.pause();
            mBtnPause.setText("resume");
            updateState(AudioRecorderActivityPState.PAUSED);
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
            updateState(AudioRecorderActivityPState.PLAYING);
        }
    }

    void stop() {
        if (mState == AudioRecorderActivityPState.PLAYING || mState == AudioRecorderActivityPState.PAUSED) {
            mAudioRecord.stop();
            updateState(AudioRecorderActivityPState.STOP);
            release();
        }
        prepare();
    }

    void release() {
        updateState(AudioRecorderActivityPState.RELEASE);
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

    void updateState(AudioRecorderActivityPState pState) {
        if (mState == pState) {
            return;
        } else {
            mState = pState;
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
                    outputFormat = AudioRecordConfig.OutputFormat.MP3;
                    Log.d(TAG, "onCheckedChanged: mp3");
                    break;
                case R.id.mr_rbOutputFormat_aac:
                    outputFormat = AudioRecordConfig.OutputFormat.AAC;
                    Log.d(TAG, "onCheckedChanged: aac");
                    break;
                case R.id.mr_rbOutputFormat_wav:
                    outputFormat = AudioRecordConfig.OutputFormat.WAV;
                    Log.d(TAG, "onCheckedChanged: wav");
                    break;
                case R.id.mr_rbOutputFormat_pcm:
                    outputFormat = AudioRecordConfig.OutputFormat.PCM;
                    Log.d(TAG, "onCheckedChanged: pcm");
                    break;
                case R.id.mr_rbSamplingRate_800:
                    sampleRate = AudioRecordConfig.SampleRate.SAMPPLERATE_800;
                    Log.d(TAG, "onCheckedChanged: sampleRate 800");
                    break;
                case R.id.mr_rbSamplingRate_1600:
                    sampleRate = AudioRecordConfig.SampleRate.SAMPPLERATE_1600;
                    Log.d(TAG, "onCheckedChanged: sampleRate 1600");
                    break;
                case R.id.mr_rbSamplingRate_44100:
                    sampleRate = AudioRecordConfig.SampleRate.SAMPPLERATE_44100;
                    Log.d(TAG, "onCheckedChanged: sampleRate 44100");
                    break;
                case R.id.mr_rbSamplingRate_48000:
                    sampleRate = AudioRecordConfig.SampleRate.SAMPPLERATE_48000;
                    Log.d(TAG, "onCheckedChanged: sampleRate 48000");
                    break;
                case R.id.mr_rbBitrate_8:
                    bitRate = AudioFormat.ENCODING_PCM_8BIT;
                    Log.d(TAG, "onCheckedChanged: bitRate 8");
                    break;
                case R.id.mr_rbBitrate_16:
                    bitRate = AudioFormat.ENCODING_PCM_16BIT;
                    Log.d(TAG, "onCheckedChanged: bitRate 16");
                    break;
                case R.id.mr_rbChannel_1:
                    channels = AudioFormat.CHANNEL_IN_MONO;
                    Log.d(TAG, "onCheckedChanged: channel MONO");
                    break;
                case R.id.mr_rbChannel_2:
                    channels = AudioFormat.CHANNEL_IN_STEREO;
                    Log.d(TAG, "onCheckedChanged: channel STEREO");
                    break;
            }
//            prepare();
        }


    }
}