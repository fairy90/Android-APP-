package com.bytedance.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdAdapter adAdapter;
    private List<Ad> adList;
    private TabLayout tabLayout;
    private TextView tvFilter;
    private Button btnSearch;
    private EditText etSearch;
    private TextView tvLoadMore;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayoutManager layoutManager;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private String currentCategory = "精选";
    private int clickedPosition = -1;
    private Set<String> visitedTabs = new HashSet<>();
    private AnalyticsManager analyticsManager;
    private ImageView ivStatistics;
    private boolean isInSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        setupListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        tabLayout = findViewById(R.id.tab_layout);
        tvFilter = findViewById(R.id.tv_filter);
        btnSearch = findViewById(R.id.btn_search);
        etSearch = findViewById(R.id.tv_search_hint);
        tvLoadMore = findViewById(R.id.tv_load_more);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        ivStatistics = findViewById(R.id.iv_statistics);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        swipeRefresh.setColorSchemeResources(R.color.accent);

        etSearch.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        etSearch.setHintTextColor(getResources().getColor(R.color.hint_text));
        
        analyticsManager = AnalyticsManager.getInstance(this);
    }

    private void initData() {
        adList = new ArrayList<>();
        adList.addAll(MockData.refreshAds(currentCategory));
        adAdapter = new AdAdapter(this, adList);
        adAdapter.setCurrentCategory(currentCategory);
        recyclerView.setAdapter(adAdapter);
        checkHasMore();
    }

    private void checkHasMore() {
        hasMore = adList.size() < MockData.getTotalPages(currentCategory) * 6;
        adAdapter.setHasMore(hasMore);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> {
            if ("搜索".equals(currentCategory)) {
                swipeRefresh.setRefreshing(false);
                return;
            }
            refreshData();
        });
//是给 RecyclerView 设置一个滚动监听器，用于实现上拉加载更多（分页加载）的功能。当用户滚动到底部时，自动触发 loadMore() 方法加载下一页数据
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (dy > 0 && !isLoading && hasMore) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                        loadMore();
                    }
                }
            }
        });

        adAdapter.setOnAdClickListener((ad, position) -> {
            clickedPosition = position;
            analyticsManager.trackClick(ad, currentCategory);
            Intent intent = new Intent(MainActivity.this, AdDetailActivity.class);
            intent.putExtra(AdDetailActivity.EXTRA_AD, ad);
            startActivityForResult(intent, REQUEST_CODE_AD_DETAIL);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String newCategory = tab.getText().toString();
                
                // 更新当前分类
                currentCategory = newCategory;
                adAdapter.setCurrentCategory(currentCategory);
                visitedTabs.add(newCategory);
                tvFilter.setText("当前筛选");
                MockData.resetPage();
                hasMore = true;
                isInSearchMode = false;
                adAdapter.setHasMore(true);

                // 立即刷新数据并滚动到顶部
                refreshData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String selectedCategory = tab.getText().toString();
                
                if (isInSearchMode && selectedCategory.equals(currentCategory)) {
                    MockData.resetPage();
                    hasMore = true;
                    isInSearchMode = false;
                    adAdapter.setHasMore(true);
                    tvFilter.setText("当前筛选");
                    refreshData();
                    recyclerView.scrollToPosition(0);
                }
            }
        });

        btnSearch.setOnClickListener(v -> {
            String query = etSearch.getText().toString().trim();
            if(!query.isEmpty()){
                performSearch(query);
            }
        });
        
        ivStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
    }

    private static final int REQUEST_CODE_AD_DETAIL = 1001;
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_AD_DETAIL && resultCode == AdDetailActivity.RESULT_CODE_UPDATE) {
            if (data != null && clickedPosition >= 0 && clickedPosition < adList.size()) {
                Ad updatedAd = (Ad) data.getSerializableExtra(AdDetailActivity.EXTRA_AD);
                if (updatedAd != null) {
                    // 更新广告的状态（点赞、收藏）
                    Ad existingAd = adList.get(clickedPosition);
                    existingAd.setLiked(updatedAd.isLiked());
                    existingAd.setLikes(updatedAd.getLikes());
                    existingAd.setFavorited(updatedAd.isFavorited());
                    existingAd.setFavorites(updatedAd.getFavorites());
                    
                    // 使用 notifyDataSetChanged 确保视图正确刷新
                    adAdapter.notifyDataSetChanged();
                }
            }
            clickedPosition = -1;
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }





    private void refreshData() {
        swipeRefresh.setRefreshing(true);
        hasMore = true;
        adAdapter.setHasMore(true);
        
        // 先立即滚动到顶部，提升用户体验
        layoutManager.scrollToPositionWithOffset(0, 0);
        
        new Handler().postDelayed(() -> {
            MockData.resetPage();
            List<Ad> newData = MockData.refreshAds1(currentCategory);
            analyticsManager.restoreAllAdStates(newData);
            adList.clear();
            adList.addAll(newData);
            adAdapter.notifyDataSetChanged();
            
            swipeRefresh.setRefreshing(false);
            checkHasMore();
        }, 1500);  // 延迟500ms刷新数据
    }

    private void loadMore() {
        if (isInSearchMode) {
            adAdapter.setHasMore(false);
            isLoading = false;
            return;
        }
        
        if ("搜索".equals(currentCategory)) {
            tvLoadMore.setText("搜索模式不支持加载更多");
            isLoading = false;
            return;
        }
        
        if (isLoading) return;
        
        isLoading = true;

        new Handler().postDelayed(() -> {
            List<Ad> moreAds = MockData.loadMoreAds(currentCategory);
            analyticsManager.restoreAllAdStates(moreAds);
            
            if (moreAds.size() > 0) {
                int startPos = adList.size();
                adList.addAll(moreAds);
                adAdapter.notifyItemRangeInserted(startPos, moreAds.size());
                checkHasMore();
            } else {
                hasMore = false;
                adAdapter.setHasMore(false);
            }

            isLoading = false;
        }, 1500);
    }

    private void performSearch(String query) {
        swipeRefresh.setRefreshing(true);
        isInSearchMode = true;
        isLoading = false;
        hasMore = false;
        adAdapter.setHasMore(false);
        
        new Handler().postDelayed(() -> {
            List<Ad> searchResults = MockData.searchAds(query, currentCategory);
            analyticsManager.restoreAllAdStates(searchResults);
            adList.clear();
            adList.addAll(searchResults);
            adAdapter.notifyDataSetChanged();
            tvFilter.setText("当前筛选：" + currentCategory + " · \"" + query + "\" 的搜索结果");
            swipeRefresh.setRefreshing(false);
            
            etSearch.setText("");
        }, 800);
    }
}