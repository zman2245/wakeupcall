package com.bigzindustries.wakeupcall.utils;

import android.telephony.PhoneNumberUtils;

public class Utils {

    /**
     * Returns input with all non-numbers removed
     * @return
     */
    public static String normalizePhoneNumber(String input) {
        return PhoneNumberUtils.formatNumber(input);
        //return input.replaceAll("[^\\d]", "");
    }
}
