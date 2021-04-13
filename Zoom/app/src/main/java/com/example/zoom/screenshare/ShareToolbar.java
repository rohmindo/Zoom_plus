package com.example.zoom.screenshare;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.zoom.R;

public class ShareToolbar {

    public interface Listener {
        void onClickStopShare();
    }

    private final WindowManager mWindowManager;

    private final Context mContext;

    private View contentView;

    private Listener mListener;
    private Display mDisplay;

    float mLastRawX = -1f;
    float mLastRawY = -1f;

    public ShareToolbar(Listener listener, Context context) {
        mListener = listener;
        mContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
    }

    private void init() {
        contentView = LayoutInflater.from(mContext).inflate(R.layout.layout_share_toolbar, null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {


            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (null != mListener) {
                    mListener.onClickStopShare();
                }
                destroy();
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) contentView.getLayoutParams();
                int dx, dy;
                if (mLastRawX == -1 || mLastRawY == -1) {
                    dx = (int) (e2.getRawX() - e1.getRawX());
                    dy = (int) (e2.getRawY() - e1.getRawY());
                } else {
                    dx = (int) (e2.getRawX() - mLastRawX);
                    dy = (int) (e2.getRawY() - mLastRawY);
                }
                layoutParams.x += dx;
                layoutParams.y += dy;
                mLastRawX = e2.getRawX();
                mLastRawY = e2.getRawY();
                mWindowManager.updateViewLayout(contentView, layoutParams);
                return true;
            }
        };

        final GestureDetector detector = new GestureDetector(listener);

        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mLastRawX = -1f;
                    mLastRawY = -1f;
                }
                return detector.onTouchEvent(event);
            }
        });
    }

    public void destroy() {
        if (null != mWindowManager) {
            if (null != contentView) {
                mWindowManager.removeView(contentView);
                contentView = null;
            }
        }
    }

    public void showToolbar() {
        if (null == contentView) {
            init();
        }
        contentView.measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.AT_MOST);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = getWindowLayoutParamsType();
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        int height = contentView.getHeight();
        if (height == 0) {
            height = 150;
        }
        layoutParams.x = 100;
        layoutParams.y = mDisplay.getHeight() - 100 - height;
        mWindowManager.addView(contentView, layoutParams);

    }

    private int getWindowLayoutParamsType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && (Settings.canDrawOverlays(mContext))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                return WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                return WindowManager.LayoutParams.TYPE_TOAST;
            }
        }
    }
}
