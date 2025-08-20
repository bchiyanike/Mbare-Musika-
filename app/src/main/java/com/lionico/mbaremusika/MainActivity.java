package com.lionico.mbaremusika;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.Toast;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainActivity extends Activity {

    private ListView listView;
    private EditText searchEdit;
    private TextView lastUpdatedText;
    private TextView statusText;
    private Button refreshButton;
    private CommodityAdapter adapter;
    private List<Commodity> allCommodities;
    private List<Commodity> filteredCommodities;
    private SharedPreferences prefs;
    private TextView emptyView;

    private static final String PREFS_NAME = "MbareMusikaPrefs";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final String KEY_DATA_JSON = "data_json";
    private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        setupListeners();

        loadCachedData();
        checkAndRefreshData();
    }

    private void initViews() {
        listView = findViewById(R.id.lv_commodities);
        searchEdit = findViewById(R.id.et_search);
        lastUpdatedText = findViewById(R.id.tv_last_updated);
        statusText = findViewById(R.id.tv_status);
        refreshButton = findViewById(R.id.btn_refresh);
        emptyView = findViewById(android.R.id.empty);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        listView.setEmptyView(emptyView);
    }

    private void initData() {
        allCommodities = new ArrayList<Commodity>();
        filteredCommodities = new ArrayList<Commodity>();
        adapter = new CommodityAdapter(this, filteredCommodities);
        listView.setAdapter(adapter);
    }

    private void setupListeners() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					refreshData();
				}
			});

        searchEdit.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					filterCommodities(s.toString());
				}

				@Override
				public void afterTextChanged(Editable s) {}
			});
    }

    private void loadCachedData() {
        String jsonData = prefs.getString(KEY_DATA_JSON, "");
        if (!jsonData.isEmpty()) {
            parseCommoditiesFromJson(jsonData);
            updateLastUpdatedText();
            statusText.setVisibility(View.GONE);
        } else {
            statusText.setText("No cached data. Pull to refresh.");
            statusText.setVisibility(View.VISIBLE);
        }
    }

    private void checkAndRefreshData() {
        long lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdate > DAY_IN_MILLIS || lastUpdate == 0) {
            refreshData();
        }
    }

    private void refreshData() {
        new FetchDataTask().execute();
    }

    private void filterCommodities(String query) {
        filteredCommodities.clear();

        if (query.isEmpty()) {
            filteredCommodities.addAll(allCommodities);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Commodity commodity : allCommodities) {
                if (commodity.getName().toLowerCase().contains(lowerQuery)) {
                    filteredCommodities.add(commodity);
                }
            }
        }

        adapter.notifyDataSetChanged();
        emptyView.setText(filteredCommodities.isEmpty() ? "No matching commodities" : "");
    }

    private void updateLastUpdatedText() {
        long lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0);
        if (lastUpdate > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy HH:mm");
            String dateStr = sdf.format(new java.util.Date(lastUpdate));
            lastUpdatedText.setText("Updated: " + dateStr);
        } else {
            lastUpdatedText.setText("Never updated");
        }
    }

    private void parseCommoditiesFromJson(String jsonData) {
        allCommodities.clear();
        if (jsonData == null || jsonData.isEmpty()) {
            loadDefaultData();
            return;
        }

        try {
            String[] items = jsonData.split("\\|ITEM\\|");
            for (String item : items) {
                if (item == null || item.trim().isEmpty()) continue;

                String[] parts = item.split("\\|FIELD\\|");
                if (parts.length >= 3) {
                    allCommodities.add(new Commodity(
										   parts[0] != null ? parts[0].trim() : "",
										   parts[1] != null ? parts[1].trim() : "",
										   parts[2] != null ? parts[2].trim() : ""
									   ));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            loadDefaultData();
        }

        filteredCommodities.clear();
        filteredCommodities.addAll(allCommodities);
        adapter.notifyDataSetChanged();
    }

    private String commoditiesToJson() {
        StringBuilder json = new StringBuilder();
        for (Commodity commodity : allCommodities) {
            if (json.length() > 0) json.append("|ITEM|");
            json.append(commodity.getName()).append("|FIELD|")
                .append(commodity.getQuantity()).append("|FIELD|")
                .append(commodity.getUsdPrice());
        }
        return json.toString();
    }

    private void loadDefaultData() {
        allCommodities.clear();
        allCommodities.add(new Commodity("Apples", "Box (12 kg)", "US$12.00"));
        allCommodities.add(new Commodity("Avocado", "Fruit (Medium)", "US$0.75"));
        allCommodities.add(new Commodity("Cabbage", "Head (Large)", "US$1.00"));
        allCommodities.add(new Commodity("Tomatoes", "Box (9 kg)", "US$2.50"));
        allCommodities.add(new Commodity("Eggs", "Crate (Large)", "US$4.00"));
    }

    private class FetchDataTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            refreshButton.setEnabled(false);
            refreshButton.setText("Loading...");
            statusText.setText("Fetching latest prices...");
            statusText.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://zimpricecheck.com/price-updates/fruit-and-vegetable-prices/");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return "ERROR: HTTP " + responseCode;
                }

                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                char[] buffer = new char[4096];
                int read;

                while ((read = reader.read(buffer)) != -1) {
                    response.append(buffer, 0, read);
                }

                reader.close();
                return response.toString();

            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            refreshButton.setEnabled(true);
            refreshButton.setText("Refresh");

            if (result.startsWith("ERROR:")) {
                statusText.setText("Network error: " + result.substring(7));
                Toast.makeText(MainActivity.this, "Using cached data", Toast.LENGTH_SHORT).show();
            } else {
                parseHtmlAndUpdateData(result);
            }
        }
    }

    private void parseHtmlAndUpdateData(String html) {
        allCommodities.clear();
        int itemsFound = 0;

        try {
            Pattern rowPattern = Pattern.compile(
                "<tr[^>]*>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = rowPattern.matcher(html);

            while (matcher.find()) {
                String name = cleanText(matcher.group(1));
                String quantity = cleanText(matcher.group(2));
                String usdPrice = cleanText(matcher.group(3));

                if (name.equalsIgnoreCase("item") || name.isEmpty()) continue;

                allCommodities.add(new Commodity(name, quantity, usdPrice));
                itemsFound++;
            }

            if (itemsFound > 0) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_DATA_JSON, commoditiesToJson());
                editor.putLong(KEY_LAST_UPDATE, System.currentTimeMillis());
                editor.apply();

                statusText.setVisibility(View.GONE);
                Toast.makeText(this, "Updated " + itemsFound + " items", Toast.LENGTH_SHORT).show();
            } else {
                parseAlternativeFormat(html);
                if (allCommodities.isEmpty()) {
                    throw new Exception("No items found");
                }
            }
        } catch (Exception e) {
            statusText.setText("Parse error: " + e.getMessage());
            loadDefaultData();
        } finally {
            filteredCommodities.clear();
            filteredCommodities.addAll(allCommodities);
            adapter.notifyDataSetChanged();
            updateLastUpdatedText();
        }
    }

    private String cleanText(String text) {
        if (text == null) return "";
        return text.replace("&nbsp;", " ")
			.replaceAll("\\s+", " ")
			.trim();
    }

    private void parseAlternativeFormat(String html) {
        Pattern altPattern = Pattern.compile(
            ">([^<]+?)</td>\\s*<td[^>]*>([^<]+?)</td>\\s*<td[^>]*>([^<]+?)</td>",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = altPattern.matcher(html);

        while (matcher.find()) {
            String name = cleanText(matcher.group(1));
            String quantity = cleanText(matcher.group(2));
            String usdPrice = cleanText(matcher.group(3));

            if (!name.isEmpty() && !name.equalsIgnoreCase("item")) {
                allCommodities.add(new Commodity(name, quantity, usdPrice));
            }
        }
    }
}