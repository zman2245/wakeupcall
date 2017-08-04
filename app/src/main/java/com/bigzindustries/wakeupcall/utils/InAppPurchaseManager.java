package com.bigzindustries.wakeupcall.utils;

import android.content.Context;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.bigzindustries.wakeupcall.models.PurchaseData;

import java.util.ArrayList;
import java.util.List;

public class InAppPurchaseManager implements BillingClientStateListener, PurchasesUpdatedListener {

    private static final String LOG_TAG = InAppPurchaseManager.class.getName();

    public static final String UNLIMITED_CONTACTS = "Unlimited Contacts";
    public static final String CUSTOMIZE_ALERT_SOUND = "Customize Alert Sound";
    public static final String ALERT_ON_SMS = "Alert on SMS";

    private BillingClient mBillingClient;
    private final WakeUpCallPurchasesListener listener;

    // need to coordinate multiple asynch requests
    private boolean dataIsFreshAndReady = false;
    private SkuDetails.SkuDetailsResult skuDetailsResult = null;
    private List<Purchase> purchases = null;

    public InAppPurchaseManager(WakeUpCallPurchasesListener listener) {
        this.listener = listener;
    }

    public void startDataLoad(Context context) {
        mBillingClient = new BillingClient.Builder(context).setListener(this).build();
        mBillingClient.startConnection(this);
    }

    public PurchaseData getPurchaseData() {
        return new PurchaseData(skuDetailsResult, purchases);
    }

    @Override
    public void onBillingSetupFinished(int resultCode) {
        Log.d(LOG_TAG, "onBillingSetupFinished. resultCode=" + resultCode);

        switch (resultCode) {
            case BillingClient.BillingResponse.OK:
                onBillingReady();
                break;

            case BillingClient.BillingResponse.USER_CANCELED:
                Log.d(LOG_TAG, "onBillingSetupFinished. Failed due to USER_CANCELED");
                break;
            case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                Log.d(LOG_TAG, "onBillingSetupFinished. Failed due to SERVICE_UNAVAILABLE");
                break;
            case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                Log.d(LOG_TAG, "onBillingSetupFinished. Failed due to BILLING_UNAVAILABLE");
                break;
            case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
                Log.d(LOG_TAG, "onBillingSetupFinished. Failed due to ITEM_UNAVAILABLE");
                break;
            default:
                Log.d(LOG_TAG, "onBillingSetupFinished. Failed due to some other error. resultCode=" + resultCode);
                break;
        }
    }

    private void onBillingReady() {
        Log.d(LOG_TAG, "onBillingReady, continuing billing initialization");

        // The billing client is ready, query purchases here.
        List<String> skuList = new ArrayList<>();
        skuList.add(UNLIMITED_CONTACTS);
        skuList.add(CUSTOMIZE_ALERT_SOUND);
        skuList.add(ALERT_ON_SMS);
        mBillingClient.querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuList,
                (results) -> {
                    Log.d(LOG_TAG, "querySkuDetailsAsync completed. responseCode=" + results.getResponseCode());
                    skuDetailsResult = results;
                    checkDataAndNotify();
                });

        Purchase.PurchasesResult purchasesResult =
                mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        Log.d(LOG_TAG, "queryPurchases completed. responseCode=" + purchasesResult.getResponseCode());
        purchases = purchasesResult.getPurchasesList();
        checkDataAndNotify();
    }

    @Override
    public void onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to the
        // In-app Billing service by calling the startConnection() method.
        // TODO: handle this case
        Log.d(LOG_TAG, "onBillingServiceDisconnected");
    }

    @Override
    public void onPurchasesUpdated(int responseCode, List<Purchase> purchases) {
        Log.d(LOG_TAG, "onPurchasesUpdated. responseCode=" + responseCode);

        this.purchases = purchases;

        checkDataAndNotify();
    }

    /**
     * See if data is ready to do something meaningful, and notify listener if it is.
     * TODO: Handle error conditions
     */
    private void checkDataAndNotify() {
        if (purchases != null &&
                skuDetailsResult != null) {

            PurchaseData purchaseData = getPurchaseData();
            listener.onPurchaseDataUpdate(purchaseData);
        }
    }
}
