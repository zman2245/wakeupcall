package com.bigzindustries.wakeupcall.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.bigzindustries.wakeupcall.utils.PrefsWrapper;

import java.util.HashSet;

/**
 * Check incoming SMS message, alerting the user when necessary
 */
public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //---get the SMS message passed in---
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String str = "";
        HashSet<String> incomingNumbers = new HashSet<>();

        if (bundle != null) {
            //---retrieve the SMS message received---
            Object[] pdus = (Object[]) bundle.get("pdus");
            String number;
            msgs = new SmsMessage[pdus.length];
            for (int i=0; i<msgs.length; i++){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = bundle.getString("format");
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }
                else {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }

                number = msgs[i].getOriginatingAddress();
                incomingNumbers.add(number);

                str += "SMS from " + number;
                str += " :";
                str += msgs[i].getMessageBody();
                str += "\n";
            }

            //---display the new SMS message---
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();


        }
    }

    private boolean isSmsEnabled(Context context) {
        return PrefsWrapper.isSmsEnabled(context);
    }
}
