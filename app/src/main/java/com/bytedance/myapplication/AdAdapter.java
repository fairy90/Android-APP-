package com.bytedance.myapplication;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;
import java.util.List;

public class AdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Ad> adList;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private OnAdClickListener listener;
    private PlayerPool playerPool;
    private String currentPlayingId;
    private AnalyticsManager analyticsManager;
    private String currentCategory = "精选";
    private boolean hasMore = true;

    public interface OnAdClickListener {
        void onAdClick(Ad ad, int position);
    }

    public void setOnAdClickListener(OnAdClickListener listener) {
        this.listener = listener;
    }

    public AdAdapter(Context context, List<Ad> adList) {
        this.context = context;
        this.adList = adList;
        this.playerPool = PlayerPool.getInstance(context);
        this.analyticsManager = AnalyticsManager.getInstance(context);
    }

    public void setCurrentCategory(String category) {
        this.currentCategory = category;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
        notifyItemChanged(adList.size());
    }

    @Override
    public int getItemViewType(int position) {
        return position == adList.size() ? TYPE_FOOTER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new FooterViewHolder(view);
        }

        View view = LayoutInflater.from(context).inflate(R.layout.ad_card_item, parent, false);
        return new AdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == adList.size()) {
            if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                if (hasMore) {
                    footerHolder.progressBar.setVisibility(View.VISIBLE);
                    footerHolder.tvLoading.setText("加载中...");
                } else {
                    footerHolder.progressBar.setVisibility(View.GONE);
                    footerHolder.tvLoading.setText("没有更多了");
                }
            }
            return;
        }
        if (holder instanceof AdViewHolder) {
            AdViewHolder adHolder = (AdViewHolder) holder;
            Ad ad = adList.get(position);

            adHolder.tvSource.setText(ad.getSource());
            adHolder.tvTitle.setText(ad.getTitle());
            
            String aiSummary = ad.getAiSummary();
            if (aiSummary == null || aiSummary.isEmpty()) {
                adHolder.tvSummary.setText("");
                adHolder.tvAiStatus.setText("AI标签 · 摘要生成中");
                generateAiSummary(ad, adHolder);
            } else {
                adHolder.tvSummary.setText(aiSummary);
                adHolder.tvAiStatus.setText("AI标签 · 摘要已生成");
            }
            
            adHolder.tvLikes.setText(String.valueOf(ad.getLikes()));
            adHolder.tvFavorites.setText(String.valueOf(ad.getFavorites()));

            adHolder.ivLike.setImageResource(ad.isLiked() ? R.drawable.ic_liked : R.drawable.ic_like);
            adHolder.ivFavorite.setImageResource(ad.isFavorited() ? R.drawable.ic_favorited : R.drawable.ic_favorite);

            setupMediaContent(adHolder, ad);

            adHolder.tagContainer.removeAllViews();
            for (String tag : ad.getTags()) {
                TextView tagView = new TextView(context);
                tagView.setText(tag);
                tagView.setTextSize(12);
                tagView.setTextColor(context.getResources().getColor(R.color.accent));
                tagView.setBackground(context.getResources().getDrawable(R.drawable.tag_bg));
                tagView.setPadding(8, 2, 8, 2);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMarginStart(8);
                tagView.setLayoutParams(params);
                adHolder.tagContainer.addView(tagView);
            }

            adHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAdClick(ad, position);
                }
            });

            adHolder.ivLike.setOnClickListener(v -> {
                boolean wasLiked = ad.isLiked();
                ad.setLiked(!ad.isLiked());
                ad.setLikes(ad.isLiked() ? ad.getLikes() + 1 : ad.getLikes() - 1);
                adHolder.ivLike.setImageResource(ad.isLiked() ? R.drawable.ic_liked : R.drawable.ic_like);
                adHolder.tvLikes.setText(String.valueOf(ad.getLikes()));
                
                if (ad.isLiked()) {
                    analyticsManager.trackLike(ad, currentCategory);
                } else {
                    analyticsManager.trackUnlike(ad, currentCategory);
                }
            });

            adHolder.ivFavorite.setOnClickListener(v -> {
                boolean wasFavorited = ad.isFavorited();
                ad.setFavorited(!ad.isFavorited());
                ad.setFavorites(ad.isFavorited() ? ad.getFavorites() + 1 : ad.getFavorites() - 1);
                adHolder.ivFavorite.setImageResource(ad.isFavorited() ? R.drawable.ic_favorited : R.drawable.ic_favorite);
                adHolder.tvFavorites.setText(String.valueOf(ad.getFavorites()));
                
                if (ad.isFavorited()) {
                    analyticsManager.trackFavorite(ad, currentCategory);
                } else {
                    analyticsManager.trackUnfavorite(ad, currentCategory);
                }
            });

            adHolder.ivShare.setOnClickListener(v -> {
                Toast.makeText(context, "分享功能", Toast.LENGTH_SHORT).show();
                analyticsManager.trackShare(ad, currentCategory);
            });
        }
    }

    private void setupMediaContent(AdViewHolder holder, Ad ad) {
        String type = ad.getImageType();

        holder.ivAdBig.setVisibility(View.GONE);
        holder.ivAdSmall.setVisibility(View.GONE);
        holder.surfaceView.setVisibility(View.GONE);
        holder.ivVideoCover.setVisibility(View.GONE);
        holder.tvSmallLabel.setVisibility(View.GONE);
        holder.tvBigLabel.setVisibility(View.GONE);
        holder.ivPlay.setVisibility(View.GONE);
        holder.tvDuration.setVisibility(View.GONE);
        holder.smallLayout.setVisibility(View.GONE);
        holder.tvTitleSmall.setVisibility(View.GONE);
        holder.tvSummarySmall.setVisibility(View.GONE);
        holder.contentLayout.setVisibility(View.VISIBLE);
        holder.videoControlBar.setVisibility(View.GONE);

        if ("big".equals(type)) {
            holder.ivAdBig.setVisibility(View.VISIBLE);
            holder.tvBigLabel.setVisibility(View.VISIBLE);
            loadImage(ad.getImageUrl(), holder.ivAdBig);
            // 清除视频点击事件，防止影响图片广告
            holder.mediaContainer.setOnClickListener(null);
        } else if ("small".equals(type)) {
            holder.smallLayout.setVisibility(View.VISIBLE);
            holder.ivAdSmall.setVisibility(View.VISIBLE);
            holder.tvSmallLabel.setVisibility(View.VISIBLE);
            holder.tvTitleSmall.setVisibility(View.VISIBLE);
            holder.tvSummarySmall.setVisibility(View.VISIBLE);
            holder.contentLayout.setVisibility(View.GONE);

            loadImage(ad.getImageUrl(), holder.ivAdSmall);
            holder.tvTitleSmall.setText(ad.getTitle());
            holder.tvSummarySmall.setText(ad.getAiSummary());
            
            // 显示标签
            holder.tagsLayoutSmall.removeAllViews();
            if (ad.getTags() != null && !ad.getTags().isEmpty()) {
                holder.tagsLayoutSmall.setVisibility(View.VISIBLE);
                for (String tag : ad.getTags()) {
                    TextView tagView = new TextView(context);
                    tagView.setText(tag);
                    tagView.setTextSize(11);
                    tagView.setTextColor(context.getResources().getColor(R.color.accent));
                    tagView.setBackground(context.getResources().getDrawable(R.drawable.tag_bg));
                    tagView.setPadding(6, 2, 6, 2);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMarginStart(6);
                    tagView.setLayoutParams(params);
                    holder.tagsLayoutSmall.addView(tagView);
                }
            } else {
                holder.tagsLayoutSmall.setVisibility(View.GONE);
            }
            
            // 清除视频点击事件，防止影响小图广告
            holder.mediaContainer.setOnClickListener(null);
            
            // 设置小图布局的点击事件，确保点击能进入详情页
            holder.smallLayout.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAdClick(ad, holder.getAdapterPosition());
                }
            });
        } else if ("video".equals(type)) {
            holder.ivVideoCover.setVisibility(View.VISIBLE);
            holder.ivPlay.setVisibility(View.VISIBLE);
            holder.tvDuration.setVisibility(View.VISIBLE);

            // 使用视频第一帧作为封面
            loadVideoFirstFrame(ad.getVideoUrl(), holder.ivVideoCover);

            holder.tvDuration.setText(formatDuration(ad.getDuration()));

            setupVideoPlayback(holder, ad);
        }
    }

    private void loadImage(String url, ImageView imageView) {
        if (url != null && url.startsWith("drawable://")) {
            // 加载本地drawable资源
            String drawableName = url.replace("drawable://", "");
            int resourceId = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
            if (resourceId > 0) {
                Glide.with(context)
                        .load(resourceId)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .apply(new RequestOptions()
                                .centerCrop())
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_image_error);
            }
        } else {
            // 加载网络图片
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .fallback(R.drawable.ic_image_placeholder)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop())
                    .into(imageView);
        }
    }

    private void loadVideoFirstFrame(String videoUrl, ImageView imageView) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_image_error);
            return;
        }

        if (videoUrl.startsWith("android.resource://")) {
            // 本地视频：使用Glide加载视频第一帧
            android.net.Uri uri = android.net.Uri.parse(videoUrl);
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .apply(new RequestOptions()
                            .centerCrop())
                    .into(imageView);
        } else {
            // 网络视频：使用Glide加载视频第一帧
            Glide.with(context)
                    .load(videoUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop())
                    .into(imageView);
        }
    }

    private String formatDuration(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void setupVideoPlayback(AdViewHolder holder, Ad ad) {
        holder.mediaContainer.setOnClickListener(v -> {
            if (holder.isPlaying) {
                pauseVideo(holder, ad);
            } else {
                playVideo(holder, ad);
            }
        });

        holder.ivMute.setOnClickListener(v -> {
            toggleMute(holder);
        });
    }

    private void playVideo(AdViewHolder holder, Ad ad) {
        if (currentPlayingId != null && !currentPlayingId.equals(ad.getId())) {
            pauseVideoByPlayerId(currentPlayingId);
        }

        holder.isPlaying = true;
        currentPlayingId = ad.getId();
        holder.setPlayerId(ad.getId());

        holder.ivPlay.setVisibility(View.GONE);
        holder.ivVideoCover.setVisibility(View.GONE);
        holder.surfaceView.setVisibility(View.VISIBLE);
        holder.videoControlBar.setVisibility(View.VISIBLE);

        // 始终重新初始化播放器，确保播放正确的视频
        // 避免复用播放器导致播放错误视频的问题
        initPlayer(holder, ad);
    }

    private void pauseVideo(AdViewHolder holder, Ad ad) {
        holder.isPlaying = false;

        // 暂停视频播放
        if (holder.mediaPlayer != null && holder.mediaPlayer.isPlaying()) {
            holder.mediaPlayer.pause();
        }

        stopProgressUpdate(holder);

        holder.ivPlay.setVisibility(View.VISIBLE);
        holder.ivVideoCover.setVisibility(View.VISIBLE);
        holder.surfaceView.setVisibility(View.GONE);
        holder.videoControlBar.setVisibility(View.GONE);
    }

    private void pauseVideoByPlayerId(String playerId) {
        // 释放播放器资源
        playerPool.releasePlayer(playerId);
    }

    private void toggleMute(AdViewHolder holder) {
        if (holder.mediaPlayer != null) {
            holder.isMuted = !holder.isMuted;
            playerPool.setMute(holder.mediaPlayer, holder.isMuted);
            holder.ivMute.setImageResource(holder.isMuted ? R.drawable.ic_mute : R.drawable.ic_unmute);
        }
    }

    private void initPlayer(AdViewHolder holder, Ad ad) {
        // 释放之前的播放器
        if (holder.mediaPlayer != null) {
            try {
                holder.mediaPlayer.stop();
                holder.mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.mediaPlayer = null;
        }
        
        // 确保SurfaceView可见
        holder.surfaceView.setVisibility(View.VISIBLE);
        
        // 获取SurfaceHolder并设置固定的callback
        SurfaceHolder holderSurface = holder.surfaceView.getHolder();
        
        // 创建MediaPlayer
        try {
            MediaPlayer player = new MediaPlayer();
            holder.mediaPlayer = player;
            holder.isPlaying = true;

            // 设置音频属性
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MOVIE)
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .build();
            player.setAudioAttributes(audioAttributes);
            
            // 设置屏幕常亮
            player.setScreenOnWhilePlaying(true);
            
            // 设置音量
            player.setVolume(holder.isMuted ? 0f : 1f, holder.isMuted ? 0f : 1f);
            
            // 设置循环播放
            player.setLooping(true);

            String videoUrl = ad.getVideoUrl();
            if (videoUrl.startsWith("android.resource://")) {
                player.setDataSource(context, android.net.Uri.parse(videoUrl));
            } else {
                player.setDataSource(videoUrl);
            }

            // 关键：先prepareAsync，在onPrepared中再设置display
            player.prepareAsync();

            player.setOnPreparedListener(mp -> {
                // 准备完成后再设置display，确保surface已就绪
                if (holderSurface.getSurface().isValid()) {
                    mp.setDisplay(holderSurface);
                }
                
                mp.start();
                holder.isPlaying = true;
                
                holder.tvDuration.setText(formatTime(mp.getDuration()));
                holder.seekBar.setMax(mp.getDuration());
                holder.seekBar.setProgress(0);
                holder.tvCurrentTime.setText("00:00");
                setupSeekBarListener(holder);
                startProgressUpdate(holder);
                
                // 隐藏封面，显示视频控件
                holder.ivVideoCover.setVisibility(View.GONE);
                holder.ivPlay.setVisibility(View.GONE);
                holder.videoControlBar.setVisibility(View.VISIBLE);
            });
            
            player.setOnCompletionListener(mp -> {
                mp.seekTo(0);
                holder.seekBar.setProgress(0);
                holder.tvCurrentTime.setText("00:00");
            });
            
            player.setOnErrorListener((mp, what, extra) -> {
                android.util.Log.e("AdAdapter", "Video playback error! URL: " + ad.getVideoUrl());
                android.util.Log.e("AdAdapter", "Error: " + what);
                releasePlayer(holder);
                return true;
            });
            
        } catch (IOException e) {
            android.util.Log.e("AdAdapter", "Failed to create player: " + e.getMessage());
            releasePlayer(holder);
        }
    }
    
    private void releasePlayer(AdViewHolder holder) {
        if (holder.mediaPlayer != null) {
            try {
                holder.mediaPlayer.stop();
                holder.mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.mediaPlayer = null;
            holder.isPlaying = false;
        }
        stopProgressUpdate(holder);
    }

    private void startProgressUpdate(AdViewHolder holder) {
        stopProgressUpdate(holder);
        holder.progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (holder.mediaPlayer != null && holder.mediaPlayer.isPlaying()) {
                    int currentPos = holder.mediaPlayer.getCurrentPosition();
                    int duration = holder.mediaPlayer.getDuration();
                    holder.tvCurrentTime.setText(formatTime(currentPos));
                    holder.seekBar.setProgress(currentPos);
                }
                holder.progressHandler.postDelayed(this, 1000);
            }
        };
        holder.progressHandler.post(holder.progressRunnable);
    }

    private void stopProgressUpdate(AdViewHolder holder) {
        if (holder.progressRunnable != null) {
            holder.progressHandler.removeCallbacks(holder.progressRunnable);
            holder.progressRunnable = null;
        }
    }

    private String getErrorWhatMeaning(int what) {
        switch (what) {
            case android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN:
                return "未知错误";
            case android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                return "媒体服务器死亡";
            case android.media.MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                return "不支持渐进式播放";
            case android.media.MediaPlayer.MEDIA_ERROR_IO:
                return "IO错误（文件不存在或网络错误）";
            case android.media.MediaPlayer.MEDIA_ERROR_MALFORMED:
                return "格式错误";
            case android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                return "不支持的格式或编码";
            case android.media.MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                return "超时";
            default:
                return "未知错误码: " + what;
        }
    }

