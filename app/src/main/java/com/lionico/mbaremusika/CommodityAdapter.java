package com.lionico.mbaremusika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CommodityAdapter extends BaseAdapter {
    private Context context;
    private List<Commodity> commodities;
    private LayoutInflater inflater;

    public CommodityAdapter(Context context, List<Commodity> commodities) {
        this.context = context;
        this.commodities = commodities;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return commodities != null ? commodities.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return commodities != null ? commodities.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_commodity, parent, false);
            holder = new ViewHolder();
            holder.nameText = convertView.findViewById(R.id.tv_commodity_name);
            holder.quantityText = convertView.findViewById(R.id.tv_quantity);
            holder.usdPriceText = convertView.findViewById(R.id.tv_usd_price);
            holder.favoriteStar = convertView.findViewById(R.id.iv_favorite);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Commodity commodity = commodities.get(position);
        if (commodity != null) {
            holder.nameText.setText(commodity.getName());
            holder.quantityText.setText(commodity.getQuantity());

            // Format USD price with dollar sign and two decimals
            try {
                double price = Double.parseDouble(commodity.getUsdPrice());
                holder.usdPriceText.setText(String.format(Locale.getDefault(), "$%.2f", price));
            } catch (NumberFormatException e) {
                holder.usdPriceText.setText("$" + commodity.getUsdPrice());
            }

            // Update favorite star
            holder.favoriteStar.setImageResource(
                commodity.isFavorite() ? 
                R.drawable.ic_star_filled : 
                R.drawable.ic_star_empty
            );

            // Favorite toggle
            holder.favoriteStar.setOnClickListener(v -> {
                boolean newState = !commodity.isFavorite();
                commodity.setFavorite(newState);
                notifyDataSetChanged();

                if (context instanceof MainActivity) {
                    ((MainActivity) context).saveFavorites();
                }
            });
        }

        return convertView;
    }

    public void updateData(List<Commodity> newCommodities) {
        this.commodities = newCommodities;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView nameText;
        TextView quantityText;
        TextView usdPriceText;
        ImageView favoriteStar;
    }
}
