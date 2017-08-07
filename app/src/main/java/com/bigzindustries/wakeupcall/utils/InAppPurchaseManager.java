package com.bigzindustries.wakeupcall.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.bigzindustries.wakeupcall.models.PurchaseData;

import java.util.ArrayList;
import java.util.List;

public class InAppPurchaseManager implements BillingClientStateListener, PurchasesUpdatedListener {

    private static final String LOG_TAG = InAppPurchaseManager.class.getName();

    public static final String PRODID_UNLIMITED_CONTACTS = "unlimited_contacts";
    public static final String PRODID_CUSTOMIZE_ALERT_SOUND = "customize_alert_sound";
    public static final String PRODID_ALERT_ON_SMS = "alert_on_sms";

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

    public int launchBillingFlow(Activity activity, BillingFlowParams params) {
        return mBillingClient.launchBillingFlow(activity, params);
    }

    @Override
    public void onBillingSetupFinished(int resultCode) {
        Log.d(LOG_TAG, "onBillingSetupFinished. resultCode=" + resultCode);

        switch (resultCode) {
            case BillingClient.BillingResponse.OK:
                onBillingReady();
                break;

            default:
                getErrorMessage("onBillingSetupFinished", resultCode);
                break;
        }
    }

    private String getErrorMessage(String source, int resultCode) {
        String msg;

        switch (resultCode) {
            case BillingClient.BillingResponse.OK:
                msg = "No error";
                break;

            case BillingClient.BillingResponse.USER_CANCELED:
                msg = "User cancelled the transaction";
                break;
            case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                msg = "Billing service is unavailable";
                break;
            case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                msg = "Billing is unavailable";
                break;
            case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
                msg = "Item is unavailable";
                break;
            default:
                msg = "Billing failed for an unknown result. Code=" + resultCode;
                break;
        }

        Log.d(LOG_TAG, source + ". " + msg);

        return msg;
    }

    private void onBillingReady() {
        Log.d(LOG_TAG, "onBillingReady, continuing billing initialization");

        // The billing client is ready, query purchases here.
        List<String> skuList = new ArrayList<>();
        skuList.add(PRODID_UNLIMITED_CONTACTS);
        skuList.add(PRODID_CUSTOMIZE_ALERT_SOUND);
        skuList.add(PRODID_ALERT_ON_SMS);
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
        String msg = getErrorMessage("onPurchasesUpdated", responseCode);


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
