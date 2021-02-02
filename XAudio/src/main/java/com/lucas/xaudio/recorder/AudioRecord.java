package com.lucas.xaudio.recorder;

import android.media.AudioFormat;
import android.util.Log;
import com.lucas.xaudio.recorder.aac.AACEncode;
import com.lucas.xaudio.recorder.wav.WAVEncode;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AudioRecord {

    private String TAG = AudioRecord.class.getSimpleName();

    public enum AudioRecordState {
        PREPARE,    // 准备状态
        RECORDING,  // 录音中
        PAUSE,      // 暂停中
        STOP,       // 停止
        RELEASE     // 录音结束,释放资源
    }

    private volatile static AudioRecordState mState = AudioRecordState.RELEASE; // 录音状态
    private ExecutorService mExecutor;
    private FileOutputStream mFileOutputStream;
    private RandomAccessFile mRandomAccessFile;
    private AACEncode mAacEncode;
    private WAVEncode mWavEncode;
    private boolean isPlaying = false;  // 边录边播 开关
    private AudioRecordConfig mRecordConfig;
    private String strFilePath;
    private String strFileName;
    private int bufferSizeInBytes;  // 录音缓存区大小
    private android.media.AudioRecord mAudioRecord;

    /**
     * @param recordConfig 录音参数 See {@link AudioRecordConfig}
     * @param filePath     录音的文件地址
     * @param fileName     录音的文件名
     */
    public AudioRecord(AudioRecordConfig recordConfig, String filePath, String fileName) {
        mExecutor = Executors.newCachedThreadPool();
        this.mRecordConfig = recordConfig;
        this.strFilePath = filePath;
        this.strFileName = fileName;
    }

    public void prepare() {
        if (mState != AudioRecordState.RELEASE) {
            throw new IllegalStateException("AudioRecord is not yet initialized.");
        } else {
            Log.d(TAG, "prepare");
            // 录音最小缓存大小
            bufferSizeInBytes = android.media.AudioRecord.getMinBufferSize(mRecordConfig.sampleRate, mRecordConfig.channelConfig, mRecordConfig.audioFormat);
            mAudioRecord = new android.media.AudioRecord(mRecordConfig.audioSource, mRecordConfig.sampleRate, AudioFormat.CHANNEL_IN_STEREO, mRecordConfig.audioFormat, bufferSizeInBytes);

            mState = AudioRecordState.PREPARE;
        }
    }

    // 开始录音
    public void start() {
        if (mState == AudioRecordState.RECORDING) {
            throw new IllegalStateException("AudioRecord is Recording.");
        }
        if (mState == AudioRecordState.RELEASE) {
            throw new IllegalStateException("AudioRecord is not yet initialized.");
        }
        Log.d(TAG, "AudioRecord begin start: ");
        mState = AudioRecordState.RECORDING;
        mAudioRecord.startRecording();

        mExecutor.execute(()->{
            try {
                initEncode();
                FSDataOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // 暂停
    public void pause() {
        if (mState == AudioRecordState.RECORDING) {
            Log.d(TAG, "pause: ");
            mAudioRecord.stop();
            mState = AudioRecordState.PAUSE;
        }
    }

    // 从暂停处恢复
    public void resume() {
        Log.d(TAG, "resume: ");
        if (mState != AudioRecordState.PAUSE) {
            throw new IllegalStateException("AudioRecord not in Status:pause. Cannot resume");
        } else {
            mAudioRecord.startRecording();
            mState = AudioRecordState.RECORDING;
            mExecutor.execute(()->{
                try {
                    FSDataOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // 停止录音
    public void stop() {
        Log.d(TAG, "stop: ");
        if (mState == AudioRecordState.RECORDING || mState == AudioRecordState.PAUSE) {
            mState = AudioRecordState.STOP;
            mAudioRecord.stop();
            release();
        }
    }

    // 释放资源
    public void release() {
        Log.d(TAG, "release: ");
        try {
            mAudioRecord.release();
            mState = AudioRecordState.RELEASE;
            FSDataOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void initEncode() throws IOException {
        switch (mRecordConfig.outputFormat) {
            case AAC:
                mFileOutputStream = new FileOutputStream(strFilePath + strFileName + mRecordConfig.outputFormat.getName());
                mAacEncode = new AACEncode();
                mAacEncode.prepare();
                break;
            case WAV:
                mWavEncode = new WAVEncode();
                mRandomAccessFile = new RandomAccessFile(strFilePath + strFileName + mRecordConfig.outputFormat.getName(), "rw");
                // 留出文件头的位置
                mRandomAccessFile.seek(44);
                break;
            case PCM:
                mFileOutputStream = new FileOutputStream(strFilePath + strFileName + mRecordConfig.outputFormat.getName());
                break;
        }
    }

    void FSDataOutputStream() throws IOException {
        // 文件输出流
        byte[] readBuffer = new byte[bufferSizeInBytes];
        short[] readMP3Buffer = new short[bufferSizeInBytes];
        int readSize = 0;
        while (mState == AudioRecordState.RECORDING && mAudioRecord.getRecordingState() == android.media.AudioRecord.RECORDSTATE_RECORDING) {
            if (mRecordConfig.outputFormat == AudioRecordConfig.OutputFormat.MP3) {
                //TODO 如果是mp3

            } else {
                readSize = mAudioRecord.read(readBuffer, 0, bufferSizeInBytes);
                Encode(readSize, readBuffer);

            }
        }

        if (mState == AudioRecordState.RELEASE) {
            switch (mRecordConfig.outputFormat) {
                case MP3:

                    break;
                case WAV:
                    int sampleRate = mRecordConfig.sampleRate;
                    int channel = mRecordConfig.channelConfig;
                    if (mRecordConfig.channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
                        channel = 2;
                    } else if (mRecordConfig.channelConfig == AudioFormat.CHANNEL_IN_MONO) {
                        channel = 1;
                    }
                    int bitRate = mRecordConfig.audioFormat;
                    if (mRecordConfig.audioFormat == AudioFormat.ENCODING_PCM_8BIT) {
                        bitRate = 8;
                    } else if (mRecordConfig.audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                        bitRate = 16;
                    }

                    long byteRate = sampleRate * bitRate * channel / 8;
                    mWavEncode.WriteWaveFileHeader(mRandomAccessFile,
                            mRandomAccessFile.length(),
                            mRecordConfig.sampleRate,
                            channel,
                            byteRate);
                    break;
            }

            if (mFileOutputStream != null) {
                mFileOutputStream.close();
            }
            if (mRandomAccessFile != null) {
                mRandomAccessFile.close();
            }
        }
    }

    void Encode(int readSize, byte[] readBuffer) throws IOException {
        if (readSize != android.media.AudioRecord.ERROR_INVALID_OPERATION) {
            switch (mRecordConfig.outputFormat) {
                case PCM:
                    if (mFileOutputStream != null) {
                        mFileOutputStream.write(readBuffer, 0, readBuffer.length);
                    }
                    break;
                case AAC:
                    if (mAacEncode != null && mFileOutputStream != null) {
                        mAacEncode.encode(readSize, readBuffer, mFileOutputStream);
                    }
                    break;
                case WAV:
                    if (mRandomAccessFile != null) {
                        mRandomAccessFile.write(readBuffer, 0, bufferSizeInBytes);
                    }
                    break;
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

}
