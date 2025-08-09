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
    private final LayoutInflater inflater;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(Commodity commodity);
    }

    public CommodityAdapter(Context context, List<Commodity> commodities, OnItemClickListener listener) {
        this.context = context;
        this.commodities = commodities;
        this.inflater = LayoutInflater.from(context);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_commodity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Commodity commodity = commodities.get(position);
        holder.nameText.setText(commodity.getName());
        holder.quantityText.setText(commodity.getQuantity());

        try {
            double price = Double.parseDouble(commodity.getUsdPrice());
            holder.usdPriceText.setText(String.format(Locale.getDefault(), "$%.2f", price));
        } catch (NumberFormatException e) {
            holder.usdPriceText.setText("$" + commodity.getUsdPrice());
        }

        holder.favoriteStar.setImageResource(
            commodity.isFavorite() ?
            R.drawable.ic_star_filled :
            R.drawable.ic_star_empty
        );

        holder.favoriteStar.setOnClickListener(v -> {
            boolean newState = !commodity.isFavorite();
            commodity.setFavorite(newState);
            notifyItemChanged(position);
            if (context instanceof MainActivity) {
                ((MainActivity) context).saveFavorites();
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(commodity);
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
        TextView nameText, quantityText, usdPriceText;
        ImageView favoriteStar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.tv_commodity_name);
            quantityText = itemView.findViewById(R.id.tv_quantity);
            usdPriceText = itemView.findViewById(R.id.tv_usd_price);
            favoriteStar = itemView.findViewById(R.id.iv_favorite);
        }
    }
}
