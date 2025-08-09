package com.lionico.mbaremusika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CommodityAdapter extends RecyclerView.Adapter<CommodityAdapter.ViewHolder> {

    private final Context context;
    private List<Commodity> commodities;

    public CommodityAdapter(Context context, List<Commodity> commodities) {
        this.context = context;
        this.commodities = commodities;
    }

    @NonNull
    @Override
    public CommodityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_commodity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommodityAdapter.ViewHolder holder, int position) {
        Commodity commodity = commodities.get(position);
        holder.nameText.setText(commodity.getName());
        holder.quantityText.setText(commodity.getQuantity());

        // Format price with $ and 2 decimals
        try {
            double price = Double.parseDouble(commodity.getUsdPrice());
            holder.usdPriceText.setText(String.format(Locale.getDefault(), "$%.2f", price));
        } catch (NumberFormatException e) {
            holder.usdPriceText.setText("$" + commodity.getUsdPrice());
        }

        // Set star icon
        holder.favoriteStar.setImageResource(
            commodity.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_empty
        );

        // Favorite toggle click listener
        holder.favoriteStar.setOnClickListener(v -> {
            boolean newState = !commodity.isFavorite();
            commodity.setFavorite(newState);
            notifyItemChanged(position);

            if (context instanceof MainActivity) {
                ((MainActivity) context).saveFavorites();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commodities != null ? commodities.size() : 0;
    }

    public void updateData(List<Commodity> newCommodities) {
        this.commodities = newCommodities;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView quantityText;
        TextView usdPriceText;
        ImageView favoriteStar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_commodity_name);
            quantityText = itemView.findViewById(R.id.tv_quantity);
            usdPriceText = itemView.findViewById(R.id.tv_usd_price);
            favoriteStar = itemView.findViewById(R.id.iv_favorite);
        }
    }
}
