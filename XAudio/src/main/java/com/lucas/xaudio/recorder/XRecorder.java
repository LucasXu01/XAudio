package com.lucas.xaudio.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import com.lucas.xaudio.recorder.aac.AACEncode;
import com.lucas.xaudio.recorder.mp3.MP3DataEncodeThread;
import com.lucas.xaudio.recorder.mp3.PCMFormat;
import com.lucas.xaudio.recorder.wav.WAVEncode;
import com.lucas.xaudio.utils.XAudioUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XRecorder extends BaseRecorder {
    private String TAG = XRecorder.class.getSimpleName();
    //=======================AudioRecord Default Settings=======================
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;   //录音来源为主麦克风
    /**
     * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
     */
    private static final int DEFAULT_SAMPLING_RATE = 44100;//模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 下面是对此的封装
     * private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
     */
    private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;

    //======================Lame Default Settings=====================
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;
    /**
     * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
     */
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;
    /**
     * Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
     */
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;

    //==================================================================

    /**
     * 自定义 每160帧作为一个周期，通知一下需要进行编码
     */
    private static final int FRAME_COUNT = 160;

    private AudioRecord mAudioRecord = null;
    private MP3DataEncodeThread mEncodeThread;
    private ArrayList<Short> dataList;

    private short[] mPCMBuffer;
    private byte[] mPCMBufferByte;
    private boolean mIsRecording = false;
    private boolean mSendError;
    private boolean mPause;
    //缓冲数量
    private int mBufferSize;
    //最大数量
    private int mMaxSize;
    //波形速度
    private int mWaveSpeed = 300;
    private AudioRecordConfig mRecordConfig;
    private String fileStr;
    private File mRecordFile;
    private FileOutputStream mFileOutputStream;
    private AACEncode mAacEncode;
    private ExecutorService mExecutor;
    private WAVEncode mWavEncode;
    private RandomAccessFile mRandomAccessFile;

    /**
     * Default constructor. Setup recorder with default sampling rate 1 channel,
     * 16 bits pcm
     *
     * @param recordConfig 录音参数 See {@link AudioRecordConfig}
     * @param filePath     录音的文件路径
     * @param fileName     录音的文件名称
     */
    public XRecorder(AudioRecordConfig recordConfig, String filePath, String fileName) {
        if (recordConfig == null) {
            //默认录音是mp3格式
            this.mRecordConfig = new AudioRecordConfig(
                    MediaRecorder.AudioSource.MIC,
                    AudioRecordConfig.SampleRate.SAMPPLERATE_44100,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioRecordConfig.OutputFormat.MP3);
        } else {
            this.mRecordConfig = recordConfig;
        }
        mExecutor = Executors.newCachedThreadPool();
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "AudioRecord: 创建录音文件失败");
                return;
            }
        }
        this.fileStr = filePath + fileName + mRecordConfig.outputFormat.getName();
    }

    public XRecorder(String filePath, String fileName) {
        mExecutor = Executors.newCachedThreadPool();
        this.mRecordConfig = new AudioRecordConfig(
                MediaRecorder.AudioSource.MIC,
                AudioRecordConfig.SampleRate.SAMPPLERATE_44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecordConfig.OutputFormat.MP3);
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "AudioRecord: 创建录音文件失败");
                return;
            }
        }
        this.fileStr = filePath + fileName + mRecordConfig.outputFormat.getName();
    }

    /**
     * Initialize audio recorder
     */
    private void initAudioRecorder() throws IOException {
        mRecordFile = new File(fileStr);
        if (mRecordConfig == null) {
            Log.e(TAG, "initAudioRecorder: mRecordConfig为空！");
            return;
        }
        mBufferSize = AudioRecord.getMinBufferSize(mRecordConfig.sampleRate, mRecordConfig.channelConfig, mRecordConfig.audioFormat);
        switch (mRecordConfig.outputFormat) {
            case MP3:
                int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();
                /* Get number of samples. Calculate the buffer size
                 * (round up to the factor of given frame size)
                 * 使能被整除，方便下面的周期性通知
                 * */
                int frameSize = mBufferSize / bytesPerFrame;
                if (frameSize % FRAME_COUNT != 0) {
                    frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
                    mBufferSize = frameSize * bytesPerFrame;
                }
                /* Setup audio recorder */
                mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat(), mBufferSize);
                mPCMBuffer = new short[mBufferSize];
                /*
                 * Initialize lame buffer
                 * mp3 sampling rate is the same as the recorded pcm sampling rate
                 * The bit rate is 32kbps
                 *
                 */
                XLame.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
                // Create and run thread used to encode data
                // The thread will
                mEncodeThread = new MP3DataEncodeThread(mRecordFile, mBufferSize);
                mEncodeThread.start();
                mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
                mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
                break;
            case AAC:
                // 录音最小缓存大小
                mAudioRecord = new AudioRecord(mRecordConfig.audioSource, mRecordConfig.sampleRate, mRecordConfig.channelConfig, mRecordConfig.audioFormat, mBufferSize);
                mPCMBuffer = new short[mBufferSize];
                mFileOutputStream = new FileOutputStream(new File(fileStr));
                mAacEncode = new AACEncode();
                mAacEncode.prepare();
                break;
            case WAV:
                mAudioRecord = new AudioRecord(mRecordConfig.audioSource, mRecordConfig.sampleRate, mRecordConfig.channelConfig, mRecordConfig.audioFormat, mBufferSize);
                mPCMBuffer = new short[mBufferSize];
                mWavEncode = new WAVEncode();
                mRandomAccessFile = new RandomAccessFile(new File(fileStr), "rw");
                // 留出文件头的位置
                mRandomAccessFile.seek(44);
                break;
            case PCM:
                // 录音最小缓存大小
                mAudioRecord = new AudioRecord(mRecordConfig.audioSource, mRecordConfig.sampleRate, mRecordConfig.channelConfig, mRecordConfig.audioFormat, mBufferSize);
                mPCMBuffer = new short[mBufferSize];
                mFileOutputStream = new FileOutputStream(new File(fileStr));
                break;
        }


    }

    /**
     * Start recording. Create an encoding thread. Start record from this thread.
     *
     * @throws IOException initAudioRecorder throws
     */
    public void start() throws IOException {
        if (mIsRecording) {
            return;
        }
        mIsRecording = true; // 提早，防止init或startRecording被多次调用
        initAudioRecorder();
        try {
            mAudioRecord.startRecording();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        mExecutor.execute(() -> {
            boolean isError = false;
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {
                while (mIsRecording) {
                    byte[] readBufferByte = new byte[mBufferSize];
                    short[] readBufferShort = new short[mBufferSize];

//                    int readSize = mAudioRecord.read(readBufferByte, 0, mBufferSize);
//                    int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);

                    switch (mRecordConfig.outputFormat) {
                        case MP3:
                            int readSize = mAudioRecord.read(readBufferShort, 0, mBufferSize);
                            if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {
                                if (!mSendError) {
                                    mSendError = true;
                                    Log.e(TAG, "run: 请检查是否有麦克风权限");
                                    mIsRecording = false;
                                    isError = true;
                                }
                            } else {
                                if (readSize > 0) {
                                    if (mPause) {
                                        continue;
                                    }
                                    mEncodeThread.addTask(readBufferShort, readSize);
                                    calculateRealVolume(readBufferShort, readSize);
                                    sendData(readBufferShort, readSize);
                                } else {
                                    if (!mSendError) {
                                        mSendError = true;
                                        mIsRecording = false;
                                        isError = true;
                                    }
                                }
                            }
                            break;
                        case AAC:
                            int readSize2 = mAudioRecord.read(readBufferByte, 0, mBufferSize);
                            if (readSize2 == AudioRecord.ERROR_INVALID_OPERATION || readSize2 == AudioRecord.ERROR_BAD_VALUE) {
                                if (!mSendError) {
                                    mSendError = true;
                                    Log.e(TAG, "run: 请检查是否有麦克风权限");
                                    mIsRecording = false;
                                    isError = true;
                                }
                            } else {
                                if (readSize2 > 0) {
                                    if (mPause) {
                                        continue;
                                    }

                                    if (mAacEncode != null && mFileOutputStream != null) {
                                        mAacEncode.encode(readSize2, readBufferByte, mFileOutputStream);
                                    }
                                    calculateRealVolume(XAudioUtils.bytesToShort(readBufferByte), XAudioUtils.bytesToShort(readBufferByte).length);
                                    sendData(XAudioUtils.bytesToShort(readBufferByte), XAudioUtils.bytesToShort(readBufferByte).length);
                                } else {
                                    if (!mSendError) {
                                        mSendError = true;
                                        mIsRecording = false;
                                        isError = true;
                                    }
                                }
                            }
                            break;
                        case WAV:
                            int readSize3 = mAudioRecord.read(readBufferByte, 0, mBufferSize);
                            if (readSize3 == AudioRecord.ERROR_INVALID_OPERATION || readSize3 == AudioRecord.ERROR_BAD_VALUE) {
                                if (!mSendError) {
                                    mSendError = true;
                                    Log.e(TAG, "run: 请检查是否有麦克风权限");
                                    mIsRecording = false;
                                    isError = true;
                                }
                            } else {
                                if (readSize3 > 0) {
                                    if (mPause) {
                                        continue;
                                    }
                                    if (mRandomAccessFile != null) {
                                        mRandomAccessFile.write(readBufferByte, 0, mBufferSize);
                                    }
                                    calculateRealVolume(XAudioUtils.bytesToShort(readBufferByte), XAudioUtils.bytesToShort(readBufferByte).length);
                                    sendData(XAudioUtils.bytesToShort(readBufferByte), XAudioUtils.bytesToShort(readBufferByte).length);

                                } else {
                                    if (!mSendError) {
                                        mSendError = true;
                                        mIsRecording = false;
                                        isError = true;
                                    }
                                }
                            }
                            break;
                        case PCM:
                            int readSize4 = mAudioRecord.read(readBufferByte, 0, mBufferSize);
                            if (readSize4 == AudioRecord.ERROR_INVALID_OPERATION || readSize4 == AudioRecord.ERROR_BAD_VALUE) {
                                if (!mSendError) {
                                    mSendError = true;
                                    Log.e(TAG, "run: 请检查是否有麦克风权限");
                                    mIsRecording = false;
                                    isError = true;
                                }
                            } else {
                                if (readSize4 > 0) {
                                    if (mPause) {
                                        continue;
                                    }

                                    if (mFileOutputStream != null) {
                                        mFileOutputStream.write(readBufferByte, 0, mBufferSize);
                                    }
                                    calculateRealVolume(XAudioUtils.bytesToShort(readBufferByte), XAudioUtils.bytesToShort(readBufferByte).length);
                                    sendData(XAudioUtils.bytesToShort(readBufferByte), XAudioUtils.bytesToShort(readBufferByte).length);
                                } else {
                                    if (!mSendError) {
                                        mSendError = true;
                                        mIsRecording = false;
                                        isError = true;
                                    }
                                }
                            }
                            break;


                    }
                }
                // release and finalize audioRecord
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;

                // 后续的一些结尾处理
                switch (mRecordConfig.outputFormat) {
                    case MP3:
                        // 根据readSize的错误码或者size来确定mp3录音是否结束
                        if (isError) {
                            mEncodeThread.sendErrorMessage();
                        } else {
                            mEncodeThread.sendStopMessage();
                        }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

    /**
     * 获取真实的音量。 [算法来自三星]
     *
     * @return 真实音量
     */
    @Override
    public int getRealVolume() {
        return mVolume;
    }

    /**
     * 获取相对音量。 超过最大值时取最大值。
     *
     * @return 音量
     */
    public int getVolume() {
        if (mVolume >= MAX_VOLUME) {
            return MAX_VOLUME;
        }
        return mVolume;
    }

    private static final int MAX_VOLUME = 2000;

    /**
     * 根据资料假定的最大值。 实测时有时超过此值。
     *
     * @return 最大音量值。
     */
    public int getMaxVolume() {
        return MAX_VOLUME;
    }

    public void stop() {
        mPause = false;
        mIsRecording = false;
    }

    public boolean isRecording() {
        return mIsRecording;
    }


    private void sendData(short[] shorts, int readSize) {
        if (dataList != null) {
            int length = readSize / mWaveSpeed;
            short resultMax = 0, resultMin = 0;
            for (short i = 0, k = 0; i < length; i++, k += mWaveSpeed) {
                for (short j = k, max = 0, min = 1000; j < k + mWaveSpeed; j++) {
                    if (shorts[j] > max) {
                        max = shorts[j];
                        resultMax = max;
                    } else if (shorts[j] < min) {
                        min = shorts[j];
                        resultMin = min;
                    }
                }
                if (dataList.size() > mMaxSize) {
                    dataList.remove(0);
                }
                dataList.add(resultMax);
            }
        }
    }

    /**
     * 设置数据的获取显示，设置最大的获取数，一般都是控件大小/线的间隔offset
     *
     * @param dataList 数据
     * @param maxSize  最大个数
     */
    public void setDataList(ArrayList<Short> dataList, int maxSize) {
        this.dataList = dataList;
        this.mMaxSize = maxSize;
    }


    public boolean isPause() {
        return mPause;
    }

    /**
     * 是否暂停
     */
    public void setPause(boolean pause) {
        this.mPause = pause;
    }

    public int getWaveSpeed() {
        return mWaveSpeed;
    }

    /**
     * pcm数据的速度，默认300
     * 数据越大，速度越慢
     */
    public void setWaveSpeed(int waveSpeed) {
        if (mWaveSpeed <= 0) {
            return;
        }
        this.mWaveSpeed = waveSpeed;
    }


}