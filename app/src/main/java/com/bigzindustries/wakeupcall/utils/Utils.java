package com.bigzindustries.wakeupcall.utils;

public class Utils {

    /**
     * Returns input with all non-numbers removed
     * @return
     */
    public static String normalizePhoneNumber(String input) {
        return input.replaceAll("[^\\d]", "");
    }
}
