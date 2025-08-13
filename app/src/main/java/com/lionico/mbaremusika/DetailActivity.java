package com.lionico.mbaremusika;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private static final String PREFS_NAME = "MbareMusikaPrefs";
    private static final String TEMP_HISTORY_KEY = "temp_history";
    private static final String CHANNEL_ID = "price_alert_channel";

    private TextView nameView;
    private TextView quantityView;
    private TextView priceView;
    private ImageView imageView;
    private EmptyRecyclerView historyList;
    private TextView emptyView;

    private String commodityId;
    private String commodityName;
    private String quantity;
    private String price;

    private final NumberFormat usdFormat = NumberFormat.getCurrencyInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        try {
            nameView = findViewById(R.id.tv_detail_name);
            quantityView = findViewById(R.id.tv_detail_quantity);
            priceView = findViewById(R.id.tv_detail_price);
            imageView = findViewById(R.id.iv_detail_image);
            historyList = findViewById(R.id.lv_price_history);
            emptyView = findViewById(R.id.tv_empty_history);

            if (nameView == null || quantityView == null || priceView == null ||
                imageView == null || historyList == null || emptyView == null) {
                Log.e(TAG, "One or more views not found in activity_detail.xml");
                finish();
                return;
            }

            historyList.setLayoutManager(new LinearLayoutManager(this));
            historyList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
            historyList.setEmptyView(emptyView);

            commodityId = getIntent().getStringExtra("COMMODITY_ID");
            commodityName = getIntent().getStringExtra("COMMODITY_NAME");
            quantity = getIntent().getStringExtra("COMMODITY_QUANTITY");
            price = getIntent().getStringExtra("COMMODITY_PRICE");

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

            loadHistoryFromCache();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Price Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for price changes");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
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
                historyList.setEmptyViewVisible(false);
            } else {
                historyList.setAdapter(null);
                historyList.setEmptyViewVisible(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying history", e);
            historyList.setAdapter(null);
            historyList.setEmptyViewVisible(true);
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

    public static class EmptyRecyclerView extends RecyclerView {
        private View emptyView;

        public EmptyRecyclerView(android.content.Context context, android.util.AttributeSet attrs) {
            super(context, attrs);
        }

        public void setEmptyView(View emptyView) {
            this.emptyView = emptyView;
            checkIfEmpty();
        }

        public void setEmptyViewVisible(boolean visible) {
            if (emptyView != null) {
                emptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }

        private void checkIfEmpty() {
            if (emptyView != null && getAdapter() != null) {
                emptyView.setVisibility(getAdapter().getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public void setAdapter(Adapter adapter) {
            super.setAdapter(adapter);
            checkIfEmpty();
        }
    }
}
