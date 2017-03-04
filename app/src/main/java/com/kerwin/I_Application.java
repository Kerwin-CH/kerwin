package com.kerwin;

import android.app.Application;
import android.util.Log;

import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

/**
 * 作者：Kerwin   时间：2017/2/24 17:02
 * 版本：
 * 说明：
 */

public class I_Application extends Application {

    private static I_Application mApplication;

    @Override
    public void onCreate() {
        mApplication = this;
        PushAgent mPushAgent = PushAgent.getInstance(this);
       // mPushAgent.setPushIntentServiceClass(I_PushService.class);
        mPushAgent.setResourcePackageName("com.kerwin");
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                Log.e("DeviceToken:", deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
    }

    public static I_Application getInstance() {
        return mApplication;
    }
}
