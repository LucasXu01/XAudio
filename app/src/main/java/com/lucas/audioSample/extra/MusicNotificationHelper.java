package com.lucas.audioSample.extra;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.lucas.audioSample.R;
import com.lucas.library.XImg;
import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.audioplayer.core.AudioController;
import com.lucas.xaudio.audioplayer.model.BaseAudioBean;
import com.lucas.xaudio.audioplayer.NotificationHelperListener;

import androidx.core.app.NotificationCompat;

/**
 * 音乐Notification帮助类
 */
public class MusicNotificationHelper {

  public static final String CHANNEL_ID = "channel_id_audio";
  public static final String CHANNEL_NAME = "channel_name_audio";
  public static final int NOTIFICATION_ID = 0x111;

  //最终的Notification显示类
  private Notification mNotification;
  private RemoteViews mRemoteViews; // 大布局
  private RemoteViews mSmallRemoteViews; //小布局
  private NotificationManager mNotificationManager;
  private NotificationHelperListener mListener;
  private String packageName;
  //当前要播的歌曲Bean
  private BaseAudioBean mAudioBean;

  public static MusicNotificationHelper getInstance() {
    return SingletonHolder.instance;
  }

  private static class SingletonHolder {
    private static MusicNotificationHelper instance = new MusicNotificationHelper();
  }

  public void init(NotificationHelperListener listener, Intent intent) {
    mNotificationManager = (NotificationManager) XAudio.getInstance().getContext()
        .getSystemService(Context.NOTIFICATION_SERVICE);
    packageName = XAudio.getInstance().getContext().getPackageName();
    mAudioBean = AudioController.getInstance().getNowPlaying();
    initNotification(intent);
    mListener = listener;
    if (mListener != null) mListener.onNotificationInit();
  }

