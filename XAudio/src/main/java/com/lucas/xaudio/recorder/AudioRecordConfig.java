package com.lucas.xaudio.recorder;

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * # 录音配置
 * The AudioRecordConfig class collects the information describing an audio recording
 */
public class AudioRecordConfig {

    // 音源
    public int audioSource = MediaRecorder.AudioSource.MIC;
    // 采样率
    public int sampleRate = SampleRate.SAMPPLERATE_44100;
    // 声道
    public int channelConfig = AudioFormat.CHANNEL_IN_MONO;//单声道
    // 采样位数
    public int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 输出格式
    public OutputFormat outputFormat = OutputFormat.MP3;

    /**
     * @param audioSource   音源 {@link MediaRecorder.AudioSource}
     *                      MIC : See {@link MediaRecorder.AudioSource#MIC}
     * @param sampleRate    采样率 {@link SampleRate}
     * @param channelConfig 声道  {@linkplain AudioFormat}
     *                      单声道：See {@link AudioFormat#CHANNEL_IN_MONO}
     *                      双声道：See {@link AudioFormat#CHANNEL_IN_STEREO}
     * @param audioFormat   采样位数 {@linkplain AudioFormat}
     *                      8Bit： See {@link AudioFormat#ENCODING_PCM_8BIT}
     *                      16Bit: See {@link AudioFormat#ENCODING_PCM_16BIT}
     * @param outputFormat  输出格式 {@link OutputFormat}
     * @see <a href="http://en.wikipedia.org/wiki/Sampling_rate">Sampling_rate</a>
     */
    public AudioRecordConfig(int audioSource, int sampleRate, int channelConfig, int audioFormat, OutputFormat outputFormat) {
        this.audioSource = audioSource;
        this.sampleRate = sampleRate;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        this.outputFormat = outputFormat;
    }


    public enum OutputFormat {
        AAC(0, ".aac"),
        MP3(1, ".mp3"),
        WAV(2, ".wav"),
        PCM(3, ".pcm");

        public static String getName(int index) {
            for (OutputFormat format : OutputFormat.values()) {
                if (format.getIndex() == index) {
                    return format.name;
                }
            }
            return null;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        private int index;
        private String name;

        OutputFormat(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    public final class SampleRate {
        public static final int SAMPPLERATE_48000 = 48000;
        public static final int SAMPPLERATE_44100 = 44100;
        public static final int SAMPPLERATE_1600 = 1600;
        public static final int SAMPPLERATE_800 = 800;

        SampleRate() {
            throw new RuntimeException("Stub!");
        }
    }


}
