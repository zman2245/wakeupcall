package com.bigzindustries.wakeupcall.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.billingclient.api.SkuDetails;
import com.bigzindustries.wakeupcall.R;
import com.bigzindustries.wakeupcall.models.PurchaseData;

public class UpgradeItemAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private PurchaseData purchaseData;

    public UpgradeItemAdapter(PurchaseData purchaseData, Context context) {
        this.purchaseData = purchaseData;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return purchaseData.getSkuDetailsResult().getSkuDetailsList().size();
    }

    @Override
    public Object getItem(int i) {
        return purchaseData.getSkuDetailsResult().getSkuDetailsList().get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        SkuDetails details = (SkuDetails)getItem(i);

        if (view == null) {
            view = inflater.inflate(R.layout.upgrade_item, viewGroup, false);
            holder = new ViewHolder();
            holder.title = view.findViewById(R.id.title);
            holder.description = view.findViewById(R.id.subtitle);
            holder.price = view.findViewById(R.id.price);

            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        holder.title.setText(details.getTitle());
        holder.description.setText(details.getDescription());
        holder.price.setText(details.getPrice());

        return view;
    }

    // Helper class for caching
    private static class ViewHolder {
        TextView title;
        TextView description;
        TextView price;
    }
}
