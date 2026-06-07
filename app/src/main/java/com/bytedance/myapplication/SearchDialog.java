package com.bytedance.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchDialog extends Dialog {

    private Context context;
    private OnSearchResultListener listener;
    private EditText etSearch;
    private Button btnSend;
    private TextView tvResultTitle;
    private RecyclerView resultRecyclerView;
    private AdAdapter resultAdapter;
    private LinearLayout chatContainer;

    public interface OnSearchResultListener {
        void onSearchResult(List<Ad> ads);
    }

    public SearchDialog(@NonNull Context context, OnSearchResultListener listener) {
        super(context, R.style.SearchDialogStyle);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnSend = findViewById(R.id.btn_send);
        tvResultTitle = findViewById(R.id.tv_result_title);
        resultRecyclerView = findViewById(R.id.result_recycler);
        chatContainer = findViewById(R.id.chat_container);

        resultRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        resultAdapter = new AdAdapter(context, null);
        resultRecyclerView.setAdapter(resultAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> performSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        findViewById(R.id.iv_close).setOnClickListener(v -> dismiss());
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            addUserMessage(query);
            simulateSearch(query);
        }
    }

    private void addUserMessage(String message) {
        View messageView = View.inflate(context, R.layout.chat_message_user, null);
        TextView tvMessage = messageView.findViewById(R.id.tv_message);
        tvMessage.setText(message);
        chatContainer.addView(messageView);
        etSearch.setText("");
    }

    private void addBotMessage(String message) {
        View messageView = View.inflate(context, R.layout.chat_message_bot, null);
        TextView tvMessage = messageView.findViewById(R.id.tv_message);
        tvMessage.setText(message);
        chatContainer.addView(messageView);
    }

    private void simulateSearch(String query) {
        addBotMessage("我理解你想找「" + query + "」相关的广告。我先帮你把信息流筛选到 #" + query + "。");

        new android.os.Handler().postDelayed(() -> {
            List<Ad> results = MockData.searchAds(query);
            
            if (results.isEmpty()) {
                addBotMessage("没有找到匹配的广告，试试其他关键词吧。");
            } else {
                addBotMessage("我找到了 " + results.size() + " 条更可能匹配的广告：");
                tvResultTitle.setVisibility(View.VISIBLE);
                resultRecyclerView.setVisibility(View.VISIBLE);
                
                for (Ad ad : results) {
                    View resultView = View.inflate(context, R.layout.search_result_item, null);
                    TextView tvTitle = resultView.findViewById(R.id.tv_title);
                    TextView tvSource = resultView.findViewById(R.id.tv_source);
                    tvTitle.setText(ad.getTitle());
                    tvSource.setText(ad.getSource());
                    resultView.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onSearchResult(results);
                        }
                        dismiss();
                    });
                    chatContainer.addView(resultView);
                }
                
                addBotMessage("你可以关闭搜索，继续在信息流里浏览。");
            }
        }, 1000);
    }
}