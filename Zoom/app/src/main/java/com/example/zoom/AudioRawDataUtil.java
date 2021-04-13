package com.example.zoom;


import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.zoom.sdk.ZoomInstantSDK;
import us.zoom.sdk.ZoomInstantSDKAudioHelper;
import us.zoom.sdk.ZoomInstantSDKAudioRawData;
import us.zoom.sdk.ZoomInstantSDKChatHelper;
import us.zoom.sdk.ZoomInstantSDKChatMessage;
import us.zoom.sdk.ZoomInstantSDKDelegate;
import us.zoom.sdk.ZoomInstantSDKLiveStreamHelper;
import us.zoom.sdk.ZoomInstantSDKLiveStreamStatus;
import us.zoom.sdk.ZoomInstantSDKPasswordHandler;
import us.zoom.sdk.ZoomInstantSDKShareHelper;
import us.zoom.sdk.ZoomInstantSDKShareStatus;
import us.zoom.sdk.ZoomInstantSDKUser;
import us.zoom.sdk.ZoomInstantSDKUserHelper;
import us.zoom.sdk.ZoomInstantSDKVideoHelper;

public class AudioRawDataUtil {

    static final String TAG = "AudioRawDataUtil";

    private Map<String, FileChannel> map = new HashMap<>();

    private Context mContext;


    public AudioRawDataUtil(Context context) {
        mContext = context.getApplicationContext();
    }

    private FileChannel createFileChannel(String userId) {
        String fileName = mContext.getFilesDir().getAbsolutePath() + "/" + userId + ".pcm";
        File file = new File(fileName);
        try {
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            FileChannel fileChannel = fileOutputStream.getChannel();

            return fileChannel;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    private ZoomInstantSDKDelegate dataDelegate = new ZoomInstantSDKDelegate() {


        @Override
        public void onMixedAudioRawDataReceived(ZoomInstantSDKAudioRawData rawData) {
            saveAudioRawData(rawData, ZoomInstantSDK.getInstance().getSession().getMySelf());

        }

        public void onOneWayAudioRawDataReceived(ZoomInstantSDKAudioRawData rawData, ZoomInstantSDKUser user) {
            saveAudioRawData(rawData, user);
        }


        @Override
        public void onSessionJoin() {

        }

        @Override
        public void onSessionLeave() {

        }

        @Override
        public void onError(int errorCode) {

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
        public void onUserActiveAudioChanged(ZoomInstantSDKAudioHelper audioHelper, List<ZoomInstantSDKUser> list) {

        }

        @Override
        public void onSessionNeedPassword(ZoomInstantSDKPasswordHandler handler) {

        }

        @Override
        public void onSessionPasswordWrong(ZoomInstantSDKPasswordHandler handler) {

        }

        @Override
        public void onUserManagerChanged(ZoomInstantSDKUser user) {

        }

        @Override
        public void onUserNameChanged(ZoomInstantSDKUser user) {

        }
    };

    private void saveAudioRawData(ZoomInstantSDKAudioRawData rawData, ZoomInstantSDKUser user) {
        try {
            String userId=user.getUserId();
            Log.d(TAG, "onMixedAudioRawDataReceived:" + rawData.getBufferLen());
            FileChannel fileChannel = map.get(userId);
            if (null == fileChannel) {
                fileChannel = createFileChannel(userId);
                map.put(userId, fileChannel);
            }
            if (null != fileChannel) {
                fileChannel.write(rawData.getBuffer(), rawData.getBufferLen());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void subscribeAudio() {

        ZoomInstantSDK.getInstance().getAudioHelper().subscribe();
        ZoomInstantSDK.getInstance().addListener(dataDelegate);
    }

    public void unSubscribe() {
        ZoomInstantSDK.getInstance().removeListener(dataDelegate);

        for (FileChannel fileChannel : map.values()) {
            if (null != fileChannel) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ZoomInstantSDK.getInstance().getAudioHelper().unSubscribe();
    }
}
