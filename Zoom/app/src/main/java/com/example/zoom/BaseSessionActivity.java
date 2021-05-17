package com.example.zoom;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.zoom.util.JWTUtil;
import com.example.zoom.util.NetworkUtil;

import java.util.List;
import java.util.Random;

import us.zoom.sdk.ZoomInstantSDK;
import us.zoom.sdk.ZoomInstantSDKAudioHelper;
import us.zoom.sdk.ZoomInstantSDKAudioOption;
import us.zoom.sdk.ZoomInstantSDKAudioRawData;
import us.zoom.sdk.ZoomInstantSDKChatHelper;
import us.zoom.sdk.ZoomInstantSDKChatMessage;
import us.zoom.sdk.ZoomInstantSDKDelegate;
import us.zoom.sdk.ZoomInstantSDKLiveStreamHelper;
import us.zoom.sdk.ZoomInstantSDKLiveStreamStatus;
import us.zoom.sdk.ZoomInstantSDKPasswordHandler;
import us.zoom.sdk.ZoomInstantSDKSession;
import us.zoom.sdk.ZoomInstantSDKSessionContext;
import us.zoom.sdk.ZoomInstantSDKShareHelper;
import us.zoom.sdk.ZoomInstantSDKShareStatus;
import us.zoom.sdk.ZoomInstantSDKUser;
import us.zoom.sdk.ZoomInstantSDKUserHelper;
import us.zoom.sdk.ZoomInstantSDKVideoHelper;
import us.zoom.sdk.ZoomInstantSDKVideoOption;

import static com.example.zoom.BaseMeetingActivity.RENDER_TYPE_OPENGLES;
import static com.example.zoom.BaseMeetingActivity.RENDER_TYPE_ZOOMRENDERER;

public class BaseSessionActivity extends AppCompatActivity implements View.OnClickListener, ZoomInstantSDKDelegate {

    protected String[] defaultNameList = {"Grand Canyon", "Yosemite", "Yellowstone", "Disneyland", "Golden Gate Bridge", "Monument Valley", "Death Valley", "Brooklyn Bridge",
            "Hoover Dam", "Lake Tahoe"};


    protected final static int REQUEST_VIDEO_AUDIO_CODE = 1010;

    protected static final String TAG = "BaseSessionActivity";

    protected TextView leftView;

    protected TextView titleTextView;

    protected Button btnJoin;
    protected View btnCopy;

    protected EditText sessionEditText;

    protected TextView nameEdit;

    protected EditText passwordEdit;
    protected TextView mTvRenderer;

