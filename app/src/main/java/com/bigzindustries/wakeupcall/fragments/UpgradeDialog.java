package com.bigzindustries.wakeupcall.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.SkuDetails;
import com.bigzindustries.wakeupcall.R;
import com.bigzindustries.wakeupcall.activities.MainActivity;
import com.bigzindustries.wakeupcall.adapters.UpgradeItemsListAdapter;
import com.bigzindustries.wakeupcall.models.PurchaseData;
import com.bigzindustries.wakeupcall.utils.InAppPurchaseManager;

public class UpgradeDialog extends DialogFragment {

    public UpgradeDialog() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.upgrade_dialog_height));
        getDialog().setTitle("Select an Upgrade");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select an Upgrade");
        MainActivity activity = (MainActivity)getActivity();

        InAppPurchaseManager purchaseHelper = activity.getPurchaseHelper();
        PurchaseData data = purchaseHelper.getPurchaseData();

        UpgradeItemsListAdapter adapter = new UpgradeItemsListAdapter(data, getActivity());

        builder.setAdapter(adapter, (dialogInterface, i) -> {
            SkuDetails details = (SkuDetails) adapter.getItem(i);
            String skuId = details.getSku();

            BillingFlowParams.Builder billingBuilder = new BillingFlowParams.Builder()
                    .setSku(skuId).setType(BillingClient.SkuType.INAPP);
            int responseCode = purchaseHelper.launchBillingFlow(activity, billingBuilder.build());

            Log.d("UpgradeDialog", "Purchase response code=" + responseCode);

            // PurchaseHelper listens for onPurchasesUpdated, so handling of the purcahse results
            // should go there. For here, just log the result code.
        });

        return builder.create();
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.upgrade_dialog, container, false);
//        ListView list = rootView.findViewById(R.id.upgrade_list);
//
//        InAppPurchaseManager purchaseHelper = ((MainActivity)getActivity()).getPurchaseHelper();
//        PurchaseData data = purchaseHelper.getPurchaseData();
//
//        UpgradeItemAdapter adapter = new UpgradeItemAdapter(data, getContext());
//
//        list.setAdapter(adapter);
//
//        list.setOnItemClickListener((adapterView, view, i, l) -> {
//            SkuDetails details = (SkuDetails) adapter.getItem(i);
//
//            // TODO: make the purchase
//        });
//
//        getDialog().setTitle("Select an Upgrade");
//
//        return rootView;
//    }
}
