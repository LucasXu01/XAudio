package com.lucas.xaudio.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.List;

public class XMusicUtils {

  /**
   * 毫秒转分秒
   */
  public static String formatTime(long time) {
    String min = (time / (1000 * 60)) + "";
    String second = (time % (1000 * 60) / 1000) + "";
    if (min.length() < 2) {
      min = 0 + min;
    }
    if (second.length() < 2) {
      second = 0 + second;
    }
    return min + ":" + second;
  }


  /**
   * 判断Service是否正在运行
   *
   * @param context     上下文
   * @param serviceName Service 类全名
   * @return true 表示正在运行，false 表示没有运行
   */
  public static boolean isServiceRunning(Context context, String serviceName) {
    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningServiceInfo> serviceInfoList = manager.getRunningServices(200);
    if (serviceInfoList.size() <= 0) {
      return false;
    }
    for (ActivityManager.RunningServiceInfo info : serviceInfoList) {
      if (info.service.getClassName().equals(serviceName)) {
        return true;
      }
    }
    return false;
  }

}


