package com.lionico.mbaremusika;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends Activity {

    private ListView listView;
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

        // Initialize views
        listView = findViewById(R.id.lv_commodities);
        searchEdit = findViewById(R.id.et_search);
        lastUpdatedText = findViewById(R.id.tv_last_updated);
        statusText = findViewById(R.id.tv_status);
        emptyView = findViewById(android.R.id.empty);
        toolbar = findViewById(R.id.toolbar);

        listView.setEmptyView(emptyView);

        adapter = new CommodityAdapter(this, manager.getFiltered());
        listView.setAdapter(adapter);

        // Setup refresh click on toolbar navigation icon
        toolbar.setNavigationOnClickListener(v -> {
            manager.loadAll(() -> {
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    updateStatusText();
                });
            });
        });

        // Setup search filter
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                manager.filter(s.toString());
                adapter.notifyDataSetChanged();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Item click opens DetailActivity
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Commodity c = (Commodity) parent.getItemAtPosition(position);
            manager.prepareHistoryForDetail(c);

            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("COMMODITY_NAME", c.getName());
            intent.putExtra("COMMODITY_QUANTITY", c.getQuantity());
            intent.putExtra("COMMODITY_PRICE", c.getUsdPrice());
            intent.putExtra("COMMODITY_ID", c.getId());
            startActivity(intent);
        });

        // Initial load
        manager.loadAll(() -> {
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                updateStatusText();
            });
        });
    }

    private void updateStatusText() {
        long lastUpdate = manager.getLastUpdate();
        if (lastUpdate == 0) {
            lastUpdatedText.setText("Never updated");
            statusText.setText("No cached data. Pull to refresh.");
            statusText.setVisibility(TextView.VISIBLE);
        } else {
            String dateStr = manager.formatDate(lastUpdate);
            lastUpdatedText.setText("Updated: " + dateStr);
            statusText.setVisibility(TextView.GONE);
        }
    }

    public void saveFavorites() {
        manager.saveFavorites();
    }
}
