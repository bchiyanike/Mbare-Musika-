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

    public DataFetcher(Context context, Callback onSuccess, Callback onError) {
        this.context = context;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "ERROR: Unknown failure";

                try {
                    URL url = new URL("https://zimpricecheck.com/price-updates/fruit-and-vegetable-prices/");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(15000);

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

                final String finalResult = result; // ✅ capture result in a final variable

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResult.startsWith("ERROR:")) {
                            onError.run(finalResult.substring(7));
                        } else {
                            onSuccess.run(finalResult);
                        }
                    }
                });
            }
        }).start();
    }
}
