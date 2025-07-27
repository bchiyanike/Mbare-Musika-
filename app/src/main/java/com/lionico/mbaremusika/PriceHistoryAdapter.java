package com.lionico.mbaremusika;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PriceHistoryAdapter extends BaseAdapter {

    private Context ctx;
    private List<PriceHistory.PriceRecord> records;

    public PriceHistoryAdapter(Context ctx, List<PriceHistory.PriceRecord> records) {
        this.ctx = ctx;
        this.records = records;
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

        TextView line1 = (TextView) v.findViewById(android.R.id.text1);
        TextView line2 = (TextView) v.findViewById(android.R.id.text2);

        PriceHistory.PriceRecord r = records.get(i);

        line1.setText("$" + r.getPrice());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        line2.setText(sdf.format(new Date(r.getTimestamp())));

        return v;
    }
}
