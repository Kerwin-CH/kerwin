package com.kerwin;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.kerwin.bean.DaoMaster;
import com.kerwin.bean.DaoSession;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

/**
 * 作者：Kerwin   时间：2017/2/24 17:02
 * 版本：
 * 说明：
 */

public class I_Application extends Application {

    private static I_Application mApplication;
    public static int screenWidth;
    public static int screenHeight;

    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        setDatabase();

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        initUMPush();

    }

    /**
     * 初始化友盟推送
     */
    private void initUMPush() {
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.setResourcePackageName("com.kerwin");
        mPushAgent.setNotificaitonOnForeground(false);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
            }

            @Override
            public void onFailure(String s, String s1) {
            }
        });
        /**
         * 自定义通知栏处理
         */
        UmengMessageHandler messageHandler = new UmengMessageHandler() {
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                switch (msg.builder_id) {
                    case 1:
                        Notification.Builder builder = new Notification.Builder(context);
                        RemoteViews myNotificationView = new RemoteViews(context.getPackageName(),
                                R.layout.notification_view);
                        myNotificationView.setTextViewText(R.id.notification_title, msg.title);
                        myNotificationView.setTextViewText(R.id.notification_text, msg.text);
                        myNotificationView.setImageViewBitmap(R.id.notification_large_icon,
                                getLargeIcon(context, msg));
                        myNotificationView.setImageViewResource(R.id.notification_small_icon,
                                getSmallIconId(context, msg));
                        builder.setContent(myNotificationView)
                                .setSmallIcon(getSmallIconId(context, msg))
                                .setTicker(msg.ticker)
                                .setAutoCancel(true);
                        return builder.getNotification();
                    default:
                        return super.getNotification(context, msg);
                }
            }
        };
        mPushAgent.setMessageHandler(messageHandler);
    }

    public static I_Application getInstance() {
        return mApplication;
    }

    /**
     * 设置greenDao
     */
    private void setDatabase() {
        mHelper = new DaoMaster.DevOpenHelper(this, "kerwin-db", null);
        db = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}
