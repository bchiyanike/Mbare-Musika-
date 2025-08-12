package com.lionico.mbaremusika;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class PriceAlertReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "price_alert_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        if ("com.lionico.mbaremusika.PRICE_ALERT".equals(action)) {
            String commodityId = intent.getStringExtra("COMMODITY_ID");
            String commodityName = intent.getStringExtra("COMMODITY_NAME");
            double oldPrice = intent.getDoubleExtra("OLD_PRICE", 0);
            double newPrice = intent.getDoubleExtra("NEW_PRICE", 0);
            
            if (commodityName != null) {
                handlePriceAlert(context, commodityName, oldPrice, newPrice);
            }
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            restartPriceMonitoring(context);
        }
    }

    private void handlePriceAlert(Context context, String name, double oldPrice, double newPrice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission for notifications
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED) {
                showNotification(context, name, oldPrice, newPrice);
            } else {
                // Fallback to Toast if permission not granted
                showPriceChangeToast(context, name, oldPrice, newPrice);
            }
        } else {
            // Pre-Android 13 can show notifications without runtime permission
            showNotification(context, name, oldPrice, newPrice);
        }
    }

    private void showNotification(Context context, String name, double oldPrice, double newPrice) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_usd)
                .setContentTitle("Price Alert")
                .setContentText(String.format("%s price changed from $%.2f to $%.2f", name, oldPrice, newPrice))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // Handle case where permission was revoked after checking
            showPriceChangeToast(context, name, oldPrice, newPrice);
        } catch (Exception e) {
            // Fallback for other notification errors
            showPriceChangeToast(context, name, oldPrice, newPrice);
        }
    }

    private void showPriceChangeToast(Context context, String name, double oldPrice, double newPrice) {
        String message = String.format("%s price changed from $%.2f to $%.2f", name, oldPrice, newPrice);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void restartPriceMonitoring(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MbareMusikaPrefs", Context.MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet("favorites", new HashSet<>());
        
        if (!favorites.isEmpty()) {
            // TODO: Implement alarm/job scheduling for price monitoring
            Toast.makeText(context, "Price monitoring restarted", Toast.LENGTH_SHORT).show();
        }
    }
}
