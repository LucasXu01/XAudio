package com.lucas.audioSample.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.lucas.audioSample.R;
import com.lucas.audioSample.extra.CustomMusicService;
import com.lucas.audioSample.utils.MyUtils;
import com.lucas.audioSample.view.IndictorView;
import com.lucas.audioSample.view.MusicListDialog;
import com.lucas.library.XImg;
import com.lucas.xaudio.XAudio;
import com.lucas.xaudio.AudioServiceListener;
import com.lucas.xaudio.audioplayer.core.AudioController;
import com.lucas.xaudio.audioplayer.events.AudioLoadEvent;
import com.lucas.xaudio.audioplayer.events.AudioPlayModeEvent;
import com.lucas.xaudio.audioplayer.events.AudioProgressEvent;
import com.lucas.xaudio.audioplayer.model.BaseAudioBean;
import com.lucas.xaudio.utils.XAudioUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

/**
 * 播放音乐Activity
 */
public class MusicPlayerActivity extends AppCompatActivity {

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
    private IndictorView indictorView;
    private Animator animator;
    private boolean isChangeSeek = false; //判定是否在滑动seek
    private BaseAudioBean mAudioBean; //当前正在播放歌曲
    private boolean isFavourite = false;

    public static void start(Activity context) {

        //初始化音乐数据
        XAudio.getInstance()
                .setNotificationIntent(new Intent(context, MusicPlayerActivity.class)) //可选
                .setAutoService(new CustomMusicService())
                .addAudio(MyUtils.getMockData());

        Intent intent = new Intent(context, MusicPlayerActivity.class);
        ActivityCompat.startActivity(context, intent, ActivityOptionsCompat.makeSceneTransitionAnimation(context).toBundle());

    }

    public AudioServiceListener audioServiceListener = new AudioServiceListener() {
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
            XImg.getIns().loadImg4ViewGroupBlur(mBgView, mAudioBean.albumPic);
            //可以与初始化时的封装一个方法
            mInfoView.setText(mAudioBean.albumInfo);
            mAuthorView.setText(mAudioBean.author);
            changeFavouriteStatus(false);
            mProgressView.setProgress(0);
            Log.e("jin", "onAudioLoad: new AudioServiceListener() indictorView.onAudioLoadEvent(event);" + mAudioBean.getAlbumInfo() );
            indictorView.onAudioLoadEvent(event);
        }

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //添加入场动画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(
                    TransitionInflater.from(this).inflateTransition(R.transition.transition_bottom2top));
        }

        BarUtils.transparentStatusBar(this); //透明状态栏
//        //隐藏actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.activity_music_player_layout);


        initData();
        initView();

        XAudio.getInstance().addAudioServiceListener(audioServiceListener);
        XAudio.getInstance().playAudio();

    }


    private void initData() {
        mAudioBean = AudioController.getInstance().getNowPlaying();
    }

    private void initView() {
        mBgView = findViewById(R.id.root_layout);
        XImg.getIns().loadImg4ViewGroupBlur(mBgView, mAudioBean.albumPic);
        findViewById(R.id.back_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.show_list_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicListDialog dialog = new MusicListDialog(MusicPlayerActivity.this);
                dialog.show();
            }
        });
        mInfoView = findViewById(R.id.album_view);
        indictorView = findViewById(R.id.indictorView);
        mInfoView.setText(mAudioBean.albumInfo);
        mInfoView.requestFocus();
        mAuthorView = findViewById(R.id.author_view);
        mAuthorView.setText(mAudioBean.author);

        mFavouriteView = findViewById(R.id.favourite_view);
        changeFavouriteStatus(isFavourite);
        mFavouriteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //收藏与否
                isFavourite = !isFavourite;
                changeFavouriteStatus(isFavourite);
            }
        });

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
        Log.e("jin", "showPlayView" );
        indictorView.onAudioStartEvent();
    }

    private void showPauseView() {
        mPlayView.setImageResource(R.mipmap.audio_aj7);
        Log.e("jin", "showPauseView" );
        indictorView.onAudioPauseEvent();
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

    private void changeFavouriteStatus(boolean isFavourite) {
        // TODO: 2021/1/8 喜欢与否
        if (isFavourite) {
            mFavouriteView.setImageResource(R.mipmap.audio_aeh);
        } else {
            mFavouriteView.setImageResource(R.mipmap.audio_aef);
        }

        if (isFavourite) {
            //这里可以将动画封到view中作为一个自定义View
            if (animator != null) animator.end();
            PropertyValuesHolder animX =
                    PropertyValuesHolder.ofFloat(View.SCALE_X.getName(), 1.0f, 1.3f, 1.0f);
            PropertyValuesHolder animY =
                    PropertyValuesHolder.ofFloat(View.SCALE_Y.getName(), 1.0f, 1.3f, 1.0f);
            animator = ObjectAnimator.ofPropertyValuesHolder(mFavouriteView, animX, animY);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.setDuration(300);
            animator.start();
        }
    }

    @Override
    protected void onDestroy() {
        XAudio.getInstance().removeAudioServiceListener(audioServiceListener);
        XAudio.getInstance().clearAudioList();
        XAudio.getInstance().releaseAudio();
        super.onDestroy();
    }
}
