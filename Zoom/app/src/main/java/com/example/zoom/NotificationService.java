package com.example.zoom;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import us.zoom.sdk.ZoomInstantSDK;


public class NotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = NotificationMgr.getConfNotification();
        if (null != notification) {
            startForeground(NotificationMgr.PT_NOTICICATION_ID, notification);
        } else {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ZoomInstantSDK.getInstance().getShareHelper().stopShare();
        ZoomInstantSDK.getInstance().leaveSession(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onTaskRemoved(Intent rootIntent) {
        NotificationMgr.removeConfNotification();
        stopSelf();
        ZoomInstantSDK.getInstance().getShareHelper().stopShare();
        ZoomInstantSDK.getInstance().leaveSession(false);
    }

}
