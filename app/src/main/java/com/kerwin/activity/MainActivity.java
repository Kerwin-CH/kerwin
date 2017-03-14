package com.kerwin.activity;

import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kerwin.R;
import com.kerwin.base.BaseActivity;
import com.kerwin.bean.Channels;
import com.kerwin.bean.ChannelsDao;
import com.kerwin.bean.JsonsRootBean;
import com.kerwin.utils.ComparatorChannels;
import com.kerwin.utils.DisplayUtil;
import com.kerwin.utils.Kutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * 作者：Kerwin   时间：2017/2/27 14:57
 * 版本：
 * 说明：
 */

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, RadioGroup.OnCheckedChangeListener {

    private boolean quit = false; //设置退出标识
    private final int DISMISS_COTROL_BAR = 0x001;
    private final int SET_PROGRESS_VALUE = 0x002;


    private final String USER_BRIGHTNESS = "kerwin_brightness";
    private final String USER_VOLUME = "kerwin_volume";
    private final String LAST_CHANNEL = "kerwin_last_channel";
    /**
     * 视频缩放模式，默认为全屏
     * VIDEO_LAYOUT_ORIGIN-画面原始大小；
     * VIDEO_LAYOUT_SCALE-画面全屏；
     * VIDEO_LAYOUT_STRETCH-画面拉伸；
     * VIDEO_LAYOUT_ZOOM-画面裁剪；
     */
    private int videoScaleType = VideoView.VIDEO_LAYOUT_ORIGIN;
    /**
     * 视频清晰度  默认中画质
     * public static final int VIDEOQUALITY_LOW = -16;
     * public static final int VIDEOQUALITY_MEDIUM = 0;
     * public static final int VIDEOQUALITY_HIGH = 16;
     */
    private int videoQualit = MediaPlayer.VIDEOQUALITY_HIGH;

    private PopupWindow popupWindow;
    private VideoView mVideoView;
    private ListView channelListView;//频道列表
    private List<Channels> channelses = new ArrayList<>();
    private ArrayList<Channels> collectionChannels = new ArrayList<>();
    private ChannelAdapter channelAdapter;
    private MediaController mMediaController;
    private boolean isUpDataChannels;//是否需要更新数据库列表
    private final int COLLECTION_LIST = 1;
    private final int CHANNEL_LIST = 0;
    private int menuType = CHANNEL_LIST;//当前菜单列表显示类型

    private int currentChannel = 0;
    private View view;
    private RelativeLayout overVideoInfoLayout;//播放界面上层提示信息布局
    private ProgressBar videoLoadProgressBar;
    private TextView videoLoadSpeedText;
    private TextView videoBufferInfo;//缓存进度
    private Button saveChannelList;//保存频道信息到本地
    private Button inputchannelList;//导入本地频道信息


    private ChannelsDao channelsDao;

    private AudioManager mAudioManager;
    private int mMaxVolume;//最大音量
    private int mVolume = -1;//当前音量
    private float mBrightness = -1f;//当前亮度
    private GestureDetectorCompat mDetector;
    private RelativeLayout viceSettingLayout;//音量、亮度布局
    private ImageView settingTypeIcon;//音量、亮度图标控件
    private ProgressBar valueProgressBar;//音量、亮度值进度条
    private ImageView collectionButton;//收藏界面入口按钮
    private ImageView settingButton;//设置界面入口按钮
    private ImageView channelButton;//频道界面入口按钮

    private LinearLayout listLayout;//节目列表父布局（收藏和频道列表公用）
    private RelativeLayout settingLayout;//设置界面父布局
    private RadioGroup qualitRadioGroup;//画质选择RadioGroup
    private RadioGroup scaleRadioGroup;//缩放模式选择RadioGroup
    private EditText inputPath;
    private Button playInputPath;
    private int scrolledY;

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

        //获取用户上次观看的频道
        currentChannel = sharedPreferences.getInt(LAST_CHANNEL, 0);
        isUpDataChannels = sharedPreferences.getBoolean("updata", true);

        channelsDao = mApplication.getDaoSession().getChannelsDao();
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
        mVideoView.setVideoQuality(videoQualit);//设置播放画质 高画质
        //mVideoView.setVideoLayout(videoScaleType, 0);
        mVideoView.requestFocus();//取得焦点
        mVideoView.setBufferSize(1024 * 1024);
        mVideoView.setOnBufferingUpdateListener(this);

        view = LayoutInflater.from(this).inflate(com.kerwin.R.layout.popup_window, null);
        popupWindow = new PopupWindow(view, DisplayUtil.dip2px(this, 220), ViewGroup.LayoutParams.MATCH_PARENT, true);


        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoView.start();
                videoLoadProgressBar.setVisibility(View.INVISIBLE);

            }
        });
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                showToast("播放错误！");
//                if (channelses.size() > currentChannel - 1) {
//                    currentChannel += 1;
//                } else {
//                    currentChannel = 0;
//                }
                if(currentChannel>=channelses.size()-1){
                    currentChannel-=1;
                }
                String url;
                if (menuType == CHANNEL_LIST) {
                    url = channelses.get(currentChannel).getUrl();
                    channelsDao.delete(channelses.get(currentChannel));
                    channelses.remove(currentChannel);
                } else {
                    url = collectionChannels.get(currentChannel).getUrl();
                }
                mVideoView.setVideoURI(Uri.parse(url));
                if (popupWindow.isShowing())
                    channelAdapter.notifyDataSetChanged();
                return true;
            }
        });

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    overVideoInfoLayout.setVisibility(View.VISIBLE);
                    videoLoadSpeedText.setText("" + extra + "kb/s" + "  ");
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
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        videoBufferInfo.setText("   已缓存：" + percent + "%");
    }

    /**
     * 初始化频道信息
     */
    private void initChannels() {
        if (isUpDataChannels) {
            String jsonStr = Kutils.getJson(this, "kerwin_channel.json");
            JsonsRootBean rootBean = (JsonsRootBean) new Gson().fromJson(jsonStr, JsonsRootBean.class);
            channelses = rootBean.getChannels();
            Log.e("Main", "解析频道列表：" + channelses.size());
            Comparator comparator = new ComparatorChannels();
            Collections.sort(channelses, comparator);
            for (Channels channels : channelses) {
                channelsDao.insert(channels);
            }
            isUpDataChannels = false;
            SPEditor.putBoolean("updata", isUpDataChannels);
            SPEditor.commit();
        } else {
            channelses = channelsDao.loadAll();
            Log.e("Main", "数据库取出频道列表：" + channelses.size());
            collectionChannels = (ArrayList<Channels>) channelsDao.queryBuilder().where(ChannelsDao.Properties.Collection.eq(true)).list();
        }
        mVideoView.setVideoURI(Uri.parse(channelses.get(currentChannel).getUrl()));
    }

    /**
     * 弹窗显示
     */
    private void showPopipWindow() {
        scrolledY = sharedPreferences.getInt("scrolledY", 0);
        popupWindow.setContentView(view);
        channelListView = (ListView) view.findViewById(R.id.lv_channel_lsit);
        channelButton = (ImageView) view.findViewById(R.id.iv_channel_enter);
        collectionButton = (ImageView) view.findViewById(R.id.iv_collection_enter);
        settingButton = (ImageView) view.findViewById(R.id.iv_setting_enter);

        //控制显示用的父布局
        listLayout = (LinearLayout) view.findViewById(R.id.ll_channels_list);
        settingLayout = (RelativeLayout) view.findViewById(R.id.rl_video_setting);

        qualitRadioGroup = (RadioGroup) view.findViewById(R.id.rg_qualit);
        scaleRadioGroup = (RadioGroup) view.findViewById(R.id.rg_scalet);
        inputPath = (EditText) view.findViewById(R.id.et_video_path);
        playInputPath = (Button) view.findViewById(R.id.bt_play_url);

        inputchannelList = (Button) view.findViewById(R.id.bt_channelist_input);
        saveChannelList = (Button) view.findViewById(R.id.bt_channelist_save);
        inputchannelList.setOnClickListener(this);
        saveChannelList.setOnClickListener(this);

        playInputPath.setOnClickListener(this);
        channelButton.setOnClickListener(this);
        collectionButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        qualitRadioGroup.setOnCheckedChangeListener(this);
        scaleRadioGroup.setOnCheckedChangeListener(this);
        //恢复位置
        channelListView.post(new Runnable() {
            @Override
            public void run() {
                channelListView.smoothScrollBy(scrolledY, 0);
            }
        });
        View viewRoot = LayoutInflater.from(this).inflate(R.layout.activity_main_new, null);
        popupWindow.showAtLocation(viewRoot, Gravity.RIGHT, 0, 0);
        channelAdapter = new ChannelAdapter();
        channelAdapter.setData(channelses);
        channelListView.setAdapter(channelAdapter);
        channelListView.setOnItemClickListener(this);
        channelListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 不滚动时保存当前滚动到的位置
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (channelses != null) {
                        View c = channelListView.getChildAt(0);
                        if (c != null) {
                            int firstVisiblePosition = channelListView.getFirstVisiblePosition();
                            int top = c.getTop();
                            scrolledY = -top + firstVisiblePosition * c.getHeight();
                        }
                        SPEditor.putInt("scrolledY", scrolledY);
                        SPEditor.commit();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());// 指定 PopupWindow 的背景
        popupWindow.setFocusable(true);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(MainActivity.this, channelses.get(position).getName(), Toast.LENGTH_LONG).show();
        currentChannel = position;
        SPEditor.putInt(LAST_CHANNEL, position);
        SPEditor.apply();
        Uri srcUri;
        /**
         * 区分是收藏列表点击还是频道列表点击
         */
        if (menuType == CHANNEL_LIST) {
            srcUri = Uri.parse(channelses.get(currentChannel).getUrl());
        } else {
            srcUri = Uri.parse(collectionChannels.get(currentChannel).getUrl());
        }
        mVideoView.setVideoURI(srcUri);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_channel_enter:
                menuType = CHANNEL_LIST;
                channelAdapter.setData(channelses);
                channelAdapter.notifyDataSetChanged();
                settingLayout.setVisibility(View.GONE);
                listLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_collection_enter:
                menuType = COLLECTION_LIST;
                channelAdapter.setData(collectionChannels);
                channelAdapter.notifyDataSetChanged();
                settingLayout.setVisibility(View.GONE);
                listLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_setting_enter:
                settingLayout.setVisibility(View.VISIBLE);
                listLayout.setVisibility(View.GONE);
                break;
            case R.id.bt_play_url:
                String url = inputPath.getText().toString().trim();
                if (!TextUtils.isEmpty(url)) {
                    mVideoView.setVideoURI(Uri.parse(url));
                } else {
                    showToast("输入路径不能为空！");
                }
                break;
            case R.id.bt_channelist_save:
                //保存频道信息至SD卡
                saveChannleListToSD();
                break;
            case R.id.bt_channelist_input:
                readChannelListFromSD();
                //从SD卡导入频道信息
                break;
        }
    }

    /**
     * 保存频道列表至本地SD卡
     */
    private void saveChannleListToSD() {
        JsonsRootBean rootBean = new JsonsRootBean();
        rootBean.setState("updata");
        rootBean.setChannels((ArrayList<Channels>) channelses);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String result = gson.toJson(rootBean);
        Kutils.putStringToSD(result, "");
        showToast("导出节目列表成功！/n 路径为SD卡根目录 ");
    }

    /**
     * 从SD卡载入频道列表
     */
    private void readChannelListFromSD() {
        String json = Kutils.getStringFromSD("mnt/sdcard/kk_channles.json");
        JsonsRootBean rootBean = (JsonsRootBean) new Gson().fromJson(json, JsonsRootBean.class);
        channelses = rootBean.getChannels();
        Comparator comparator = new ComparatorChannels();
        Collections.sort(channelses, comparator);
        channelsDao.deleteAll();
        for (Channels channels : channelses) {
            channelsDao.insert(channels);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.cb_qualit_heigh:
                videoQualit = android.widget.VideoView.DRAWING_CACHE_QUALITY_HIGH;
                showToast("已设置高画质");
                break;
            case R.id.cb_qualit_auto:
                videoQualit = android.widget.VideoView.DRAWING_CACHE_QUALITY_AUTO;
                showToast("已设置自动画质");
                break;
            case R.id.cb_qualit_low:
                videoQualit = android.widget.VideoView.DRAWING_CACHE_QUALITY_LOW;
                showToast("已设置低画质");
                break;
            case R.id.cb_scale_origin:
                videoScaleType = VideoView.VIDEO_LAYOUT_ORIGIN;
                showToast("已设置原始大小");
                break;
            case R.id.cb_scale_fullscreen:
                videoScaleType = VideoView.VIDEO_LAYOUT_SCALE;
                showToast("已设置等比全屏");
                break;
            case R.id.cb_scale_stretch:
                videoScaleType = VideoView.VIDEO_LAYOUT_STRETCH;
                showToast("已设置拉伸全屏");
                break;
        }
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
//            int index = (int) (percent * mMaxVolume / 2) + mVolume;
            int index = (int) (percent * mMaxVolume / 3) + mVolume;
            if (index > mMaxVolume)
                index = mMaxVolume;
            else if (index < 0)
                index = 0;

            mVolume = index;
            // 变更声音
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
            SPEditor.putInt(USER_VOLUME, index);
            SPEditor.apply();
            // 变更进度条
            Message msg = mHandler.obtainMessage();
            msg.what = SET_PROGRESS_VALUE;
            msg.obj = index * 100 / mMaxVolume;
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
        private ImageView collectionIconView;
    }

    class ChannelAdapter extends BaseAdapter {
        private List<Channels> channelses;

        private void setData(List list) {
            this.channelses = list;
        }

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_channels, null);
                viewHolder.channelNameView = (TextView) convertView.findViewById(R.id.tv_channel_name);
                viewHolder.channelIconView = (ImageView) convertView.findViewById(R.id.iv_channel_icon);
                viewHolder.collectionIconView = (ImageView) convertView.findViewById(R.id.iv_is_collection);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            String channelName = channelses.get(position).getName();
            int number = Kutils.isIncloudNumerOrLetter(channelName);
            if (channelName.length() >= 7 + number / 2)
                channelName = channelName.substring(0, 6 + number / 2);
            viewHolder.channelNameView.setText(channelName);
            if (channelses.get(position).getCollection())
                viewHolder.collectionIconView.setImageResource(R.mipmap.collection_seleced);
            else
                viewHolder.collectionIconView.setImageResource(R.mipmap.collection_nomarl);
            if (channelName.contains("CCTV")) {
                viewHolder.channelIconView.setImageResource(R.mipmap.tv_cctv_icon);
            } else if (channelName.contains("高清")) {
                viewHolder.channelIconView.setImageResource(R.mipmap.tv_hd_icon);
            } else {
                viewHolder.channelIconView.setImageResource(R.mipmap.tv_normal_icon);
            }

            viewHolder.collectionIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (channelses.get(position).getCollection()) {
                        channelses.get(position).setCollection(false);
                        channelsDao.update(channelses.get(position));
                        collectionChannels.remove(channelses.get(position));
                        notifyDataSetChanged();
                    } else {
                        channelses.get(position).setCollection(true);
                        channelsDao.update(channelses.get(position));
                        collectionChannels.add(channelses.get(position));
                        notifyDataSetChanged();
                    }
                }
            });
            return convertView;
        }
    }
}
