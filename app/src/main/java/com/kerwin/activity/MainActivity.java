package com.kerwin.activity;

import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * 作者：Kerwin   时间：2017/2/27 14:57
 * 版本：
 * 说明：
 */

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

    private boolean quit = false; //设置退出标识
    private final int DISMISS_COTROL_BAR = 0x001;
    private final int SET_PROGRESS_VALUE = 0x002;


    private final String USER_BRIGHTNESS = "kerwin_brightness";
    private final String USER_VOLUME = "kerwin_volume";
    private final String LAST_CANNEL = "kerwin_last_channel";

    private PopupWindow popupWindow;
    private VideoView mVideoView;
    private ListView channelListView;//频道列表
    private List<Channels> channelses = new ArrayList<>();
    private ChannelAdapter channelAdapter;
    private MediaController mMediaController;

    private int currentChannel = 0;
    private View view;
    private RelativeLayout overVideoInfoLayout;//播放界面上层提示信息布局
    private ProgressBar videoLoadProgressBar;
    private TextView videoLoadSpeedText;
    private TextView videoBufferInfo;//缓存进度

    private AudioManager mAudioManager;
    private int mMaxVolume;//最大音量
    private int mVolume = -1;//当前音量
    private float mBrightness = -1f;//当前亮度
    private GestureDetectorCompat mDetector;
    private RelativeLayout viceSettingLayout;//音量、亮度布局
    private ImageView settingTypeIcon;//音量、亮度图标控件
    private ProgressBar valueProgressBar;//音量、亮度值进度条

    /**
     * 定时隐藏
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS_COTROL_BAR:
                    viceSettingLayout.setVisibility(View.GONE);
                    break;
                case SET_PROGRESS_VALUE:
                    int progress = (int) msg.obj;
                    if (progress > 100) {
                        progress = 100;
                    } else if (progress < 0) {
                        progress = 0;
                    }
                    valueProgressBar.setProgress(progress);

            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kerwin.R.layout.activity_main_new);
        mAudioManager = (AudioManager) getSystemService(mApplication.AUDIO_SERVICE);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        /**
         * 获取用户上次启动设置的音量和亮度，并设置；
         */
        WindowManager.LayoutParams layoutPrarams = getWindow().getAttributes();
        layoutPrarams.screenBrightness = sharedPreferences.getFloat(USER_BRIGHTNESS, -1f);
        getWindow().setAttributes(layoutPrarams);
        int volume = sharedPreferences.getInt(USER_VOLUME, -1);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        //后去用户上次观看的频道
        currentChannel = sharedPreferences.getInt(LAST_CANNEL, 0);


        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mBrightness = getWindow().getAttributes().screenBrightness;
        initView();
        initChannels();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView = null;
        }
    }

    /**
     * 初始化UI
     */
    private void initView() {
        mVideoView = (VideoView) findViewById(R.id.vv_video_view);
        overVideoInfoLayout = (RelativeLayout) findViewById(R.id.rl_info_over_movie);
        videoLoadProgressBar = (ProgressBar) findViewById(R.id.pb_movie_load);
        videoLoadSpeedText = (TextView) findViewById(R.id.tv_movie_load);
        videoBufferInfo = (TextView) findViewById(R.id.tv_movie_buffer_info);
        viceSettingLayout = (RelativeLayout) findViewById(R.id.rl_vioce_controler);
        settingTypeIcon = (ImageView) findViewById(R.id.iv_control_icon);
        valueProgressBar = (ProgressBar) findViewById(R.id.pb_setting_value);

        mMediaController = new MediaController(this);//实例化控制器
        mMediaController.show(5000);//控制器显示5s后自动隐藏
        mVideoView.setMediaController(mMediaController);//绑定控制器
        mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);//设置播放画质 高画质
        mVideoView.requestFocus();//取得焦点
        mVideoView.setBufferSize(1024 * 1024);
        mVideoView.setOnBufferingUpdateListener(this);

        view = LayoutInflater.from(this).inflate(com.kerwin.R.layout.popup_window, null);
        popupWindow = new PopupWindow(view, 650, ViewGroup.LayoutParams.MATCH_PARENT, true);


        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                videoLoadProgressBar.setVisibility(View.INVISIBLE);

            }
        });
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "播放错误", Toast.LENGTH_SHORT).show();
                if (channelses.size() > currentChannel - 1) {
                    currentChannel += 1;
                } else {
                    currentChannel = 0;
                }
                //mVideoView.setVideoURI(Uri.parse(parseUrl(channelses.get(currentChannel).getUrl())));
                // channelses.remove(currentChannel == 0 ? 0 : currentChannel - 1);
                if (popupWindow.isShowing())
                    channelAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    /**
     * 播放加载信息
     *
     * @param mp    the MediaPlayer the info pertains to.
     * @param what  the type of info or warning.
     *              <ul>
     * @param extra an extra code, specific to the info. Typically implementation
     *              dependant.
     * @return
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    overVideoInfoLayout.setVisibility(View.VISIBLE);
                    videoLoadSpeedText.setText("");
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                overVideoInfoLayout.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                videoLoadSpeedText.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    /**
     * 缓存进度
     *
     * @param mp      the MediaPlayer the update pertains to
     * @param percent the percentage (0-100) of the buffer that has been filled thus
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        videoBufferInfo.setText("   已缓存：" + percent + "%");
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
        } else
            url = str;
        return url;
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
        mVideoView.setVideoURI(Uri.parse(parseUrl(channelses.get(currentChannel).getUrl())));
    }

    /**
     * 弹窗显示
     */
    private void showPopipWindow() {
        popupWindow.setContentView(view);
        channelListView = (ListView) view.findViewById(R.id.lv_channel_lsit);
        View viewRoot = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        popupWindow.showAtLocation(viewRoot, Gravity.RIGHT, 0, 0);
        channelAdapter = new ChannelAdapter();
        channelListView.setAdapter(channelAdapter);
        channelListView.setOnItemClickListener(this);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());// 指定 PopupWindow 的背景
        popupWindow.setFocusable(true);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(MainActivity.this, channelses.get(position).getName(), Toast.LENGTH_LONG).show();
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        currentChannel = position;
        SPEditor.putInt(LAST_CANNEL, position);
        SPEditor.apply();
        mVideoView.setVideoURI(Uri.parse(parseUrl(channelses.get(currentChannel).getUrl())));
    }


    @Override
    public void onClick(View v) {

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * 单击显示popupWindow
         *
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (popupWindow != null & popupWindow.isShowing()) {
                popupWindow.dismiss();
            } else {
                showPopipWindow();
            }
            return super.onSingleTapConfirmed(e);
        }

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mVideoView.isPlaying()) {
                mVideoView.pause();
            } else {
                mVideoView.start();
            }
            return true;
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getRawX(), mOldY = e1.getRawY();
            int y = (int) e2.getRawY();
            int windowWidth = mApplication.screenWidth;
            int windowHeight = mApplication.screenHeight;

            if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (mOldX < windowWidth / 5.0)// 左边滑动
                onBrightnessSlide(distanceY / windowHeight);

            mHandler.sendEmptyMessageDelayed(DISMISS_COTROL_BAR, 4000);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        /**
         * 滑动改变声音大小
         *
         * @param percent
         */
        private void onVolumeSlide(float percent) {

            if (mVolume == -1) {
                mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (mVolume < 0)
                    mVolume = 0;
                // 显示
                int index = (int) (mVolume * 100 / mMaxVolume);
                Message msg = mHandler.obtainMessage();
                msg.what = SET_PROGRESS_VALUE;
                msg.obj = index;
                mHandler.sendMessage(msg);
            }

            // 显示
            settingTypeIcon.setImageResource(R.mipmap.vice);
            viceSettingLayout.setVisibility(View.VISIBLE);
            int index = (int) (percent * mMaxVolume) + mVolume;
            if (index > mMaxVolume)
                index = mMaxVolume;
            else if (index < 0)
                index = 0;

            // 变更声音
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
            SPEditor.putInt(USER_VOLUME, index);
            SPEditor.apply();
            // 变更进度条
            Message msg = mHandler.obtainMessage();
            msg.what = SET_PROGRESS_VALUE;
            msg.obj = index;
            mHandler.sendMessage(msg);

        }


        /**
         * 滑动改变亮度
         *
         * @param percent <p>
         */

        private void onBrightnessSlide(float percent) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            settingTypeIcon.setImageResource(R.mipmap.brightness);
            viceSettingLayout.setVisibility(View.VISIBLE);

            int leftProgress = (int) (mBrightness * 100);
            WindowManager.LayoutParams lpa = getWindow().getAttributes();
            lpa.screenBrightness = mBrightness + percent;
            if (lpa.screenBrightness > 1.0f)
                lpa.screenBrightness = 1.0f;
            else if (lpa.screenBrightness < 0.01f)
                lpa.screenBrightness = 0.01f;
            // 显示
            SPEditor.putFloat(USER_BRIGHTNESS, lpa.screenBrightness);
            SPEditor.apply();

            getWindow().setAttributes(lpa);
            Message msg = mHandler.obtainMessage();
            msg.what = SET_PROGRESS_VALUE;
            msg.obj = (int) (leftProgress + percent * 100);
            mHandler.sendMessage(msg);
        }
    }


    @Override
    public void onBackPressed() {
        if (quit == false) { //询问退出程序
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            new Timer(true).schedule(new TimerTask() {
                @Override
                public void run() {
                    quit = false; //重置退出标识
                }
            }, 2000);
            quit = true;
        } else {
            super.onBackPressed();
            finish();
        }
    }

    class ViewHolder {
        private TextView channelNameView;
        private ImageView channelIconView;
        private TextView channelUrlView;
    }

    class ChannelAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return channelses.size();
        }

        @Override
        public Object getItem(int position) {
            return channelses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_channels, null);
                viewHolder.channelNameView = (TextView) convertView.findViewById(R.id.tv_channel_name);
                viewHolder.channelIconView = (ImageView) convertView.findViewById(R.id.iv_channel_icon);
                viewHolder.channelUrlView = (TextView) convertView.findViewById(R.id.tv_channel_url);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String channelName = channelses.get(position).getName();
            viewHolder.channelNameView.setText(channelName);
            String url = channelses.get(position).getUrl();
            viewHolder.channelUrlView.setText(url.length() > 35 ? url.substring(0, 32) + "..." : url);
            if (channelName.contains("CCTV")) {
                viewHolder.channelIconView.setImageResource(R.mipmap.tv_cctv_icon);
            } else if (channelName.contains("高清")) {
                viewHolder.channelIconView.setImageResource(R.mipmap.tv_hd_icon);
            } else {
                viewHolder.channelIconView.setImageResource(R.mipmap.tv_normal_icon);
            }
            return convertView;
        }
    }
}
