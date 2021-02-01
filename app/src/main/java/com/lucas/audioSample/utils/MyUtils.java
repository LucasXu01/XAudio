package com.lucas.audioSample.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.lucas.xaudio.mediaplayer.model.AudioBean;

import java.util.ArrayList;

public class MyUtils {
    /**
     * 获取屏幕的宽度px
     *
     * @param context 上下文
     * @return 屏幕宽px
     */
    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(outMetrics);// 给白纸设置宽高
        return outMetrics.widthPixels;
    }

    /**
     * 获取屏幕的高度px
     *
     * @param context 上下文
     * @return 屏幕高px
     */
    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();// 创建了一张白纸
        windowManager.getDefaultDisplay().getMetrics(outMetrics);// 给白纸设置宽高
        return outMetrics.heightPixels;
    }

    /**
     * dip转为PX
     */
    public static int dip2px(Context context, float dipValue) {
        float fontScale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * fontScale + 0.5f);
    }

    /**
     * 获取一些音频数据
     */
    public static ArrayList<AudioBean> getMockData() {
        ArrayList<AudioBean> audioBeanList = new ArrayList<>();

        AudioBean audioBean1 = new AudioBean("100001",
                "http://qiniu.yanfriends.com/obj_w5zDlMODwrDDiGjCn8Ky_3087361631_ea94_69e0_6a9e_c9e5b3d7a25d66f5b6b28ec92cdec944.mp3", "焰火青年",
                "华北浪革",
                "废柴",
                "2019年盛夏，华北平原的小镇青年开始大声唱出自己的歌词，说说自己的心里话，第一张EP《废柴》。",
                "http://qiniu.yanfriends.com/5d77a37b54143_426809737_1568121723.jpg",
                "4:09"
        );

        AudioBean audioBean2 = new AudioBean("100002",
                "http://qiniu.yanfriends.com/%E9%9D%92%E8%8A%B1%E7%93%B7.mp3", "青花瓷",
                "周杰伦",
                "我很忙",
                "再忙 … 也要陪你听一首歌！",
                "http://qiniu.yanfriends.com/T002R300x300M000002eFUFm2XYZ7z_1.jpg",
                "4:00"
        );

        AudioBean audioBean3 = new AudioBean("100003",
                "http://sr-sycdn.kuwo.cn/resource/n2/33/25/2629654819.mp3", "小情歌",
                "五月天",
                "小幸运",
                "电影《不能说的秘密》主题曲,尤其以最美的不是下雨天,是与你一起躲过雨的屋檐最为经典",
                "http://qiniu.yanfriends.com/timg.jpeg",
                "4:26"
        );

        audioBeanList.add(audioBean1);
        audioBeanList.add(audioBean2);
        audioBeanList.add(audioBean3);
        
        return audioBeanList;

    }

}
