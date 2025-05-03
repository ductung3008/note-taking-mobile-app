package com.haui.notetakingapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    /**
     * Format a timestamp to a string representing the date in "dd tháng MM" format.
     *
     * @param millis The timestamp in milliseconds.
     * @return A string representing the date in "dd tháng MM" format.
     */
    public static String formatToDayMonth(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd 'tháng' MM", new Locale("vi", "VN"));
        return sdf.format(new Date(millis));
    }

    /**
     * Format a timestamp to a string representing the date and time in "dd/MM/yyyy HH:mm" format.
     *
     * @param millis The timestamp in milliseconds.
     * @return A string representing the date and time in "dd/MM/yyyy HH:mm" format.
     */
    public static String formatToFullDateTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
        return sdf.format(new Date(millis));
    }
}
