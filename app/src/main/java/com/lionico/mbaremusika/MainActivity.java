package com.lionico.mbaremusika;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private EditText searchEdit;
    private TextView lastUpdatedText;
    private TextView statusText;
    private TextView emptyView;
    private MaterialToolbar toolbar;

    private CommodityAdapter adapter;
    private CommodityManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            manager = new CommodityManager(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize CommodityManager", e);
            statusText.setText("Error initializing app");
            statusText.setVisibility(View.VISIBLE);
            return;
        }

        initViews();
        initRecycler();
        initToolbar();
        initSearch();
        loadData();
    }

    private void initViews() {
        try {
            recyclerView = findViewById(R.id.rv_commodities);
            searchEdit = findViewById(R.id.et_search);
            lastUpdatedText = findViewById(R.id.tv_last_updated);
            statusText = findViewById(R.id.tv_status);
            emptyView = findViewById(R.id.empty_view);
            toolbar = findViewById(R.id.toolbar);

            if (recyclerView == null || searchEdit == null || lastUpdatedText == null ||
                statusText == null || emptyView == null || toolbar == null) {
                Log.e(TAG, "One or more views not found in activity_main.xml");
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            finish();
        }
    }

    private void initRecycler() {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            List<Commodity> filtered = manager.getFiltered();
            if (filtered == null) {
                Log.e(TAG, "Filtered commodities list is null");
                filtered = new ArrayList<>();
            }
            adapter = new CommodityAdapter(this, filtered, this::openDetail);
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing RecyclerView", e);
            statusText.setText("Error loading commodities");
            statusText.setVisibility(View.VISIBLE);
        }
    }

    private void initToolbar() {
        try {
            toolbar.setNavigationOnClickListener(v -> loadData());
        } catch (Exception e) {
            Log.e(TAG, "Error setting toolbar navigation", e);
        }
    }

    private void initSearch() {
        try {
            searchEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        manager.filter(s.toString());
                        adapter.notifyDataSetChanged();
                        toggleEmptyView();
                    } catch (Exception e) {
                        Log.e(TAG, "Error filtering commodities", e);
                        statusText.setText("Error searching");
                        statusText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing search", e);
        }
    }

    private void loadData() {
        try {
            manager.loadAll(() -> runOnUiThread(() -> {
                try {
                    adapter.notifyDataSetChanged();
                    updateStatusText();
                    toggleEmptyView();
                } catch (Exception e) {
                    Log.e(TAG, "Error updating UI after data load", e);
                    statusText.setText("Error loading data");
                    statusText.setVisibility(View.VISIBLE);
                }
            }));
        } catch (Exception e) {
            Log.e(TAG, "Error loading data", e);
            statusText.setText("Failed to load data");
            statusText.setVisibility(View.VISIBLE);
        }
    }

    private void toggleEmptyView() {
        try {
            List<Commodity> filtered = manager.getFiltered();
            if (filtered == null || filtered.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error toggling empty view", e);
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void updateStatusText() {
        try {
            long lastUpdate = manager.getLastUpdate();
            if (lastUpdate == 0) {
                lastUpdatedText.setText("Never updated");
                statusText.setText("No cached data. Pull to refresh.");
                statusText.setVisibility(View.VISIBLE);
            } else {
                String dateStr = manager.formatDate(lastUpdate);
                lastUpdatedText.setText("Updated: " + dateStr);
                statusText.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating status text", e);
            statusText.setText("Error updating status");
            statusText.setVisibility(View.VISIBLE);
        }
    }

    private void openDetail(Commodity c) {
        try {
            if (c == null) {
                Log.e(TAG, "Commodity is null");
                return;
            }
            manager.prepareHistoryForDetail(c);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("COMMODITY_NAME", c.getName());
            intent.putExtra("COMMODITY_QUANTITY", c.getQuantity());
            intent.putExtra("COMMODITY_PRICE", c.getUsdPrice());
            intent.putExtra("COMMODITY_ID", c.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening DetailActivity", e);
            statusText.setText("Error viewing details");
            statusText.setVisibility(View.VISIBLE);
        }
    }

    public void saveFavorites() {
        try {
            manager.saveFavorites();
        } catch (Exception e) {
            Log.e(TAG, "Error saving favorites", e);
        }
    }
}
