package com.lucas.xaudio.recorder.aac;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * android.media.AudioRecord对音频进行采集pcm编码的原始数据，
 * 通过MediaCodec编码成aac音频，此时编码出来是aac裸流无法播放，
 * 在每一帧音频前加adts头才可播放，adts头包含音频数据的采样率，
 * 声道，帧长度等信息。
 * See  {@link MediaCodec}
 */
public class AACEncode {

    String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;
    int CHANNEL_COUNT = 2;
    int SAMPLE_RATE = 44100;
    int BIT_RATE = 64000;
    int AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    MediaCodec mEncoder;
    ByteBuffer[] mInputBuffers;
    ByteBuffer[] mOutputBuffers;
    MediaCodec.BufferInfo mBufferInfo;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void prepare() throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, CHANNEL_COUNT);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, AAC_PROFILE);
        mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoder.start();

        mInputBuffers = mEncoder.getInputBuffers();
        mOutputBuffers = mEncoder.getOutputBuffers();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void encode(int readSize, byte[] readBuffer, FileOutputStream fileOutputStream) throws IOException {
        if (mInputBuffers != null) {
            int inputBufferIndex = mEncoder.dequeueInputBuffer(-1L);
            int overCount = 0;
            int lastSize = 0;
            byte[] lastReadBuffer = new byte[readBuffer.length];
            ByteBuffer inputBuffer;
            int remainSize;

            if (inputBufferIndex >= 0) {
                inputBuffer = mInputBuffers[inputBufferIndex];
                inputBuffer.clear();
                remainSize = inputBuffer.remaining();
                if (remainSize < readBuffer.length) {
                    inputBuffer.put(readBuffer, 0, remainSize);
                    inputBuffer.limit(remainSize);

                    mEncoder.queueInputBuffer(inputBufferIndex, 0, remainSize, System.nanoTime(), 0);
                    overCount = readBuffer.length / remainSize;
                    lastSize = readBuffer.length - remainSize;
                    System.arraycopy(readBuffer, remainSize, lastReadBuffer, 0, lastSize);
                } else {
                    inputBuffer.put(readBuffer);
                    inputBuffer.limit(readBuffer.length);
                    mEncoder.queueInputBuffer(inputBufferIndex, 0, readBuffer.length, System.nanoTime(), 0);
                }
            }

            getOutputBufferAndWrite(fileOutputStream);
            fileOutputStream.flush();

            while (overCount > 0 && lastSize > 0) {
                inputBufferIndex = this.mEncoder.dequeueInputBuffer(-1L);
                if (inputBufferIndex >= 0) {
                    inputBuffer = mInputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    remainSize = inputBuffer.remaining();
                    if (remainSize < lastSize) {
                        inputBuffer.put(lastReadBuffer, 0, remainSize);
                        inputBuffer.limit(remainSize);
                        mEncoder.queueInputBuffer(inputBufferIndex, 0, remainSize, System.nanoTime(), 0);
                        lastSize -= remainSize;
                        System.arraycopy(readBuffer, readBuffer.length - lastSize, lastReadBuffer, 0, lastSize);
                    } else {
                        inputBuffer.put(lastReadBuffer, 0, lastSize);
                        inputBuffer.limit(lastSize);
                        mEncoder.queueInputBuffer(inputBufferIndex, 0, lastSize, System.nanoTime(), 0);
                        lastSize = 0;
                    }
                }

                getOutputBufferAndWrite(fileOutputStream);
                fileOutputStream.flush();
                --overCount;
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void getOutputBufferAndWrite(FileOutputStream fileOutputStream) throws IOException {
        for (int outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 0L);
             outputBufferIndex >= 0;
             outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 0L)) {
            int outBitsSize = mBufferInfo.size;
            int outPacketSize = outBitsSize + 7;
            ByteBuffer outputBuffer = mOutputBuffers[outputBufferIndex];
            outputBuffer.position(mBufferInfo.offset);
            outputBuffer.limit(mBufferInfo.size + outBitsSize);
            byte[] outData = new byte[outPacketSize];
            addADTStoPacket(outData, outPacketSize);
            // 给adts头字段空出前7个字节
            outputBuffer.get(outData, 7, outBitsSize);
            outputBuffer.position(mBufferInfo.offset);
            fileOutputStream.write(outData);
            mEncoder.releaseOutputBuffer(outputBufferIndex, false);
        }
    }

    /**
     * 给编码出的aac裸流添加adts头字段
     *
     * @param packet    空出前7个字节
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //wav LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //双通道
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}

