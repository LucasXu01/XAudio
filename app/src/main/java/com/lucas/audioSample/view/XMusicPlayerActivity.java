package com.lucas.audioSample.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lucas.audioSample.R;
import com.lucas.audioSample.custom.CustomMusicService;
import com.lucas.audioSample.utils.MyUtils;
import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.extra.AudioServiceListener;
import com.lucas.xaudio.mediaplayer.core.AudioController;
import com.lucas.xaudio.mediaplayer.core.CustomMediaPlayer;
import com.lucas.xaudio.mediaplayer.events.AudioFavouriteEvent;
import com.lucas.xaudio.mediaplayer.events.AudioLoadEvent;
import com.lucas.xaudio.mediaplayer.events.AudioPlayModeEvent;
import com.lucas.xaudio.mediaplayer.events.AudioProgressEvent;
import com.lucas.xaudio.mediaplayer.events.AudioReleaseEvent;
import com.lucas.xaudio.mediaplayer.image_loader.ImageLoaderManager;
import com.lucas.xaudio.mediaplayer.model.AudioBean;
import com.lucas.xaudio.mediaplayer.view.MusicListDialog;
import com.lucas.xaudio.utils.XAudioUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

/**
 * 播放音乐Activity
 */
public class XMusicPlayerActivity extends AppCompatActivity {

    private RelativeLayout mBgView;
    private TextView mInfoView;
    private TextView mAuthorView;
    private ImageView mFavouriteView;
    private SeekBar mProgressView;
    private TextView mStartTimeView;
    private TextView mTotalTimeView;
    private ImageView mPlayModeView;
    private ImageView mPlayView;
    private ImageView mNextView;
    private ImageView mPreViousView;
    private Animator animator;
    private boolean isChangeSeek = false; //判定是否在滑动seek
    private AudioBean mAudioBean; //当前正在播放歌曲

    public static void start(Activity context) {

        //初始化音乐数据
        XAudio.getInstance()
                .setNotificationIntent(new Intent(context, XMusicPlayerActivity.class)) //可选
                .setAutoService()  //可选：不调用setAutoService不会有服务，不带参为自带的服务，或者根据需要填写自己的Service参
                .setAutoService(new CustomMusicService())
                .setAudioQueen(MyUtils.getMockData());

        Intent intent = new Intent(context, XMusicPlayerActivity.class);
        ActivityCompat.startActivity(context, intent, ActivityOptionsCompat.makeSceneTransitionAnimation(context).toBundle());

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //添加入场动画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(
                    TransitionInflater.from(this).inflateTransition(R.transition.transition_bottom2top));
        }

        //隐藏actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //融合状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_xmusic_player_layout);



        initData();
        initView();

        XAudio.getInstance().addAudioServiceListener(new AudioServiceListener() {
            @Override
            public void onAudioPause() {
                //更新activity为暂停状态
                showPauseView();
            }

            @Override
            public void onAudioStart() {
                //更新activity为播放状态
                showPlayView();
            }

            @Override
            public void onAudioProgress(AudioProgressEvent event) {
                int totalTime = event.maxLength;
                int currentTime = event.progress;
                //更新时间
                mStartTimeView.setText(XAudioUtils.formatTime(currentTime));
                mTotalTimeView.setText(XAudioUtils.formatTime(totalTime));
                if (!isChangeSeek) {
                    mProgressView.setProgress(currentTime);
                    mProgressView.setMax(totalTime);
                }
                if (event.mStatus == CustomMediaPlayer.Status.PAUSED) {
                    showPauseView();
                } else {
                    showPlayView();
                }
            }

            @Override
            public void onAudioModeChange(AudioPlayModeEvent audioPlayModeEvent) {
                //更新播放模式
                updatePlayModeView(audioPlayModeEvent.mPlayMode);
            }

            @Override
            public void onAudioLoad(AudioLoadEvent event) {
                //更新notifacation为load状态
                mAudioBean = event.mAudioBean;
                ImageLoaderManager.getInstance().displayImageForViewGroup(mBgView, mAudioBean.albumPic);
                //可以与初始化时的封装一个方法
                mInfoView.setText(mAudioBean.albumInfo);
                mAuthorView.setText(mAudioBean.author);
                changeFavouriteStatus(false);
                mProgressView.setProgress(0);
            }

            @Override
            public void onAudioFavouriteEvent(AudioFavouriteEvent event) {

            }

            @Override
            public void onAudioReleaseEvent(AudioReleaseEvent event) {

            }
        });

        XAudio.getInstance().playAudio();
