package com.lucas.xaudio.recorder.waveview;

import android.content.Context;

import com.danikula.videocache.HttpProxyCacheServer;

/**
 * 保留所有版权，禁止分享
 * author : 许进进
 * time   : 2021/1/26 5:45 PM
 * des    : 缓存
 */

public class Manager {

    private static Manager mInstance;

    private HttpProxyCacheServer mProxy; //视频代理


    public static synchronized Manager newInstance() {
        if (mInstance == null) {
            mInstance = new Manager();
        }
        return mInstance;
    }

    public HttpProxyCacheServer getProxy(Context context) {
        if (mProxy == null) {
            mProxy = newProxy(context);
        }
        return mProxy;
    }

    /**
     * 创建缓存代理服务
     */
    private HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer(context.getApplicationContext());
    }

}