//    private String getErrorExtraMeaning(int extra) {
//        switch (extra) {
//            case android.media.MediaPlayer.MEDIA_ERROR_SYSTEM_ERR:
//                return "系统错误";
//            case android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED_MEDIA_SERVER:
//                return "媒体服务器死亡";
//            case android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED_VIDEO_HOST:
//                return "视频主机死亡";
//            default:
//            return "额外信息: " + extra;
//        }
//    }

    private void setupSeekBarListener(AdViewHolder holder) {
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && holder.mediaPlayer != null) {
                    holder.mediaPlayer.seekTo(progress);
                    holder.tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof AdViewHolder) {
            AdViewHolder adHolder = (AdViewHolder) holder;
            String playerId = adHolder.getPlayerId();
            if (playerId != null && playerPool != null) {
                playerPool.releasePlayer(playerId);
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof AdViewHolder) {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && position < adList.size()) {
                Ad ad = adList.get(position);
                if (ad != null) {
                    AnalyticsManager.getInstance(context).trackExposure(ad, "feed");
                }
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // 曝光埋点已移除
    }

    public void releaseAllPlayers() {
        playerPool.releaseAllPlayers();
    }

    @Override
    public int getItemCount() {
        return adList.size() + 1;
    }

    public void addFooterView(View view) {
        adList.add(null);
        notifyItemInserted(adList.size() - 1);
    }

    public void removeFooterView() {
        if (adList.size() > 0 && adList.get(adList.size() - 1) == null) {
            adList.remove(adList.size() - 1);
            notifyItemRemoved(adList.size());
        }
    }

    public void updateAd(int position, Ad ad) {
        adList.set(position, ad);
        notifyItemChanged(position);
    }

    public void addAll(List<Ad> ads) {
        int startPos = adList.size();
        adList.addAll(ads);
        notifyItemRangeInserted(startPos, ads.size());
    }

    private void generateAiSummary(Ad ad, AdViewHolder holder) {
        String currentAdId = ad.getId();
        DeepSeekApiManager.getInstance().generateSummary(ad.getTitle(), ad.getDescription(),
                new DeepSeekApiManager.OnSummaryGeneratedListener() {
                    @Override
                    public void onSuccess(String summary) {
                        ad.setAiSummary(summary);
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && position < adList.size()) {
                            Ad currentAd = adList.get(position);
                            if (currentAdId.equals(currentAd.getId())) {
                                holder.tvSummary.setText(summary);
                                holder.tvAiStatus.setText("AI标签 · 摘要已生成");
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("AdAdapter", "生成AI摘要失败: " + error);
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && position < adList.size()) {
                            Ad currentAd = adList.get(position);
                            if (currentAdId.equals(currentAd.getId())) {
//                                holder.tvAiStatus.setText("AI标签 · 摘要生成失败");
                            }
                        }
                    }
                });
    }

    public void clear() {
        releaseAllPlayers();
        adList.clear();
        notifyDataSetChanged();
    }

    public static class AdViewHolder extends RecyclerView.ViewHolder {
        TextView tvSource, tvTitle, tvSummary, tvLikes, tvFavorites, tvSmallLabel, tvBigLabel, tvDuration, tvTitleSmall, tvSummarySmall, tvCurrentTime, tvAiStatus;
        ImageView ivAdBig, ivAdSmall, ivLike, ivFavorite, ivShare, ivVideoCover, ivPlay, ivMute;
        LinearLayout tagContainer, smallLayout, videoControlBar, contentLayout, tagsLayoutSmall;
        FrameLayout mediaContainer;
        SurfaceView surfaceView;
        SeekBar seekBar;
        String playerId;
        boolean isPlaying;
        boolean isMuted;
        MediaPlayer mediaPlayer;
        Handler progressHandler;
        Runnable progressRunnable;

        public AdViewHolder(View itemView) {
            super(itemView);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvTitleSmall = itemView.findViewById(R.id.tv_title_small);
            tvSummarySmall = itemView.findViewById(R.id.tv_summary_small);
            tvLikes = itemView.findViewById(R.id.tv_likes);
            tvFavorites = itemView.findViewById(R.id.tv_favorites);
            tvSmallLabel = itemView.findViewById(R.id.tv_small_label);
            tvBigLabel = itemView.findViewById(R.id.tv_big_label);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvCurrentTime = itemView.findViewById(R.id.tv_current_time);
            tvAiStatus = itemView.findViewById(R.id.tv_ai_status);

            ivAdBig = itemView.findViewById(R.id.iv_ad_big);
            ivAdSmall = itemView.findViewById(R.id.iv_ad_small);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            ivShare = itemView.findViewById(R.id.iv_share);
            ivVideoCover = itemView.findViewById(R.id.iv_video_cover);
            ivPlay = itemView.findViewById(R.id.iv_play);
            ivMute = itemView.findViewById(R.id.iv_mute);

            tagContainer = itemView.findViewById(R.id.tag_container);
            smallLayout = itemView.findViewById(R.id.small_layout);
            mediaContainer = itemView.findViewById(R.id.media_container);
            surfaceView = itemView.findViewById(R.id.surface_view);
            videoControlBar = itemView.findViewById(R.id.video_control_bar);
            contentLayout = itemView.findViewById(R.id.content_layout);
            tagsLayoutSmall = itemView.findViewById(R.id.tags_layout_small);
            seekBar = itemView.findViewById(R.id.seek_bar);

            isPlaying = false;
            isMuted = true;

            progressHandler = new Handler();
        }

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        TextView tvLoading;

        public FooterViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
            tvLoading = itemView.findViewById(R.id.tv_loading);
        }
    }
}