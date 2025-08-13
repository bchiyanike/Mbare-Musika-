package com.lionico.mbaremusika;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;

public class PriceAlertReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "price_alert_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            return;
        }

        String name = intent.getStringExtra("COMMODITY_NAME");
        double oldPrice = intent.getDoubleExtra("OLD_PRICE", 0.0);
        double newPrice = intent.getDoubleExtra("NEW_PRICE", 0.0);

        if (name == null || name.isEmpty()) {
            return;
        }

        showPriceChangeNotification(context, name, oldPrice, newPrice);
        showPriceChangeToast(context, name, oldPrice, newPrice);
    }

    private void showPriceChangeNotification(Context context, String name, double oldPrice, double newPrice) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_usd)
            .setContentTitle("Price Alert")
            .setContentText(String.format(Locale.getDefault(), "%s price changed from $%.2f to $%.2f", name, oldPrice, newPrice))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            // Handle case where notification permission is not granted
        }
    }

    private void showPriceChangeToast(Context context, String name, double oldPrice, double newPrice) {
        String message = String.format(Locale.getDefault(), "%s price changed from $%.2f to $%.2f", name, oldPrice, newPrice);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
