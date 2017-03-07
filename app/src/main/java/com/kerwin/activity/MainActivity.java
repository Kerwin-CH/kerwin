package com.kerwin.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.kerwin.I_Application;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kerwin.R.layout.activity_main_new);
        initView();
        initChannels();
    }

    @Override
    public void onPause() {
        super.onPause();
        mVideoView.pause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
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
        mVideoView = (VideoView) findViewById(com.kerwin.R.id.vv_video_view);
        overVideoInfoLayout = (RelativeLayout) findViewById(R.id.rl_info_over_movie);
        videoLoadProgressBar = (ProgressBar) findViewById(R.id.pb_movie_load);
        videoLoadSpeedText = (TextView) findViewById(R.id.tv_movie_load);
        videoBufferInfo = (TextView) findViewById(R.id.tv_movie_buffer_info);

        mMediaController = new MediaController(this);//实例化控制器
        // mMediaController.show(5000);//控制器显示5s后自动隐藏
       // mVideoView.setMediaController(mMediaController);//绑定控制器
        // mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);//设置播放画质 高画质
        mVideoView.requestFocus();//取得焦点
        //mVideoView.setBufferSize(1024 * 1024);
        mVideoView.setOnBufferingUpdateListener(this);

        view = LayoutInflater.from(this).inflate(com.kerwin.R.layout.popup_window, null);
        popupWindow = new PopupWindow(view, 650, ViewGroup.LayoutParams.MATCH_PARENT, true);


        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "播放错误" + what + "，尝试播放下一个", Toast.LENGTH_LONG).show();
                if (channelses.size() > currentChannel - 1) {
                    currentChannel += 1;
                } else {
                    currentChannel = 0;
                }

                mVideoView.setVideoURI(Uri.parse(parseUrl(channelses.get(currentChannel).getUrl())));
                channelses.remove(currentChannel == 0 ? 0 : currentChannel - 1);
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
        videoBufferInfo.setText("| 已缓存：" + percent + "%");
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
     * 初始化频道信息
     */
    private void initChannels() {
        String jsonStr = Kutils.getJson(this, "kerwin_channel.json");
        JsonsRootBean rootBean = (JsonsRootBean) new Gson().fromJson(jsonStr, JsonsRootBean.class);
        channelses = rootBean.getChannels();
        Comparator comparator = new ComparatorChannels();
        Collections.sort(channelses, comparator);
        mVideoView.setVideoURI(Uri.parse(channelses.get(currentChannel).getUrl()));
        mVideoView.start();
    }

    /**
     * 弹窗显示
     */
    private void showPopipWindow() {
        popupWindow.setContentView(view);
        channelListView = (ListView) view.findViewById(com.kerwin.R.id.lv_channel_lsit);
        View viewRoot = LayoutInflater.from(this).inflate(com.kerwin.R.layout.activity_main, null);
        popupWindow.showAtLocation(viewRoot, Gravity.RIGHT, 0, 0);
        channelAdapter = new ChannelAdapter();
        channelListView.setAdapter(channelAdapter);
        channelListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(MainActivity.this, channelses.get(position).getName(), Toast.LENGTH_LONG).show();
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        currentChannel = position;
        mVideoView.setVideoURI(Uri.parse(channelses.get(currentChannel).getUrl()));
        mVideoView.start();
    }


    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.vv_video_view:
//                showPopipWindow();
//                break;
//            default:
//                break;
//        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getX() > I_Application.screenHeight / 2)
                showPopipWindow();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (quit == false) { //询问退出程序
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            new Timer(true).schedule(new TimerTask() {
                //启动定时任务
                @Override
                public void run() {
                    quit = false; //重置退出标识
                }
            }, 2000);
            //2秒后运行run()方法
            quit = true;
        } else {
            //确认退出程序
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
