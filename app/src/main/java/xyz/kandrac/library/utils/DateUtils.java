package xyz.kandrac.library.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by kandrac on 13/12/15.
 */
public class DateUtils {

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd. MM. yyyy", Locale.getDefault());

    public static long getTodayZeroTime() {
        return getZeroTime(System.currentTimeMillis());
    }

    public static long getTodayHourTime(int hour) {
        return getHourTime(System.currentTimeMillis(), hour);
    }

    public static long getZeroTime(long timeInDay) {
        return timeInDay - timeInDay % (1000 * 60 * 60 * 24);
    }

    public static long getHourTime(long timeInDay, int hour) {
        return getZeroTime(timeInDay) + 1000 * 60 * 60 * hour;
    }
}
