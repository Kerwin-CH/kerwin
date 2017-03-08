package com.kerwin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kerwin.R;
import com.kerwin.base.BaseActivity;

public class SplashActivity extends BaseActivity {


    private TextView textLoadInfo;//载入信息文本控件
    private ProgressBar progressBarLoad;//加载进度条
    int progress;

    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == 0) {
                progressBarLoad.setProgress(progress);
                textLoadInfo.setText("载入中：" + progress + "%");
                return;
            }
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();


        new Thread() {
            @Override
            public void run() {
                while (progress <= 100) {
                    mHandler.sendEmptyMessage(0);
                    try {
                        sleep(29);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progress += 1;
                    super.run();
                }
            }
        }.start();
        mHandler.sendEmptyMessageDelayed(1, 4000);
    }

    private void initView() {
        textLoadInfo = (TextView) findViewById(R.id.tv_splash_load_info);
        progressBarLoad = (ProgressBar) findViewById(R.id.pb_load_bar);
        progressBarLoad.setMax(100);
    }
}
