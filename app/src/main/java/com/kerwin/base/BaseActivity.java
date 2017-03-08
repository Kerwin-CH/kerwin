package com.kerwin.base;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.kerwin.I_Application;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import io.vov.vitamio.utils.Log;

/**
 * 作者：Kerwin   时间：2017/2/24 17:02
 * 版本：
 * 说明：
 */

public class BaseActivity extends Activity {
    protected I_Application mApplication;
    protected SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushAgent.getInstance(this).onAppStart();
        mApplication = I_Application.getInstance();
        sharedPreferences = getSharedPreferences("com.kerwin", Context.MODE_WORLD_WRITEABLE);
        editor = sharedPreferences.edit();
        Log.d("Activity OnCreated!");
    }

    protected boolean putSharedString(String name, String str) {
        editor.putString(name, str);
        return editor.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Activity onStarted!");
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        Log.d("Activity onResumed!");
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        Log.d("Activity onPaused!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Activity onStop!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Activity onDestroyed!");
    }
}
