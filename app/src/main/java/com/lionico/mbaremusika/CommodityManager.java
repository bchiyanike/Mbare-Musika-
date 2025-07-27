package com.lionico.mbaremusika;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CommodityManager {

    private static final String TAG = "CommodityManager";
    private static final String PREFS_NAME = "MbareMusikaPrefs";
    private static final String KEY_DATA_JSON = "data_json";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_HISTORY_PREFIX = "history_";
    private static final String TEMP_HISTORY_KEY = "temp_history";

    private final Context context;
    private final SharedPreferences prefs;
    private final List<Commodity> allCommodities = new ArrayList<Commodity>();
    private final List<Commodity> filteredCommodities = new ArrayList<Commodity>();
    private final Map<String, PriceHistory> priceHistories = new HashMap<String, PriceHistory>();

    public CommodityManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void loadAll(final Runnable callback) {
        loadFavorites();
        loadPriceHistories();

        FirebaseFirestore.getInstance()
            .collection("commodities")
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    allCommodities.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            String name = doc.getString("name");
                            String quantity = doc.getString("quantity");
                            String price = doc.getString("price");
                            String date = doc.getString("date");

                            if (name != null && quantity != null && price != null) {
                                Commodity commodity = new Commodity(name, quantity, price);
                                allCommodities.add(commodity);

                                Object historyObj = doc.get("history");
                                if (historyObj != null && historyObj instanceof List) {
                                    List<Map<String, Object>> historyList = (List<Map<String, Object>>) historyObj;
                                    PriceHistory history = new PriceHistory(name); // Set name in constructor
                                    for (Map<String, Object> entry : historyList) {
                                        try {
                                            double entryPrice = Double.parseDouble(entry.get("price").toString());
                                            long timestamp = Long.parseLong(entry.get("timestamp").toString());
                                            history.addRecord(entryPrice, timestamp);
                                        } catch (Exception e) {
                                            Log.w(TAG, "Skipping invalid history entry", e);
                                        }
                                    }
                                    priceHistories.put(name, history);
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error parsing document", e);
                        }
                    }

                    filter("");
                    cacheData();
                    callback.run();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error loading from Firestore", e);
                    loadCachedData();
                    filter("");
                    callback.run();
                }
            });
    }

    public List<Commodity> getFiltered() {
        return Collections.unmodifiableList(filteredCommodities);
    }

    public void filter(String query) {
        filteredCommodities.clear();
        String lowerQuery = query.toLowerCase();
        for (Commodity commodity : allCommodities) {
            if (commodity.getName().toLowerCase().contains(lowerQuery)) {
                filteredCommodities.add(commodity);
            }
        }
    }

    public void saveFavorites() {
        Set<String> favorites = new HashSet<String>();
        for (Commodity commodity : allCommodities) {
            if (commodity.isFavorite()) {
                favorites.add(commodity.getName());
            }
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public void prepareHistoryForDetail(Commodity commodity) {
        PriceHistory history = priceHistories.get(commodity.getName());
        if (history != null) {
            prefs.edit().putString(TEMP_HISTORY_KEY, serializeHistory(history)).apply();
        }
    }

    private String serializeHistory(PriceHistory history) {
        StringBuilder sb = new StringBuilder();
        sb.append(history.getName()).append("|"); // Store commodity name first
        for (PriceHistory.PriceRecord record : history.getRecords()) {
            sb.append(record.getPrice()).append(",")
				.append(record.getTimestamp()).append(";");
        }
        return sb.toString();
    }

    public long getLastUpdate() {
        return prefs.getLong(KEY_LAST_UPDATE, 0);
    }

    public String formatDate(long millis) {
        return new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            .format(new Date(millis));
    }

    private void loadCachedData() {
        String raw = prefs.getString(KEY_DATA_JSON, "");
        allCommodities.clear();
        if (!raw.isEmpty()) {
            // Implement your commodity parsing logic here
        }
    }

    private void loadFavorites() {
        Set<String> favorites = prefs.getStringSet(KEY_FAVORITES, new HashSet<String>());
        for (Commodity commodity : allCommodities) {
            commodity.setFavorite(favorites.contains(commodity.getName()));
        }
    }

    private void loadPriceHistories() {
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getKey().startsWith(KEY_HISTORY_PREFIX)) {
                String name = entry.getKey().substring(KEY_HISTORY_PREFIX.length());
                String data = (String) entry.getValue();
                PriceHistory history = parseHistory(data);
                if (history != null) {
                    priceHistories.put(name, history);
                }
            }
        }
    }

    private PriceHistory parseHistory(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        String[] parts = data.split("\\|");
        if (parts.length < 2) return null;

        String commodityName = parts[0];
        PriceHistory history = new PriceHistory(commodityName);

        String[] records = parts[1].split(";");
        for (String record : records) {
            if (!record.isEmpty()) {
                String[] values = record.split(",");
                if (values.length == 2) {
                    try {
                        double price = Double.parseDouble(values[0]);
                        long timestamp = Long.parseLong(values[1]);
                        history.addRecord(price, timestamp);
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid history record format: " + record);
                    }
                }
            }
        }
        return history;
    }

    private void cacheData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());

        for (Map.Entry<String, PriceHistory> entry : priceHistories.entrySet()) {
            editor.putString(KEY_HISTORY_PREFIX + entry.getKey(), 
							 serializeHistory(entry.getValue()));
        }

        editor.apply();
    }

    public PriceHistory getHistoryFor(String name) {
        return priceHistories.get(name);
    }
}
