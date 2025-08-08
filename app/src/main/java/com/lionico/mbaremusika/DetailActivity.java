package com.lionico.mbaremusika;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.SharedPreferences;
import android.view.ViewGroup;
import android.view.View;
import android.util.Log;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailActivity extends Activity {

    private static final String TAG = "DetailActivity";
    private static final String PREFS_NAME = "MbareMusikaPrefs";
    private static final String TEMP_HISTORY_KEY = "temp_history";

    // UI Components
    private TextView nameView;
    private TextView quantityView;
    private TextView priceView;
    private ImageView imageView;
    private ListView historyList;

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

        // Initialize views
        nameView = findViewById(R.id.tv_detail_name);
        quantityView = findViewById(R.id.tv_detail_quantity);
        priceView = findViewById(R.id.tv_detail_price);
        imageView = findViewById(R.id.iv_detail_image);
        historyList = findViewById(R.id.lv_price_history);

        // Get intent data safely
        commodityId = getIntent().getStringExtra("COMMODITY_ID"); // optional, future-proof
        commodityName = getIntent().getStringExtra("COMMODITY_NAME");
        quantity = getIntent().getStringExtra("COMMODITY_QUANTITY");
        price = getIntent().getStringExtra("COMMODITY_PRICE");

        // Set basic commodity info
        if (commodityName != null && !commodityName.isEmpty()) {
            nameView.setText(commodityName);
        }
        if (quantity != null && !quantity.isEmpty()) {
            quantityView.setText(quantity);
        }

        // Format price as USD with two decimals
        if (price != null && !price.isEmpty()) {
            try {
                double p = Double.parseDouble(price);
                priceView.setText(usdFormat.format(p)); // e.g., $12.00
            } catch (NumberFormatException nfe) {
                Log.w(TAG, "Price not a number: " + price, nfe);
                priceView.setText("$" + price);
            }
        }

        imageView.setImageResource(R.drawable.placeholder);
        imageView.setContentDescription(getString(R.string.desc_product_image));

        attachEmptyView();
        loadHistoryFromCache();
    }

    private void loadHistoryFromCache() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String raw = prefs.getString(TEMP_HISTORY_KEY, "");
        PriceHistory history = CommodityParser.parseHistory(raw);
        displayHistory(history);
    }

    private void displayHistory(PriceHistory history) {
        if (history != null && history.getRecords() != null && !history.getRecords().isEmpty()) {
            PriceHistoryAdapter adapter = new PriceHistoryAdapter(this, history.getRecords());
            historyList.setAdapter(adapter);
        } else {
            // trigger empty view
            historyList.setAdapter(null);
        }
    }

    // Programmatically attach an empty view to the ListView with theme-aware colors
    private void attachEmptyView() {
        ViewGroup root = findViewById(android.R.id.content);
        if (root == null) return;

        TextView emptyView = new TextView(this);
        emptyView.setText(getString(R.string.msg_no_data));
        emptyView.setTextSize(16);
        emptyView.setGravity(android.view.Gravity.CENTER);
        emptyView.setPadding(24, 24, 24, 24);
        emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        emptyView.setVisibility(View.GONE);
        emptyView.setId(android.R.id.empty);

        // Place the empty view over content but not intercepting layout
        if (root instanceof FrameLayout) {
            ((FrameLayout) root).addView(emptyView,
                new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
        } else if (root.getChildCount() > 0 && root.getChildAt(0) instanceof ViewGroup) {
            ((ViewGroup) root.getChildAt(0)).addView(emptyView,
                new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
        }

        historyList.setEmptyView(emptyView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear temporary history only when Activity is being destroyed,
        // not merely paused (avoids losing data when app goes to background)
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .remove(TEMP_HISTORY_KEY)
            .apply();
    }
}