  /*
   * 创建Notification,
   */
  private void initNotification(Intent intent) {
    if (mNotification == null) {
      //首先创建布局
      initRemoteViews();
      //再构建Notification
//      Intent intent = new Intent(AudioHelper.getInstance().getContext(), XMusicPlayerActivity.class);
      if(intent == null){
        intent = new Intent();
      }
      PendingIntent pendingIntent = PendingIntent.getActivity(XAudio.getInstance().getContext(), 0, intent,
          PendingIntent.FLAG_UPDATE_CURRENT);

      //适配安卓8.0的消息渠道
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel =
            new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(false);
        channel.enableVibration(false);
        mNotificationManager.createNotificationChannel(channel);
      }
      NotificationCompat.Builder builder =
          new NotificationCompat.Builder(XAudio.getInstance().getContext(), CHANNEL_ID).setContentIntent(
              pendingIntent)
              .setSmallIcon(R.mipmap.ic_launcher)
              .setCustomBigContentView(mRemoteViews) //大布局
              .setContent(mSmallRemoteViews); //正常布局，两个布局可以切换
      mNotification = builder.build();

      showLoadStatus(mAudioBean);
    }
  }

  /*
   * 创建Notification的布局,默认布局为Loading状态
   */
  private void initRemoteViews() {
    int layoutId = R.layout.notification_big_layout;
    mRemoteViews = new RemoteViews(packageName, layoutId);
    mRemoteViews.setTextViewText(R.id.title_view, mAudioBean.name);
    mRemoteViews.setTextViewText(R.id.tip_view, mAudioBean.album);
    // TODO: 2021/1/8 喜欢收藏的逻辑
//    if (null != GreenDaoHelper.selectFavourite(mAudioBean)) {
//      mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_loved);
//    } else {
//      mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
//    }

    int smalllayoutId = R.layout.notification_small_layout;
    mSmallRemoteViews = new RemoteViews(packageName, smalllayoutId);
    mSmallRemoteViews.setTextViewText(R.id.title_view, mAudioBean.name);
    mSmallRemoteViews.setTextViewText(R.id.tip_view, mAudioBean.album);

    //点击播放按钮广播
    Intent playIntent = new Intent(XAudio.ACTION_STATUS_BAR);
    playIntent.putExtra(XAudio.EXTRA, XAudio.EXTRA_PLAY);
    PendingIntent playPendingIntent =
            PendingIntent.getBroadcast(XAudio.getInstance().getContext(), 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mRemoteViews.setOnClickPendingIntent(R.id.play_view, playPendingIntent);
    mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
    mSmallRemoteViews.setOnClickPendingIntent(R.id.play_view, playPendingIntent);
    mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);

    //点击上一首按钮广播
    Intent previousIntent = new Intent(XAudio.ACTION_STATUS_BAR);
    previousIntent.putExtra(XAudio.EXTRA, XAudio.EXTRA_PRE);
    PendingIntent previousPendingIntent =
        PendingIntent.getBroadcast(XAudio.getInstance().getContext(), 2, previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    mRemoteViews.setOnClickPendingIntent(R.id.previous_view, previousPendingIntent);
    mRemoteViews.setImageViewResource(R.id.previous_view, R.mipmap.note_btn_pre_white);

    //点击下一首按钮广播
    Intent nextIntent = new Intent(XAudio.ACTION_STATUS_BAR);
    nextIntent.putExtra(XAudio.EXTRA, XAudio.EXTRA_PRE);
    PendingIntent nextPendingIntent =
        PendingIntent.getBroadcast(XAudio.getInstance().getContext(), 3, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    mRemoteViews.setOnClickPendingIntent(R.id.next_view, nextPendingIntent);
    mRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);
    mSmallRemoteViews.setOnClickPendingIntent(R.id.next_view, nextPendingIntent);
    mSmallRemoteViews.setImageViewResource(R.id.next_view, R.mipmap.note_btn_next_white);

    //点击收藏按钮广播
    Intent favouriteIntent = new Intent(XAudio.ACTION_STATUS_BAR);
    favouriteIntent.putExtra(XAudio.EXTRA, XAudio.EXTRA_FAV);
    PendingIntent favouritePendingIntent =
        PendingIntent.getBroadcast(XAudio.getInstance().getContext(), 4, favouriteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
    mRemoteViews.setOnClickPendingIntent(R.id.favourite_view, favouritePendingIntent);

    //点击关闭按钮
    Intent closeIntent = new Intent(XAudio.ACTION_STATUS_BAR);
    closeIntent.putExtra(XAudio.EXTRA, XAudio.EXTRA_CLOSE);
    PendingIntent closePendingIntent =
            PendingIntent.getBroadcast(XAudio.getInstance().getContext(), 5, closeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
    mRemoteViews.setOnClickPendingIntent(R.id.img_close, closePendingIntent);

  }

  public Notification getNotification() {
    return mNotification;
  }

  /**
   * 显示Notification的加载状态
   */
  public void showLoadStatus(BaseAudioBean bean) {
    //防止空指针crash
    mAudioBean = bean;
    if (mRemoteViews != null) {
      mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
      mRemoteViews.setTextViewText(R.id.title_view, mAudioBean.name);
      mRemoteViews.setTextViewText(R.id.tip_view, mAudioBean.album);
      XImg.getIns()
          .displayImageForNotification(XAudio.getInstance().getContext(), mRemoteViews, R.id.image_view,
              mNotification, NOTIFICATION_ID, mAudioBean.albumPic);
      //更新收藏view
      // TODO: 2021/1/8  更新收藏view
//      if (null != GreenDaoHelper.selectFavourite(mAudioBean)) {
//        mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_loved);
//      } else {
//        mRemoteViews.setImageViewResource(R.id.favourite_view, R.mipmap.note_btn_love_white);
//      }

      //小布局也要更新
      mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
      mSmallRemoteViews.setTextViewText(R.id.title_view, mAudioBean.name);
      mSmallRemoteViews.setTextViewText(R.id.tip_view, mAudioBean.album);
      XImg.getIns()
          .displayImageForNotification(XAudio.getInstance().getContext(), mSmallRemoteViews, R.id.image_view,
              mNotification, NOTIFICATION_ID, mAudioBean.albumPic);

      mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
  }

  public void showPlayStatus() {
    if (mRemoteViews != null) {
      mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
      mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_pause_white);
      mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
  }

  public void showPauseStatus() {
    if (mRemoteViews != null) {
      mRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
      mSmallRemoteViews.setImageViewResource(R.id.play_view, R.mipmap.note_btn_play_white);
      mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
  }

  public void changeFavouriteStatus(boolean isFavourite) {
    if (mRemoteViews != null) {
      mRemoteViews.setImageViewResource(R.id.favourite_view,
          isFavourite ? R.mipmap.note_btn_loved : R.mipmap.note_btn_love_white);
      mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }
  }
}
