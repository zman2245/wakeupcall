package com.bigzindustries.wakeupcall.utils;

import com.bigzindustries.wakeupcall.models.PurchaseData;

public interface WakeUpCallPurchasesListener {
    void onPurchaseDataUpdate(PurchaseData purchaseData);
    void onError(String msg);
}
