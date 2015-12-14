package xyz.kandrac.library.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by kandrac on 13/12/15.
 */
public class DateUtils {

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("d. M. yyyy", Locale.getDefault());
    public static SimpleDateFormat fullDateFormat = new SimpleDateFormat("d. M. yyyy H:mm:ss", Locale.getDefault());

    public static long getTodayZeroTime() {
        return getZeroTime(System.currentTimeMillis());
    }

    public static long getTodayHourTime(int hour) {
        return getHourTime(System.currentTimeMillis(), hour);
    }

    public static long getZeroTime(long timeInDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInDay);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getHourTime(long timeInDay, int hour) {
        return getZeroTime(timeInDay) + TimeUnit.HOURS.toMillis(hour);
    }
}
