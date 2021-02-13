#include <jni.h>
#include <string>
#include "lame/lame.h"

static lame_global_flags *glf = NULL;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_lucas_audioSample_view_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    // TODO: implement stringFromJNI()
    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
    return env->NewStringUTF(get_lame_version());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lucas_xaudio_recorder_XLame_init(JNIEnv *env, jclass clazz, jint in_samplerate,
                                          jint in_channel, jint out_samplerate, jint out_bitrate,
                                          jint quality) {
    // TODO: implement init()
    if (glf != NULL) {
        lame_close(glf);
        glf = NULL;
    }
    glf = lame_init();
    lame_set_in_samplerate(glf, in_samplerate);
    lame_set_num_channels(glf, in_channel);
    lame_set_out_samplerate(glf, out_samplerate);
    lame_set_brate(glf, out_bitrate);
    lame_set_quality(glf, quality);
    lame_init_params(glf);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lucas_xaudio_recorder_XLame_encode(JNIEnv *env, jclass clazz, jshortArray buffer_left,
                                            jshortArray buffer_right, jint samples,
                                            jbyteArray mp3buf_) {
    // TODO: implement encode()
    jshort *buffer_l = env->GetShortArrayElements(buffer_left, NULL);
    jshort *buffer_r = env->GetShortArrayElements(buffer_right, NULL);
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);
    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);
    int result = lame_encode_buffer(glf, buffer_l, buffer_r, samples, (u_char *) mp3buf,
                                    mp3buf_size);
    env->ReleaseShortArrayElements(buffer_left, buffer_l, 0);
    env->ReleaseShortArrayElements(buffer_right, buffer_r, 0);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
    return result;
}




extern "C"
JNIEXPORT jint JNICALL
Java_com_lucas_xaudio_recorder_XLame_flush(JNIEnv *env, jclass clazz, jbyteArray mp3buf_) {
    // TODO: implement flush()
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);
    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);
    int result = lame_encode_flush(glf, (u_char *) mp3buf, mp3buf_size);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lucas_xaudio_recorder_XLame_close(JNIEnv *env, jclass clazz) {
    // TODO: implement close()
    lame_close(glf);
    glf = NULL;
}