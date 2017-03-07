package com.kerwin.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kerwin.R;
import com.kerwin.base.BaseActivity;
import com.kerwin.bean.Channels;
import com.kerwin.bean.JsonsRootBean;
import com.kerwin.utils.ComparatorChannels;
import com.kerwin.utils.Kutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.CenterLayout;

/**
 * Created by Kerwin on 2017/3/5.
 */

public class VideoActivity extends BaseActivity implements SurfaceHolder.Callback, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, View.OnTouchListener {
    private static final String TAG = "VideoActivity";
    private MediaPlayer mMediaPlayer;
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    private TextView txVideoTime;
    private SeekBar liveSeekBar;
    private Timer mTimer;
    private TextView txVideoCurrentTime;
    private long videoTime;
    private ImageView playBT;
    public final static int HANDER_UPDATE = 1;
    public final static int HANDER_HIDE = 2;
    private RelativeLayout liveRelative;
    private ImageButton liveImgbtPlay;
    private GestureDetector mGestureDetector;

    private ArrayList<Channels> channelses;//频道信息列表
    private String currentDataPath;//当前播放url
    private int currentPostion;//当前播放索引位置

    //屏幕的宽高
    private int windowWidth;
    private int windowHeight;
    //竖屏宽高
    private int glayoutHeight;
    private int glayoutWidth;
    private ViewGroup.LayoutParams layoutParams;
    private CenterLayout glayout;
    //获取视频播放父类的padding，方便全屏的时候修改
    private RelativeLayout liveGlRl;
    private int liveGlRlPaddingBottom;
    private int liveGlRlPaddingLeft;
    private int liveGlRlPaddingRight;
    private int liveGlRlPaddingTop;


    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            if (mMediaPlayer == null)
                return;
            if (mMediaPlayer.isPlaying()) {
                Message msg = Message.obtain();
                msg.what = HANDER_UPDATE;
                mHandler.sendMessage(msg);
            }
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mMediaPlayer == null) {
                return;
            }
            switch (msg.what) {
                case HANDER_UPDATE:
                    long position = mMediaPlayer.getCurrentPosition();
                    long progress = liveSeekBar.getMax() * position / videoTime;
                    txVideoCurrentTime.setText(formatDuring(position));
                    liveSeekBar.setProgress((int) progress);
                    break;
                case HANDER_HIDE:
                    if (mMediaPlayer.isPlaying()) {
                        playBT.setVisibility(View.GONE);
                        liveRelative.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化库。若少了会报错！！
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        //禁止休眠
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        windowWidth = displayMetrics.widthPixels;
        windowHeight = displayMetrics.heightPixels;

        /**
         * 注册定时器定时更新播放时间进度
         */
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 0, 1000);

        initView();
        initAction();
        initChannels();
        currentPostion = 0;
        currentDataPath = parseUrl(channelses.get(currentPostion).getUrl());
        //playVideo();
        //startVideoPlay();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    /**
     * 初始化UI界面
     */
    public void initView() {
        //加载布局文件
        setContentView(R.layout.activity_video_player);
        txVideoTime = (TextView) findViewById(R.id.live_tv_end);
        txVideoCurrentTime = (TextView) findViewById(R.id.live_tv_current);
        liveSeekBar = (SeekBar) findViewById(R.id.live_seekbar);
        playBT = (ImageView) findViewById(R.id.live_iv_play);
        liveRelative = (RelativeLayout) findViewById(R.id.live_rl_clear);
        liveImgbtPlay = (ImageButton) findViewById(R.id.live_imgbt_play);
        glayout = (CenterLayout) findViewById(R.id.live_cl);
        layoutParams = glayout.getLayoutParams();
        glayoutHeight = layoutParams.height;
        glayoutWidth = layoutParams.width;
        liveGlRl = (RelativeLayout) findViewById(R.id.live_gl_rl);
        liveGlRlPaddingBottom = liveGlRl.getPaddingBottom();
        liveGlRlPaddingLeft = liveGlRl.getPaddingLeft();
        liveGlRlPaddingRight = liveGlRl.getPaddingRight();
        liveGlRlPaddingTop = liveGlRl.getPaddingTop();
        //注册手势时间
        mGestureDetector = new GestureDetector(this, new MyOnGestureListenner());
        //查找组件
        mPreview = (SurfaceView) findViewById(R.id.myvideo);
        //获取此surfaceView的holder对象。此holder对象即为mediaplayer显示的地方。
        holder = mPreview.getHolder();
        //设置回调。这里主要是surfaceChanged、surfaceDestroyed、surfaceCreated三个方法。
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBA_8888);
    }

    /**
     * 初始化频道信息
     */
    private void initChannels() {
        String jsonStr = Kutils.getJson(this, "kerwin_channel.json");
        JsonsRootBean rootBean = (JsonsRootBean) new Gson().fromJson(jsonStr, JsonsRootBean.class);
        channelses = rootBean.getChannels();
        Comparator comparator = new ComparatorChannels();
        Collections.sort(channelses, comparator);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    /**
     * 处理手势动作
     */
    private class MyOnGestureListenner extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            liveRelative.setVisibility(View.VISIBLE);
            playBT.setVisibility(View.VISIBLE);
            Message msg = Message.obtain();
            msg.what = HANDER_HIDE;
            mHandler.sendMessageDelayed(msg, 3000);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.v("onDoubleTap", "onDoubleTap");
            if (mMediaPlayer == null) {
                Log.v(TAG, "屏幕高：" + windowHeight + "；屏幕宽：" +
                        windowWidth + "竖屏高：" + glayoutHeight + "宽：" + glayoutWidth);
            }
            int mCurrentOrientation = VideoActivity.this.getResources().getConfiguration().orientation;
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            //如果是横屏
            if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {

                VideoActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().setAttributes(attrs);
                //取消全屏设置
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                layoutParams.height = glayoutHeight;
                layoutParams.width = glayoutWidth;
                liveGlRl.setPadding(liveGlRlPaddingLeft, liveGlRlPaddingTop,
                        liveGlRlPaddingRight, liveGlRlPaddingBottom);
                glayout.setLayoutParams(layoutParams);
            }
            //若果是竖屏
            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                VideoActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                getWindow().setAttributes(attrs);
                //设置全屏
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                layoutParams.height = windowWidth;
                layoutParams.width = windowHeight;
                liveGlRl.setPadding(0, 0, 0, 0);
                glayout.setLayoutParams(layoutParams);
            }
            //return super.onDoubleTap(e);
            return true;
        }
    }

    /**
     * 处理播放路径
     *
     * @param str
     * @return
     */
    private String parseUrl(String str) {
        String url = new String();
        if (!str.startsWith("http")) {
            url = "http://" + str;
        }
        url = str;
        return url;
    }

    /**
     * 注册操作按钮事件
     */
    public void initAction() {
        //设置触摸监听
        mPreview.setOnTouchListener(this);
        mPreview.setFocusable(true);//使能控件获得焦点
        mPreview.setClickable(true);//表明控件可以点击
        mPreview.setLongClickable(true);
        playBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                } else {
                    mMediaPlayer.start();
                }
            }
        });
        liveImgbtPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                } else {
                    mMediaPlayer.start();
                }
            }
        });
    }


    private void playVideo() {
        try {
            //初始化mediaplayer。
            mMediaPlayer = new MediaPlayer(this);
            //设置数据源
            mMediaPlayer.setDataSource(currentDataPath);
            //设置显示
            mMediaPlayer.setDisplay(holder);
            //准备
            mMediaPlayer.prepareAsync();
            //设置缓冲监听
            mMediaPlayer.setOnBufferingUpdateListener(this);
            //设置播放完毕监听
            mMediaPlayer.setOnCompletionListener(this);
            //设置准备完毕监听
            mMediaPlayer.setOnPreparedListener(this);
            //设置显示大小监听
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Log.d(TAG, "onBufferingUpdate percent:" + percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion called");
        mTimer.cancel();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        videoTime = mMediaPlayer.getDuration();
        txVideoTime.setText(formatDuring(videoTime));
        startVideoPlay();
    }

    /**
     * 释放MediaPlayer资源
     */
    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * 播放视频
     */
    private void startVideoPlay() {
        mMediaPlayer.start();
    }

    //计算时间
    private String formatDuring(long mss) {
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        String hour = "";
        if (hours < 10) {
            hour = "0" + hours;
        } else {
            hour = hours + "";
        }
        String minute = "";
        if (minutes < 10) {
            minute = "0" + minutes;
        } else {
            minute = hours + "";
        }
        String second = "";
        if (seconds < 10) {
            second = "0" + seconds;
        } else {
            second = seconds + "";
        }
        return hour + ":" + minute + ":"
                + second;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated called");
        final ImageView begin = (ImageView) findViewById(R.id.live_iv_begin);
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                begin.setVisibility(View.GONE);
                playVideo();
            }
        });

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged called");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed called");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v(TAG, "onConfigurationChanged");
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }


}
