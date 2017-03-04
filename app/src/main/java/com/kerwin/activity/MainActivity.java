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
import android.widget.TextView;
import android.widget.Toast;

import com.kerwin.R;
import com.kerwin.base.BaseActivity;
import com.kerwin.bean.TvChannel;

import java.util.ArrayList;
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

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private boolean quit = false; //设置退出标识
    private PopupWindow popupWindow;
    private VideoView videoView;
    private ListView channelListView;//频道列表
    private ArrayList<TvChannel> channels = new ArrayList<>();
    private ChannelAdapter channelAdapter;
    private MediaController mMediaController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        videoView = (VideoView) findViewById(R.id.vv_video_view);
        videoView.setOnClickListener(this);
        mMediaController = new MediaController(this);
        videoView.setMediaController(mMediaController);
        videoView.requestFocus();
        initChannels();
    }

    private void initChannels() {
        channels.add(new TvChannel("CCTV-1综合高清", "http://gslbserv.itv.cmvideo.cn/HDcctv1.m3u8?authCode=07110409322147352675&stbId=006001FF0018120000060019F0D496A1&Contentid=8813322615956633846&mos=jbjhhzstsl&livemode=1&channel-id=wasusyt"));
        channels.add(new TvChannel("CCTV-5体育高清", "http://gslbserv.itv.cmvideo.cn/HDcctv5.m3u8?authCode=07110409322147352675&stbId=003801FF001381001513BC20BA6E48D6&Contentid=4867251683694877276&mos=jbjhhzstsl&livemode=1&channel-id=wasusyt"));
        channels.add(new TvChannel("湖南卫视高清", "http://122.96.52.11:8080/gitv_live/HNWS-HD/index.m3u8"));
        channels.add(new TvChannel("广西卫视", "http://222.216.111.82:8088/ts000"));
        channels.add(new TvChannel("海峡卫视高清", "http://iptv.ac.cn/fjtv.m3u8?id=3"));
    }

    private void showPopipWindow() {
        View view = LayoutInflater.from(this).inflate(R.layout.popup_window, null);
        popupWindow = new PopupWindow(view, 800, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(view);
        channelListView = (ListView) view.findViewById(R.id.lv_channel_lsit);
        View viewRoot = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        popupWindow.showAtLocation(viewRoot, Gravity.RIGHT, 0, 0);
        channelAdapter = new ChannelAdapter();
        channelListView.setAdapter(channelAdapter);
        channelListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(MainActivity.this, "选择频道：" + channels.get(position).getChannelName(), Toast.LENGTH_LONG).show();
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        videoView.setVideoURI(Uri.parse(channels.get(position).getChannelUrl()));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vv_video_view:
                showPopipWindow();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
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
            return channels.size();
        }

        @Override
        public Object getItem(int position) {
            return channels.get(position);
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
            viewHolder.channelNameView.setText(channels.get(position).getChannelName());
            viewHolder.channelUrlView.setText(channels.get(position).getChannelUrl());
            return convertView;
        }
    }
}
