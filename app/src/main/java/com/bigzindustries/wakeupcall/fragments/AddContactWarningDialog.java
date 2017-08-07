package com.bigzindustries.wakeupcall.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.bigzindustries.wakeupcall.activities.MainActivity;
import com.bigzindustries.wakeupcall.utils.InAppPurchaseManager;

public class AddContactWarningDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainActivity activity = (MainActivity)getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You may add up to 2 contacts free of charge. " +
                "To add more please purchase the ability to add more contacts. Thank you!")
                .setPositiveButton("Upgrade", (dialog, id) -> {
                    BillingFlowParams.Builder billingBuilder = new BillingFlowParams.Builder()
                            .setSku(InAppPurchaseManager.PRODID_UNLIMITED_CONTACTS)
                            .setType(BillingClient.SkuType.INAPP);
                    activity.getPurchaseHelper().launchBillingFlow(activity, billingBuilder.build());
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });

        return builder.create();
    }
}
