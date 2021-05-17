package com.example.zoom;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zoom.screenshare.ShareToolbar;
import com.example.zoom.util.ErrorMsgUtil;
import com.example.zoom.util.ZMAdapterOsBugHelper;
import com.example.zoom.view.ChatMsgAdapter;
import com.example.zoom.view.KeyBoardLayout;
import com.example.zoom.view.UserVideoAdapter;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import us.zoom.sdk.ZoomInstantSDK;
import us.zoom.sdk.ZoomInstantSDKAudioHelper;
import us.zoom.sdk.ZoomInstantSDKAudioRawData;
import us.zoom.sdk.ZoomInstantSDKAudioStatus;
import us.zoom.sdk.ZoomInstantSDKChatHelper;
import us.zoom.sdk.ZoomInstantSDKChatMessage;
import us.zoom.sdk.ZoomInstantSDKDelegate;
import us.zoom.sdk.ZoomInstantSDKErrors;
import us.zoom.sdk.ZoomInstantSDKLiveStreamHelper;
import us.zoom.sdk.ZoomInstantSDKLiveStreamStatus;
import us.zoom.sdk.ZoomInstantSDKPasswordHandler;
import us.zoom.sdk.ZoomInstantSDKSession;
import us.zoom.sdk.ZoomInstantSDKShareHelper;
import us.zoom.sdk.ZoomInstantSDKShareStatus;
import us.zoom.sdk.ZoomInstantSDKUser;
import us.zoom.sdk.ZoomInstantSDKUserHelper;
import us.zoom.sdk.ZoomInstantSDKVideoHelper;
import us.zoom.sdk.ZoomInstantSDKVideoStatisticInfo;

