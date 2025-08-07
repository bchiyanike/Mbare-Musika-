package com.lionico.mbaremusika;

import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.List;
import java.util.Locale;

public class PriceHistoryAdapter extends BaseAdapter {

    private Context ctx;
    private List<PriceHistory.PriceRecord> records;

    public PriceHistoryAdapter(Context ctx, List<PriceHistory.PriceRecord> records) {
        this.ctx = ctx;
        this.records = records != null ? records : List.of();
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int i) {
        return records.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View v, ViewGroup parent) {
        if (v == null) {
            v = LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView line1 = v.findViewById(android.R.id.text1);
        TextView line2 = v.findViewById(android.R.id.text2);

        PriceHistory.PriceRecord current = records.get(i);
        double price = current.getPrice();
        line1.setText(String.format(Locale.getDefault(), "$%.2f", price));
        line2.setText(current.getFormattedDateTime());

        // Highlight price drop
        if (i > 0) {
            double previous = records.get(i - 1).getPrice();
            if (price < previous) {
                line1.setTextColor(Color.parseColor("#D32F2F")); // red for drop
            } else if (price > previous) {
                line1.setTextColor(Color.parseColor("#388E3C")); // green for rise
            } else {
                line1.setTextColor(Color.BLACK); // neutral
            }
        } else {
            line1.setTextColor(Color.BLACK);
        }

        return v;
    }
}
