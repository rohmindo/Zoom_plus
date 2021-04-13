package com.example.zoom.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zoom.BaseMeetingActivity;
import com.example.zoom.R;
import com.example.zoom.rawdata.RawDataRenderer;

import java.util.ArrayList;
import java.util.List;

import us.zoom.sdk.ZoomInstantSDK;
import us.zoom.sdk.ZoomInstantSDKUser;
import us.zoom.sdk.ZoomInstantSDKVideoAspect;
import us.zoom.sdk.ZoomInstantSDKVideoResolution;
import us.zoom.sdk.ZoomInstantSDKVideoView;

public class UserVideoAdapter extends RecyclerView.Adapter<UserVideoAdapter.BaseHolder> {

    public interface ItemTapListener {
        void onSingleTap(ZoomInstantSDKUser user);
    }

    private ItemTapListener tapListener;

    private List<ZoomInstantSDKUser> userList = new ArrayList<>();

    private Context context;

    private int renderType;

    private ZoomInstantSDKUser selectedVideoUser;

    private List<ZoomInstantSDKUser> activeAudioList;

    public UserVideoAdapter(Context context, ItemTapListener listener, int renderType) {
        this.context = context;
        tapListener = listener;
        this.renderType = renderType;
    }

    public ZoomInstantSDKUser getSelectedVideoUser() {
        return selectedVideoUser;
    }

    public void updateSelectedVideoUser(ZoomInstantSDKUser user) {
        if (null == user) {
            return;
        }
        int index = userList.indexOf(user);
        if (index >= 0) {
            selectedVideoUser = user;
            notifyItemRangeChanged(0, userList.size(), "active");
        }
    }

    public int getIndexByUser(ZoomInstantSDKUser user) {
        return userList.indexOf(user);
    }

    public void clear(boolean resetSelect) {
        userList.clear();
        if (resetSelect) {
            selectedVideoUser = null;
        }
        notifyDataSetChanged();
    }


    public void onUserVideoStatusChanged(List<ZoomInstantSDKUser> changeList) {

        for (ZoomInstantSDKUser user : changeList) {
            int index = userList.indexOf(user);
            if (index >= 0) {
                notifyItemChanged(index, "avar");
            }
        }
    }

    public void addAll() {
        userList.clear();
        List<ZoomInstantSDKUser> all = ZoomInstantSDK.getInstance().getSession().getAllUsers();
        userList.addAll(all);
        notifyDataSetChanged();
    }

    public void onUserJoin(List<ZoomInstantSDKUser> joinList) {
        for (ZoomInstantSDKUser user : joinList) {
            if (!userList.contains(user)) {
                userList.add(user);
                notifyItemInserted(userList.size());
            }
        }
        checkUserList();
    }

    private void checkUserList() {
        List<ZoomInstantSDKUser> all = ZoomInstantSDK.getInstance().getSession().getAllUsers();
        if (all.size() != userList.size()) {
            userList.clear();
            for (ZoomInstantSDKUser userInfo : all) {
                userList.add(userInfo);
            }
            notifyDataSetChanged();
        }
    }

    public void onUserLeave(List<ZoomInstantSDKUser> leaveList) {

        boolean refreshActive=false;
        if (null != selectedVideoUser && leaveList.contains(selectedVideoUser)) {
            selectedVideoUser = ZoomInstantSDK.getInstance().getSession().getMySelf();
            refreshActive=true;
        }
        for (ZoomInstantSDKUser user : leaveList) {
            int index = userList.indexOf(user);
            if (index >= 0) {
                userList.remove(index);
                notifyItemRemoved(index);
            }
        }
        if (refreshActive) {
            notifyItemRangeChanged(0, userList.size(), "active");
        }
        checkUserList();
    }


