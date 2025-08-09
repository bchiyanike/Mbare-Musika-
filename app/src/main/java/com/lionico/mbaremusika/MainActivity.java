package com.lionico.mbaremusika;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

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

        manager = new CommodityManager(this);

        initViews();
        initRecycler();
        initToolbar();
        initSearch();

        loadData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rv_commodities);
        searchEdit = findViewById(R.id.et_search);
        lastUpdatedText = findViewById(R.id.tv_last_updated);
        statusText = findViewById(R.id.tv_status);
        emptyView = findViewById(R.id.empty_view);
        toolbar = findViewById(R.id.toolbar);
    }

    private void initRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommodityAdapter(this, manager.getFiltered(), this::openDetail);
        recyclerView.setAdapter(adapter);
    }

    private void initToolbar() {
        toolbar.setNavigationOnClickListener(v -> loadData());
    }

    private void initSearch() {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                manager.filter(s.toString());
                adapter.notifyDataSetChanged();
                toggleEmptyView();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadData() {
        manager.loadAll(() -> runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            updateStatusText();
            toggleEmptyView();
        }));
    }

    private void toggleEmptyView() {
        if (manager.getFiltered().isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatusText() {
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
    }

    private void openDetail(Commodity c) {
        manager.prepareHistoryForDetail(c);
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("COMMODITY_NAME", c.getName());
        intent.putExtra("COMMODITY_QUANTITY", c.getQuantity());
        intent.putExtra("COMMODITY_PRICE", c.getUsdPrice());
        intent.putExtra("COMMODITY_ID", c.getId());
        startActivity(intent);
    }

    public void saveFavorites() {
        manager.saveFavorites();
    }
}
