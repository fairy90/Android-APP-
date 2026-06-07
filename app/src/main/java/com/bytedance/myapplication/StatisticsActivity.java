package com.bytedance.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private AnalyticsManager analyticsManager;
    private LinearLayout summaryContainer;
    private LinearLayout detailContainer;
    private TextView tvTotalEvents;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        analyticsManager = AnalyticsManager.getInstance(this);

        initViews();
        displayStatistics();
    }

    private void initViews() {
        summaryContainer = findViewById(R.id.summary_container);
        detailContainer = findViewById(R.id.detail_container);
        tvTotalEvents = findViewById(R.id.tv_total_events);
        scrollView = findViewById(R.id.scroll_view);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            analyticsManager.clearAllEvents();
            displayStatistics();
        });
    }

    private void displayStatistics() {
        displaySummary();
        displayDetailedEvents();
    }

    private void displaySummary() {
        summaryContainer.removeAllViews();

        Map<String, Integer> summary = analyticsManager.getStatisticsSummary();
        int totalEvents = 0;
        for (Integer count : summary.values()) {
            totalEvents += count;
        }
        tvTotalEvents.setText("总事件数: " + totalEvents);

        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            View itemView = getLayoutInflater().inflate(R.layout.item_statistic, null);
            TextView tvLabel = itemView.findViewById(R.id.tv_label);
            TextView tvValue = itemView.findViewById(R.id.tv_value);

            tvLabel.setText(entry.getKey());
            tvValue.setText(String.valueOf(entry.getValue()));

            summaryContainer.addView(itemView);
        }
    }

    private void displayDetailedEvents() {
        detailContainer.removeAllViews();

        List<AnalyticsEvent> events = analyticsManager.getAllEvents();
        for (AnalyticsEvent event : events) {
            View itemView = getLayoutInflater().inflate(R.layout.item_event_detail, null);
            TextView tvEventType = itemView.findViewById(R.id.tv_event_type);
            TextView tvAdTitle = itemView.findViewById(R.id.tv_ad_title);
            TextView tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            TextView tvCategory = itemView.findViewById(R.id.tv_category);

            tvEventType.setText(getEventTypeDisplayName(event.getEventType()));
            tvEventType.setTextColor(getEventColor(event.getEventType()));
            tvAdTitle.setText(event.getAdTitle());
            tvTimestamp.setText(analyticsManager.getFormattedTimestamp(event.getTimestamp()));
            tvCategory.setText("分类: " + event.getCategory());

            detailContainer.addView(itemView);
        }
    }

    private String getEventTypeDisplayName(String eventType) {
        switch (eventType) {
            case AnalyticsEvent.TYPE_EXPOSURE:
                return "曝光";
            case AnalyticsEvent.TYPE_CLICK:
                return "点击";
            case AnalyticsEvent.TYPE_LIKE:
                return "点赞";
            case AnalyticsEvent.TYPE_UNLIKE:
                return "取消点赞";
            case AnalyticsEvent.TYPE_FAVORITE:
                return "收藏";
            case AnalyticsEvent.TYPE_UNFAVORITE:
                return "取消收藏";
            case AnalyticsEvent.TYPE_SHARE:
                return "分享";
            default:
                return eventType;
        }
    }

    private int getEventColor(String eventType) {
        switch (eventType) {
            case AnalyticsEvent.TYPE_EXPOSURE:
                return getResources().getColor(R.color.secondary_text);
            case AnalyticsEvent.TYPE_CLICK:
                return getResources().getColor(R.color.accent);
            case AnalyticsEvent.TYPE_LIKE:
                return getResources().getColor(R.color.like_color);
            case AnalyticsEvent.TYPE_UNLIKE:
                return getResources().getColor(R.color.secondary_text);
            case AnalyticsEvent.TYPE_FAVORITE:
                return getResources().getColor(R.color.favorite_color);
            case AnalyticsEvent.TYPE_UNFAVORITE:
                return getResources().getColor(R.color.secondary_text);
            case AnalyticsEvent.TYPE_SHARE:
                return getResources().getColor(R.color.share_color);
            default:
                return getResources().getColor(R.color.primary_text);
        }
    }
}
