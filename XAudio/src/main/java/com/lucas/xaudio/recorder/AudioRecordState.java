package com.lucas.xaudio.recorder;

public enum AudioRecordState {
    /**
     * 准备状态
     */
    PREPARE,
    /**
     * 录音中
     */
    RECORDING,
    /**
     * 暂停中
     */
    PAUSE,
    /**
     * 停止
     */
    STOP,
    /**
     * 录音结束,释放资源
     */
    RELEASE
}
