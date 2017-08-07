package com.bigzindustries.wakeupcall.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.billingclient.api.SkuDetails;
import com.bigzindustries.wakeupcall.R;
import com.bigzindustries.wakeupcall.models.PurchaseData;

/**
 * Created by zack on 8/7/17.
 */

public class UpgradeItemsListAdapter implements ListAdapter {

    private final LayoutInflater inflater;
    private PurchaseData purchaseData;

    // Helper class for caching
    private static class ViewHolder {
        TextView title;
        TextView description;
        TextView price;
    }

    public UpgradeItemsListAdapter(PurchaseData purchaseData, Context context) {
        this.purchaseData = purchaseData;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

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
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        UpgradeItemsListAdapter.ViewHolder holder;
        SkuDetails details = (SkuDetails)getItem(i);

        if (view == null) {
            view = inflater.inflate(R.layout.upgrade_item, viewGroup, false);
            holder = new UpgradeItemsListAdapter.ViewHolder();
            holder.title = view.findViewById(R.id.title);
            holder.description = view.findViewById(R.id.subtitle);
            holder.price = view.findViewById(R.id.price);

            view.setTag(holder);
        }
        else {
            holder = (UpgradeItemsListAdapter.ViewHolder) view.getTag();
        }

        holder.title.setText(details.getTitle());
        holder.description.setText(details.getDescription());
        holder.price.setText(details.getPrice());

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }
}
