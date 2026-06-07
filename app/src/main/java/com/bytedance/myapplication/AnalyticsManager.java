package com.bytedance.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AnalyticsManager {
    private static AnalyticsManager instance;
    private Context context;
    private List<AnalyticsEvent> events;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "analytics_data";
    private static final String KEY_EVENTS = "events_list";
    private static final String KEY_LIKED_ADS = "liked_ads";
    private static final String KEY_FAVORITED_ADS = "favorited_ads";

    private AnalyticsManager(Context context) {
        this.context = context.getApplicationContext();
        this.events = new ArrayList<>();
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadEvents();
    }

    public static synchronized AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context);
        }
        return instance;
    }

    public void trackExposure(Ad ad, String category) {
        AnalyticsEvent event = createEvent(ad, AnalyticsEvent.TYPE_EXPOSURE, category);
        events.add(event);
        ad.setExposureCount(ad.getExposureCount() + 1);
        saveEvents();
    }

    public void trackClick(Ad ad, String category) {
        AnalyticsEvent event = createEvent(ad, AnalyticsEvent.TYPE_CLICK, category);
        events.add(event);
        ad.setClickCount(ad.getClickCount() + 1);
        saveEvents();
    }

    public void trackLike(Ad ad, String category) {
        AnalyticsEvent event = createEvent(ad, AnalyticsEvent.TYPE_LIKE, category);
        events.add(event);
        saveEvents();
        saveLikedAd(ad.getId(), true);
    }

    public void trackUnlike(Ad ad, String category) {
        AnalyticsEvent event = createEvent(ad, AnalyticsEvent.TYPE_UNLIKE, category);
        events.add(event);
        saveEvents();
        saveLikedAd(ad.getId(), false);
    }

    public void trackFavorite(Ad ad, String category) {
        AnalyticsEvent event = createEvent(ad, AnalyticsEvent.TYPE_FAVORITE, category);
        events.add(event);
        saveEvents();
        saveFavoritedAd(ad.getId(), true);
    }

    public void trackUnfavorite(Ad ad, String category) {
        AnalyticsEvent event = createEvent(ad, AnalyticsEvent.TYPE_UNFAVORITE, category);
        events.add(event);
        saveEvents();
        saveFavoritedAd(ad.getId(), false);
    }

    public boolean isAdLiked(String adId) {
        String likedAds = sharedPreferences.getString(KEY_LIKED_ADS, "");
        return likedAds.contains("," + adId + ",");
    }

    public boolean isAdFavorited(String adId) {
        String favoritedAds = sharedPreferences.getString(KEY_FAVORITED_ADS, "");
        return favoritedAds.contains("," + adId + ",");
    }

    private void saveLikedAd(String adId, boolean liked) {
        String likedAds = sharedPreferences.getString(KEY_LIKED_ADS, ",");
        if (liked) {
            if (!likedAds.contains("," + adId + ",")) {
                likedAds += adId + ",";
            }
        } else {
            likedAds = likedAds.replace("," + adId + ",", ",");
        }
        sharedPreferences.edit().putString(KEY_LIKED_ADS, likedAds).apply();
    }

    private void saveFavoritedAd(String adId, boolean favorited) {
        String favoritedAds = sharedPreferences.getString(KEY_FAVORITED_ADS, ",");
        if (favorited) {
            if (!favoritedAds.contains("," + adId + ",")) {
                favoritedAds += adId + ",";
            }
        } else {
            favoritedAds = favoritedAds.replace("," + adId + ",", ",");
        }
        sharedPreferences.edit().putString(KEY_FAVORITED_ADS, favoritedAds).apply();
    }

    public void restoreAdState(Ad ad) {
        ad.setLiked(isAdLiked(ad.getId()));
        ad.setFavorited(isAdFavorited(ad.getId()));
    }

    public void restoreAllAdStates(List<Ad> ads) {
        for (Ad ad : ads) {
            restoreAdState(ad);
        }
    }

    public void trackShare(Ad ad, String category) {
        AnalyticsEvent event = createEvent(ad, AnalyticsEvent.TYPE_SHARE, category);
        events.add(event);
        saveEvents();
    }

    private AnalyticsEvent createEvent(Ad ad, String eventType, String category) {
        String eventId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        return new AnalyticsEvent(eventId, ad.getId(), ad.getTitle(), eventType, timestamp, category);
    }

    public List<AnalyticsEvent> getAllEvents() {
        return new ArrayList<>(events);
    }

    public List<AnalyticsEvent> getEventsByType(String eventType) {
        List<AnalyticsEvent> filteredEvents = new ArrayList<>();
        for (AnalyticsEvent event : events) {
            if (event.getEventType().equals(eventType)) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

    public List<AnalyticsEvent> getEventsByAdId(String adId) {
        List<AnalyticsEvent> filteredEvents = new ArrayList<>();
        for (AnalyticsEvent event : events) {
            if (event.getAdId().equals(adId)) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

    public Map<String, Integer> getStatisticsSummary() {
        Map<String, Integer> summary = new HashMap<>();
        int exposureCount = 0;
        int clickCount = 0;
        int likeCount = 0;
        int unlikeCount = 0;
        int favoriteCount = 0;
        int unfavoriteCount = 0;
        int shareCount = 0;

        for (AnalyticsEvent event : events) {
            switch (event.getEventType()) {
                case AnalyticsEvent.TYPE_EXPOSURE:
                    exposureCount++;
                    break;
                case AnalyticsEvent.TYPE_CLICK:
                    clickCount++;
                    break;
                case AnalyticsEvent.TYPE_LIKE:
                    likeCount++;
                    break;
                case AnalyticsEvent.TYPE_UNLIKE:
                    unlikeCount++;
                    break;
                case AnalyticsEvent.TYPE_FAVORITE:
                    favoriteCount++;
                    break;
                case AnalyticsEvent.TYPE_UNFAVORITE:
                    unfavoriteCount++;
                    break;
                case AnalyticsEvent.TYPE_SHARE:
                    shareCount++;
                    break;
            }
        }

        summary.put("曝光次数", exposureCount);
        summary.put("点击次数", clickCount);
        summary.put("点赞次数", likeCount);
        summary.put("取消点赞", unlikeCount);
        summary.put("收藏次数", favoriteCount);
        summary.put("取消收藏", unfavoriteCount);
        summary.put("分享次数", shareCount);

        return summary;
    }

    public Map<String, Map<String, Integer>> getStatisticsByAd() {
        Map<String, Map<String, Integer>> adStats = new HashMap<>();

        for (AnalyticsEvent event : events) {
            String adId = event.getAdId();
            if (!adStats.containsKey(adId)) {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("曝光", 0);
                stats.put("点击", 0);
                stats.put("点赞", 0);
                stats.put("取消点赞", 0);
                stats.put("收藏", 0);
                stats.put("取消收藏", 0);
                stats.put("分享", 0);
                adStats.put(adId, stats);
            }

            Map<String, Integer> stats = adStats.get(adId);
            switch (event.getEventType()) {
                case AnalyticsEvent.TYPE_EXPOSURE:
                    stats.put("曝光", stats.get("曝光") + 1);
                    break;
                case AnalyticsEvent.TYPE_CLICK:
                    stats.put("点击", stats.get("点击") + 1);
                    break;
                case AnalyticsEvent.TYPE_LIKE:
                    stats.put("点赞", stats.get("点赞") + 1);
                    break;
                case AnalyticsEvent.TYPE_UNLIKE:
                    stats.put("取消点赞", stats.get("取消点赞") + 1);
                    break;
                case AnalyticsEvent.TYPE_FAVORITE:
                    stats.put("收藏", stats.get("收藏") + 1);
                    break;
                case AnalyticsEvent.TYPE_UNFAVORITE:
                    stats.put("取消收藏", stats.get("取消收藏") + 1);
                    break;
                case AnalyticsEvent.TYPE_SHARE:
                    stats.put("分享", stats.get("分享") + 1);
                    break;
            }
        }

        return adStats;
    }

    public void clearAllEvents() {
        events.clear();
        saveEvents();
    }

    private void saveEvents() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder sb = new StringBuilder();
        for (AnalyticsEvent event : events) {
            sb.append(event.getEventId()).append("|");
            sb.append(event.getAdId()).append("|");
            sb.append(event.getAdTitle()).append("|");
            sb.append(event.getEventType()).append("|");
            sb.append(event.getTimestamp()).append("|");
            sb.append(event.getCategory()).append(";;");
        }
        editor.putString(KEY_EVENTS, sb.toString());
        editor.apply();
    }

    private void loadEvents() {
        String eventsStr = sharedPreferences.getString(KEY_EVENTS, "");
        if (!eventsStr.isEmpty()) {
            String[] eventArray = eventsStr.split(";;");
            for (String eventStr : eventArray) {
                if (!eventStr.isEmpty()) {
                    String[] parts = eventStr.split("\\|");
                    if (parts.length == 6) {
                        AnalyticsEvent event = new AnalyticsEvent(
                                parts[0],
                                parts[1],
                                parts[2],
                                parts[3],
                                Long.parseLong(parts[4]),
                                parts[5]
                        );
                        events.add(event);
                    }
                }
            }
        }
    }

    public String getFormattedTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
