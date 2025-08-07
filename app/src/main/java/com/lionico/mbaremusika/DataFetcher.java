package com.lionico.mbaremusika;

import android.content.Context;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DataFetcher {

    public interface Callback {
        void run(String result);
    }

    private Context context;
    private Callback onSuccess;
    private Callback onError;
    private String sourceUrl;
    private String apiKey;

    public DataFetcher(Context context, String sourceUrl, Callback onSuccess, Callback onError) {
        this.context = context;
        this.sourceUrl = sourceUrl;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    // Optional constructor for JSONbin with API key
    public DataFetcher(Context context, String sourceUrl, String apiKey, Callback onSuccess, Callback onError) {
        this.context = context;
        this.sourceUrl = sourceUrl;
        this.apiKey = apiKey;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    public void start() {
        new Thread(() -> {
            String result = "ERROR: Unknown failure";

            try {
                URL url = new URL(sourceUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);

                if (apiKey != null && !apiKey.isEmpty()) {
                    conn.setRequestProperty("X-Master-Key", apiKey);
                }

                int code = conn.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    result = "ERROR: HTTP " + code;
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    reader.close();
                    result = builder.toString();
                }

                conn.disconnect();
            } catch (Exception e) {
                result = "ERROR: " + e.getMessage();
            }

            final String finalResult = result;

            new Handler(context.getMainLooper()).post(() -> {
                if (finalResult.startsWith("ERROR:")) {
                    onError.run(finalResult.substring(7));
                } else {
                    onSuccess.run(finalResult);
                }
            });
        }).start();
    }
}
