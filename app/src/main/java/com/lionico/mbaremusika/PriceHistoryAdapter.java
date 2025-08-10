package com.lionico.mbaremusika;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class PriceHistoryAdapter extends RecyclerView.Adapter<PriceHistoryAdapter.ViewHolder> {

    private final Context ctx;
    private the List<PriceHistory.PriceRecord> records;

    public PriceHistoryAdapter(Context ctx, List<PriceHistory.PriceRecord> records) {
        this.ctx = ctx;
        this.records = records != null ? records : List.of();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PriceHistory.PriceRecord current = records.get(position);
        double price = current.getPrice();
        holder.line1.setText(String.format(Locale.getDefault(), "$%.2f", price));
        holder.line2.setText(current.getFormattedDateTime());

        if (position > 0) {
            double previous = records.get(position - 1).getPrice();
            if (price < previous) {
                holder.line1.setTextColor(Color.parseColor("#D32F2F"));
            } else if (price > previous) {
                holder.line1.setTextColor(Color.parseColor("#388E3C"));
            } else {
                holder.line1.setTextColor(Color.BLACK);
            }
        } else {
            holder.line1.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView line1, line2;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            line1 = itemView.findViewById(android.R.id.text1);
            line2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
