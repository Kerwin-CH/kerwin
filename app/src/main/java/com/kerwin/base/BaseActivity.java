package com.kerwin.base;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.kerwin.I_Application;
import com.kerwin.bean.Channels;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.vov.vitamio.utils.Log;

/**
 * 作者：Kerwin   时间：2017/2/24 17:02
 * 版本：
 * 说明：
 */

public class BaseActivity extends Activity {
    protected I_Application mApplication;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor SPEditor;
    public Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PushAgent.getInstance(this).onAppStart();
        mApplication = I_Application.getInstance();
        sharedPreferences = getSharedPreferences("com.kerwin", Context.MODE_WORLD_WRITEABLE);
        SPEditor = sharedPreferences.edit();
        mRealm = Realm.getInstance(new RealmConfiguration.Builder(this).name("kerwin.realm").build());
        Log.d("Activity OnCreated!");
    }

    protected boolean putSharedString(String name, String str) {
        SPEditor.putString(name, str);
        return SPEditor.commit();
    }


    protected void saveCollctionChannel(Channels channels) {
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(channels);
        mRealm.commitTransaction();
    }

    public void deleteCollctionChannel(String code) {
        Channels searchChannel = mRealm.where(Channels.class).equalTo("code", code).findFirst();
        mRealm.beginTransaction();
        searchChannel.removeFromRealm();
        mRealm.commitTransaction();
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
