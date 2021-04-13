package com.example.zoom;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.zoom.rawdata.RawDataRenderer;

import java.util.List;
import java.util.Random;

import us.zoom.sdk.ZoomInstantSDK;
import us.zoom.sdk.ZoomInstantSDKErrors;
import us.zoom.sdk.ZoomInstantSDKShareHelper;
import us.zoom.sdk.ZoomInstantSDKShareStatus;
import us.zoom.sdk.ZoomInstantSDKUser;
import us.zoom.sdk.ZoomInstantSDKUserHelper;
import us.zoom.sdk.ZoomInstantSDKVideoAspect;
import us.zoom.sdk.ZoomInstantSDKVideoHelper;
import us.zoom.sdk.ZoomInstantSDKVideoResolution;
import us.zoom.sdk.ZoomInstantSDKVideoView;


public class MeetingActivity extends BaseMeetingActivity {

    private static final String TAG = "MeetingActivity";

    ZoomInstantSDKVideoView zoomCanvas;

    RawDataRenderer rawDataRenderer;

    private FrameLayout videoContain;

    private AudioRawDataUtil audioRawDataUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioRawDataUtil = new AudioRawDataUtil(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void onSessionJoin() {
        super.onSessionJoin();
//        audioRawDataUtil.subscribeAudio();
        startMeetingService();

    }

    private void startMeetingService() {
        //Intent intent = new Intent(this, NotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //startForegroundService(intent);
        } else {
           // startService(intent);
        }
    }

    private void stopMeetingService() {
        //Intent intent = new Intent(this, NotificationService.class);
        //stopService(intent);
    }

