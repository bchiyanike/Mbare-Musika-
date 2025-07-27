package com.lionico.mbaremusika;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private String commodityName;
    private String quantity;
    private String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize views
        nameView = (TextView) findViewById(R.id.tv_detail_name);
        quantityView = (TextView) findViewById(R.id.tv_detail_quantity);
        priceView = (TextView) findViewById(R.id.tv_detail_price);
        imageView = (ImageView) findViewById(R.id.iv_detail_image);
        historyList = (ListView) findViewById(R.id.lv_price_history);

        // Get intent data
        commodityName = getIntent().getStringExtra("COMMODITY_NAME");
        quantity = getIntent().getStringExtra("COMMODITY_QUANTITY");
        price = getIntent().getStringExtra("COMMODITY_PRICE");

        // Set basic commodity info
        if (commodityName != null) nameView.setText(commodityName);
        if (quantity != null) quantityView.setText(quantity);
        if (price != null) priceView.setText("$" + price);

        imageView.setImageResource(R.drawable.placeholder);
        loadHistoryFromFirebase();
    }

    private void loadHistoryFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("commodities").document(commodityName)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot doc) {
                    List<Map<String, Object>> raw = (List<Map<String, Object>>) doc.get("history");
                    if (raw != null && raw.size() > 0) {
                        PriceHistory history = new PriceHistory(commodityName);
                        for (Map<String, Object> item : raw) {
                            Object priceObj = item.get("price");
                            Object timeObj = item.get("timestamp");

                            if (priceObj != null && timeObj != null) {
                                try {
                                    double p = Double.parseDouble(priceObj.toString());
                                    long t = Long.parseLong(timeObj.toString());
                                    history.addRecord(p, t);
                                } catch (Exception ex) {
                                    Log.w(TAG, "Skipping malformed history entry", ex);
                                }
                            }
                        }
                        displayHistory(history);
                    } else {
                        loadHistoryFromCache();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error loading history from Firebase", e);
                    loadHistoryFromCache();
                }
            });
    }

    private void loadHistoryFromCache() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String raw = prefs.getString(TEMP_HISTORY_KEY, "");
        PriceHistory history = CommodityParser.parseHistory(raw);
        displayHistory(history);
    }

    private void displayHistory(PriceHistory history) {
        if (history != null && history.getRecords().size() > 0) {
            PriceHistoryAdapter adapter = new PriceHistoryAdapter(this, history.getRecords());
            historyList.setAdapter(adapter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear temporary history when leaving the activity
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .remove(TEMP_HISTORY_KEY)
            .apply();
    }
}
