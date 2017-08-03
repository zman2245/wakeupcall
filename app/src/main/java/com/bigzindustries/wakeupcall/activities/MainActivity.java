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
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bigzindustries.wakeupcall.R;
import com.bigzindustries.wakeupcall.adapters.AlarmContactsAdapter;
import com.bigzindustries.wakeupcall.db.AlarmContactsDbHelper;
import com.bigzindustries.wakeupcall.fragments.DoNotDisturbPermissionDialog;
import com.bigzindustries.wakeupcall.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ALARM_PERMISSIONS = 1;
    private static final int REQUEST_CODE_CONTACT_PICK = 2;

    private static final String DIALOG_TAG = "DoNotDisturbPermissionDialog";

    private AlarmContactsDbHelper dbHelper;

    private Button addButton;
    private ListView alarmContactsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new AlarmContactsDbHelper(this);

        addButton = (Button)findViewById(R.id.add_button);
        alarmContactsList = (ListView)findViewById(R.id.alarm_contacts_list);

        // get this out of the way right away; critical for basic app function
        handleAlarmPermissions();
        handleDoNotDisturbPermissions();

        configList();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAddButtonClick();
            }
        });
    }

    private void configList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT  * FROM " + AlarmContactsDbHelper.TABLE_NAME, null);

        AlarmContactsAdapter adapter = new AlarmContactsAdapter(this, cursor);
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

                // need to reset the list to account for the new addition
                configList();
            } else {
                Toast.makeText(this, "Something went wrong while picking contacts", Toast.LENGTH_SHORT);
            }
        }
    }

    private void insertAlarmContact(String name, String number) {
//      Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

//      Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);

//      Insert the new row, returning the primary key value of the new row
        long id = db.insert(AlarmContactsDbHelper.TABLE_NAME, null, values);

        Log.d("ContactPicker", "Inserted row into DB. id=" + id);
    }

    private void handleAlarmPermissions() {
        int readPhoneState = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        int readPhoneNumbers = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_NUMBERS);
        int dND = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY);

        Log.d("Main", "readPhoneState=" + readPhoneState + ", readPhoneNumbers=" + readPhoneNumbers);

        if (readPhoneState == PackageManager.PERMISSION_DENIED ||
                readPhoneNumbers == PackageManager.PERMISSION_DENIED ||
                dND == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_PHONE_NUMBERS,
                            Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                    REQUEST_CODE_ALARM_PERMISSIONS);
        }
    }

    private void handleDoNotDisturbPermissions() {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            DoNotDisturbPermissionDialog dialog = new DoNotDisturbPermissionDialog();
            dialog.show(getFragmentManager(), DIALOG_TAG);
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
                return;
            }
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
