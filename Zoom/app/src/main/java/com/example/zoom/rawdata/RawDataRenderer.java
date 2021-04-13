package com.example.zoom.rawdata;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;

import us.zoom.rawdatarender.RawDataBufferType;
import us.zoom.rawdatarender.ZoomSurfaceViewRender;
import us.zoom.sdk.ZoomInstantSDKRawDataPipeDelegate;
import us.zoom.sdk.ZoomInstantSDKUser;
import us.zoom.sdk.ZoomInstantSDKVideoRawData;
import us.zoom.sdk.ZoomInstantSDKVideoResolution;

//note  use ZoomSurfaceViewRender for performance
//use ZooTmextureViewRender for  ui animation
public class RawDataRenderer extends ZoomSurfaceViewRender implements ZoomInstantSDKRawDataPipeDelegate {

    private static final String TAG = "RawDataRenderer";

    private static HandlerThread handlerThread;

    private static Handler handler;

    private Context mContext = null;

    private ZoomInstantSDKUser user;

    private boolean isSubscribeShare = false;

    public RawDataRenderer(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public RawDataRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        setBufferType(RawDataBufferType.BYTE_ARRAY);
        initRender();
        startRender();
        if (null == handlerThread) {
            handlerThread = new HandlerThread("RawDataRenderer");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }
    }

    /**
     * recycle view : move out then move in sometime cache hit  without call onBindViewHolder
     */
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startRender();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // onDetachedFromWindow  unSubscribe video and stop render get better performance
        stopRender();
        if (null != user) {
            user.getVideoPipe().unSubscribe(this);
        }
    }

    @Override
    public void onRawDataStatusChanged(RawDataStatus status) {
        if (status == RawDataStatus.RawData_Off) {
            clearImage(0.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    @Override
    public void onRawDataFrameReceived(final ZoomInstantSDKVideoRawData rawData) {
        boolean isMainThread = Thread.currentThread() == Looper.getMainLooper().getThread();
        if (isMainThread && rawData.canAddRef()) {
            rawData.addRef();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    drawI420YUV(rawData.getyBuffer(), rawData.getuBuffer(), rawData.getvBuffer(),
                            rawData.getStreamWidth(), rawData.getStreamHeight(), rawData.getRotation(), 30);
                    rawData.releaseRef();
                }
            });
        } else {
            drawI420YUV(rawData.getyBuffer(), rawData.getuBuffer(), rawData.getvBuffer(),
                    rawData.getStreamWidth(), rawData.getStreamHeight(), rawData.getRotation(), 30);

        }
    }

    public void subscribe(ZoomInstantSDKUser user, ZoomInstantSDKVideoResolution resolution, boolean isShare) {
        if (null == user) {
            return;
        }
//        unSubscribe();

        this.user = user;
        if (isShare) {
            user.getSharePipe().subscribe(resolution, this);
        } else {
            user.getVideoPipe().subscribe(resolution, this);
        }
        isSubscribeShare = isShare;
    }

    public void unSubscribe() {
        if (null != user) {
            if (isSubscribeShare) {
                user.getSharePipe().unSubscribe(this);
            } else {
                user.getVideoPipe().unSubscribe(this);
            }
        }
    }


}

