package com.lucas.audioSample.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lucas.audioSample.R;
import com.lucas.audioSample.utils.MyUtils;
import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.mediaplayer.model.AudioBean;
import com.lucas.xaudio.recorder.mp3recorder.MP3Recorder;
import com.lucas.xaudio.recorder.waveview.AudioPlayer;
import com.lucas.xaudio.recorder.waveview.AudioWaveView;
import com.lucas.xaudio.utils.FileUtils;
import com.lucas.xaudio.utils.XMusicUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

import static com.lucas.audioSample.utils.MyUtils.getScreenWidth;

public class RecordSampleActivity extends AppCompatActivity {


    private Button bt_record;
    private Button bt_record_pause;
    private Button bt_record_stop;
    private Button bt_play;
    private Button bt_reset;
    private AudioWaveView audioWave;
    private TextView playText;

    public String filePath;
    MP3Recorder mRecorder;
    AudioPlayer audioPlayer;
    boolean mIsRecord = false;
    boolean mIsPlay = false;
    int duration;
    int curPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        bt_record = findViewById(R.id.bt_record);
        bt_record_pause = findViewById(R.id.bt_record_pause);
        bt_record_stop = findViewById(R.id.bt_record_stop);
        bt_play = findViewById(R.id.bt_play);
        bt_reset = findViewById(R.id.bt_reset);
        audioWave = (AudioWaveView) findViewById(R.id.audioWave);
        playText = (TextView)findViewById(R.id.playText);


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

        //播放
        bt_play.setOnClickListener(v->{
            resolvePlayRecord();
        });

        //重置
        bt_reset.setOnClickListener(v->{
            resolveResetPlay();
        });

        resolveNormalUI();

        audioPlayer = new AudioPlayer(this, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case AudioPlayer.HANDLER_CUR_TIME://更新的时间
                        curPosition = (int) msg.obj;
                        playText.setText(toTime(curPosition) + " / " + toTime(duration));
                        break;
                    case AudioPlayer.HANDLER_COMPLETE://播放结束
                        playText.setText(" ");
                        mIsPlay = false;
                        break;
                    case AudioPlayer.HANDLER_PREPARED://播放开始
                        duration = (int) msg.obj;
                        playText.setText(toTime(curPosition) + " / " + toTime(duration));
                        break;
                    case AudioPlayer.HANDLER_ERROR://播放错误
                        resolveResetPlay();
                        break;
                }

            }
        });

    }

    /**
     * 开始录音
     */
    private void resolveRecord() {
        filePath = FileUtils.getAppPath();
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Toast.makeText(this, "创建文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int offset = MyUtils.dip2px(this, 7);
        filePath = FileUtils.getAppPath() + UUID.randomUUID().toString() + ".mp3";
        mRecorder = new MP3Recorder(new File(filePath));
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

        mRecorder.setErrorHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MP3Recorder.ERROR_TYPE) {
                    Toast.makeText(RecordSampleActivity.this, "没有麦克风权限", Toast.LENGTH_SHORT).show();
                    resolveError();
                }
            }
        });

        //audioWave.setBaseRecorder(mRecorder);

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
     * 播放
     */
    private void resolvePlayRecord() {
        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        playText.setText(" ");
        mIsPlay = true;
        audioPlayer.playUrl(filePath);
        resolvePlayUI();
    }

    /**
     * 录音异常
     */
    private void resolveError() {
        resolveNormalUI();
        FileUtils.deleteFile(filePath);
        filePath = "";
        if (mRecorder != null && mRecorder.isRecording()) {
            mRecorder.stop();
            audioWave.stopView();
        }
    }

    /**
     * 重置
     */
    private void resolveResetPlay() {
        filePath = "";
        playText.setText("");
        if (mIsPlay) {
            mIsPlay = false;
            audioPlayer.pause();
        }
        resolveNormalUI();
    }

    private void resolveRecordUI() {
        bt_record.setEnabled(false);
        bt_record_pause.setEnabled(true);
        bt_record_stop.setEnabled(true);
        bt_play.setEnabled(false);
        bt_reset.setEnabled(false);
    }

    private void resolveNormalUI() {
        bt_record.setEnabled(true);
        bt_record_pause.setEnabled(false);
        bt_record_stop.setEnabled(false);
        bt_play.setEnabled(false);
        bt_reset.setEnabled(false);
    }

    private void resolveStopUI() {
        bt_record.setEnabled(true);
        bt_record_stop.setEnabled(false);
        bt_record_pause.setEnabled(false);
        bt_play.setEnabled(true);
        bt_reset.setEnabled(true);
    }

    private void resolvePlayUI() {
        bt_record.setEnabled(false);
        bt_record_stop.setEnabled(false);
        bt_record_pause.setEnabled(false);
        bt_play.setEnabled(true);
        bt_reset.setEnabled(true);
    }

    private void resolvePauseUI() {
        bt_record.setEnabled(false);
        bt_record_pause.setEnabled(true);
        bt_record_stop.setEnabled(false);
        bt_play.setEnabled(false);
        bt_reset.setEnabled(false);
    }

    private String toTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        String dateString = formatter.format(time);
        return dateString;
    }


}