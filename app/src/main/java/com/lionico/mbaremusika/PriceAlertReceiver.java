package com.lionico.mbaremusika;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;
import java.util.Set;

public class PriceAlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.lionico.mbaremusika.PRICE_ALERT")) {
            String commodityName = intent.getStringExtra("COMMODITY_NAME");
            double oldPrice = intent.getDoubleExtra("OLD_PRICE", 0);
            double newPrice = intent.getDoubleExtra("NEW_PRICE", 0);

            // Show notification
            showNotification(context, commodityName, oldPrice, newPrice);
        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Restart price monitoring after reboot
            restartPriceMonitoring(context);
        }
    }

    private void showNotification(Context context, String name, double oldPrice, double newPrice) {
        String message = name + " price changed from $" + oldPrice + " to $" + newPrice;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        // In a real app, you would use NotificationCompat here
        // This is simplified for dependency-free implementation
    }

    private void restartPriceMonitoring(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
            "MbareMusikaPrefs", Context.MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("favorites", null);

        if (favorites != null) {
            // In a real implementation, you would restart alarms for each favorite
            Toast.makeText(context, "Price monitoring restarted", Toast.LENGTH_SHORT).show();
        }
    }
}
