package com.lionico.mbaremusika;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Set;

public class PriceAlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("com.lionico.mbaremusika.PRICE_ALERT".equals(action)) {
            String commodityId = intent.getStringExtra("COMMODITY_ID");
            String commodityName = intent.getStringExtra("COMMODITY_NAME");
            double oldPrice = intent.getDoubleExtra("OLD_PRICE", 0);
            double newPrice = intent.getDoubleExtra("NEW_PRICE", 0);

            showNotification(context, commodityName, oldPrice, newPrice);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            restartPriceMonitoring(context);
        }
    }

    private void showNotification(Context context, String name, double oldPrice, double newPrice) {
        String message = String.format("%s price changed from $%.2f to $%.2f", name, oldPrice, newPrice);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        // TODO: Replace with NotificationCompat for real notifications
        // You can use NotificationManager + PendingIntent here
    }

    private void restartPriceMonitoring(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MbareMusikaPrefs", Context.MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("favorites", null);

        if (favorites != null && !favorites.isEmpty()) {
            // TODO: Restart alarms or jobs for each favorite commodity
            Toast.makeText(context, "Price monitoring restarted", Toast.LENGTH_SHORT).show();
        }
    }
}
