package com.lucas.audioSample.view;

import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.lucas.audioSample.R;
import com.lucas.audioSample.utils.MyUtils;
import com.lucas.xaudio.recorder.AudioRecordConfig;
import com.lucas.xaudio.recorder.XRecorder;
import com.lucas.xaudio.recorder.waveview.AudioWaveView;

import java.io.IOException;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

import static com.lucas.audioSample.utils.MyUtils.getScreenWidth;

public class RecordSampleActivity extends AppCompatActivity {

    private static final String TAG = RecordSampleActivity.class.getSimpleName();


    private Button bt_record;
    private Button bt_record_pause;
    private Button bt_record_stop;
    private Button bt_reset;
    private AudioWaveView audioWave;
    private RadioGroup rg_OutputFormat;

    XRecorder mRecorder;
    boolean mIsRecord = false;

    private AudioRecordConfig mConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sample);


        bt_record = findViewById(R.id.bt_record);
        bt_record_pause = findViewById(R.id.bt_record_pause);
        bt_record_stop = findViewById(R.id.bt_record_stop);
        bt_reset = findViewById(R.id.bt_reset);
        audioWave = (AudioWaveView) findViewById(R.id.audioWave);
        rg_OutputFormat = findViewById(R.id.rg_OutputFormat);

        rg_OutputFormat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_rbOutputFormat_mp3:
                         mConfig = new AudioRecordConfig(
                                MediaRecorder.AudioSource.MIC,
                                AudioRecordConfig.SampleRate.SAMPPLERATE_44100,
                                AudioFormat.CHANNEL_IN_STEREO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                AudioRecordConfig.OutputFormat.MP3);
                        break;
                    case R.id.rb_OutputFormat_aac:
                        mConfig = new AudioRecordConfig(
                                MediaRecorder.AudioSource.MIC,
                                AudioRecordConfig.SampleRate.SAMPPLERATE_44100,
                                AudioFormat.CHANNEL_IN_STEREO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                AudioRecordConfig.OutputFormat.AAC);
                        break;
                    case R.id.mr_rbOutputFormat_wav:
                        mConfig = new AudioRecordConfig(
                                MediaRecorder.AudioSource.MIC,
                                AudioRecordConfig.SampleRate.SAMPPLERATE_44100,
                                AudioFormat.CHANNEL_IN_STEREO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                AudioRecordConfig.OutputFormat.WAV);
                        break;
                    case R.id.mr_rbOutputFormat_pcm:
                        mConfig = new AudioRecordConfig(
                                MediaRecorder.AudioSource.MIC,
                                AudioRecordConfig.SampleRate.SAMPPLERATE_44100,
                                AudioFormat.CHANNEL_IN_STEREO,
                                AudioFormat.ENCODING_PCM_16BIT,
                                AudioRecordConfig.OutputFormat.PCM);
                        break;

                }
            }
        });


        //录音
        bt_record.setOnClickListener(v->{
            resolveRecord();
        });

        //暂停
        bt_record_pause.setOnClickListener(v->{
            resolvePause();
        });

        //停止
        bt_record_stop.setOnClickListener(v->{
            resolveStopRecord();
        });

        //重置
        bt_reset.setOnClickListener(v->{
            resolveResetPlay();
        });

        resolveNormalUI();

    }

    /**
     * 开始录音
     */
    private void resolveRecord() {


        int offset = MyUtils.dip2px(this, 7);

        mRecorder = new XRecorder(mConfig, PathUtils.getExternalStoragePath() + "/XAudio/", UUID.randomUUID().toString());
        int size = getScreenWidth(this) / offset;//控件默认的间隔是1
        mRecorder.setDataList(audioWave.getRecList(), size);

        //高级用法
//        int size2 = (getScreenWidth(this) / 2) / MyUtils.dip2px(this, 1);
//        mRecorder.setWaveSpeed(600);
//        mRecorder.setDataList(audioWave.getRecList(), size2);
//        audioWave.setDrawStartOffset((getScreenWidth(this) / 2));
//        audioWave.setDrawReverse(true);
//        audioWave.setDataReverse(true);

        //自定义paint
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(6);
        audioWave.setLinePaint(paint);
        audioWave.setOffset(offset);   // 设置线与线之间的偏移
        audioWave.setWaveCount(2);     // 1单边  2双边
        audioWave.setDrawBase(false);  // 是否画出基线
//        audioWave.setBaseRecorder(mRecorder); //设置好偶波形会变色

        try {
            mRecorder.start();
            audioWave.startView();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "录音出现异常", Toast.LENGTH_SHORT).show();
            resolveError();
            return;
        }
        resolveRecordUI();
        mIsRecord = true;
    }

    /**
     * 暂停
     */
    private void resolvePause() {
        if (!mIsRecord)
            return;
        resolvePauseUI();
        if (mRecorder.isPause()) {
            resolveRecordUI();
            audioWave.setPause(false);
            mRecorder.setPause(false);
            bt_record_pause.setText("暂停");
        } else {
            audioWave.setPause(true);
            mRecorder.setPause(true);
            bt_record_pause.setText("继续");
        }
    }

    /**
     * 停止录音
     */
    private void resolveStopRecord() {
        resolveStopUI();
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.setPause(false);
            mRecorder.stop();
            audioWave.stopView();
        }
        mIsRecord = false;
        bt_record_pause.setText("暂停");

    }

    /**
     * 录音异常
     */
    private void resolveError() {
        resolveNormalUI();
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.stop();
            audioWave.stopView();
        }
    }

    /**
     * 重置
     */
    private void resolveResetPlay() {
        resolveNormalUI();
    }

    private void resolveRecordUI() {
        bt_record.setEnabled(false);
        bt_record_pause.setEnabled(true);
        bt_record_stop.setEnabled(true);
        bt_reset.setEnabled(false);
    }

    private void resolveNormalUI() {
        bt_record.setEnabled(true);
        bt_record_pause.setEnabled(false);
        bt_record_stop.setEnabled(false);
        bt_reset.setEnabled(false);
    }

    private void resolveStopUI() {
        bt_record.setEnabled(true);
        bt_record_stop.setEnabled(false);
        bt_record_pause.setEnabled(false);
        bt_reset.setEnabled(true);
    }

    private void resolvePauseUI() {
        bt_record.setEnabled(false);
        bt_record_pause.setEnabled(true);
        bt_record_stop.setEnabled(false);
        bt_reset.setEnabled(false);
    }


}