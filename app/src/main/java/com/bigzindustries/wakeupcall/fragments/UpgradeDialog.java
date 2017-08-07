package com.bigzindustries.wakeupcall.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.billingclient.api.SkuDetails;
import com.bigzindustries.wakeupcall.R;
import com.bigzindustries.wakeupcall.activities.MainActivity;
import com.bigzindustries.wakeupcall.adapters.UpgradeItemAdapter;
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

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Select an Upgrade");
//
//        InAppPurchaseManager purchaseHelper = ((MainActivity)getActivity()).getPurchaseHelper();
//        PurchaseData data = purchaseHelper.getPurchaseData();
//
//        UpgradeItemAdapter adapter = new UpgradeItemAdapter(data, getContext());
//
//        builder.setAdapter(adapter);
//        builder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//
//        return builder.create();
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.upgrade_dialog, container, false);
        ListView list = rootView.findViewById(R.id.upgrade_list);

        InAppPurchaseManager purchaseHelper = ((MainActivity)getActivity()).getPurchaseHelper();
        PurchaseData data = purchaseHelper.getPurchaseData();

        UpgradeItemAdapter adapter = new UpgradeItemAdapter(data, getContext());

        list.setAdapter(adapter);

        list.setOnItemClickListener((adapterView, view, i, l) -> {
            SkuDetails details = (SkuDetails) adapter.getItem(i);

            // TODO: make the purchase
        });

        getDialog().setTitle("Select an Upgrade");

        return rootView;
    }
}