    public void onUserActiveAudioChanged(List<ZoomInstantSDKUser> list, RecyclerView userVideoList) {
        activeAudioList = list;
        int childCount = userVideoList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = userVideoList.getChildAt(i);
            int position = userVideoList.getChildAdapterPosition(view);
            if (position >= 0 && position < userList.size()) {
                ZoomInstantSDKUser userId = userList.get(position);
                VideoHolder holder = (VideoHolder) userVideoList.findViewHolderForAdapterPosition(position);
                if (null != holder) {
                    if (null != activeAudioList && activeAudioList.contains(userId)) {
                        holder.audioStatus.setVisibility(View.VISIBLE);
                    } else {
                        holder.audioStatus.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_video, parent, false);
        return new VideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {

        onBindViewHolder(holder, position, null);
    }


    @Override
    public void onViewRecycled(@NonNull BaseHolder holder) {
        super.onViewRecycled(holder);
        VideoHolder viewHolder = (VideoHolder) holder;
        if (renderType == BaseMeetingActivity.RENDER_TYPE_ZOOMRENDERER) {
            viewHolder.user.getVideoCanvas().unSubscribe(viewHolder.videoRenderer);
        } else {
            viewHolder.user.getVideoPipe().unSubscribe(viewHolder.rawDataRenderer);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position, @NonNull List<Object> payloads) {

        ZoomInstantSDKUser user = userList.get(position);
        VideoHolder viewHolder = (VideoHolder) holder;
        if (null == payloads || payloads.isEmpty() || payloads.contains("video")) {
            subscribeVideo(user, viewHolder);
        }
        viewHolder.user = user;

        if (null != user) {
            if (!user.getVideoStatus().isOn()) {
                viewHolder.video_off_contain.setVisibility(View.VISIBLE);
                viewHolder.video_off_tips.setImageResource(R.drawable.zm_conf_no_avatar);
            } else {
                viewHolder.video_off_contain.setVisibility(View.INVISIBLE);
            }
            viewHolder.userNameText.setText(user.getUserName());
        }

        if (selectedVideoUser == user) {
            viewHolder.itemView.setBackgroundResource(R.drawable.video_active_item_bg);
        } else {
            viewHolder.itemView.setBackgroundResource(R.drawable.video_item_bg);
        }

        if (null != activeAudioList && activeAudioList.contains(user)) {
            viewHolder.audioStatus.setVisibility(View.VISIBLE);
        } else {
            viewHolder.audioStatus.setVisibility(View.GONE);
        }

    }


    private void subscribeVideo(ZoomInstantSDKUser user, VideoHolder viewHolder) {
        if (renderType == BaseMeetingActivity.RENDER_TYPE_ZOOMRENDERER) {
            user.getVideoCanvas().unSubscribe(viewHolder.videoRenderer);
            user.getVideoCanvas().subscribe(viewHolder.videoRenderer, ZoomInstantSDKVideoAspect.ZoomInstantSDKVideoAspect_PanAndScan);
        } else {
            viewHolder.rawDataRenderer.unSubscribe();
            user.getVideoPipe().subscribe(ZoomInstantSDKVideoResolution.VideoResolution_90P, viewHolder.rawDataRenderer);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class BaseHolder extends RecyclerView.ViewHolder {
        protected View view;

        BaseHolder(View view) {
            super(view);
            this.view = view;
        }
    }

    class VideoHolder extends BaseHolder {

        ZoomInstantSDKVideoView videoRenderer;

        RawDataRenderer rawDataRenderer;

        ImageView audioStatus;

        View itemView;

        TextView userNameText;

        ImageView video_off_tips;

        View video_off_contain;

        ZoomInstantSDKUser user;

        VideoHolder(View view) {
            super(view);
            itemView = view;
            video_off_tips = view.findViewById(R.id.video_off_tips);
            audioStatus = view.findViewById(R.id.item_audio_status);
            userNameText = view.findViewById(R.id.item_user_name);
            video_off_contain = view.findViewById(R.id.video_off_contain);

            videoRenderer = view.findViewById(R.id.videoRenderer);
            rawDataRenderer = view.findViewById(R.id.videoRawDataRenderer);


            if (renderType == BaseMeetingActivity.RENDER_TYPE_ZOOMRENDERER) {
                videoRenderer.setVisibility(View.VISIBLE);
                videoRenderer.setZOrderMediaOverlay(true);
            } else {
                ((ViewGroup) rawDataRenderer.getParent()).setVisibility(View.VISIBLE);
                rawDataRenderer.setVisibility(View.VISIBLE);
                //open when user ZoomSurfaceViewRender
                rawDataRenderer.setZOrderMediaOverlay(true);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != tapListener) {
                        if (selectedVideoUser == user) {
                            return;
                        }
                        tapListener.onSingleTap(user);
                        selectedVideoUser = user;
                        notifyItemRangeChanged(0, getItemCount(), "active");
                    }
                }
            });
        }
    }
}
