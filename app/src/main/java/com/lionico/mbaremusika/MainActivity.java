package com.lionico.mbaremusika;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import com.google.firebase.FirebaseApp;

public class MainActivity extends Activity {

    private ListView listView;
    private EditText searchEdit;
    private TextView lastUpdatedText;
    private TextView statusText;
    private Button refreshButton;
    private TextView emptyView;

    private CommodityAdapter adapter;
    private CommodityManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        manager = new CommodityManager(this);
        initViews();

        manager.loadAll(new Runnable() {
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
					updateStatusText();
				}
			});

        setupListeners();
    }

    private void initViews() {
        listView = findViewById(R.id.lv_commodities);
        searchEdit = findViewById(R.id.et_search);
        lastUpdatedText = findViewById(R.id.tv_last_updated);
        statusText = findViewById(R.id.tv_status);
        refreshButton = findViewById(R.id.btn_refresh);
        emptyView = findViewById(android.R.id.empty);

        listView.setEmptyView(emptyView);
        adapter = new CommodityAdapter(this, manager.getFiltered());
        listView.setAdapter(adapter);
    }

    private void setupListeners() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					manager.loadAll(new Runnable() {
							@Override
							public void run() {
								adapter.notifyDataSetChanged();
								updateStatusText();
							}
						});
				}
			});

        searchEdit.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					manager.filter(s.toString());
					adapter.notifyDataSetChanged();
				}

				@Override
				public void afterTextChanged(Editable s) {}
			});

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Commodity c = (Commodity) parent.getItemAtPosition(position);
					manager.prepareHistoryForDetail(c);

					Intent intent = new Intent(MainActivity.this, DetailActivity.class);
					intent.putExtra("COMMODITY_NAME", c.getName());
					intent.putExtra("COMMODITY_QUANTITY", c.getQuantity());
					intent.putExtra("COMMODITY_PRICE", c.getUsdPrice());
					startActivity(intent);
				}
			});
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

    public void saveFavorites() {
        manager.saveFavorites();
    }
}
