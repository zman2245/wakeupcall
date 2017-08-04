package com.bigzindustries.wakeupcall.models;

import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;

import java.util.List;

public class PurchaseData {
    private SkuDetails.SkuDetailsResult skuDetailsResult = null;
    private List<Purchase> purchases = null;

    public PurchaseData(SkuDetails.SkuDetailsResult skuDetailsResult, List<Purchase> purchases) {
        this.skuDetailsResult = skuDetailsResult;
        this.purchases = purchases;
    }

    public boolean isDataReady() {
        return purchases != null && skuDetailsResult != null;
    }

    public SkuDetails.SkuDetailsResult getSkuDetailsResult() {
        return skuDetailsResult;
    }

    public void setSkuDetailsResult(SkuDetails.SkuDetailsResult skuDetailsResult) {
        this.skuDetailsResult = skuDetailsResult;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Purchase> purchases) {
        this.purchases = purchases;
    }
}
