package com.lionico.mbaremusika;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private final List<Commodity> allCommodities = new ArrayList<>();
    private final List<Commodity> filteredCommodities = new ArrayList<>();
    private final Map<String, PriceHistory> priceHistories = new HashMap<>();

    public CommodityManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void loadAll(final Runnable callback) {
        loadFavorites();
        loadPriceHistories();

        new Thread(() -> {
            try {
                URL url = new URL("https://api.jsonbin.io/v3/b/68949862f7e7a370d1f64e61/latest");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();

                String json = builder.toString();
                parseJsonbinData(json);
                cacheData();
            } catch (Exception e) {
                Log.e(TAG, "Error loading from JSONbin", e);
                loadCachedData();
            }

            filter("");
            callback.run();
        }).start();
    }

    private void parseJsonbinData(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject record = root.getJSONObject("record");

            allCommodities.clear();
            JSONArray currentPrices = record.getJSONArray("currentPrices");
            for (int i = 0; i < currentPrices.length(); i++) {
                JSONObject obj = currentPrices.getJSONObject(i);
                String name = obj.getString("itemName");
                String quantity = obj.getString("quantity");
                String price = String.valueOf(obj.getDouble("priceUSD"));
                String id = obj.getString("id");

                Commodity commodity = new Commodity(name, quantity, price);
                commodity.setId(id);
                allCommodities.add(commodity);
            }

            JSONArray historyArray = record.getJSONArray("priceHistory");
            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject day = historyArray.getJSONObject(i);
                String date = day.getString("date");
                JSONArray prices = day.getJSONArray("prices");

                for (int j = 0; j < prices.length(); j++) {
                    JSONObject entry = prices.getJSONObject(j);
                    String name = entry.getString("itemName");
                    String quantity = entry.getString("quantity");
                    double price = entry.getDouble("priceUSD");
                    String id = entry.getString("id");

                    long timestamp = parseDateToMillis(date);
                    PriceHistory history = priceHistories.getOrDefault(id, new PriceHistory(name));
                    history.addRecord(price, timestamp);
                    priceHistories.put(id, history);
                }
            }

            prefs.edit()
                .putString(KEY_DATA_JSON, json)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .apply();

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSONbin data", e);
        }
    }

    private long parseDateToMillis(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr).getTime();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
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
        Set<String> favorites = new HashSet<>();
        for (Commodity commodity : allCommodities) {
            if (commodity.isFavorite()) {
                favorites.add(commodity.getId());
            }
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public void prepareHistoryForDetail(Commodity commodity) {
        PriceHistory history = priceHistories.get(commodity.getId());
        if (history != null) {
            prefs.edit().putString(TEMP_HISTORY_KEY, serializeHistory(history)).apply();
        }
    }

    private String serializeHistory(PriceHistory history) {
        StringBuilder sb = new StringBuilder();
        sb.append(history.getName()).append("|");
        for (PriceHistory.PriceRecord record : history.getRecords()) {
            sb.append(record.getPrice()).append(",").append(record.getTimestamp()).append(";");
        }
        return sb.toString();
    }

    public long getLastUpdate() {
        return prefs.getLong(KEY_LAST_UPDATE, 0);
    }

    public String formatDate(long millis) {
        return new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    private void loadCachedData() {
        String raw = prefs.getString(KEY_DATA_JSON, "");
        if (!raw.isEmpty()) {
            parseJsonbinData(raw);
        }
    }

    private void loadFavorites() {
        Set<String> favorites = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
        for (Commodity commodity : allCommodities) {
            commodity.setFavorite(favorites.contains(commodity.getId()));
        }
    }

    private void loadPriceHistories() {
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            if (entry.getKey().startsWith(KEY_HISTORY_PREFIX)) {
                String id = entry.getKey().substring(KEY_HISTORY_PREFIX.length());
                String data = (String) entry.getValue();
                PriceHistory history = parseHistory(data);
                if (history != null) {
                    priceHistories.put(id, history);
                }
            }
        }
    }

    private PriceHistory parseHistory(String data) {
        if (data == null || data.isEmpty()) return null;

        String[] parts = data.split("\\|");
        if (parts.length < 2) return null;

        String name = parts[0];
        PriceHistory history = new PriceHistory(name);

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
            editor.putString(KEY_HISTORY_PREFIX + entry.getKey(), serializeHistory(entry.getValue()));
        }

        editor.apply();
    }

    public PriceHistory getHistoryFor(String id) {
        return priceHistories.get(id);
    }
}