    @Override
    public void onSessionLeave() {
        super.onSessionLeave();
        audioRawDataUtil.unSubscribe();
        if (null != shareToolbar) {
            shareToolbar.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMeetingService();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void initView() {
        super.initView();
        videoContain = findViewById(R.id.big_video_contain);
        videoContain.setOnClickListener(onEmptyContentClick);
        chatListView.setOnClickListener(onEmptyContentClick);
    }

    View.OnClickListener onEmptyContentClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!ZoomInstantSDK.getInstance().isInSession()) {
                return;
            }
            boolean isShow = actionBar.getVisibility() == View.VISIBLE;
            toggleView(!isShow);
//            if (BuildConfig.DEBUG) {
//                changeResolution();
//            }
        }
    };

    private void changeResolution() {
        if (renderType == RENDER_TYPE_OPENGLES) {
            int resolution = new Random().nextInt(3);
            resolution++;
            if (resolution > ZoomInstantSDKVideoResolution.VideoResolution_360P.getValue()) {
                resolution = 0;
            }
            ZoomInstantSDKVideoResolution size = ZoomInstantSDKVideoResolution.fromValue(resolution);
            Log.d(TAG, "changeResolution:" + size);
            if (null == currentShareUser && null != mActiveUser) {
                mActiveUser.getVideoPipe().subscribe(size, rawDataRenderer);
            }
        }
    }

    @Override
    public void onItemClick() {
        if (!ZoomInstantSDK.getInstance().isInSession()) {
            return;
        }
        boolean isShow = actionBar.getVisibility() == View.VISIBLE;
        toggleView(!isShow);
    }

    protected void toggleView(boolean show) {
        if (!show) {
            if (keyBoardLayout.isKeyBoardShow()) {
                keyBoardLayout.dismissChat(true);
                return;
            }
        }
        actionBar.setVisibility(show ? View.VISIBLE : View.GONE);
        chatListView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initMeeting() {
        ZoomInstantSDK.getInstance().addListener(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            zoomCanvas = new ZoomInstantSDKVideoView(this, !renderWithSurfaceView);
            videoContain.addView(zoomCanvas, 0, params);
        } else {
            rawDataRenderer = new RawDataRenderer(this);
            videoContain.addView(rawDataRenderer, 0, params);
        }

        ZoomInstantSDKUser mySelf = ZoomInstantSDK.getInstance().getSession().getMySelf();
        subscribeVideoByUser(mySelf);
        refreshFps();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (null != mActiveUser) {
                if (mActiveUser == currentShareUser) {
                    updateFps(mActiveUser.getShareStatisticInfo());
                } else {
                    updateFps(mActiveUser.getVideoStatisticInfo());
                }
            }

            refreshFps();
        }
    };

    private void refreshFps() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 500);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_meeting;
    }


    @Override
    public void onClickSwitchShare(View view) {
        if (null != currentShareUser) {
            updateVideoAvatar(true);
            subscribeShareByUser(currentShareUser);
            selectAndScrollToUser(mActiveUser);
        }
    }

    protected void unSubscribe() {
        if (null != currentShareUser) {
            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                currentShareUser.getVideoCanvas().unSubscribe(zoomCanvas);
                currentShareUser.getShareCanvas().unSubscribe(zoomCanvas);
            } else {
                currentShareUser.getVideoPipe().unSubscribe(rawDataRenderer);
            }
        }

        if (null != mActiveUser) {
            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
                mActiveUser.getShareCanvas().unSubscribe(zoomCanvas);
            } else {
                mActiveUser.getVideoPipe().unSubscribe(rawDataRenderer);
            }
        }
    }

    protected void subscribeVideoByUser(ZoomInstantSDKUser user) {
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            ZoomInstantSDKVideoAspect aspect = ZoomInstantSDKVideoAspect.ZoomInstantSDKVideoAspect_LetterBox;
            if (ZoomInstantSDK.getInstance().isInSession()) {
                aspect = ZoomInstantSDKVideoAspect.ZoomInstantSDKVideoAspect_Original;
            }
            if (null != currentShareUser) {
                currentShareUser.getShareCanvas().unSubscribe(zoomCanvas);
            }
            user.getVideoCanvas().unSubscribe(zoomCanvas);
            int ret=user.getVideoCanvas().subscribe(zoomCanvas, aspect);
            if(ret!= ZoomInstantSDKErrors.Errors_Success)

            {
                Toast.makeText(this,"subscribe error:"+ret, Toast.LENGTH_LONG).show();
            }
        } else {
            if (ZoomInstantSDK.getInstance().isInSession()) {
                rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Original);
            } else {
                rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Full_Filled);
            }
            if (null != currentShareUser) {
                currentShareUser.getSharePipe().unSubscribe(rawDataRenderer);
            }
            user.getVideoPipe().unSubscribe(rawDataRenderer);
           int ret= user.getVideoPipe().subscribe(ZoomInstantSDKVideoResolution.VideoResolution_360P, rawDataRenderer);
           if(ret!= ZoomInstantSDKErrors.Errors_Success)
           {
               Toast.makeText(this,"subscribe error:"+ret, Toast.LENGTH_LONG).show();
           }
        }
        mActiveUser = user;

        if (null != user.getVideoStatus()) {
            updateVideoAvatar(user.getVideoStatus().isOn());
        }

        if (null != currentShareUser) {
            btnViewShare.setVisibility(View.VISIBLE);
        } else {
            btnViewShare.setVisibility(View.GONE);
        }
    }


    protected void subscribeShareByUser(ZoomInstantSDKUser user) {
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            if (null != mActiveUser) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
                mActiveUser.getShareCanvas().unSubscribe(zoomCanvas);
            }
            user.getShareCanvas().subscribe(zoomCanvas, ZoomInstantSDKVideoAspect.ZoomInstantSDKVideoAspect_Original);
        } else {
            rawDataRenderer.setVideoAspectModel(RawDataRenderer.VideoAspect_Original);
            rawDataRenderer.subscribe(user, ZoomInstantSDKVideoResolution.VideoResolution_720P, true);
        }
        mActiveUser = user;
        btnViewShare.setVisibility(View.GONE);
    }

    private void updateVideoAvatar(boolean isOn) {
        if (isOn) {
            videoOffView.setVisibility(View.GONE);
        } else {
            videoOffView.setVisibility(View.VISIBLE);
            text_fps.setVisibility(View.GONE);
            videoOffView.setImageResource(R.drawable.zm_conf_no_avatar);
        }
    }


    @Override
    public void onUserLeave(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {
        super.onUserLeave(userHelper, userList);
        if (null == mActiveUser || userList.contains(mActiveUser)) {
            subscribeVideoByUser(session.getMySelf());
            selectAndScrollToUser(session.getMySelf());
        }
    }

    @Override
    public void onUserVideoStatusChanged(ZoomInstantSDKVideoHelper videoHelper, List<ZoomInstantSDKUser> userList) {
        super.onUserVideoStatusChanged(videoHelper, userList);

        if (null != mActiveUser && userList.contains(mActiveUser)) {
            updateVideoAvatar(mActiveUser.getVideoStatus().isOn());
//            if (renderType == RENDER_TYPE_ZOOMRENDERER) {
//                if (null==currentShareUser&&mActiveUser.getVideoStatus().isOn()) {
//                    subscribeVideoByUser(mActiveUser);
//                    adapter.notifyDataSetChanged();
//                }
//            }
        }
    }

    @Override
    protected void onStartShareView() {
        super.onStartShareView();
        if (renderType == RENDER_TYPE_ZOOMRENDERER) {
            if (null != mActiveUser) {
                mActiveUser.getVideoCanvas().unSubscribe(zoomCanvas);
            }
        } else {
            rawDataRenderer.unSubscribe();
        }
        adapter.clear(false);
    }

    @Override
    public void onUserShareStatusChanged(ZoomInstantSDKShareHelper shareHelper, ZoomInstantSDKUser userInfo, ZoomInstantSDKShareStatus status) {
        super.onUserShareStatusChanged(shareHelper, userInfo, status);
        if (status == ZoomInstantSDKShareStatus.ZoomInstantSDKShareStatus_Start) {
            if (userInfo != session.getMySelf()) {
                subscribeShareByUser(userInfo);
                updateVideoAvatar(true);
                selectAndScrollToUser(userInfo);
            } else {
                if (!ZoomInstantSDK.getInstance().getShareHelper().isScreenSharingOut()) {
                    unSubscribe();
                    adapter.clear(false);
                }
            }
        } else if (status == ZoomInstantSDKShareStatus.ZoomInstantSDKShareStatus_Stop) {
            currentShareUser = null;
            subscribeVideoByUser(userInfo);
            if (adapter.getItemCount() == 0) {
                adapter.addAll();
            }
            selectAndScrollToUser(userInfo);
        }
    }



    @Override
    public void onBackPressed() {
    }

}