public class BaseMeetingActivity extends AppCompatActivity implements ZoomInstantSDKDelegate, ShareToolbar.Listener, KeyBoardLayout.KeyBoardListener
        , UserVideoAdapter.ItemTapListener, ChatMsgAdapter.ItemClickListener {

    protected static final String TAG = BaseMeetingActivity.class.getSimpleName();
    public static final int RENDER_TYPE_ZOOMRENDERER = 0;
    public static final int RENDER_TYPE_OPENGLES = 1;
    public final static int REQUEST_SHARE_SCREEN_PERMISSION = 1001;
    public final static int REQUEST_SYSTEM_ALERT_WINDOW = 1002;
    public final static int REQUEST_SELECT_ORIGINAL_PIC = 1003;
    protected Display display;
    protected DisplayMetrics displayMetrics;
    protected RecyclerView userVideoList;
    protected LinearLayout videoListContain;
    protected UserVideoAdapter adapter;
    private Intent mScreenInfoData;
    protected ShareToolbar shareToolbar;
    protected ImageView iconShare;
    protected ImageView iconVideo;
    protected ImageView iconAudio;
    protected ImageView iconMore;
    protected TextView practiceText;
    protected TextView sessionNameText;
    protected TextView mtvInput;
    protected ImageView iconLock;
    protected View actionBar;
    protected ScrollView actionBarScroll;
    protected View btnViewShare;
    protected KeyBoardLayout keyBoardLayout;
    protected RecyclerView chatListView;
    private ChatMsgAdapter chatMsgAdapter;
    protected String myDisplayName = "";
    protected String meetingPwd = "";
    protected String sessionName;
    protected int renderType;
    protected ImageView videoOffView;
    private View shareViewGroup;
    private ImageView shareImageView;
    protected TextView text_fps;
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected boolean isActivityPaused = false;
    protected ZoomInstantSDKUser mActiveUser;
    protected ZoomInstantSDKUser currentShareUser;
    protected ZoomInstantSDKSession session;
    protected boolean renderWithSurfaceView=true;
    public Socket mSocket;
    //숨겨진 페이지가 열렸는지 확인하는 변수
    boolean isPageOpen=false;
    Animation tranlateLeftAnim;
    Animation tranlateRightAnim;
    //현재시각 가져오기
    String currenttime="";
    Button button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!renderWithSurfaceView) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        //소켓연결
        try{
            IO.Options opts =new IO.Options();
            opts.reconnection = true;
            opts.reconnectionDelay = 1000;
            opts.timeout = 10000;
            opts.transports = new String[]{WebSocket.NAME};
            mSocket = IO.socket(getString(R.string.app_domain)+":3000",opts);
        }catch (Exception e){
            Log.e("chaterror", "error "+e.toString());
        }
        mSocket.on(Socket.EVENT_CONNECT,Onconnect);
        mSocket.connect();

        //자막 애니메이션 연동

        tranlateLeftAnim= AnimationUtils.loadAnimation(this,R.anim.translate_left);
        tranlateRightAnim= AnimationUtils.loadAnimation(this,R.anim.translate_right);
        //페이지 슬라이딩 이벤트가 발생했을때 애니메이션이 시작 됐는지 종료 됐는지 감지할 수 있다.
        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
        tranlateLeftAnim.setAnimationListener(animListener);
        tranlateRightAnim.setAnimationListener(animListener);





        getWindow().addFlags(WindowManager.LayoutParams.
                FLAG_KEEP_SCREEN_ON);
        setContentView(getLayout());
        display = ((WindowManager) getSystemService(Service.WINDOW_SERVICE)).getDefaultDisplay();
        displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        session = ZoomInstantSDK.getInstance().getSession();
        ZoomInstantSDK.getInstance().addListener(this);
        parseIntent();
        initView();
        initMeeting();
        updateSessionInfo();
    }
    public void onclickcc(View v){

        LinearLayout page=(LinearLayout)findViewById(R.id.page);
        if(isPageOpen){
            page.startAnimation(tranlateRightAnim);
        }else{
            page.setVisibility(View.VISIBLE);
            page.startAnimation(tranlateLeftAnim);
        }
    }
    private Emitter.Listener Onconnect = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {
            Log.d("connect!!","connect!!");
        }
    };
    private class SlidingPageAnimationListener implements Animation.AnimationListener{
        @Override public void onAnimationStart(Animation animation) {

        }
        public void onAnimationEnd(Animation animation){
            LinearLayout page=(LinearLayout)findViewById(R.id.page);
            if(isPageOpen){
                page.setVisibility(View.INVISIBLE);
                //button.setText("열기");
                isPageOpen = false;
            }else{
                //button.setText("닫기");
                isPageOpen = true;
            }
        }
        @Override public void onAnimationRepeat(Animation animation) {
        } }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityPaused = true;
        unSubscribe();
        adapter.clear(false);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    protected void parseIntent() {
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            myDisplayName = bundle.getString("name");
            meetingPwd = bundle.getString("password");
            sessionName = bundle.getString("sessionName");
            renderType = bundle.getInt("render_type", RENDER_TYPE_ZOOMRENDERER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isActivityPaused) {
            resumeSubscribe();
        }
        isActivityPaused = false;
        refreshRotation();
        updateActionBarLayoutParams();
        updateChatLayoutParams();
    }

    protected void resumeSubscribe() {
        if (null != currentShareUser) {
            subscribeShareByUser(currentShareUser);
        } else if (null != mActiveUser) {
            subscribeVideoByUser(mActiveUser);
        }

        if (ZoomInstantSDK.getInstance().isInSession()) {
            List<ZoomInstantSDKUser> userInfoList = session.getAllUsers();
            if (null != userInfoList && userInfoList.size() > 0) {
                List<ZoomInstantSDKUser> list = new ArrayList<>(userInfoList.size());
                for (ZoomInstantSDKUser userInfo : userInfoList) {
                    list.add(userInfo);
                }
                adapter.onUserJoin(list);
                selectAndScrollToUser(mActiveUser);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateFpsOrientation();
        refreshRotation();
        updateActionBarLayoutParams();
        updateChatLayoutParams();
        updateSmallVideoLayoutParams();
    }

    private void updateFpsOrientation() {
        text_fps.setVisibility(View.GONE);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            text_fps = findViewById(R.id.text_fps_landscape);
        } else {
            text_fps = findViewById(R.id.text_fps);
        }
        if (ZoomInstantSDK.getInstance().isInSession()) {
            text_fps.setVisibility(View.VISIBLE);
        }
    }


    private void updateSmallVideoLayoutParams() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoListContain.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else {
            videoListContain.setGravity(Gravity.CENTER);
        }
    }

    private void updateChatLayoutParams() {
        if (chatMsgAdapter.getItemCount() > 0) {
            chatListView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
        }
    }

    private void updateActionBarLayoutParams() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) actionBar.getLayoutParams();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.topMargin = (int) (35 * displayMetrics.scaledDensity);
