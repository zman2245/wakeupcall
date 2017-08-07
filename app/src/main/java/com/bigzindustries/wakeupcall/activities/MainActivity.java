package com.bigzindustries.wakeupcall.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.billingclient.api.SkuDetails;
import com.bigzindustries.wakeupcall.R;
import com.bigzindustries.wakeupcall.adapters.AlarmContactsAdapter;
import com.bigzindustries.wakeupcall.adapters.AlarmContactsDbDelegate;
import com.bigzindustries.wakeupcall.db.AlarmContactsDbHelper;
import com.bigzindustries.wakeupcall.fragments.DoNotDisturbPermissionDialog;
import com.bigzindustries.wakeupcall.fragments.UpgradeDialog;
import com.bigzindustries.wakeupcall.models.PurchaseData;
import com.bigzindustries.wakeupcall.utils.InAppPurchaseManager;
import com.bigzindustries.wakeupcall.utils.Utils;
import com.bigzindustries.wakeupcall.utils.WakeUpCallPurchasesListener;

public class MainActivity extends AppCompatActivity
        implements AlarmContactsDbDelegate, WakeUpCallPurchasesListener {

    private static final int REQUEST_CODE_ALARM_PERMISSIONS = 1;
    private static final int REQUEST_CODE_CONTACT_PICK = 2;

    private static final String DND_DIALOG_TAG = "DoNotDisturbPermissionDialog";
    private static final String UPGRADE_DIALOG_TAG = "UpgradeDialog";

    private AlarmContactsDbHelper dbHelper;

    private InAppPurchaseManager purchaseHelper;

    private Button addButton;
    private ListView alarmContactsList;
    private View permissionInfo;
    private Button standardPermissionButton;
    private Button doNotDisturbPermissionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new AlarmContactsDbHelper(this);
        purchaseHelper = new InAppPurchaseManager(this);

        addButton = (Button)findViewById(R.id.add_button);
        alarmContactsList = (ListView)findViewById(R.id.alarm_contacts_list);
        permissionInfo = findViewById(R.id.permission_info);
        standardPermissionButton = (Button)findViewById(R.id.standard_permission_button);
        doNotDisturbPermissionButton = (Button)findViewById(R.id.dnd_permission_button);

        configList();

        addButton.setOnClickListener(view -> handleAddButtonClick());
        standardPermissionButton.setOnClickListener((view) -> promptForStandardPermissions());
        doNotDisturbPermissionButton.setOnClickListener((view) -> promptForDoNotDisturbPermissions());

        // get this out of the way right away; critical for basic app function
        promptForStandardPermissions();
        promptForDoNotDisturbPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        purchaseHelper.startDataLoad(this);

        updatePermissionInfoViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem upgradeItm = menu.findItem(R.id.menu_item_upgrade);
        upgradeItm.setVisible(purchaseHelper.getPurchaseData().isDataReady());

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_upgrade:
                showStore();
                return true;
        }

        return false;
    }

    private void showStore() {
        UpgradeDialog dialog = new UpgradeDialog();
        dialog.show(getFragmentManager(), UPGRADE_DIALOG_TAG);
    }

    public InAppPurchaseManager getPurchaseHelper() {
        return purchaseHelper;
    }

    private void configList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT  * FROM " + AlarmContactsDbHelper.TABLE_NAME, null);

        AlarmContactsAdapter adapter = new AlarmContactsAdapter(this, cursor, this);
        alarmContactsList.setAdapter(adapter);
    }

    private void handleAddButtonClick() {
        // Right now, this only supports selecting one number
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_CODE_CONTACT_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == REQUEST_CODE_CONTACT_PICK) {
            if (resultCode == RESULT_OK) {
                Uri uri = intent.getData();
                String[] projection = {
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                };

                Cursor cursor = getContentResolver().query(uri, projection,
                        null, null, null);
                cursor.moveToFirst();

                int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberColumnIndex);
                number = Utils.normalizePhoneNumber(number);

                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = cursor.getString(nameColumnIndex);

                Log.d("ContactPicker", "ZZZ number : " + number + " , name : " + name);

                insertAlarmContact(name, number);
            } else {
                Toast.makeText(this, "Something went wrong while picking contacts", Toast.LENGTH_SHORT);
            }
        }
    }

    private void updatePermissionInfoViews() {
        boolean needsStandard = needsStandardPermissions();
        boolean needsDoNotDisturb = needsDoNotDisturbPermissions();

        if (needsDoNotDisturb || needsStandard) {
            permissionInfo.setVisibility(View.VISIBLE);
        } else {
            permissionInfo.setVisibility(View.GONE);
        }

        doNotDisturbPermissionButton.setVisibility(needsDoNotDisturb ? View.VISIBLE : View.GONE);
        standardPermissionButton.setVisibility(needsStandard ? View.VISIBLE : View.GONE);
    }

    private boolean needsStandardPermissions() {
        int readPhoneState = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        int dND = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY);

        Log.d("Main", "readPhoneState=" + readPhoneState + ", dnd=" + dND);

        return readPhoneState == PackageManager.PERMISSION_DENIED ||
                dND == PackageManager.PERMISSION_DENIED;
    }

    private boolean needsDoNotDisturbPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false;
        }

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        return !notificationManager.isNotificationPolicyAccessGranted();
    }

    private void promptForStandardPermissions() {
        // Reminder: Manifest.permission.READ_PHONE_NUMBERS is a subset of READ_PHONE_STATE
        if (needsStandardPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                    REQUEST_CODE_ALARM_PERMISSIONS);
        }
    }

    private void promptForDoNotDisturbPermissions() {
        if (needsDoNotDisturbPermissions()) {
            DoNotDisturbPermissionDialog dialog = new DoNotDisturbPermissionDialog();
            dialog.show(getFragmentManager(), DND_DIALOG_TAG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ALARM_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!

                } else {

                    // permission denied, boo!
                }

                updatePermissionInfoViews();

                return;
            }
        }
    }

    @Override
    public void removeAlarmContact(String number) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(AlarmContactsDbHelper.TABLE_NAME, "number=?", new String[] {number});

        // need to reset the list to account for the removal
        configList();
    }

    private void insertAlarmContact(String name, String number) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);

        long id = db.insert(AlarmContactsDbHelper.TABLE_NAME, null, values);

        Log.d("ContactPicker", "Inserted row into DB. id=" + id);

        // need to reset the list to account for the addition
        configList();
    }

    @Override
    public void onPurchaseDataUpdate(PurchaseData purchaseData) {
        invalidateOptionsMenu();
        SkuDetails.SkuDetailsResult results = purchaseData.getSkuDetailsResult();

        for (SkuDetails skuDetails : results.getSkuDetailsList()) {
            String sku = skuDetails.getSku();
            String price = skuDetails.getPrice();
            String title = skuDetails.getTitle();
        }
    }

//    private void queryContactsDirectly() {
//        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
//        while (cursor.moveToNext()) {
//            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
//            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//            if("1".equals(hasPhone) || Boolean.parseBoolean(hasPhone)) {
//                // You know it has a number so now query it like this
//                Cursor phones = this.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null);
//                while (phones.moveToNext()) {
//                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                    int itype = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
//
//                    final boolean isMobile =
//                            itype == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ||
//                                    itype == ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;
//
//                    // Do something here with 'phoneNumber' such as saving into
//                    // the List or Array that will be used in your 'ListView'.
//
//                }
//                phones.close();
//            }
//        }
//    }
}
