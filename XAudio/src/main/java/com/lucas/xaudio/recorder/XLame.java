package com.lucas.xaudio.recorder;

/**
 * @Description: Lame的jni映射库
 * @Author: Kosmos
 * @Date: 2019.05.24 23:36
 * @Email: KosmoSakura@gmail.com
 */
public class XLame {
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * @param inSamplerate  采样率(Hz)
     * @param inChannel     流中的通道数
     * @param outSamplerate 输出采样率(Hz)
     * @param outBitrate    压缩比(KHz)
     * @param quality       mp3质量
     * @apiNote 初始化
     * 关于质量∈[0,9]
     * 0->最高质量，最慢
     * 9->最低质量，最快
     * 通常：
     * 2->接近最好的质量，不太慢
     * 5->质量好，速度快
     * 7->音质还凑活, 非常快
     */
    public native static void init(int inSamplerate, int inChannel, int outSamplerate,
                                   int outBitrate, int quality);

    /**
     * @param bufferLeft  左声道的PCM数据
     * @param bufferRight 右声道的PCM数据.
     * @param samples     每个采样通道的样本数
     * @param mp3buf      指定最终编码的MP3流=>数组长度=7200+(1.25 * bufferLeft.length)
     *                    "7200 + (1.25 * buffer_l.length)" length array.
     * @return mp3buf中输出的字节数。可以为0。
     * -1: mp3buf太小
     * -2: 内存分配异常
     * -3: lame初始化失败
     * -4: 音质解析异常
     * @apiNote 缓冲区编码为mp3
     */
    public native static int encode(short[] bufferLeft, short[] bufferRight, int samples, byte[] mp3buf);


    /**
     * @param mp3buf 结果编码的MP3流。您必须指定至少7200字节。
     * @return 输出到encode中mp3buf的字节数，可能为0
     * @apiNote flush掉lame的缓冲区
     * 关于刷流：
     * 0.可能会返回最后的几个mp3帧列数组
     * 1.将刷新lame的内部PCM编码缓冲区，不足数列用0补满最终帧
     * 2.encode中的mp3buf至少>7200字节（否则可能一列都不够用）
     * 3.(如果有)将id3v1标签写入比特流
     */
    public native static int flush(byte[] mp3buf);

    /**
     * 关闭Lame.
     */
    public native static void close();
}