//            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//            params.bottomMargin = (int) (22 * displayMetrics.scaledDensity);
            actionBarScroll.scrollTo(0, 0);
        } else {
            params.topMargin = 0;
//            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.toolbar_bottom_margin);
        }
        actionBar.setLayoutParams(params);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != shareToolbar) {
            shareToolbar.destroy();
        }
        if (ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ZMAdapterOsBugHelper.getInstance().stopListenOverlayPermissionChange(this);
            }
        }
        ZoomInstantSDK.getInstance().removeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SHARE_SCREEN_PERMISSION:
                if (resultCode != RESULT_OK) {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "onActivityResult REQUEST_SHARE_SCREEN_PERMISSION no ok ");
                    break;
                }
                startShareScreen(data);
                break;
            case REQUEST_SYSTEM_ALERT_WINDOW:
                if (ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ZMAdapterOsBugHelper.getInstance().stopListenOverlayPermissionChange(this);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if ((!Settings.canDrawOverlays(this)) && (!ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged() || !ZMAdapterOsBugHelper.getInstance().ismCanDraw())) {
                        return;
                    }
                }
                onStartShareScreen(mScreenInfoData);
                break;
            case REQUEST_SELECT_ORIGINAL_PIC: {
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = data.getData();
                        if (null != selectedImage) {
                            if (currentShareUser == null) {
                                shareImageView.setImageURI(selectedImage);
                                shareViewGroup.setVisibility(View.VISIBLE);
                                int ret = ZoomInstantSDK.getInstance().getShareHelper().startShareView(shareImageView);
                                Log.d(TAG, "start share " + ret);
                                if (ret == ZoomInstantSDKErrors.Errors_Success) {
                                    onStartShareView();
                                } else {
                                    shareImageView.setImageBitmap(null);
                                    shareViewGroup.setVisibility(View.GONE);
                                    boolean isLocked = ZoomInstantSDK.getInstance().getShareHelper().isShareLocked();
                                    Toast.makeText(this, "Share Fail isLocked=" + isLocked + " ret:" + ret, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Other is sharing", Toast.LENGTH_LONG).show();
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                break;
            }
        }
    }

    protected void onStartShareView() {

    }

    public void onClickStopShare(View view) {
        ZoomInstantSDK.getInstance().getShareHelper().stopShare();
    }

    public void onSingleTap(ZoomInstantSDKUser user) {
//        if (user != mActiveUser) {
        subscribeVideoByUser(user);
//        }
    }

    @Override
    public void onKeyBoardChange(boolean isShow, int height, int inputHeight) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) chatListView.getLayoutParams();

        if (isShow) {
            params.gravity = Gravity.START | Gravity.BOTTOM;
            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp_13) + height + inputHeight;
        } else {
            params.gravity = Gravity.START | Gravity.BOTTOM;
            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp_160);
        }
        chatListView.setLayoutParams(params);
        if (chatMsgAdapter.getItemCount() > 0) {
            chatListView.scrollToPosition(chatMsgAdapter.getItemCount() - 1);
        }
    }

    protected void onStartShareScreen(Intent data) {
        if (null == shareToolbar) {
            shareToolbar = new ShareToolbar(this,this);
        }
        if (Build.VERSION.SDK_INT >= 29) {
            //MediaProjection  need service with foregroundServiceType mediaProjection in android Q
            boolean hasForegroundNotification = NotificationMgr.hasNotification(NotificationMgr.PT_NOTICICATION_ID);
            if (!hasForegroundNotification) {
                Intent intent = new Intent(this, NotificationService.class);
                startForegroundService(intent);
            }
        }
        int ret = ZoomInstantSDK.getInstance().getShareHelper().startShareScreen(data);
        if (ret == ZoomInstantSDKErrors.Errors_Success) {
            shareToolbar.showToolbar();
            showDesktop();
        }
    }

    protected void showDesktop() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(home);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onClickStopShare() {
        if (ZoomInstantSDK.getInstance().getShareHelper().isSharingOut()) {
            ZoomInstantSDK.getInstance().getShareHelper().stopShare();
            showMeetingActivity();
        }
    }

    private void showMeetingActivity() {
        Intent intent = new Intent(getApplicationContext(), IntegrationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setAction(IntegrationActivity.ACTION_RETURN_TO_CONF);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @SuppressLint("NewApi")
    protected void startShareScreen(Intent data) {
        if (data == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 24 && !Settings.canDrawOverlays(this)) {
            if (ZMAdapterOsBugHelper.getInstance().isNeedListenOverlayPermissionChanged())
                ZMAdapterOsBugHelper.getInstance().startListenOverlayPermissionChange(this);
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            mScreenInfoData = data;
            startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW);
        } else {
            onStartShareScreen(data);
        }
    }

    protected void refreshRotation() {
        int displayRotation = display.getRotation();
        Log.d(TAG, "rotateVideo:" + displayRotation);
        ZoomInstantSDK.getInstance().getVideoHelper().rotateMyVideo(displayRotation);
    }

    protected void initMeeting() {

    }

    public void updateFps(final ZoomInstantSDKVideoStatisticInfo statisticInfo) {
        if (null == statisticInfo) {
            return;
        }
        final int fps = statisticInfo.getFps();
        text_fps.post(new Runnable() {
            @Override
            public void run() {
                if (statisticInfo.getWidth() > 0 && statisticInfo.getHeight() > 0) {
                    text_fps.setVisibility(View.VISIBLE);
                    String text = statisticInfo.getWidth() + "X" + statisticInfo.getHeight() + " " + fps + " FPS";
                    if (fps < 10) {
                        text = statisticInfo.getWidth() + "X" + statisticInfo.getHeight() + "  " + fps + " FPS";
                    }
                    text_fps.setText(text);
                } else {
                    text_fps.setVisibility(View.GONE);
                }
            }
        });
    }


    protected void initView() {
        sessionNameText = findViewById(R.id.sessionName);
        mtvInput = findViewById(R.id.tv_input);
        userVideoList = findViewById(R.id.userVideoList);
        videoListContain = findViewById(R.id.video_list_contain);
        adapter = new UserVideoAdapter(this, this, renderType);
        userVideoList.setItemViewCacheSize(0);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        layoutManager.setItemPrefetchEnabled(false);
        userVideoList.setLayoutManager(layoutManager);
        userVideoList.setAdapter(adapter);

        text_fps = findViewById(R.id.text_fps);

        iconVideo = findViewById(R.id.icon_video);
        iconAudio = findViewById(R.id.icon_audio);
        iconMore = findViewById(R.id.icon_more);
        practiceText = findViewById(R.id.text_meeting_user_size);

        keyBoardLayout = findViewById(R.id.chat_input_layout);

        chatListView = findViewById(R.id.chat_list);

        chatListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        chatMsgAdapter = new ChatMsgAdapter(this);
        chatListView.setAdapter(chatMsgAdapter);

        keyBoardLayout.setKeyBoardListener(this);
        actionBar = findViewById(R.id.action_bar);

        iconLock = findViewById(R.id.meeting_lock_status);

        iconShare = findViewById(R.id.icon_share);
        actionBarScroll = findViewById(R.id.action_bar_scroll);

        videoOffView = findViewById(R.id.video_off_tips);

        btnViewShare = findViewById(R.id.btn_view_share);

        shareViewGroup = findViewById(R.id.share_view_group);
        shareImageView = findViewById(R.id.share_image);
        onKeyBoardChange(false, 0, 30);
        final int margin = (int) (5 * displayMetrics.scaledDensity);
        userVideoList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(margin, 0, margin, 0);
            }
        });

        userVideoList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
                    View view = linearLayoutManager.getChildAt(0);
                    if (null == view) {
                        return;
                    }
                    int index = linearLayoutManager.findFirstVisibleItemPosition();
                    int left = view.getLeft();
                    if (left < 0) {
                        if (-left > view.getWidth() / 2) {
                            index = index + 1;
                            if (index == adapter.getItemCount() - 1) {
                                recyclerView.scrollBy(view.getWidth(), 0);
                            } else {
                                recyclerView.scrollBy(view.getWidth() + left + 2 * margin, 0);
                            }
                        } else {
                            recyclerView.scrollBy(left - margin, 0);
                        }
                        if (index == 0) {
                            recyclerView.scrollTo(0, 0);
                        }
                    }
                    view = linearLayoutManager.getChildAt(0);
                    if (null == view) {
                        return;
                    }
                    scrollVideoViewForMargin(view);

                }
            }
        });
    }

    @Override
    public void onItemClick() {

    }

    public void onClickSwitchShare(View view) {

    }

    protected int getLayout() {
        return 0;
    }

    public void onClickInfo(View view) {
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.dialog_session_info);

        final TextView sessionNameText = builder.findViewById(R.id.info_session_name);
        final TextView sessionPwdText = builder.findViewById(R.id.info_session_pwd);
        final TextView sessionUserSizeText = builder.findViewById(R.id.info_user_size);
        int size = session.getAllUsers().size();
        if (size <= 0) {
            size = 1;
        }
        sessionUserSizeText.setText(size + "");

        ZoomInstantSDKSession sessionInfo = ZoomInstantSDK.getInstance().getSession();
        meetingPwd = sessionInfo.getSessionPassword();
        sessionPwdText.setText(meetingPwd);

        if (TextUtils.isEmpty(meetingPwd)) {
            sessionPwdText.setText("Not set");
            sessionPwdText.setTextColor(getResources().getColor(R.color.color_not_set));
        }

        String name = sessionInfo.getSessionName();
        if (null == name) {
            name = "";
        }
        sessionNameText.setText(name);
        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.show();


    }

    public void onClickEnd(View view) {
        ZoomInstantSDKUser userInfo = session.getMySelf();

        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.setContentView(R.layout.dialog_leave_alert);
        builder.findViewById(R.id.btn_leave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
                releaseResource();
                int ret = ZoomInstantSDK.getInstance().leaveSession(false);
                Log.d(TAG, "leaveSession ret = " + ret);
            }
        });

        boolean end = false;
        if (null != userInfo && userInfo.isHost()) {
            ((TextView) builder.findViewById(R.id.btn_end)).setText(getString(R.string.leave_end_text));
            end = true;
        }
        final boolean endSession = end;
        builder.findViewById(R.id.btn_end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
                if (endSession) {
                    releaseResource();
                    int ret = ZoomInstantSDK.getInstance().leaveSession(true);
                    Log.d(TAG, "leaveSession ret = " + ret);
                }
            }
        });
        builder.show();

    }

    private void releaseResource() {
        unSubscribe();
        adapter.clear(true);
        actionBar.setVisibility(View.GONE);
        mtvInput.setVisibility(View.GONE);
    }

    public void onClickVideo(View view) {
        ZoomInstantSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        if (zoomSDKUserInfo.getVideoStatus().isOn()) {
            ZoomInstantSDK.getInstance().getVideoHelper().stopVideo();
        } else {
            ZoomInstantSDK.getInstance().getVideoHelper().startVideo();
        }
    }


    public void onClickShare(View view) {
        ZoomInstantSDKShareHelper sdkShareHelper = ZoomInstantSDK.getInstance().getShareHelper();

        boolean isShareLocked = sdkShareHelper.isShareLocked();
        if (isShareLocked && !session.getMySelf().isHost()) {
            Toast.makeText(this, "Share is locked by host", Toast.LENGTH_SHORT).show();
            return;
        }

        if (null != currentShareUser && currentShareUser != session.getMySelf()) {
            Toast.makeText(this, "Other is shareing", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentShareUser == session.getMySelf()) {
            sdkShareHelper.stopShare();
            return;
        }

        final Dialog builder = new Dialog(this, R.style.MyDialog);

        builder.setContentView(R.layout.dialog_share_view);
        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.findViewById(R.id.group_screen_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
                if (ZoomInstantSDK.getInstance().getShareHelper().isSharingOut()) {
                    ZoomInstantSDK.getInstance().getShareHelper().stopShare();
                    if (null != shareToolbar) {
                        shareToolbar.destroy();
                    }
                } else {
                    askScreenSharePermission();
                }
            }
        });

        builder.findViewById(R.id.group_picture_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFromGallery();
                builder.dismiss();
            }
        });
        builder.show();
    }


    private void selectFromGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_SELECT_ORIGINAL_PIC);
    }

    protected void toggleView(boolean show) {

    }

    public void onClickChat(View view) {
        keyBoardLayout.showChat();
        toggleView(true);
    }

    public void onClickAudio(View view) {
        ZoomInstantSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        if (zoomSDKUserInfo.getAudioStatus().getAudioType() == ZoomInstantSDKAudioStatus.ZoomInstantSDKAudioType.ZoomInstantSDKAudioType_None) {
            ZoomInstantSDK.getInstance().getAudioHelper().startAudio();
        } else {
            if (zoomSDKUserInfo.getAudioStatus().isMuted()) {
                ZoomInstantSDK.getInstance().getAudioHelper().unMuteAudio(zoomSDKUserInfo);
            } else {
                ZoomInstantSDK.getInstance().getAudioHelper().muteAudio(zoomSDKUserInfo);
            }
        }
    }

    public void onClickMoreSpeaker() {
        boolean speaker = ZoomInstantSDK.getInstance().getAudioHelper().getSpeakerStatus();
        ZoomInstantSDK.getInstance().getAudioHelper().setSpeaker(!speaker);

    }

    public void onClickMoreSwitchCamera() {
        ZoomInstantSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        if (zoomSDKUserInfo.getVideoStatus().isHasVideoDevice() && zoomSDKUserInfo.getVideoStatus().isOn()) {
            ZoomInstantSDK.getInstance().getVideoHelper().switchCamera();
            refreshRotation();
        }
    }

    private boolean isSpeakerOn() {
        return ZoomInstantSDK.getInstance().getAudioHelper().getSpeakerStatus();
    }

    public void onClickMore(View view) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("추가 항목을 선택하세요"); //제목
        final String[] versionArray = new String[] {"참가자","채팅","퀴즈","질문"};
        dlg.setIcon(R.drawable.loggo_prev); // 아이콘 설정

        dlg.setItems(versionArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               Log.d("input",versionArray[which]);
               //채팅
               if(which==1){
                   Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
                   startActivity(intent);
               }else if(which==3){
                   Intent intent=new Intent(getApplicationContext(),QuestionActivity.class);
                   startActivity(intent);
               }
               //Intent intent=new Intent(getApplicationContext(),SignUp_Student.class);
               //startActivity(intent);
            }
        });
