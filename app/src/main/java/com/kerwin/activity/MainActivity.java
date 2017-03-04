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

import com.google.gson.Gson;
import com.kerwin.R;
import com.kerwin.base.BaseActivity;
import com.kerwin.bean.Channels;
import com.kerwin.bean.JsonsRootBean;
import com.kerwin.utils.Kutils;

import java.util.ArrayList;
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

public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private boolean quit = false; //设置退出标识
    private PopupWindow popupWindow;
    private VideoView videoView;
    private ListView channelListView;//频道列表
    private List<Channels> channelses = new ArrayList<>();
    private ChannelAdapter channelAdapter;
    private MediaController mMediaController;

    private int currentChannel = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kerwin.R.layout.activity_main_new);
        initView();
        initChannels();
    }

    /**
     * 初始化UI
     */
    private void initView() {
        videoView = (VideoView) findViewById(com.kerwin.R.id.vv_video_view);
        videoView.setOnClickListener(this);
        mMediaController = new MediaController(this);
        videoView.setMediaController(mMediaController);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "播放错误" + what + "，尝试播放下一个", Toast.LENGTH_LONG).show();
                if (channelses.size() > currentChannel - 1) {
                    currentChannel = +1;
                } else {
                    currentChannel = 0;
                }
                videoView.setVideoURI(Uri.parse(channelses.get(currentChannel).getUrl()));
                return false;
            }
        });
    }

    /**
     * 初始化频道信息
     */
    private void initChannels() {
        String jsonStr = Kutils.getJson(this, "kerwin_channel.json");
        JsonsRootBean rootBean = (JsonsRootBean) new Gson().fromJson(jsonStr, JsonsRootBean.class);
        channelses = rootBean.getChannels();
        videoView.setVideoURI(Uri.parse(channelses.get(currentChannel).getUrl()));
    }

    /**
     * 弹窗显示
     */
    private void showPopipWindow() {
        View view = LayoutInflater.from(this).inflate(com.kerwin.R.layout.popup_window, null);
        popupWindow = new PopupWindow(view, 600, ViewGroup.LayoutParams.MATCH_PARENT, true);
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
        Toast.makeText(MainActivity.this, "选择频道：" + channelses.get(position).getName(), Toast.LENGTH_LONG).show();
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        currentChannel = position;
        videoView.setVideoURI(Uri.parse(channelses.get(currentChannel).getUrl()));
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
            viewHolder.channelNameView.setText(channelses.get(position).getName());
            String url = channelses.get(position).getUrl();
            viewHolder.channelUrlView.setText(url.length() > 35 ? url.substring(0, 32) + "..." : url);
            return convertView;
        }
    }
}
