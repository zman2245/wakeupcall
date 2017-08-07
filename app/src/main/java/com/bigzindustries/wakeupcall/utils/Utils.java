package com.bigzindustries.wakeupcall.utils;

import android.telephony.PhoneNumberUtils;

public class Utils {

    public static final int APP_COLOR_HEX = 0x50E3C2;

    /**
     * Returns input with all non-numbers removed
     * @return
     */
    public static String normalizePhoneNumber(String input) {
        return PhoneNumberUtils.formatNumber(input);
        //return input.replaceAll("[^\\d]", "");
    }
}
