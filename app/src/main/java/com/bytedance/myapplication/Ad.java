package com.bytedance.myapplication;

import java.io.Serializable;
import java.util.List;

public class Ad implements Serializable {
    private String id;
    private String title;
    private String description;
    private String aiSummary;
    private String source;
    private String imageUrl;
    private String imageType;
    private String videoUrl;
    private String coverUrl;
    private int duration;
    private List<String> tags;
    private int likes;
    private int favorites;
    private boolean isLiked;
    private boolean isFavorited;
    private int exposureCount;
    private int clickCount;

    public Ad() {
    }

    public Ad(String id, String title, String description, String aiSummary, String source,
              String imageUrl, String imageType, List<String> tags, int likes, int favorites,
              boolean isLiked, boolean isFavorited, int exposureCount, int clickCount) {
        this(id, title, description, aiSummary, source, imageUrl, imageType, null, null, 0,
                tags, likes, favorites, isLiked, isFavorited, exposureCount, clickCount);
    }

    public Ad(String id, String title, String description, String aiSummary, String source,
              String imageUrl, String imageType, String videoUrl, String coverUrl, int duration,
              List<String> tags, int likes, int favorites, boolean isLiked, boolean isFavorited,
              int exposureCount, int clickCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.aiSummary = aiSummary;
        this.source = source;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.videoUrl = videoUrl;
        this.coverUrl = coverUrl;
        this.duration = duration;
        this.tags = tags;
        this.likes = likes;
        this.favorites = favorites;
        this.isLiked = isLiked;
        this.isFavorited = isFavorited;
        this.exposureCount = exposureCount;
        this.clickCount = clickCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    public int getExposureCount() {
        return exposureCount;
    }

    public void setExposureCount(int exposureCount) {
        this.exposureCount = exposureCount;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }
}