//        MusicService.startMusicService();

    }


    private void initData() {
        mAudioBean = AudioController.getInstance().getNowPlaying();
    }

    private void initView() {
        mBgView = findViewById(R.id.root_layout);

        ImageLoaderManager.getInstance().displayImageForViewGroup(mBgView, mAudioBean.albumPic);
        findViewById(R.id.back_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.show_list_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicListDialog dialog = new MusicListDialog(XMusicPlayerActivity.this);
                dialog.show();
            }
        });
        mInfoView = findViewById(R.id.album_view);
        mInfoView.setText(mAudioBean.albumInfo);
        mInfoView.requestFocus();
        mAuthorView = findViewById(R.id.author_view);
        mAuthorView.setText(mAudioBean.author);

        mFavouriteView = findViewById(R.id.favourite_view);
        mFavouriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //收藏与否
                AudioController.getInstance().changeFavourite();
            }
        });
        changeFavouriteStatus(false);
        mStartTimeView = findViewById(R.id.start_time_view);
        mTotalTimeView = findViewById(R.id.total_time_view);
        mProgressView = findViewById(R.id.progress_view);
        mProgressView.setProgress(0);

        mPlayModeView = findViewById(R.id.play_mode_view);
        mPlayModeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放模式
                switch (XAudio.getInstance().getPlayMode()) {
                    case LOOP:
                        XAudio.getInstance().setPlayMode(AudioController.PlayMode.RANDOM);
                        break;
                    case RANDOM:
                        XAudio.getInstance().setPlayMode(AudioController.PlayMode.REPEAT);
                        break;
                    case REPEAT:
                        XAudio.getInstance().setPlayMode(AudioController.PlayMode.LOOP);
                        break;
                }
            }
        });

        updatePlayModeView(XAudio.getInstance().getPlayMode());
        mPreViousView = findViewById(R.id.previous_view);
        mPreViousView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioController.getInstance().previous();
            }
        });
        mPlayView = findViewById(R.id.play_view);
        mPlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XAudio.getInstance().playOrPauseAudio();
            }
        });
        mNextView = findViewById(R.id.next_view);
        mNextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioController.getInstance().next();
            }
        });

        mProgressView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChangeSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isChangeSeek = false;
                XAudio.getInstance().seekTo(seekBar.getProgress());
            }
        });


    }


    private void showPlayView() {
        mPlayView.setImageResource(R.mipmap.audio_aj6);
    }

    private void showPauseView() {
        mPlayView.setImageResource(R.mipmap.audio_aj7);
    }


    private void updatePlayModeView(AudioController.PlayMode playMode) {
        switch (playMode) {
            case LOOP:
                mPlayModeView.setImageResource(R.mipmap.player_loop);
                break;
            case RANDOM:
                mPlayModeView.setImageResource(R.mipmap.player_random);
                break;
            case REPEAT:
                mPlayModeView.setImageResource(R.mipmap.player_once);
                break;
        }
    }

    private void changeFavouriteStatus(boolean anim) {
        // TODO: 2021/1/8 喜欢与否 
//        if (GreenDaoHelper.selectFavourite(mAudioBean) != null) {
//            mFavouriteView.setImageResource(R.mipmap.audio_aeh);
//        } else {
//            mFavouriteView.setImageResource(R.mipmap.audio_aef);
//        }

        if (anim) {
            //留个作业，将动画封到view中作为一个自定义View
            if (animator != null) animator.end();
            PropertyValuesHolder animX =
                    PropertyValuesHolder.ofFloat(View.SCALE_X.getName(), 1.0f, 1.2f, 1.0f);
            PropertyValuesHolder animY =
                    PropertyValuesHolder.ofFloat(View.SCALE_Y.getName(), 1.0f, 1.2f, 1.0f);
            animator = ObjectAnimator.ofPropertyValuesHolder(mFavouriteView, animX, animY);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.setDuration(300);
            animator.start();
        }
    }

    @Override
    protected void onDestroy() {
//        AudioHelper.getInstance().releaseAudio();
        super.onDestroy();
    }
}
