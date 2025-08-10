package com.lionico.mbaremusika;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private static final String PREFS_NAME = "MbareMusikaPrefs";
    private static final String TEMP_HISTORY_KEY = "temp_history";

    // UI Components
    private TextView nameView;
    private TextView quantityView;
    private TextView priceView;
    private ImageView imageView;
    private RecyclerView historyList;

    // Data
    private String commodityId;
    private String commodityName;
    private String quantity;
    private String price; // raw string from intent

    private final NumberFormat usdFormat =
            NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        try {
            // Initialize views
            nameView = findViewById(R.id.tv_detail_name);
            quantityView = findViewById(R.id.tv_detail_quantity);
            priceView = findViewById(R.id.tv_detail_price);
            imageView = findViewById(R.id.iv_detail_image);
            historyList = findViewById(R.id.lv_price_history);

            if (nameView == null || quantityView == null || priceView == null ||
                imageView == null || historyList == null) {
                Log.e(TAG, "One or more views not found in activity_detail.xml");
                finish();
                return;
            }

            historyList.setLayoutManager(new LinearLayoutManager(this));

            // Get intent data safely
            commodityId = getIntent().getStringExtra("COMMODITY_ID");
            commodityName = getIntent().getStringExtra("COMMODITY_NAME");
            quantity = getIntent().getStringExtra("COMMODITY_QUANTITY");
            price = getIntent().getStringExtra("COMMODITY_PRICE");

            // Set basic commodity info
            if (commodityName != null && !commodityName.isEmpty()) {
                nameView.setText(commodityName);
            } else {
                Log.w(TAG, "Commodity name is null or empty");
                nameView.setText("Unknown");
            }
            if (quantity != null && !quantity.isEmpty()) {
                quantityView.setText(quantity);
            } else {
                Log.w(TAG, "Quantity is null or empty");
                quantityView.setText("N/A");
            }

            // Format price as USD with two decimals
            if (price != null && !price.isEmpty()) {
                try {
                    double p = Double.parseDouble(price);
                    priceView.setText(usdFormat.format(p));
                } catch (NumberFormatException nfe) {
                    Log.w(TAG, "Price not a number: " + price, nfe);
                    priceView.setText("$" + price);
                }
            } else {
                Log.w(TAG, "Price is null or empty");
                priceView.setText("$0.00");
            }

            try {
                imageView.setImageResource(R.drawable.placeholder);
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Placeholder drawable not found", e);
                imageView.setImageDrawable(null);
            }
            try {
                imageView.setContentDescription(getString(R.string.desc_product_image));
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "String desc_product_image not found", e);
                imageView.setContentDescription("Product image");
            }

            attachEmptyView();
            loadHistoryFromCache();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    private void loadHistoryFromCache() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String raw = prefs.getString(TEMP_HISTORY_KEY, "");
            if (raw.isEmpty()) {
                Log.w(TAG, "No cached history found");
                historyList.setAdapter(null);
                return;
            }
            PriceHistory history = CommodityParser.parseHistory(raw);
            if (history == null) {
                Log.w(TAG, "Failed to parse cached history: " + raw);
                historyList.setAdapter(null);
                return;
            }
            displayHistory(history);
        } catch (Exception e) {
            Log.e(TAG, "Error loading history", e);
            historyList.setAdapter(null);
        }
    }

    private void displayHistory(PriceHistory history) {
        try {
            if (history != null && history.getRecords() != null && !history.getRecords().isEmpty()) {
                PriceHistoryAdapter adapter = new PriceHistoryAdapter(this, history.getRecords());
                historyList.setAdapter(adapter);
            } else {
                historyList.setAdapter(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying history", e);
            historyList.setAdapter(null);
        }
    }

    private void attachEmptyView() {
        try {
            TextView emptyView = new TextView(this);
            try {
                emptyView.setText(getString(R.string.msg_no_data));
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "String msg_no_data not found", e);
                emptyView.setText("No data available");
            }
            emptyView.setTextSize(16);
            emptyView.setGravity(android.view.Gravity.CENTER);
            emptyView.setPadding(24, 24, 24, 24);
            emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            emptyView.setVisibility(View.GONE);
            historyList.setEmptyView(emptyView);
            DividerItemDecoration divider = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
            historyList.addItemDecoration(divider);
        } catch (Exception e) {
            Log.e(TAG, "Error attaching empty view", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .remove(TEMP_HISTORY_KEY)
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing SharedPreferences", e);
        }
    }
}