//                버튼 클릭시 동작
        dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                //토스트 메시지
                Toast.makeText(getApplicationContext(),"확인을 눌르셨습니다.",Toast.LENGTH_SHORT).show();
            }
        });
        dlg.show();
        /*
        ZoomInstantSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.dialog_more_action);

        final View llSwitchCamera = builder.findViewById(R.id.llSwitchCamera);
        final View llSpeaker = builder.findViewById(R.id.llSpeaker);
        final TextView tvSpeaker = builder.findViewById(R.id.tvSpeaker);
        final ImageView ivSpeaker = builder.findViewById(R.id.ivSpeaker);

        boolean hasLast = false;
        if (zoomSDKUserInfo.getVideoStatus().isOn()) {
            llSwitchCamera.setVisibility(View.VISIBLE);
            llSwitchCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    builder.dismiss();
                    onClickMoreSwitchCamera();
                }
            });
            hasLast = true;
        } else {
            llSwitchCamera.setVisibility(View.GONE);
        }
        if (canSwitchAudioSource()) {
            llSpeaker.setVisibility(View.VISIBLE);
            llSpeaker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    builder.dismiss();
                    onClickMoreSpeaker();
                }
            });
            if (!hasLast) {
                hasLast = true;
                llSpeaker.setBackground(getResources().getDrawable(R.drawable.more_action_last_bg));
            }
        } else {
            llSpeaker.setVisibility(View.GONE);
        }

        if (!hasLast) {
            return;
        }
        if (isSpeakerOn()) {
            tvSpeaker.setText("Turn off Speaker");
            ivSpeaker.setImageResource(R.drawable.icon_speaker_off);
        } else {
            tvSpeaker.setText("Turn on Speaker");
            ivSpeaker.setImageResource(R.drawable.icon_speaker_on);
        }

        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.show();*/
    }


    private void checkMoreAction() {
        ZoomInstantSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null == zoomSDKUserInfo)
            return;
        if (!zoomSDKUserInfo.getVideoStatus().isOn() && !canSwitchAudioSource()) {
            iconMore.setVisibility(View.GONE);
        } else {
            iconMore.setVisibility(View.VISIBLE);
        }
    }

    private boolean canSwitchAudioSource() {
        return ZoomInstantSDK.getInstance().getAudioHelper().canSwitchSpeaker();
    }

    @SuppressLint("NewApi")
    protected void askScreenSharePermission() {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        if (ZoomInstantSDK.getInstance().getShareHelper().isSharingOut()) {
            return;
        }
        MediaProjectionManager mgr = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mgr != null) {
            Intent intent = mgr.createScreenCaptureIntent();
            try {
                startActivityForResult(intent, REQUEST_SHARE_SCREEN_PERMISSION);
            } catch (Exception e) {
                Log.e(TAG, "askScreenSharePermission failed");
            }
        }
    }

    protected void updateSessionInfo() {
        ZoomInstantSDKSession sessionInfo = ZoomInstantSDK.getInstance().getSession();
        if (ZoomInstantSDK.getInstance().isInSession()) {
            int size = sessionInfo.getAllUsers().size();
            if (size <= 0) {
                size = 1;
            }
            practiceText.setText("Participants:" + size);
            if (sessionInfo != null) meetingPwd = sessionInfo.getSessionPassword();
            mtvInput.setVisibility(View.VISIBLE);
            text_fps.setVisibility(View.VISIBLE);
        } else {
            if (keyBoardLayout.isKeyBoardShow()) {
                keyBoardLayout.dismissChat(true);
                return;

            }
            actionBar.setVisibility(View.GONE);
            mtvInput.setVisibility(View.GONE);
            text_fps.setVisibility(View.GONE);
            practiceText.setText("Connecting ...");
        }
        if (sessionInfo != null) sessionNameText.setText(sessionInfo.getSessionName());
        if (TextUtils.isEmpty(meetingPwd)) {
            iconLock.setImageResource(R.drawable.unlock);
        } else {
            iconLock.setImageResource(R.drawable.small_lock);
        }
    }


    protected void unSubscribe() {

    }

    @Override
    public void onSessionJoin() {
        Log.d(TAG, "onSessionJoin ");
        updateSessionInfo();
        updateFpsOrientation();
        actionBar.setVisibility(View.VISIBLE);
        if (ZoomInstantSDK.getInstance().getShareHelper().isSharingOut()) {
            ZoomInstantSDK.getInstance().getShareHelper().stopShare();
        }

        adapter.onUserJoin(session.getAllUsers());
        refreshUserListAdapter();
        mtvInput.setVisibility(View.VISIBLE);
    }


    @Override
    public void onSessionLeave() {
        Log.d(TAG, "onSessionLeave");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onError(int errorcode) {
        Toast.makeText(this, ErrorMsgUtil.getMsgByErrorCode(errorcode) + ". Error code: "+errorcode, Toast.LENGTH_LONG).show();
        if (errorcode == ZoomInstantSDKErrors.Errors_Session_Disconnect) {
            unSubscribe();
            adapter.clear(true);
            updateSessionInfo();
            currentShareUser = null;
            mActiveUser=null;
            chatMsgAdapter.clear();
            chatListView.setVisibility(View.GONE);
            btnViewShare.setVisibility(View.GONE);
        } else if (errorcode == ZoomInstantSDKErrors.Errors_Session_Reconncting) {
            //start preview
            subscribeVideoByUser(session.getMySelf());
        } else {
            ZoomInstantSDK.getInstance().leaveSession(false);
            finish();
        }

    }

    protected void subscribeVideoByUser(ZoomInstantSDKUser user) {

    }

    protected void subscribeShareByUser(ZoomInstantSDKUser user) {

    }

    @Override
    public void onUserJoin(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {

        Log.d(TAG, "onUserJoin " + userList.size());
        updateVideoListLayout();
        if (!isActivityPaused) {
            adapter.onUserJoin(userList);
        }
        refreshUserListAdapter();
        updateSessionInfo();
    }

    protected void selectAndScrollToUser(ZoomInstantSDKUser user) {
        if (null == user) {
            return;
        }
        adapter.updateSelectedVideoUser(user);
        int index = adapter.getIndexByUser(user);
        if (index >= 0) {
            LinearLayoutManager manager = (LinearLayoutManager) userVideoList.getLayoutManager();
            int first = manager.findFirstVisibleItemPosition();
            int last = manager.findLastVisibleItemPosition();
            if (index > last || index < first) {
                userVideoList.scrollToPosition(index);
                adapter.notifyDataSetChanged();
            }
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
        View view = linearLayoutManager.getChildAt(0);
        if (null != view) {
            scrollVideoViewForMargin(view);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) userVideoList.getLayoutManager();
                    View view = linearLayoutManager.getChildAt(0);
                    scrollVideoViewForMargin(view);
                }
            }, 50);
        }
    }

    private void scrollVideoViewForMargin(View view) {
        if (null == view) {
            return;
        }
        int left = view.getLeft();
        int margin = 5;
        if (left > margin || left <= 0) {
            userVideoList.scrollBy(left - margin, 0);
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "left:" + left + " view left:" + view.getLeft());
        }
    }

    private void refreshUserListAdapter() {
        if (adapter.getItemCount() > 0) {
            videoListContain.setVisibility(View.VISIBLE);
            if (adapter.getSelectedVideoUser() == null) {
                ZoomInstantSDKUser zoomSDKUserInfo = session.getMySelf();
                if (null != zoomSDKUserInfo) {
                    selectAndScrollToUser(zoomSDKUserInfo);
                }
            }
        }
    }

    private void updateVideoListLayout() {
        int size = session.getAllUsers().size();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) userVideoList.getLayoutParams();
        int preWidth = params.width;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (size - 1 >= 3) {
            int maxWidth = (int) (325 * displayMetrics.scaledDensity);
            width = maxWidth;
        }
        if (width != preWidth) {
            params.width = width;
            userVideoList.setLayoutParams(params);
        }
    }

    @Override
    public void onUserLeave(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {
        updateVideoListLayout();
        Log.d(TAG, "onUserLeave " + userList.size());
        adapter.onUserLeave(userList);
        if (adapter.getItemCount() == 0) {
            videoListContain.setVisibility(View.INVISIBLE);
        }
        updateSessionInfo();
    }

    @Override
    public void onUserVideoStatusChanged(ZoomInstantSDKVideoHelper videoHelper, List<ZoomInstantSDKUser> userList) {
        Log.d(TAG, "onUserVideoStatusChanged ");
        if (null == iconVideo) {
            return;
        }

        ZoomInstantSDKUser zoomSDKUserInfo = session.getMySelf();
        if (null != zoomSDKUserInfo) {
            iconVideo.setImageResource(zoomSDKUserInfo.getVideoStatus().isOn() ? R.drawable.icon_video_off : R.drawable.icon_video_on);
            if (userList.contains(zoomSDKUserInfo)) {
                checkMoreAction();
            }
        }
        adapter.onUserVideoStatusChanged(userList);
    }

    @Override
    public void onUserAudioStatusChanged(ZoomInstantSDKAudioHelper audioHelper, List<ZoomInstantSDKUser> userList) {
        ZoomInstantSDKUser zoomSDKUserInfo = session.getMySelf();
        if (zoomSDKUserInfo != null && userList.contains(zoomSDKUserInfo)) {
            if (zoomSDKUserInfo.getAudioStatus().getAudioType() == ZoomInstantSDKAudioStatus.ZoomInstantSDKAudioType.ZoomInstantSDKAudioType_None) {
                iconAudio.setImageResource(R.drawable.icon_join_audio);
            } else {
                if (zoomSDKUserInfo.getAudioStatus().isMuted()) {
                    iconAudio.setImageResource(R.drawable.icon_unmute);
                } else {
                    iconAudio.setImageResource(R.drawable.icon_mute);
                }
            }
            checkMoreAction();
        }
    }

    @Override
    public void onUserShareStatusChanged(ZoomInstantSDKShareHelper shareHelper, ZoomInstantSDKUser userInfo, ZoomInstantSDKShareStatus status) {
        Log.d(TAG, "onUserShareStatusChanged " + userInfo.getUserId() + ":" + status);
        if (status == ZoomInstantSDKShareStatus.ZoomInstantSDKShareStatus_Start) {
            currentShareUser = userInfo;
            if (userInfo== session.getMySelf()) {
                iconShare.setImageResource(R.drawable.icon_stop_share);
            }
        } else if (status == ZoomInstantSDKShareStatus.ZoomInstantSDKShareStatus_Stop) {
            currentShareUser = null;
            iconShare.setImageResource(R.drawable.icon_share);
            shareViewGroup.setVisibility(View.GONE);
            if (null != shareToolbar) {
                shareToolbar.destroy();
            }
        }
    }

    @Override
    public void onLiveStreamStatusChanged(ZoomInstantSDKLiveStreamHelper liveStreamHelper, ZoomInstantSDKLiveStreamStatus status) {

    }

    @Override
    public void onChatNewMessageNotify(ZoomInstantSDKChatHelper chatHelper, ZoomInstantSDKChatMessage messageItem) {
        chatMsgAdapter.onReceive(messageItem);

        updateChatLayoutParams();
    }

    @Override
    public void onUserHostChanged(ZoomInstantSDKUserHelper userHelper, ZoomInstantSDKUser userInfo) {

    }


    @Override
    public void onSessionNeedPassword(ZoomInstantSDKPasswordHandler handler) {
        Log.d(TAG, "onSessionNeedPassword ");
        showInputPwdDialog(handler);
    }

    private void showInputPwdDialog(final ZoomInstantSDKPasswordHandler handler) {
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.dialog_session_input_pwd);
        builder.setCancelable(false);
        builder.setCanceledOnTouchOutside(false);
        final EditText editText = builder.findViewById(R.id.edit_pwd);
        builder.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd = editText.getText().toString();
                if (!TextUtils.isEmpty(pwd)) {
                    handler.inputSessionPassword(pwd);
                    builder.dismiss();
                }
            }
        });

        builder.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.leaveSessionIgnorePassword();
                builder.dismiss();
            }
        });

        builder.show();
    }


    @Override
    public void onSessionPasswordWrong(ZoomInstantSDKPasswordHandler handler) {
        Log.d(TAG, "onSessionPasswordWrong ");
        Toast.makeText(this, "Password wrong", Toast.LENGTH_LONG).show();
        showInputPwdDialog(handler);
    }

    @Override
    public void onUserActiveAudioChanged(ZoomInstantSDKAudioHelper audioHelper, List<ZoomInstantSDKUser> list) {
//        Log.d(TAG, "onUserActiveAudioChanged " + list);
        adapter.onUserActiveAudioChanged(list, userVideoList);
    }

    @Override
    public void onMixedAudioRawDataReceived(ZoomInstantSDKAudioRawData rawData) {

    }

    @Override
    public void onOneWayAudioRawDataReceived(ZoomInstantSDKAudioRawData rawData, ZoomInstantSDKUser user) {

    }

    @Override
    public void onUserManagerChanged(ZoomInstantSDKUser user) {
        Log.d(TAG,"onUserManagerChanged:"+user);
    }

    @Override
    public void onUserNameChanged(ZoomInstantSDKUser user) {
        Log.d(TAG,"onUserNameChanged:"+user);
        
    }
    public void onclickunderstandok(View view){
        AlertDialog.Builder ad = new AlertDialog.Builder(BaseMeetingActivity.this);

        ad.setMessage("'이해가 잘되요' 평가를 전달하겠습니까?");


        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //현재시각 가져오기
                long now=System.currentTimeMillis();
                Date mDate=new Date(now);
                SimpleDateFormat simpleDate=new SimpleDateFormat("hh:mm");
                currenttime=simpleDate.format(mDate);
                JSONObject input_data=new JSONObject();
                try {
                    input_data.put("time",currenttime);
                    input_data.put("name","test");
                    input_data.put("type","up");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("understandingStu",input_data);
                Log.d("understand","currenttime : "+currenttime);
                dialog.dismiss();
            }
        });


        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }
    public void onclickunderstandnot(View view){
        AlertDialog.Builder ad = new AlertDialog.Builder(BaseMeetingActivity.this);
        ad.setIcon(R.drawable.loggo_small);
        ad.setMessage("'이해가 안되요' 평가를 전달하겠습니까?");


        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //현재시각 가져오기
                long now=System.currentTimeMillis();
                Date mDate=new Date(now);
                SimpleDateFormat simpleDate=new SimpleDateFormat("hh:mm");
                currenttime=simpleDate.format(mDate);
                JSONObject input_data=new JSONObject();
                try {
                    input_data.put("time",currenttime);
                    input_data.put("name","test");
                    input_data.put("type","down");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("understandingStu",input_data);
                dialog.dismiss();
            }
        });


        ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }
    public void SearchOnclick(View v){
        EditText txt=(EditText)findViewById(R.id.Search_txt);
        txt.setText("");
    }
}
