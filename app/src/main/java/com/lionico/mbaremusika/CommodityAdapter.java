package com.lionico.mbaremusika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
            holder.nameText = (TextView) convertView.findViewById(R.id.tv_commodity_name);
            holder.quantityText = (TextView) convertView.findViewById(R.id.tv_quantity);
            holder.usdPriceText = (TextView) convertView.findViewById(R.id.tv_usd_price);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Commodity commodity = commodities.get(position);
        if (commodity != null) {
            holder.nameText.setText(commodity.getName());
            holder.quantityText.setText(commodity.getQuantity());

            // Format USD price with dollar sign
            String price = commodity.getUsdPrice();
            if (!price.startsWith("$")) {
                price = "$" + price;
            }
            holder.usdPriceText.setText(price);
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
    }
}