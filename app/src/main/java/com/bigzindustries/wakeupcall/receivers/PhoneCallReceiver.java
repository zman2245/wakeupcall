package com.bigzindustries.wakeupcall.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bigzindustries.wakeupcall.db.AlarmContactsDbHelper;
import com.bigzindustries.wakeupcall.utils.Utils;

public class PhoneCallReceiver extends BroadcastReceiver {

    static final String FIND_NUMBER_QUERY =
            "SELECT name FROM " + AlarmContactsDbHelper.TABLE_NAME_CONTACTS + " WHERE number=?";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CallStateChanged", "action=" + intent.getAction());

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            // savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else {
            // detecting incoming phone call
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.d("CallStateChanged", "state=" + stateStr + ", number" + number);

                if (checkNumber(context, number)) {
                    playAlarmSound(context);
                }
            } else {

            }
        }
    }

    /**
     * Returns true if this is a number controlled by the app
     *
     * @param context
     * @param number
     * @return
     */
    public boolean checkNumber(Context context, String number) {
        number = Utils.normalizePhoneNumber(number);
        AlarmContactsDbHelper dbHelper = new AlarmContactsDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Log.d("CallStateChanged", "checking number=" + number);
        Cursor cursor = db.rawQuery(FIND_NUMBER_QUERY, new String[]{number});

        try {
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public void playAlarmSound(Context context) {
        configForAudio(context);

        Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone ringtone = RingtoneManager.getRingtone(context, alarmTone);
        ringtone.play();

        // TODO: not a real solution
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ringtone.stop();
            }
        }, 1000 * 5);
    }

    private void configForAudio(Context context) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (true) {
            volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        }

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
}
