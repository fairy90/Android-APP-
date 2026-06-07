package com.bytedance.myapplication;

import java.io.Serializable;

public class AnalyticsEvent implements Serializable {
    public static final String TYPE_EXPOSURE = "exposure";
    public static final String TYPE_CLICK = "click";
    public static final String TYPE_LIKE = "like";
    public static final String TYPE_UNLIKE = "unlike";
    public static final String TYPE_FAVORITE = "favorite";
    public static final String TYPE_UNFAVORITE = "unfavorite";
    public static final String TYPE_SHARE = "share";

    private String eventId;
    private String adId;
    private String adTitle;
    private String eventType;
    private long timestamp;
    private String category;

    public AnalyticsEvent(String eventId, String adId, String adTitle, String eventType, long timestamp, String category) {
        this.eventId = eventId;
        this.adId = adId;
        this.adTitle = adTitle;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.category = category;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getAdTitle() {
        return adTitle;
    }

    public void setAdTitle(String adTitle) {
        this.adTitle = adTitle;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
