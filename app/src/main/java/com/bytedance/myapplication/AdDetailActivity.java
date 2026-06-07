package com.bytedance.myapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;

public class AdDetailActivity extends AppCompatActivity {

    public static final int RESULT_CODE_UPDATE = 1001;
    public static final String EXTRA_AD = "extra_ad";

    private Ad ad;
    private TextView tvSource, tvTitle, tvSummary, tvDescription, tvLikes, tvFavorites;
    private ImageView ivAd, ivLike, ivFavorite, ivShare, ivBack;
    
    // 视频相关控件
    private FrameLayout videoContainer;
    private SurfaceView surfaceView;
    private ImageView ivVideoCover, ivPlay, ivMute;
    private LinearLayout videoControlBar;
    private SeekBar seekBar;
    private TextView tvDuration;
    private MediaPlayer mediaPlayer;
    private Handler progressHandler;
    private Runnable progressRunnable;
    private boolean isPlaying = false;
    private boolean isMuted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_detail);

        initViews();
        initData();
        setupListeners();
    }

    private void initViews() {
        tvSource = findViewById(R.id.tv_source);
        tvTitle = findViewById(R.id.tv_title);
        tvSummary = findViewById(R.id.tv_summary);
        tvDescription = findViewById(R.id.tv_description);
        tvLikes = findViewById(R.id.tv_likes);
        tvFavorites = findViewById(R.id.tv_favorites);
        ivAd = findViewById(R.id.iv_ad);
        ivLike = findViewById(R.id.iv_like);
        ivFavorite = findViewById(R.id.iv_favorite);
        ivShare = findViewById(R.id.iv_share);
        ivBack = findViewById(R.id.iv_back);
        
        // 视频控件
        videoContainer = findViewById(R.id.video_container);
        surfaceView = findViewById(R.id.surface_view_detail);
        ivVideoCover = findViewById(R.id.iv_video_cover_detail);
        ivPlay = findViewById(R.id.iv_play_detail);
        ivMute = findViewById(R.id.iv_mute_detail);
        videoControlBar = findViewById(R.id.video_control_bar_detail);
        seekBar = findViewById(R.id.seek_bar_detail);
        tvDuration = findViewById(R.id.tv_duration_detail);
        
        progressHandler = new Handler();
    }

    private void initData() {
        if (getIntent() != null && getIntent().getSerializableExtra(EXTRA_AD) != null) {
            ad = (Ad) getIntent().getSerializableExtra(EXTRA_AD);

            tvSource.setText(ad.getSource());
            tvTitle.setText(ad.getTitle());
            
            String aiSummary = ad.getAiSummary();
            if (aiSummary == null || aiSummary.isEmpty()) {
                tvSummary.setText("AI摘要生成中...");
                generateAiSummary();
            } else {
                tvSummary.setText(aiSummary);
            }
            
            tvDescription.setText(ad.getDescription());
            tvLikes.setText(String.valueOf(ad.getLikes()));
            tvFavorites.setText(String.valueOf(ad.getFavorites()));

            ivLike.setImageResource(ad.isLiked() ? R.drawable.ic_liked : R.drawable.ic_like);
            ivFavorite.setImageResource(ad.isFavorited() ? R.drawable.ic_favorited : R.drawable.ic_favorite);

            // 判断是否是视频广告
            if ("video".equals(ad.getImageType())) {
                ivAd.setVisibility(View.GONE);
                videoContainer.setVisibility(View.VISIBLE);
                loadVideoFirstFrame(ad.getVideoUrl(), ivVideoCover);
                setupVideoSurface();
            } else {
                ivAd.setVisibility(View.VISIBLE);
                videoContainer.setVisibility(View.GONE);
                loadImage(ad.getImageUrl(), ivAd);
            }

            LinearLayout tagContainer = findViewById(R.id.tag_container);
            tagContainer.removeAllViews();
            for (String tag : ad.getTags()) {
                TextView tagView = new TextView(this);
                tagView.setText(tag);
                tagView.setTextSize(12);
                tagView.setTextColor(getResources().getColor(R.color.accent));
                tagView.setBackground(getResources().getDrawable(R.drawable.tag_bg));
                tagView.setPadding(8, 2, 8, 2);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMarginStart(8);
                tagView.setLayoutParams(params);
                tagContainer.addView(tagView);
            }
        }
    }

    private void loadImage(String url, ImageView imageView) {
        if (url != null && url.startsWith("drawable://")) {
            String drawableName = url.replace("drawable://", "");
            int resourceId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
            if (resourceId > 0) {
                Glide.with(this)
                        .load(resourceId)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .apply(new RequestOptions().centerCrop())
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_image_error);
            }
        } else {
            Glide.with(this)
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
            Uri uri = Uri.parse(videoUrl);
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .apply(new RequestOptions().centerCrop())
                    .into(imageView);
        } else {
            Glide.with(this)
                    .load(videoUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop())
                    .into(imageView);
        }
    }

    private void setupVideoSurface() {
        // 初始化 SurfaceView 的 callback
        SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                android.util.Log.d("AdDetailActivity", "Surface created");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                android.util.Log.d("AdDetailActivity", "Surface changed");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                android.util.Log.d("AdDetailActivity", "Surface destroyed");
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        
        surfaceView.getHolder().addCallback(callback);
        
        // 设置点击事件
        setupVideoPlayback();
    }

    private void setupVideoPlayback() {
        videoContainer.setOnClickListener(v -> {
            android.util.Log.d("AdDetailActivity", "Video container clicked, isPlaying: " + isPlaying);
            if (isPlaying) {
                pauseVideo();
            } else {
                playVideo();
            }
        });

        ivPlay.setOnClickListener(v -> {
            android.util.Log.d("AdDetailActivity", "Play button clicked");
            playVideo();
        });

        ivMute.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                isMuted = !isMuted;
                mediaPlayer.setVolume(isMuted ? 0f : 1f, isMuted ? 0f : 1f);
                ivMute.setImageResource(isMuted ? R.drawable.ic_mute : R.drawable.ic_unmute);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playVideo() {
        if (mediaPlayer == null) {
            initPlayer();
        } else {
            mediaPlayer.start();
            isPlaying = true;
            ivPlay.setVisibility(View.GONE);
            ivVideoCover.setVisibility(View.GONE);
            videoControlBar.setVisibility(View.VISIBLE);
            startProgressUpdate();
        }
    }

    private void initPlayer() {
        SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDisplay(holder);
                    
                    String videoUrl = ad.getVideoUrl();
                    if (videoUrl.startsWith("android.resource://")) {
                        mediaPlayer.setDataSource(AdDetailActivity.this, Uri.parse(videoUrl));
                    } else {
                        mediaPlayer.setDataSource(videoUrl);
                    }
                    
                    mediaPlayer.prepareAsync();
                    
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        isPlaying = true;
                        ivPlay.setVisibility(View.GONE);
                        ivVideoCover.setVisibility(View.GONE);
                        videoControlBar.setVisibility(View.VISIBLE);
                        tvDuration.setText(formatTime(mp.getDuration()));
                        seekBar.setMax(mp.getDuration());
                        
                        if (isMuted) {
                            mp.setVolume(0f, 0f);
                        }
                        
                        startProgressUpdate();
                    });
                    
                    mediaPlayer.setOnCompletionListener(mp -> {
                        mp.seekTo(0);
                        mp.start();
                    });
                    
                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        android.util.Log.e("AdDetailActivity", "Video error: " + what + ", " + extra);
                        return true;
                    });
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        
        surfaceView.getHolder().addCallback(callback);
    }

    private void pauseVideo() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            ivPlay.setVisibility(View.VISIBLE);
            stopProgressUpdate();
        }
    }

    private void startProgressUpdate() {
        stopProgressUpdate();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPos = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPos);
                }
                progressHandler.postDelayed(this, 1000);
            }
        };
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdate() {
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> {
            returnResult();
        });

        ivLike.setOnClickListener(v -> {
            boolean wasLiked = ad.isLiked();
            ad.setLiked(!ad.isLiked());
            ad.setLikes(ad.isLiked() ? ad.getLikes() + 1 : ad.getLikes() - 1);
            ivLike.setImageResource(ad.isLiked() ? R.drawable.ic_liked : R.drawable.ic_like);
            tvLikes.setText(String.valueOf(ad.getLikes()));
            
            // 埋点统计
            if (ad.isLiked()) {
                AnalyticsManager.getInstance(this).trackLike(ad, "详情页");
            } else {
                AnalyticsManager.getInstance(this).trackUnlike(ad, "详情页");
            }
        });

        ivFavorite.setOnClickListener(v -> {
            boolean wasFavorited = ad.isFavorited();
            ad.setFavorited(!ad.isFavorited());
            ad.setFavorites(ad.isFavorited() ? ad.getFavorites() + 1 : ad.getFavorites() - 1);
            ivFavorite.setImageResource(ad.isFavorited() ? R.drawable.ic_favorited : R.drawable.ic_favorite);
            tvFavorites.setText(String.valueOf(ad.getFavorites()));
            
            // 埋点统计
            if (ad.isFavorited()) {
                AnalyticsManager.getInstance(this).trackFavorite(ad, "详情页");
            } else {
                AnalyticsManager.getInstance(this).trackUnfavorite(ad, "详情页");
            }
        });

        ivShare.setOnClickListener(v -> {
            Toast.makeText(this, "分享功能", Toast.LENGTH_SHORT).show();
        });
    }

    private void generateAiSummary() {
        DeepSeekApiManager.getInstance().generateSummary(ad.getTitle(), ad.getDescription(),
                new DeepSeekApiManager.OnSummaryGeneratedListener() {
                    @Override
                    public void onSuccess(String summary) {
                        ad.setAiSummary(summary);
                        tvSummary.setText(summary);
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
    }

    private void returnResult() {
        releasePlayer();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_AD, ad);
        setResult(RESULT_CODE_UPDATE, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void releasePlayer() {
        stopProgressUpdate();
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        returnResult();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