    protected int renderType = RENDER_TYPE_ZOOMRENDERER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getIntent().getExtras()) {
            renderType = getIntent().getIntExtra("render_type", RENDER_TYPE_ZOOMRENDERER);
        }
        setContentView(R.layout.activity_session);
        init();
    }

    protected String getDefaultSessionName() {
        Random rand = new Random();
        int index = rand.nextInt(defaultNameList.length);
        return defaultNameList[index];
    }

    protected void init() {

        leftView = findViewById(R.id.tvBack);

        titleTextView = findViewById(R.id.title);
        mTvRenderer = findViewById(R.id.tvRenderer);

        if (null != leftView) {
            leftView.setOnClickListener(this);
        }

        sessionEditText = findViewById(R.id.session_edit);

        String defaultSessionName = getDefaultSessionName().toLowerCase();
        sessionEditText.setText(defaultSessionName);

        sessionEditText.setSelection(0, sessionEditText.getText().length());
        sessionEditText.setTransformationMethod(new ReplacementTransformationMethod() {
            @NonNull
            private char[] lower = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            @NonNull
            private char[] upper = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

            @NonNull
            @Override
            protected char[] getOriginal() {
                return upper;
            }

            @NonNull
            @Override
            protected char[] getReplacement() {
                return lower;
            }
        });

        nameEdit = findViewById(R.id.userName_edit);
        btnCopy = findViewById(R.id.btn_copy);
        if (null != nameEdit) {
            nameEdit.setText(Build.MODEL + "-" + Build.VERSION.SDK_INT);
        }
        passwordEdit = findViewById(R.id.password_edit);
        btnJoin = findViewById(R.id.btn_join);

        updateJoinButton();
        sessionEditText.addTextChangedListener(textWatcher);
        nameEdit.addTextChangedListener(textWatcher);

        sessionEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                if (result == EditorInfo.IME_ACTION_DONE) {
                    joinOrCreateSession();
                }
                return false;
            }
        });


        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                onPasswordChanged();
            }
        });
        updateSelectedRenderer();
        if (null != ZoomInstantSDK.getInstance()) {
            ZoomInstantSDK.getInstance().addListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionEditText.setSelection(0, sessionEditText.getText().length());
        sessionEditText.requestFocus();
        hasInJoinorCreate = false;
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            updateJoinButton();
        }
    };

    protected void onPasswordChanged() {
    }

    private void updateJoinButton() {
        if (TextUtils.isEmpty(sessionEditText.getText().toString())) {
            btnCopy.setVisibility(View.GONE);
            btnJoin.setEnabled(false);
            return;
        }
        btnCopy.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(nameEdit.getText().toString())) {
            btnJoin.setEnabled(false);
        } else {
            btnJoin.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != ZoomInstantSDK.getInstance()) {
            ZoomInstantSDK.getInstance().removeListener(this);
        }

    }

    protected void setHeadTile(int id) {
        if (null != titleTextView && id > 0) {
            titleTextView.setText(id);
        }
    }

    protected boolean requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_VIDEO_AUDIO_CODE);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_VIDEO_AUDIO_CODE) {
            if (Build.VERSION.SDK_INT >= 23 && (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {

                onPermissionGranted();
            }
        }
    }

    protected void onPermissionGranted() {
        joinOrCreateSession();
    }

    private boolean hasInJoinorCreate = false;

    protected void joinOrCreateSession() {
        if (hasInJoinorCreate)
            return;
        if (!requestPermission())
            return;
        if (!NetworkUtil.hasDataNetwork(this)) {
            Toast.makeText(this, "Connection Failed. Please check your network connection and try again.", Toast.LENGTH_LONG).show();
            return;
        }

        if (null == ZoomInstantSDK.getInstance()) {
            Toast.makeText(this, "Please initialize SDK", Toast.LENGTH_LONG).show();
            return;
        }

        String sessionName = sessionEditText.getText().toString().toLowerCase();

        if (TextUtils.isEmpty(sessionName)) {
            Toast.makeText(this, "Session name is empty", Toast.LENGTH_LONG).show();
            return;
        }

        ZoomInstantSDKSessionContext sessionContext = new ZoomInstantSDKSessionContext();

        ZoomInstantSDKAudioOption audioOption = new ZoomInstantSDKAudioOption();
        audioOption.connect = true;
        audioOption.mute = false;
        sessionContext.audioOption = audioOption;

        ZoomInstantSDKVideoOption videoOption = new ZoomInstantSDKVideoOption();
        videoOption.localVideoOn = true;
        sessionContext.videoOption = videoOption;

        String name = nameEdit.getText().toString();
        if (TextUtils.isEmpty(name)) {
            name = Build.MODEL;
        }

        String password = passwordEdit.getText().toString();

        //Required
        sessionContext.sessionName = sessionName;
        sessionContext.userName = name;

        //GET START
        String token = Constants.TOKEN;
        if (TextUtils.isEmpty(token)) {
            token = JWTUtil.createJWTAccessToken("harry");
        }

        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Token is empty", Toast.LENGTH_LONG).show();
            return;
        }
        //GET END
        sessionContext.token = token;

        //Optional
        sessionContext.sessionPassword = password;

//        sessionContext.externalVideoSource=new VirtualVideoSource();

        ZoomInstantSDKSession session = ZoomInstantSDK.getInstance().joinSession(sessionContext);
        if(null==session){
            return;
        }
        hasInJoinorCreate = true;
        Intent intent = new Intent(this, MeetingActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("password", password);
        intent.putExtra("sessionName", sessionName);
        intent.putExtra("render_type", renderType);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v == leftView) {
            onBackPressed();
        }
    }


    @Override
    public void onSessionJoin() {

    }

    @Override
    public void onSessionLeave() {

    }

    @Override
    public void onError(int errorcode) {
        Toast.makeText(this, "Session error:"+errorcode, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onUserJoin(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {

    }

    @Override
    public void onUserLeave(ZoomInstantSDKUserHelper userHelper, List<ZoomInstantSDKUser> userList) {

    }

    @Override
    public void onUserVideoStatusChanged(ZoomInstantSDKVideoHelper videoHelper, List<ZoomInstantSDKUser> userList) {

    }

    @Override
    public void onUserAudioStatusChanged(ZoomInstantSDKAudioHelper audioHelper, List<ZoomInstantSDKUser> userList) {

    }

    @Override
    public void onUserShareStatusChanged(ZoomInstantSDKShareHelper shareHelper, ZoomInstantSDKUser userInfo, ZoomInstantSDKShareStatus status) {

    }

    @Override
    public void onLiveStreamStatusChanged(ZoomInstantSDKLiveStreamHelper liveStreamHelper, ZoomInstantSDKLiveStreamStatus status) {

    }

    @Override
    public void onChatNewMessageNotify(ZoomInstantSDKChatHelper chatHelper, ZoomInstantSDKChatMessage messageItem) {

    }

    @Override
    public void onUserHostChanged(ZoomInstantSDKUserHelper userHelper, ZoomInstantSDKUser userInfo) {

    }


    @Override
    public void onSessionNeedPassword(ZoomInstantSDKPasswordHandler handler) {

    }


    @Override
    public void onSessionPasswordWrong(ZoomInstantSDKPasswordHandler handler) {
        Toast.makeText(this, R.string.wrong_pass_tips, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onUserActiveAudioChanged(ZoomInstantSDKAudioHelper audioHelper, List<ZoomInstantSDKUser> list) {

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

    public void onClickCopy(View view) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (null == cm)
            return;
        String input = sessionEditText.getText().toString();
        if (TextUtils.isEmpty(input))
            return;
        ClipData mClipData = ClipData.newPlainText("Label", input);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, R.string.copy_tips, Toast.LENGTH_LONG).show();
    }

    public void onClickJoinSession(View view) {
        joinOrCreateSession();
    }

    public void onClickRenderer(View view) {
        if (isRendererOptionsShowing) return;
        isRendererOptionsShowing = true;
        showRendererOptions();
    }


    private void updateSelectedRenderer() {
        mTvRenderer.setText(renderType == RENDER_TYPE_ZOOMRENDERER ? R.string.renderer_option_zoom : R.string.renderer_option_opengl);
    }

    private boolean isRendererOptionsShowing = false;

    private void showRendererOptions() {
        final Dialog builder = new Dialog(this, R.style.MyDialog);
        builder.setContentView(R.layout.dialog_renderer);

        final View llZoom = builder.findViewById(R.id.llZoom);
        final TextView tvZoom = builder.findViewById(R.id.tvZoom);
        final ImageView ivZoom = builder.findViewById(R.id.ivZoom);
        final TextView tvZoomInfo = builder.findViewById(R.id.tvZoomInfo);

        final View llOpengl = builder.findViewById(R.id.llOpengl);
        final TextView tvOpengl = builder.findViewById(R.id.tvOpengl);
        final ImageView ivOpengl = builder.findViewById(R.id.ivOpengl);
        final TextView tvOpenglInfo = builder.findViewById(R.id.tvOpenglInfo);

        tvZoom.setEnabled(renderType == RENDER_TYPE_ZOOMRENDERER);
        tvZoomInfo.setEnabled(renderType == RENDER_TYPE_ZOOMRENDERER);
        tvOpengl.setEnabled(renderType == RENDER_TYPE_OPENGLES);
        tvOpenglInfo.setEnabled(renderType == RENDER_TYPE_OPENGLES);
        ivZoom.setVisibility(renderType == RENDER_TYPE_ZOOMRENDERER ? View.VISIBLE : View.INVISIBLE);
        ivOpengl.setVisibility(renderType == RENDER_TYPE_OPENGLES ? View.VISIBLE : View.INVISIBLE);

        llZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderType = RENDER_TYPE_ZOOMRENDERER;
                builder.dismiss();
                updateSelectedRenderer();
            }
        });
        llOpengl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderType = RENDER_TYPE_OPENGLES;
                builder.dismiss();
                updateSelectedRenderer();
            }
        });


        builder.setCanceledOnTouchOutside(true);
        builder.setCancelable(true);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isRendererOptionsShowing = false;
            }
        });
        builder.show();
    }

}